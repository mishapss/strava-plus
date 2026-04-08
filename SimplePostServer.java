import com.sun.net.httpserver.*; // import von allen eingebauten Java-HTTP-Server Klassen

import java.io.IOException; // Fehlerbehandlung
import java.io.OutputStream; // um antworten an den Client zu senden
import java.net.InetSocketAddress; // um die addresse und den Port zu definieren
import java.io.InputStream; // um die saten vom Client zu lesen

public class SimplePostServer{
    
    private static String savedContent = "";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);                              // erstellt einen http-server auf port 8000; localhost (standard)
                                      

        server.createContext("/notes", exchange -> {                                                        // definiert einen pfad; alles, was an "/notes" geschickt wird, landet hier
            
            if ("POST".equals(exchange.getRequestMethod())) {                                               // prüft ob es eine post-anfrage ist
                InputStream is = exchange.getRequestBody();                                                 // holt den body aus der anfrage (genau das, was FileUploader geschickt hat)
                byte[] data = is.readAllBytes();                                                            // liest alle Bytes aus dem Body; komplette Datei im Speicher
                savedContent = new String(data, "UTF-8");                                                   // wandelt Bytes in text um, dait sie eingelesen werden können

                System.out.println("File content: " + savedContent);
                System.out.println("received Data size: " + data.length);

                String response = "POST request received";
                exchange.sendResponseHeaders(200, response.getBytes().length);                              // sendet, dass statuscode 200 ist (alles ok) und die lände der antwort
                try (OutputStream os = exchange.getResponseBody()) {                                        // öffnet den response-body
                    os.write(response.getBytes("UTF-8"));                                                          // schreibt den text rein, das später an client gesendet wird
                }
            
            } else if ("GET".equals(exchange.getRequestMethod())) {                                         // browser sendet immer get; ich brauche die Teil, um etwas im Browser zu sehen
                String response = "<html><body>"
                + "<meta charset='UTF-8'>"
                + "<h1>Upload Server läuft</h1>"
                + "<p>Nutze FileUploader für POST.</p>"
                + "<pre>" + savedContent + "</pre>";                                                        // HTML seite als string
                
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");              // sagt dem browser, dass es html ist
                exchange.sendResponseHeaders(200, response.getBytes().length);                              
                try (OutputStream os = exchange.getResponseBody()) {                                        // html wird an der seite gesendet und angezeigt
                    os.write(response.getBytes("UTF-8"));
                }
            
            } else {
                exchange.sendResponseHeaders(405, -1); //Method ist nicht erlaubt
            }
        });
        server.start();
        System.out.println("Server started on port 8000");
    }
}
