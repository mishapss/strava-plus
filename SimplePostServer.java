
import com.sun.net.httpserver.*; // import von allen eingebauten Java-HTTP-Server Klassen

import java.io.IOException; // Fehlerbehandlung
import java.io.OutputStream; // um antworten an den Client zu senden
import java.net.InetSocketAddress; // um die addresse und den Port zu definieren
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.InputStream; // um die saten vom Client zu lesen


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper; 

public class SimplePostServer{
    private static String savedGeoJson = "";
    private static String savedHtml = "";
    public static String savedContent = "";

    //public static final String url = "jdbc:sqlite:C:/Users/MikhailLeshchenko/strava_plus/db/test.db"; 
    public static final String url = "jdbc:sqlite:C:\\Users\\MikhailLeshchenko\\strava_plus\\db\\test.db";
    

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
                Path path = Paths.get("html/about.html");

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

        server.createContext("/register", exchange -> {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                Path path = Paths.get("html/register.html");

                if (Files.exists(path)) {
                    byte[] htmlBytes = Files.readAllBytes(path); //in array den inhalt speichern
                    exchange.getResponseHeaders().set("Content-type", "text/html; charset=UTF-8"); //sagen dem browser, dass es html ist
                    exchange.sendResponseHeaders(200, htmlBytes.length); //200 und länge senden 

                    try (OutputStream os = exchange.getResponseBody()) { //speichern die daten der seite in exchange.getResponseBody()
                        os.write(htmlBytes);
                    }
                } else {exchange.sendResponseHeaders(404, -1);}
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    InputStream is = exchange.getRequestBody(); //die daten vorbereiten zum lesen
                    byte[] data = is.readAllBytes(); //eingabe vom user lesen                    
                    
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode jsonNode = mapper.readTree(data); //in json umwandeln
                    
                    String firstName = jsonNode.get("firstName").asText();
                    String secondName = jsonNode.get("secondName").asText();
                    int age = jsonNode.get("age").asInt();
                    String sex = jsonNode.get("sex").asText();
                    int weight = jsonNode.get("weight").asInt();
                    int height = jsonNode.get("height").asInt();
                    int maxHR = jsonNode.get("maxHR").asInt();
                    int restingHR = jsonNode.get("restingHR").asInt();
                    String username = jsonNode.get("username").asText();
                    String plainPassword = jsonNode.get("password").asText();

                    String hashedPassword = PasswordUtil.hashPasswort(plainPassword);
                    
                    System.out.println("plainPassword: " + plainPassword);
                    System.out.println("hashedPassword: " + hashedPassword); 

                    SQLite.saveNewUserToDatabase(firstName, secondName, age, sex, weight, height, maxHR, restingHR, username, hashedPassword);

                    String response = "{\"status\": \"success\", \"message\": \"User erfolgreich registriert\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);

                    System.out.println("erfolgreich gespeichert");
                    
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                    os.close();

                } catch (Throwable e) { // Throwable fängt auch NoClassDefFoundError ab!
                    System.err.println("Fehler bei der Registrierung:");
                    e.printStackTrace(); 
                    
                    // Dem Browser eine 500-Antwort schicken, damit der Stream nicht leer bleibt
                    try {
                        String errResponse = "{\"status\": \"error\", \"message\": \"" + e.getMessage() + "\"}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(500, errResponse.getBytes(StandardCharsets.UTF_8).length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(errResponse.getBytes(StandardCharsets.UTF_8));
                        }
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        });

