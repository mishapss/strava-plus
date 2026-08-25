import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProfileLoader {
    //public static final String url = "jdbc:sqlite:C:\\Users\\User\\projects_programming\\strava-plus-main\\db\\test.db";
    public static final String url = "jdbc:sqlite:C:\\Users\\MikhailLeshchenko\\strava_plus\\db\\test.db";

    public static List<ProfileData> getProfileDataFromDB() { //nimmt Daten aus users tabelle und gibt list zurück
        List<ProfileData> list = new ArrayList<>();
    
        String sqlQueryGetData = "SELECT * FROM users WHERE userID = ?";

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlQueryGetData)) {
                
                pstmt.setInt(1,1);

                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) { //felder aus db
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
                        rs.getDouble("maxDistance"),
                        rs.getDouble("maxSpeed"),
                        rs.getDouble("maxElevationGain"),
                        rs.getString("profilePhoto"),
                        rs.getDouble("calorieBurn"),
                        rs.getInt("trainingLoad")
                    ));
                }
            }catch (SQLException e) {
                e.printStackTrace();
            }
        return list;
    }

    public static List<TrainingEntry> getDistanceInMonthforChart() { //bekommt Distanz für jedes Monat aus DB
        String sqlQueryGetDistance = """
        SELECT strftime('%Y-%m', date) AS monat, 
        SUM(distance) AS totalDistance FROM
        training GROUP BY strftime('%Y-%m', date) 
        ORDER BY monat ASC
        """;              
                
        List<TrainingEntry> listMonths = new ArrayList<>();

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlQueryGetDistance)) {
                
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
    }

    public static List<TrainingEntry> getDistanceForLastMonthFromDBForChart() { //bekommt Distanz des letzten Monat aus DB
        String sqlQueryGetDistance = """
        SELECT date, distance FROM training 
        WHERE strftime('%Y-%m', date) = 
        strftime('%Y-%m', 'now')
        ORDER BY date ASC
        """;
                
        List<TrainingEntry> listLastMonth = new ArrayList<>();

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlQueryGetDistance)) {
                
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