//import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ObjectNode;
// com.sun.net.httpserver.Authenticator.Result;


public class ChallengeLoader {
    public static final String url = "jdbc:sqlite:C:/Users/MikhailLeshchenko/strava_plus/db/test.db";

    public static List<ChallengeData> getDatenAusDBToUpload() {//nimmt daten aus db für upload auf der webseite und gibt als json zurück
        List<ChallengeData> liste = new ArrayList<>();
        String sqlAbfrage = "SELECT challengeID, challengeName, challengeDescription, challengeStartDate, challengeEndDate, ziel, status, bild FROM challenges";

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlAbfrage)) {

                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    liste.add(new ChallengeData(
                        rs.getInt("challengeID"),
                        rs.getString("challengeName"), 
                        rs.getString("challengeDescription"), 
                        rs.getString("challengeStartDate"), 
                        rs.getString("challengeEndDate"), 
                        rs.getInt("status"), 
                        rs.getInt("ziel"),
                        rs.getString("bild")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        return liste;
    }

    public static boolean challengePruefer(int challengeID) {
        String sqlAbfrage = "SELECT status FROM challenges WHERE challengeID = ?";

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlAbfrage)) {

                pstmt.setInt(1, challengeID);
                
                ResultSet rs = pstmt.executeQuery();               

                if (rs.next()) {
                    return rs.getInt(1) > 0; //true, wenn eintrag existiert
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
    }

    public static boolean saveParticipationInDB(int challengeID) {
        String sqlAbfrage = "UPDATE challenges SET status = 1 WHERE challengeID = ?";

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlAbfrage)) {
                
                pstmt.setInt(1, challengeID);

                int rowsAffected = pstmt.executeUpdate(); //gibt zurück wie viele Zeilen geändert wurden

                return rowsAffected > 0;
            }
            catch (SQLException e) {
                e.printStackTrace();
                return false;
            }

    }
}