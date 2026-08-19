//import com.sun.jndi.toolkit.ctx.HeadTail;
import com.sun.net.httpserver.*; // import von allen eingebauten Java-HTTP-Server Klassen
//import java.io.*;

//import java.io.File;
import java.io.IOException; // Fehlerbehandlung
import java.io.OutputStream; // um antworten an den Client zu senden
import java.net.InetSocketAddress; // um die addresse und den Port zu definieren
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.io.InputStream; // um die saten vom Client zu lesen
import com.fasterxml.jackson.databind.ObjectMapper; 

public class SimplePostServer{
    private static String savedGeoJson = "";
    private static String savedHtml = "";
    public static String savedContent = "";

    public static void main(String[] args) throws IOException, Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000),0);  // erstellt einen http-server auf port 8000; localhost (standard)

        try {
            // XmlReader.loadGpx("test_xml.gpx");
            System.out.println("GeoJSON erzeugt: " + XmlReader.geoJsonData);

            savedGeoJson = XmlReader.geoJsonData;
            System.out.println("Initiale Route geladen");
        } catch (Exception e) {
            System.out.println("keine initiale datei gefunden, warte auf upload");
            savedGeoJson = "{}";
        }                           
                                      

        server.createContext("/geojson", exchange -> {                                                      //http://localhost:8000/geojson
            String method = exchange.getRequestMethod();                                                    //get oder post

            if ("GET".equals(method)) {
                // GeoJSON aus XmlReader holen
                String response = (savedGeoJson != null) ? savedGeoJson : "{}";                             

                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");       //sagt dem browser, dass es json ist
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");       
                exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes("UTF-8"));                                                   //sendet daten
                }

            } else if ("POST".equals(method)) {
                try {
                    // Datei vom FileUploader empfangen
                    InputStream is = exchange.getRequestBody();                                             //daten vom user lesen
                    byte[] data = is.readAllBytes();

                    // 1. Сохраняем присланный с сайта файл (один раз!)
                    Files.write(Paths.get("uploaded_file.gpx"), data);

                    // 2. Парсим этот новый файл
                    XmlReader.loadGpx("uploaded_file.gpx");
                        
                    // 3. Обновляем память сервера НОВЫМИ данными!
                    savedGeoJson = XmlReader.geoJsonData; 
                    System.out.println("xmlreader hat die datei verarbeitet");

                    // 4. Отправляем успешный ответ браузеру
                    String response = "datei verarbeitet";
                    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");       
                    exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);

                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes("UTF-8"));
                    }
                } catch (Exception e) { 
                    System.out.println("Ошибка при парсинге GPX: " + e.getMessage());
                    e.printStackTrace();
                    exchange.sendResponseHeaders(500, -1);
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });

        server.createContext("/about", exchange -> {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                Path path = Paths.get("about.html");

                if (Files.exists(path)) {
                    byte[] htmlBytes = Files.readAllBytes(path);
                    exchange.getResponseHeaders().set("Content-type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(200, htmlBytes.length);

                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(htmlBytes);
                    }
                } else {
                    exchange.sendResponseHeaders(404, -1);
                }
            }
        });

        server.createContext("/api/rewards", exchange -> {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                List<ChallengeData> data = ChallengeLoader.getDatenAusDBToUpload();

                ObjectMapper mapper = new ObjectMapper();
                String jsonResponse = mapper.writeValueAsString(data); //aus daten ein json-string 

                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, responseBytes.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            }
        });

        server.createContext("/rewards", exchange -> {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                
                Path path = Paths.get("abzeichnungen.html"); // verweist auf die datei in ordner

                if (Files.exists(path)) { //prüft ob die datei existiert 
                    byte[] htmlBytes = Files.readAllBytes(path); //list den inhalt der html-datei als array ein
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); 
                    exchange.sendResponseHeaders(200, htmlBytes.length);

                
                    try (OutputStream os = exchange.getResponseBody()) { //schließt os nach dem senden
                        os.write(htmlBytes); //schiebt die daten-bytes direkt zum browser
                    }
                } else {
                    exchange.sendResponseHeaders(404, -1);
                }
            }
        });

        server.createContext("/api/challenges", exchange -> { //exchange = wenn jemand die adresse aufruft, wird dieser teil ausgeführt
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) { //prüft, ob der client get anfrage sendet(daten aufruft)

                List<ChallengeData> data = ChallengeLoader.getDatenAusDBToUpload();

                ObjectMapper mapper = new ObjectMapper();
                String jsonResponse = mapper.writeValueAsString(data); //baut aus meinen daten ein json-string

                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8"); //sagt dem browser, dass die daten kommen in json, utf-8

                byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8); //umwandelt die daten in bytes, damit in die internet übertragen werden können
                exchange.sendResponseHeaders(200, responseBytes.length); //sagt dem browser, wie viele bytes er erwarten kann

                try (OutputStream os = exchange.getResponseBody()) { //schließt os nach dem senden
                    os.write(responseBytes); //schiebt die daten-bytes direkt zum browser
                }
            }
        });

        server.createContext("/api/join-challenge", new JoinChallengeHandler());

        server.createContext("/challenges", exchange -> {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                Path path = Paths.get("challenges.html"); // verweist auf die datei in ordner

                if (Files.exists(path)) { //prüft ob die datei existiert 
                    byte[] htmlBytes = Files.readAllBytes(path); //list den inhalt der html-datei als array ein
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(200, htmlBytes.length);
                    
                    try (OutputStream os = exchange.getResponseBody()) { //schließt os nach dem senden
                        os.write(htmlBytes); //schiebt die daten-bytes direkt zum browser
                    } 
                } else {
                        exchange.sendResponseHeaders(404, -1);
                }
            }
        });

        server.createContext("/bilds", exchange -> {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) { // prüft ob der user den server anspricht
                String requestPath = exchange.getRequestURI().getPath(); //holt die genaue aufgerufene URL

                Path filePath = Paths.get("." + requestPath); //wandelt die url in dateipfad um

                if (Files.exists(filePath) && !Files.isDirectory(filePath)) {//prüft ob das bild existiert und ob es kein ordner ist
                    String contentType = Files.probeContentType(filePath);//erkennt bildtyp

                    if (contentType == null) {//notfalltyp fürs bild, wenn nciht erkannt wurde
                        contentType = "application/octet-stream";
                    }

                    byte[] imageBytes = Files.readAllBytes(filePath); //list den inhalt der bild datei als byte-array ein 

                    exchange.getResponseHeaders().set("Content-Type", contentType);
                    exchange.sendResponseHeaders(200, imageBytes.length);

                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(imageBytes);
                    }
                } else {
                    exchange.sendResponseHeaders(404, -1);
                }
            }
        });


        server.createContext("/upload-html", exchange -> {
            String method = exchange.getRequestMethod();

            if ("POST".equals(method)) {
                InputStream is = exchange.getRequestBody();
                byte[] data = is.readAllBytes();
                savedHtml = new String(data, "UTF-8");                                                  //html wird gespeichert

                String response = "HTML uploaded successfully";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes("UTF-8"));
                }

            } else if ("GET".equals(exchange.getRequestMethod())) {
                String response = (savedHtml != null && !savedHtml.isEmpty()) ? savedHtml : "<h1>No HTML uploaded</h1>";    //liefert html an browser
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");                              //sagt dem browser, dass eine website ist
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");                                      //erlaubt zugriff
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes("UTF-8"));
                }

            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });

        server.createContext("/api/check-participation", exchange -> {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                
                int challengeID = 0;
                if (query != null && query.contains("=")) {
                    challengeID = Integer.parseInt(query.split("=")[1]);
                }

                boolean userTeilnimmt = ChallengeLoader.challengePruefer(challengeID);

                String jsonResponse = "{\"userTeilnimmt\": " + userTeilnimmt + "}";

                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

                byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, responseBytes.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            }
        }); 

        
        server.start();
        System.out.println("Server started on port 8000");
        System.out.println("Post geojson to /geojson");
        System.out.println("Post geojson to /upload-html");
    }
}