        server.createContext("/check-username", exchange -> {
            try {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    String query = exchange.getRequestURI().getQuery(); //bekommt den vollständigen url ziel vom user, getQuery filtert den teil, der nach dem ? steht
                    String username = ""; //leere variable

                    if (query != null && query.startsWith("username=")) {
                        for (String param : query.split("&")) {
                            String[] pair = param.split("=");
                            if (pair.length > 1 && "username".equals(pair[0])) {
                                username = pair[1];
                                break;
                            }
                        }
                    }

                    boolean isTaken = SQLite.checkUsernameInDB(username);

                    String jsonResponse = "{\"isTaken\": " + isTaken + "}";

                    byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, responseBytes.length);

                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(responseBytes);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
                exchange.sendResponseHeaders(500, -1);
            } finally {
                exchange.close();
            }   
        });

        server.createContext("/profile", exchange -> {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                Path path = Paths.get("html/profile.html");

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

        server.createContext("/api/training-history", exchange -> {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                List<TrainingEntry> trainingEntries = ProfileLoader.getDistanceInMonthforChart(); //liste mit trainings für Monate
                List<TrainingEntry> distanceLastMonth = ProfileLoader.getDistanceForLastMonthFromDBForChart(); //liste mit trainings für letztes Monat

                Map<String, List<TrainingEntry>> responseMap = new HashMap<>();
                responseMap.put("allTime", trainingEntries); //für alle monate
                responseMap.put("thisMonth", distanceLastMonth); //füt letztes monat
                
                ObjectMapper mapper = new ObjectMapper();
                String jsonString = mapper.writeValueAsString(responseMap); //wandelt die daten in json-string um

                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                byte[]  responceBytes = jsonString.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, responceBytes.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responceBytes);
                } catch (Exception e) {
                    e.printStackTrace();
                    exchange.sendResponseHeaders(500, -1);
                } finally {
                    exchange.close();
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
            }
        });

        server.createContext("/api/profile", exchange -> {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                List<ProfileData> data = ProfileLoader.getProfileDataFromDB();

                ObjectMapper mapper = new ObjectMapper();
                String jsonResponse = mapper.writeValueAsString(data);

                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, responseBytes.length);

                try (OutputStream os = exchange.getResponseBody()){
                    os.write(responseBytes);
                } catch (Exception e) {
                    e.printStackTrace();
                    exchange.sendResponseHeaders(500,-1);
                }
            } else {
                exchange.sendResponseHeaders(405,-1);
            }
        });

        server.createContext("/api/upload-avatar", exchange -> {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    
                    InputStream is = exchange.getRequestBody(); //liest die eingabe-stream
                    byte[] data = is.readAllBytes();

                    Path uploadDir = Paths.get("images"); //definiert zielpfad
                    if (!Files.exists(uploadDir)) {
                        Files.createDirectories(uploadDir);
                    }

                    Path filePath = uploadDir.resolve("user_1.png"); //relative pfad-angabe für die datei
                    Files.write(filePath, data);

                    String imagePathForDB = "/images/user_1.png";
                    ProfileLoader.saveAvatarPathToDB(1, imagePathForDB); //speichert pfad in db

                    String responseText = "alles lief gut"; //schickt antwort
                    byte[] responseBytes = responseText.getBytes(StandardCharsets.UTF_8);

                    exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                    exchange.sendResponseHeaders(200, responseBytes.length);
                } catch (Exception e) {
                    e.printStackTrace();
                    exchange.sendResponseHeaders(500, -1);
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            } 
        });

        server.createContext("/api/update-profile", exchange -> { 
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())){ // Schritt 1

                try {
                    InputStream is = exchange.getRequestBody(); // Schritt 2
                    byte[] bytes = is.readAllBytes(); 
                    String stringBytes = new String(bytes, "UTF-8"); 
                    
                    // Schritt 3: JSON parsen
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode jsonNode = mapper.readTree(stringBytes);

                    String field = jsonNode.get("field").asText();
                    String value = jsonNode.get("value").asText();

                    List<String> allowedFields = List.of("name", "secondName", "age", "sex", "weight", "height", "maxHR", "restingHR");
                    if (!allowedFields.contains(field)) {
                        exchange.sendResponseHeaders(400, -1); // Bad Request bei ungültigem Spaltennamen
                        return;
                    }
                    
                    String sqlQueryToChangeData = "UPDATE users SET " + field + " = ? WHERE userID = ?";

                    try (var conn = DriverManager.getConnection(url); 
                        PreparedStatement pstmt = conn.prepareStatement(sqlQueryToChangeData)) {

                        pstmt.setString(1, value); 
                        pstmt.setInt(2, 1);        

                        pstmt.executeUpdate();
                    }

                    exchange.sendResponseHeaders(200, -1);

                } catch (Exception e) {
                    e.printStackTrace();
                    
                    exchange.sendResponseHeaders(500, -1);
                } finally {
                    exchange.close(); 
                }
                
            } else {
                exchange.sendResponseHeaders(405, -1); 
                exchange.close();
            }
        });

        server.createContext("/images", exchange -> {
            try {
                String requestPath = exchange.getRequestURI().getPath();
                Path filePath = Paths.get("." + requestPath);

                if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                    byte[] bytes = Files.readAllBytes(filePath);

                    exchange.getResponseHeaders().set("Content-Type", "image/png");
                    exchange.sendResponseHeaders(200, bytes.length);

                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                } else {
                    exchange.sendResponseHeaders(404, -1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                exchange.sendResponseHeaders(500, -1);
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
                
                Path path = Paths.get("html/abzeichnungen.html"); // verweist auf die datei in ordner

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
                Path path = Paths.get("html/challenges.html"); // verweist auf die datei in ordner

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