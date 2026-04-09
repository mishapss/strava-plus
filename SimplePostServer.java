import com.sun.net.httpserver.*; // import von allen eingebauten Java-HTTP-Server Klassen
import java.io.*;


import java.io.IOException; // Fehlerbehandlung
import java.io.OutputStream; // um antworten an den Client zu senden
import java.net.InetSocketAddress; // um die addresse und den Port zu definieren
import java.io.InputStream; // um die saten vom Client zu lesen

public class SimplePostServer{
    private static String savedGeoJson = "";
    private static String savedHtml = "";
    private static String savedContent = "";

    public static void main(String[] args) throws IOException, Exception {
        XmlReader.loadGpx("ride.gpx");                                                                      //gpx lesen und geojson erstellen
        System.out.println("GeoJSON erzeugt: " + XmlReader.geoJsonData);

        savedGeoJson = XmlReader.geoJsonData;                                                               //Server speichert die route

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);                              // erstellt einen http-server auf port 8000; localhost (standard)
                                      

        server.createContext("/geojson", exchange -> {                                                      //http://localhost:8000/geojson
            String method = exchange.getRequestMethod();                                                    //get oder post

            if ("GET".equals(method)) {
                // GeoJSON aus XmlReader holen
                String response = (savedGeoJson != null) ? savedGeoJson : "{}";                             

                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");       //sagt dem browser, dass es json ist
                exchange.sendResponseHeaders(200, response.getBytes().length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes("UTF-8"));                                                   //sendet daten
                }

            } else if ("POST".equals(method)) {
                // Datei vom FileUploader empfangen 
                InputStream is = exchange.getRequestBody();                                                 //daten vom user lesen
                byte[] data = is.readAllBytes();
                savedContent = new String(data, "UTF-8");

                String response = "GeoJson uploaded";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes("UTF-8"));
                }

            } else {
                // Andere Methoden nicht erlaubt
                exchange.sendResponseHeaders(405, -1);
            }
        });

        server.createContext("/upload-html", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
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

        
        server.start();
        System.out.println("Server started on port 8000");
        System.out.println("Post geojson to /geojson");
        System.out.println("Post geojson to /upload-html");
    }
}