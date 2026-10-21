import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;

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
        int loggedUserId = UserSession.getCurrentUserID();
        
        String sqlAbfrage = """
        SELECT 
            c.challengeID, 
            c.challengeName, 
            c.challengeDescription, 
            c.challengeStartDate,
            c.challengeEndDate, 
            c.goal, 
            c.pictureChallenge, 
            c.pictureReward,
            cut.status
        FROM challenges c
        LEFT JOIN challengeUserTable cut
            ON c.challengeID = cut.challengeID AND cut.userID = ?
        """;
                
        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlAbfrage)) {

                pstmt.setInt(1, loggedUserId);

                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    int status = rs.getInt("status");

                    liste.add(new ChallengeData(
                        rs.getInt("challengeID"),
                        rs.getString("challengeName"), 
                        rs.getString("challengeDescription"), 
                        rs.getString("challengeStartDate"), 
                        rs.getString("challengeEndDate"), 
                        status, 
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

    public static boolean challengePruefer(int challengeID, int userID) { //prüft ob benutzer beim challenge teilnimmt
        String sqlAbfrage = "SELECT status FROM challengeUserTable WHERE challengeID = ? AND userID = ?";

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlAbfrage)) {

                pstmt.setInt(1, challengeID);
                pstmt.setInt(2, userID);
                System.out.print("challengeID: " + challengeID + "userID: " + userID);
                
                ResultSet rs = pstmt.executeQuery();               

                if (rs.next()) {
                    return rs.getInt(1) > 0; //true, wenn eintrag existiert
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
    }

    public static double parseTimeStringToMin(String activeTime) {
        try {
            String[] parts = activeTime.split(":");
            if (parts.length == 3) {
                // Format: HH:mm:ss (z.B. 1:37:16)
                double hours = Double.parseDouble(parts[0]);
                double minutes = Double.parseDouble(parts[1]);
                double seconds = Double.parseDouble(parts[2]);
                
                // Umrechnung in Gesamtminuten
                return (hours * 60.0) + minutes + (seconds / 60.0);
            } else if (parts.length == 2) {
                // Falls mal nur Minuten:Sekunden dastehen (mm:ss)
                double minutes = Double.parseDouble(parts[0]);
                double seconds = Double.parseDouble(parts[1]);
                return minutes + (seconds / 60.0);
            }
        } catch (NumberFormatException e) {
            System.err.println("Fehler beim Parsen der Zeit: " + activeTime);
        }
        return 0.0;
    }

    //prüft alte trainings
    public static double checkOldTrainingsToUploadInChallenge(int userID, String startDatum, String endDatum) {
        String sqlAbfrageSelectAllTrainings = """
            SELECT
            date,
            distance,
            duration
            FROM training 
            WHERE userID = ? and date >= ? AND date < ?""";

        double oldDistance = 0.0;
        int oldDuration = 0;
        System.out.println("Suche alte Trainings für User: " + userID + " von " + startDatum + " bis " + endDatum);

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlAbfrageSelectAllTrainings)) {

                pstmt.setInt(1, userID);
                pstmt.setString(2, startDatum);
                pstmt.setString(3, endDatum);

                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    double distance = rs.getDouble("distance");
                    String trainingDate = rs.getString("date");
                    String duration = rs.getInt("activeTime");
//TODO: MIT DER ZEITPARSUNG SICH SCHLAU MACHEN
                    if (duration != null && !duration.isEmpty()) {
                        double minutes = parseTimeStringToMin(duration);
                        oldDuration += minutes;
                    }

                    System.out.println("Gefundenes altes Training am " + trainingDate + " mit Distanz: " + distance);
                    oldDistance += distance;
                    
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        System.out.println("Berechnete alte Gesamtdistanz: " + oldDistance);
        return oldDistance;
    }

    public static boolean saveParticipationInDB(
        int challengeID, 
        int goal, 
        String challengeStartDate, 
        String challengeEndDate, 
        String goalDataType) { 

        String sqlAbfrage = """
        INSERT INTO challengeUserTable (
        challengeID,
        userID, 
        status,
        goal,
        challengeStartDate,
        challengeEndDate,
        progressValue,
        challengeProgress,
        goalDataType
        )
        VALUES (?, ?, 1, ?, ?, ?, 0.0, 0.0, ?)
        """;
        
        int loggedUserID = UserSession.getCurrentUserID();
        System.out.println("debug");
        
        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlAbfrage)) {
                
                pstmt.setInt(1, challengeID);
                pstmt.setInt(2, loggedUserID);
                pstmt.setInt(3, goal);
                pstmt.setString(4, challengeStartDate);
                pstmt.setString(5, challengeEndDate);
                pstmt.setString(6, goalDataType);

                int rowsAffected = pstmt.executeUpdate(); 

                if (rowsAffected > 0) {
                    // ERST HIER, nach erfolgreichem INSERT, alte Trainings holen und einfügen!
                    double oldDistance = checkOldTrainingsToUploadInChallenge(loggedUserID, challengeStartDate, challengeEndDate);
                    
                    if (oldDistance > 0) {
                        updateStartChallengeDataKilometers(challengeID, loggedUserID, oldDistance, goal);
                    }
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
        }

        return false;
    }

    public static void updateStartChallengeDataKilometers(int challengeID, int userID, double oldDistance, int goal) { 

        String sqlAbfrageUpdateKM = """
            UPDATE challengeUserTable 
            SET progressValue = ?
            WHERE challengeID = ? AND userID = ? AND goalDataType = 'km'
            """;

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlAbfrageUpdateKM)) {

                pstmt.setDouble(1, oldDistance);
                pstmt.setInt(2, challengeID);
                pstmt.setInt(3, userID);
                pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }


        //challengeprogress updaten
        String sqlAbfrageUpdateChallengeProgress = """
            UPDATE challengeUserTable
            SET challengeProgress = ?
            WHERE challengeID = ? AND userID = ? AND goalDataType = 'km'
            """;        

        try (var connection = DriverManager.getConnection(url);
            PreparedStatement pstmt = connection.prepareStatement(sqlAbfrageUpdateChallengeProgress)) {

                pstmt.setDouble(1, (oldDistance / goal));
                pstmt.setInt(2, challengeID);
                pstmt.setInt(3, userID);
                pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    } 
}