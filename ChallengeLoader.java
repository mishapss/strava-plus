import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class ChallengeLoader {
    public static final String url = "jdbc:sqlite:C:/Users/MikhailLeshchenko/strava_plus/db/test.db";

    public static List<ChallengeData> getDatenAusDBToUpload() {//nimmt daten aus db und gibt als json zurück
        List<ChallengeData> liste = new ArrayList<>();
        String sqlAbfrage = "SELECT challengeName, challengeDescription, challengeStartDate, challengeEndDate, ziel, status FROM challenges";

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlAbfrage)) {

                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    liste.add(new ChallengeData(
                        rs.getString("challengeName"), 
                        rs.getString("challengeDescription"), 
                        rs.getString("challengeStartDate"), 
                        rs.getString("challengeEndDate"), 
                        rs.getInt("status"), 
                        rs.getInt("ziel")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        return liste;
    }
}