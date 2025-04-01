import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File

import java.io.{FileReader, FileOutputStream}

object Main {
  def main(args: Array[String]): Unit = {
    println("=== Pokretanje Scala aplikacije za Google Drive API ===")

    // Učitaj korisničke kredencijale iz JSON fajla
    val clientSecrets = GoogleClientSecrets.load(
      JacksonFactory.getDefaultInstance,
      new FileReader("driveApi.json")
    )

    // Postavi tok za autorizaciju
    val flow = new GoogleAuthorizationCodeFlow.Builder(
      com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport(),
      JacksonFactory.getDefaultInstance,
      clientSecrets,
      java.util.Collections.singletonList(DriveScopes.DRIVE)
    )
      .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
      .setAccessType("offline")
      .build()

    // Pokreni autorizaciju putem preglednika
    val credentials = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user")

    // Inicijalizuj Drive API
    val driveService = new Drive.Builder(
      com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport(),
      JacksonFactory.getDefaultInstance,
      credentials
    )
      .setApplicationName("Scala Google Drive API")
      .build()

    println("=== Uspostavljena konekcija sa Google Drive API ===")
    println("=== Preuzimanje fajlova iz foldera ===")

    // Folder ID
    val folderId = "***"

    // Rekurzivno preuzimanje fajlova
    downloadFolderContents(driveService, folderId)
  }

  def downloadFolderContents(driveService: Drive, folderId: String): Unit = {
    val result = driveService.files().list()
      .setQ(s"'$folderId' in parents")
      .setFields("files(id, name, mimeType)")
      .execute()
    val files = result.getFiles

    if (files == null || files.isEmpty) {
      println(s"Nema fajlova u folderu sa ID-jem $folderId.")
    } else {
      files.forEach { file =>
        println(s"Preuzimam fajl: ${file.getName} (${file.getMimeType})")

        try {
          file.getMimeType match {
            case "application/vnd.google-apps.folder" =>
              println(s"Fajl ${file.getName} je folder. Preuzimam sadržaj...")
              downloadFolderContents(driveService, file.getId) // Rekurzivno preuzimanje sadržaja foldera

            case "application/vnd.google-apps.document" =>
              val exportRequest = driveService.files().export(file.getId, "application/pdf")
              val outputStream = new FileOutputStream(s"${file.getName}.pdf")
              exportRequest.executeMediaAndDownloadTo(outputStream)
              outputStream.close()
              println(s"Fajl ${file.getName} je uspešno eksportovan kao PDF.")

            case _ =>
              val outputStream = new FileOutputStream(file.getName)
              driveService.files().get(file.getId).executeMediaAndDownloadTo(outputStream)
              outputStream.close()
              println(s"Fajl ${file.getName} je uspešno preuzet.")
          }
        } catch {
          case e: Exception =>
            println(s"Greška pri preuzimanju fajla ${file.getName}: ${e.getMessage}")
        }
      }
    }
  }
}
