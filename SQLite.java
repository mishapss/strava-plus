import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLite {
    public static int trainingLoad;


    public static void connect(int trainingLoad) {

        var url = "jdbc:sqlite:C:/Users/MikhailLeshchenko/strava_plus/db/test.db";

        String a = "Mikhail233";

        try (var conn = DriverManager.getConnection(url); //verbindung zum DB
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users(name, second) VALUES(?,?)")) { //vorbereitung des sql befehls
            
            pstmt.setInt(1, trainingLoad);
            pstmt.setString(2, a);
            pstmt.executeUpdate();

            System.out.println("Verbindung zum DB hergestellt + Daten gespeichert");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static int addTrainingDatenToDB(
        String dateString, double distanceBetweenPointsGerundet, String time, String activeTimeFormatted,
        double averageSpeedGerundet, double maxGeschwindigkeitKmh, double hoeheSummeGerundet,int averageHR,
        int maxHeartRate, int trainingLoad, double aerobicTrainingEffect, double anaerobicTrainingEffect,
        double kalorien,String dateiName) {

        int training_id = -1;

        var url = "jdbc:sqlite:C:/Users/MikhailLeshchenko/strava_plus/db/test.db";

        try (var conn = DriverManager.getConnection(url); //verbindung zum DB
            PreparedStatement pstmt = conn.prepareStatement(
                """
                INSERT INTO training (
                    datum, 
                    distanz, 
                    dauer,
                    activeTime,
                    durchschnittlicheGeschwindigkeit,
                    maxGeschwindigkeit, 
                    hoehemeter,
                    durchschnittPuls,
                    maxPuls,
                    trainingsbelastung,
                    aerobicTrainingsEffekt,  
                    anaeobicTrainingsEffekt,
                    kalorien,
                    dateiName
                )
                VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                Statement.RETURN_GENERATED_KEYS);
            )
            { 
            
                pstmt.setString(1, dateString);
                pstmt.setDouble(2, distanceBetweenPointsGerundet);
                pstmt.setString(3, time);
                pstmt.setString(4, activeTimeFormatted);
                pstmt.setDouble(5, averageSpeedGerundet);
                pstmt.setDouble(6, maxGeschwindigkeitKmh);
                pstmt.setDouble(7, hoeheSummeGerundet);
                pstmt.setInt(8, averageHR);
                pstmt.setInt(9, maxHeartRate);
                pstmt.setInt(10, trainingLoad);
                pstmt.setDouble(11, aerobicTrainingEffect);
                pstmt.setDouble(12, anaerobicTrainingEffect);
                pstmt.setDouble(13, kalorien);
                pstmt.setString(14, dateiName);

                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                

                if (rs.next()) {
                    training_id = rs.getInt(1);
                }

                System.out.println("Verbindung zum DB hergestellt + Daten gespeichert");
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            return training_id;
    }

    public static void addHRDatenToDB(
        int training_id,
        String timeIn0HrZone, 
        String timeIn1HrZone, 
        String timeIn2HrZone, 
        String timeIn3HrZone, 
        String timeIn4HrZone,
        String timeIn5HrZone,
        int averageHR,
        int maxHeartRate) {

        var url = "jdbc:sqlite:C:/Users/MikhailLeshchenko/strava_plus/db/test.db";

        try (var conn = DriverManager.getConnection(url); //verbindung zum DB
            PreparedStatement pstmt = conn.prepareStatement(
                " INSERT INTO heart_zone " + 
                " (training_id, zone0, zone1, zone2, zone3, zone4," + 
                "  zone5, durchschnittPuls, maxPuls) " + 
                " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)")) { 
            
            pstmt.setInt(1, training_id);
            pstmt.setString(2, timeIn0HrZone);
            pstmt.setString(3, timeIn1HrZone);
            pstmt.setString(4, timeIn2HrZone);
            pstmt.setString(5, timeIn3HrZone);
            pstmt.setString(6, timeIn4HrZone);
            pstmt.setString(7, timeIn5HrZone);
            pstmt.setInt(8, averageHR);
            pstmt.setInt(9, maxHeartRate);


            pstmt.executeUpdate();

            System.out.println("Verbindung zum DB hergestellt + Daten gespeichert");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        //File xmlFile = new File(fileName);
        //ArrayList<TrkPt> points = XmlReader.loadGpx(fileName);
        
        //connect();
    }
}