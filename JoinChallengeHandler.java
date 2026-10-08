import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.charset.StandardCharsets;

public class JoinChallengeHandler implements HttpHandler{ //die klasse kann die methoden httphandler benutzen
    //public static final String url = "jdbc:sqlite:C:\\Users\\User\\projects_programming\\strava-plus-main\\db\\test.db";
    public static final String url = "jdbc:sqlite:C:\\Users\\MikhailLeshchenko\\strava_plus\\db\\test.db";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-type");

        String method = exchange.getRequestMethod();

        if ("OPTIONS".equals(method)) {
            exchange.sendResponseHeaders(204, -1);
            return; //damit nur options-anfragen abbrechen
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) { //b-teil
            try {
                InputStream is = exchange.getRequestBody(); //c-teil
                JsonNode isJson = objectMapper.readTree(is); 
                System.out.println("Empfangener Body: " + isJson);

                if (isJson == null || !isJson.has("challengeID")) {
                    System.out.println("JSON ungültig oder challengeID fehlt!");
                    exchange.sendResponseHeaders(400, -1);
                    return; 
                }

                int challengeID = isJson.get("challengeID").asInt();
                
                //Anfrage an DB, um die restliche daten für saveparticipation zu bekommen
                String sqlQueryGetData = "SELECT goal, challengeStartDate, challengeEndDate, goalDataType FROM challenges WHERE challengeID = ?";
                int goal = 0;
                String challengeStartDate = null;
                String challengeEndDate = null;
                String goalDataType = null;

                try(var conn = DriverManager.getConnection(url);
                    PreparedStatement pstmt = conn.prepareStatement(sqlQueryGetData)) {
                        pstmt.setInt(1, challengeID);

                        ResultSet rs = pstmt.executeQuery();

                        if (rs.next()) {
                            goal = rs.getInt("goal");
                            challengeStartDate = rs.getString("challengeStartDate");
                            challengeEndDate = rs.getString("challengeEndDate");
                            goalDataType = rs.getString("goalDataType");
                        } else {
                            // Challenge wurde in DB nicht gefunden -> 404 Not Found zurückgeben
                            System.out.println("Challenge mit ID " + challengeID + " existiert nicht.");
                            exchange.sendResponseHeaders(404, -1);
                            return;
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        exchange.sendResponseHeaders(500, -1);
                    }


                boolean successfullySave = ChallengeLoader.saveParticipationInDB(challengeID, goal, challengeStartDate, challengeEndDate, goalDataType); //d-teil

                String jsonResponse = "{\"success\": " + successfullySave + "}"; //1.e-teil 

                byte[] jsonResponseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);             // 2.e-teil

                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8"); // 3.e-teil

                exchange.sendResponseHeaders(200, jsonResponseBytes.length); //4.e-teil

                try (OutputStream os = exchange.getResponseBody()) { //5.e-teil
                    os.write(jsonResponseBytes);
                }
            } catch (Exception e) {
                e.printStackTrace();
                exchange.sendResponseHeaders(405, -1); // f-teil
            }
        } 
    }     
}