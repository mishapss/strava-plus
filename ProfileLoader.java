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
                        rs.getInt("ruheHR"),
                        rs.getDouble("gesamteDistanzProJahr"),
                        rs.getDouble("maxDistance"),
                        rs.getDouble("maxSpeed"),
                        rs.getDouble("maxElevationGain"),
                        rs.getString("profilePhoto")
                    ));
                }
            }catch (SQLException e) {
                e.printStackTrace();
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