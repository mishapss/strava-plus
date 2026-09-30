import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class ChallengeLoader {
    //public static final String url = "jdbc:sqlite:C:\\Users\\User\\projects_programming\\strava-plus-main\\db\\test.db";
    public static final String url = "jdbc:sqlite:C:\\Users\\MikhailLeshchenko\\strava_plus\\db\\test.db";

    public static List<Integer> getChallengeIDFromChallengeUserTable() { //bekommt challengeID mit status 1
        int userID = UserSession.getCurrentUserID();
        List<Integer> challengeIDs = new ArrayList<>(); 
        String sqlQueryGetChallengeID = "SELECT challengeID FROM challengeUserTable WHERE userID = ? AND status = 1";

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlQueryGetChallengeID)) {
                pstmt.setInt(1, userID);

                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    challengeIDs.add(rs.getInt("challengeID"));
                    
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        return challengeIDs;
    }

    //funktion, die status anzeigt
    public static List<Integer> getStatusesFromDB() {
        List<Integer> statuses = new ArrayList<>();
        return statuses;
    }

    public static List<ChallengeData> getDatenAusDBToUpload() {//nimmt daten aus db für upload auf der webseite und gibt als json zurück
        //status aus challengeUserTable bekommen für user und nur für users challenges anzeigen
        //zeigt alle verfügbare challenges an
        List<ChallengeData> liste = new ArrayList<>();
        //int loggedInUserId = UserSession.getCurrentUserID();

        String sqlAbfrage = "SELECT challengeID, challengeName, challengeDescription, challengeStartDate, challengeEndDate, goal, pictureChallenge, pictureReward FROM challenges";
        //String sqlAbfrageForStaus = "SELECT status FROM challengeUserTable WHERE userId = ? AND challengeID = ?";
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
                        //rs.getInt("status"), 
                        rs.getInt("goal"),
                        rs.getString("pictureChallenge"),
                        rs.getString("pictureReward")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        return liste;
    }

    public static boolean challengePruefer(int challengeID) { //prüft ob benutzer beim challenge teilnimmt
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

    public static boolean saveParticipationInDB(int challengeID) { //speichert 1 in db, wenn benutzer auf button "an herausforderung teilnehmen" clickt
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