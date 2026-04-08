import java.net.http.HttpClient; // klasse, um http-anfrage zu senden
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths; // klasse, um mit dateien auf system zu arbeiten 
import java.nio.file.Path;
import java.nio.file.Files;
import java.net.URI; // klasse, um der url des servers zu erstellen
import java.util.List; // zum erstellen einer liste von byte-arrays
import java.net.http.HttpRequest.BodyPublishers; // hilft request-body senden und response lesen
import java.net.http.HttpResponse.BodyHandlers;
import java.io.IOException;

public class FileUploader {
    private static final String BASE_URL = "http://localhost:8000/notes";                           // URL-Addresse des Servers, an den wir etwas schicken (Variable)
    private static final String BOUNDARY = "boundary";                                              // string für multipart/form-data, der die teile der datei trennt (notwendig bei multipart/form-data) (Variable)


    public static void main(String[] args) {
        String fileName = "world_map.html";                                                         // definiren die datei, dei hochgeladen werden soll (Variable)
        Path filePath = Paths.get(fileName);                                                        //pfad zu datei als pfad objekt
        HttpClient client = HttpClient.newHttpClient();                                             // erstellt HttpClient, der die anfrage sendet

        boolean exist = Files.exists(filePath);                                                     // überprüfung, ob die datei existiert
        System.out.println(exist); 

        //System.out.println("File exists: " + Files.exists(filePath));
        System.out.println("Absolute path: " + filePath.toAbsolutePath()); 

        try {
            HttpRequest request = HttpRequest.newBuilder()                                          // startet den aufbau der anfrage
                .uri(URI.create(BASE_URL))                                                          // Ziel-URL
                .header("Content-Type", "text/plain")                                               // informiert den Server über den Datentyp (Text)
                .POST(BodyPublishers.ofFile(filePath))                                              // POST-Methode mit Dateiinhalt
                .build();                                                                           // erstellt endgültige httprequest objekt

            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());          // sendet die anfrage und wartet auf antwort
                                                                                                    // BodyHandlers.ofString() sagt, dass wir den Antwort als string bekommen wollen
            int statusCode = response.statusCode();                                                 // Variable um den Code der Antwort zu speichern

            // Debug-Ausgaben
            System.out.println("DEBUG OUTPUT");
            System.out.println("Status code: " + statusCode);
            System.out.println("Response headers: " + response.headers());
            System.out.println("Response body: '" + response.body() + "'");
            
            if (statusCode >= 200 && statusCode < 300) {
                System.out.println("file '" +  fileName + "' downloaded succesfully, file path: " + filePath);
            } else {
                System.out.println("failed to upload the file: '" +  fileName + "'");
                System.out.println("Error: " + response.body());
                //System.out.println(output);
            }
        } catch(IOException | InterruptedException e ){
            System.out.println("Error:" + e.getMessage());
            e.printStackTrace();                                                                    // zeigt detaliert, was schief ist
        }
        //System.out.println(output);
        
    }
}



    


