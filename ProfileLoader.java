import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProfileLoader {
    //public static final String url = "jdbc:sqlite:C:\\Users\\User\\projects_programming\\strava-plus-main\\db\\test.db";
    public static final String url = "jdbc:sqlite:C:\\Users\\MikhailLeshchenko\\strava_plus\\db\\test.db";

    public static int getMaxDistanceFromTrainingTable() {
        
        int loggedUserID = UserSession.getCurrentUserID();
        int maxDistanceTrainingID = -1;
        

        String sqlQuery = "SELECT distance, trainingID FROM training WHERE userID = ? ORDER BY distance DESC LIMIT 1";

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {
                
                pstmt.setInt(1, loggedUserID);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    
                    maxDistanceTrainingID = rs.getInt("trainingID");
                    
                }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return maxDistanceTrainingID;
        
    }

    public static List<ProfileData> getProfileDataFromDB() { 
        List<ProfileData> list = new ArrayList<>();
        int loggedInUserID = UserSession.getCurrentUserID();

        if (loggedInUserID == -1) {
            System.err.println("Kein Nutzer eingeloggt");
            return list;
        }

        String sqlQueryPersonalBest = """
                SELECT 
                trainingID, 
                distance, 
                maxSpeed, 
                elevationGain, 
                calories, 
                trainingLoad, 
                fileName 
                FROM training 
                WHERE userID = ?
                """;

        double maxDistance = 0.0;
        String maxDistanceFileName = "";
        double maxSpeed = 0.0;
        String maxSpeedFileName = "";
        double maxElevationGain = 0.0;
        String maxElevationGainFileName = "";
        double maxCalories = 0.0;
        String maxCaloriesFileName = "";
        int maxTrainingLoad = 0;
        String maxTrainingLoadFileName = "";

        // 1. Schritt: Bestleistungen ermitteln
        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlQueryPersonalBest)) {
                
                pstmt.setInt(1, loggedInUserID);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    double distance = rs.getDouble("distance");
                    double speed = rs.getDouble("maxSpeed");
                    double elevationGain = rs.getDouble("elevationGain");
                    double calories = rs.getDouble("calories");
                    int trainingLoad = rs.getInt("trainingLoad");
                    String fileName = rs.getString("filename");

                    if (distance > maxDistance) {
                        maxDistance = distance;
                        maxDistanceFileName = fileName;
                    }
                    if (speed > maxSpeed) {
                        maxSpeed = speed;
                        maxSpeedFileName = fileName;
                    }
                    if (elevationGain > maxElevationGain) {
                        maxElevationGain = elevationGain;
                        maxElevationGainFileName = fileName;
                    }
                    if (calories > maxCalories) {
                        maxCalories = calories;
                        maxCaloriesFileName = fileName;
                    }
                    if (trainingLoad > maxTrainingLoad) {
                        maxTrainingLoad = trainingLoad;
                        maxTrainingLoadFileName = fileName;
                    }
                } 
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int maxDistanceTrainingID = getMaxDistanceFromTrainingTable();

        // 2. Schritt: Benutzerdaten abrufen und zusammenführen
        String sqlQueryGetData = """
            SELECT 
            userID,
            name,
            secondName,
            age,
            sex,
            weight,
            height,
            maxHR,
            restingHR,
            totalDistancePerYear,
            photoFilePath,
            username
            FROM users 
            WHERE userID = ?
        """;

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlQueryGetData)) {
                
            pstmt.setInt(1, loggedInUserID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(new ProfileData(
                    rs.getInt("userID"),
                    rs.getString("name"),
                    rs.getString("secondName"),
                    rs.getInt("age"),
                    rs.getString("sex"),
                    rs.getDouble("weight"),
                    rs.getDouble("height"),
                    rs.getInt("maxHR"),
                    rs.getInt("restingHR"),
                    rs.getDouble("totalDistancePerYear"),
                    maxDistanceTrainingID,
                    maxDistance,
                    maxDistanceFileName,
                    maxSpeed,
                    maxSpeedFileName,
                    maxElevationGain,
                    maxElevationGainFileName,
                    maxCalories,
                    maxCaloriesFileName,
                    maxTrainingLoad,
                    maxTrainingLoadFileName,
                    rs.getString("photoFilePath"),
                    rs.getString("username")
                ));
            }
        } catch (SQLException f) {
            f.printStackTrace();
        }

        return list;
    }


    public static List<TrainingEntry> getDistanceInMonthforChart() { //bekommt Distanz für jedes Monat aus DB
        List<TrainingEntry> list = new ArrayList<>();
        
        int loggedInUserID = UserSession.getCurrentUserID();

        if (loggedInUserID != -1) {
            String sqlQueryGetDistance = """
            SELECT strftime('%Y-%m', date) AS monat, 
            SUM(distance) AS totalDistance 
            FROM training 
            WHERE authorID = ? 
            GROUP BY strftime('%Y-%m', date) 
            ORDER BY monat ASC
            """;              
                    
            List<TrainingEntry> listMonths = new ArrayList<>();

            try (var conn = DriverManager.getConnection(url);
                PreparedStatement pstmt = conn.prepareStatement(sqlQueryGetDistance)) {

                    pstmt.setInt(1, loggedInUserID);

                    ResultSet rs = pstmt.executeQuery();

                    while(rs.next()) {
                        listMonths.add(new TrainingEntry(
                            rs.getString("monat"),
                            rs.getDouble("totalDistance")
                        ));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            System.out.print(listMonths);
            return listMonths;
        } else {
            System.out.println("kein Nutzer eingelogt");
        }

        return list;
        
    }

    public static List<TrainingEntry> getDistanceForLastMonthFromDBForChart() { //bekommt Distanz des letzten Monat aus DB
        List<TrainingEntry> list = new ArrayList<>();
        
        int loggedInUserId = UserSession.getCurrentUserID();
        
        if (loggedInUserId != -1) {
            String sqlQueryGetDistance = """
            SELECT date, distance FROM training 
            WHERE authorID = ? AND
            strftime('%Y-%m', date) = strftime('%Y-%m', 'now')
            ORDER BY date ASC 
            """;
                    
            List<TrainingEntry> listLastMonth = new ArrayList<>();

            try (var conn = DriverManager.getConnection(url);
                PreparedStatement pstmt = conn.prepareStatement(sqlQueryGetDistance)) {

                    pstmt.setInt(1, loggedInUserId);
                    
                    ResultSet rs = pstmt.executeQuery();

                    while(rs.next()) {
                        listLastMonth.add(new TrainingEntry(
                            rs.getString("date"),
                            rs.getDouble("distance")
                        ));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            System.out.print(listLastMonth);
            return listLastMonth;
        } else {
            System.out.println("kein Nutzer eingelogt");
        }

        return list;
        
    }

    public static void saveAvatarPathToDB(int userID, String imagePath) {
        String sqlQueryToSaveImagePath = "UPDATE users SET profilePhoto = ? WHERE userID = ?";

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlQueryToSaveImagePath)) {
                pstmt.setString(1, imagePath);
                pstmt.setInt(2,1);

                pstmt.executeUpdate();
                
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }
}