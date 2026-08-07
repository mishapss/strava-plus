import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLite {
    public static int trainingLoad;

    public static final String url = "jdbc:sqlite:C:/Users/MikhailLeshchenko/strava_plus/db/test.db";

    private static void setParameters(PreparedStatement ptsmt, Object... values) throws SQLException{ //kann so viele werten übergeben, wie ich will

        for (int i = 0; i < values.length; i++) { //
            ptsmt.setObject(i + 1, values[i]); //setzt selber wert und fügt in db
        }
    }    

    public static int[] getHRDaten(){
        String sqlAbfrage = "SELECT maxHR, ruheHR FROM users WHERE userID = ?";

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlAbfrage)) {
                
                pstmt.setInt(1, 1);
                ResultSet rs = pstmt.executeQuery(); //führt die abfrage aus und liefert das ergebnis

                if (rs.next()) {
                    int maxHR = rs.getInt("maxHR");
                    int ruheHR = rs.getInt("ruheHR");
                    int[] hr = {maxHR, ruheHR};

                    return hr;
                };
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
            return null;
    }

    public static int getTrainingID(double distanceBetweenPointsGerundet, String dateString) {
        String sqlAbfrage = "SELECT trainingID FROM training WHERE distanz = ? AND datum = ?";

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement ptsmt = conn.prepareStatement(sqlAbfrage)) {
                ptsmt.setDouble(1, distanceBetweenPointsGerundet);
                ptsmt.setString(2, dateString);

                ResultSet rs = ptsmt.executeQuery();

                if (rs.next()) {
                    return rs.getInt("trainingID");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return -1;
    }

    public static boolean checkTrainingID(int training_id) {
        String sqlAbfrage = "SELECT training_id FROM heart_zone WHERE training_id = ?";

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement ptsmt = conn.prepareStatement(sqlAbfrage)) {

                ptsmt.setInt(1, training_id);

                ResultSet rs = ptsmt.executeQuery();

                return rs.next();

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
    }
    
    //Methode um die verbindung zur db erstellen
    /* 
    public static Connection connect() throws SQLException{
        try (var conn = DriverManager.getConnection(url); //verbindung zum DB
            PreparedStatement pstmt = conn.prepareStatement(sqlAbfrage)) { //vorbereitung des sql befehls            

            System.out.println("Verbindung zum DB hergestellt + Daten gespeichert");
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }*/

    public static int addTrainingDatenToDB(
        String dateString,
        double distanceBetweenPointsGerundet, 
        String time, 
        String activeTimeFormatted,
        double averageSpeedGerundet, 
        double maxGeschwindigkeitKmh, 
        double hoeheSummeGerundet,
        int averageHR,
        int maxHeartRate, 
        int trainingLoad, 
        double aerobicTrainingEffect, 
        double anaerobicTrainingEffect,
        double kalorien,
        String dateiName
        ) {

        

        int training_id = -1;

        boolean trainingVorhanden = checkTraining(distanceBetweenPointsGerundet, dateString);

        if (trainingVorhanden) {
            training_id = getTrainingID(distanceBetweenPointsGerundet, dateString);
            return training_id;
        }

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
            setParameters( //einfügt die daten in db
                pstmt,
                dateString,
                distanceBetweenPointsGerundet,
                time,
                activeTimeFormatted,
                averageSpeedGerundet,
                maxGeschwindigkeitKmh,
                hoeheSummeGerundet,
                averageHR,
                maxHeartRate,
                trainingLoad,
                aerobicTrainingEffect,
                anaerobicTrainingEffect,
                kalorien,
                dateiName
            ); 

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

        boolean trainingIDVorhanden = checkTrainingID(training_id);

        if (trainingIDVorhanden) {
            return;
        }

        String sql = """
            INSERT INTO heart_zone (
                training_id,
                zone0,
                zone1,
                zone2,
                zone3,
                zone4,
                zone5,
                durchschnittPuls,
                maxPuls
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (var conn = DriverManager.getConnection(url); //verbindung zum DB
            PreparedStatement pstmt = conn.prepareStatement(sql)) { 

            setParameters(
                pstmt,
                training_id,
                timeIn0HrZone,
                timeIn1HrZone,
                timeIn2HrZone,
                timeIn3HrZone,
                timeIn4HrZone,
                timeIn5HrZone,
                averageHR,
                maxHeartRate
            );
                       
            pstmt.executeUpdate();

            System.out.println("Verbindung zum DB hergestellt + Daten gespeichert");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static boolean checkTraining(double distanceBetweenPointsGerundet, String dateString) {
        //abfrage zur tabelle, ob das training schon hochgeladen wurde
        String sqlAbfrage = "SELECT * FROM training WHERE distanz = ? and DATUM = ?";

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlAbfrage)) {
                
                pstmt.setDouble(1, distanceBetweenPointsGerundet);
                pstmt.setString(2, dateString);
                ResultSet rs = pstmt.executeQuery(); //führt die abfrage aus und liefert das ergebnis

                return rs.next();

            } catch (SQLException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
                return false;
            }
    }

    public static double getDistanzAusDB(String name) { //methode prüfen
        String sqlAbfrage = "SELECT gesamteDistanzproJar FROM users WHERE name = ?";

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlAbfrage)) {

                pstmt.setString(1, name);

                ResultSet rs = pstmt.executeQuery(); //führt die abfrage aus und liefert das ergebnis
                
                if (rs.next()) {
                    return rs.getDouble("gesamteDistanzProJahr");
                }

            } catch (SQLException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }

            return 0.0;
    }

    public static void addDistanzToJahr(String dateString, double distanceBetweenPointsGerundet) { //methode um die distanz zu datenbank distanzProJahr hinzufügen

    }

    public static void main(String[] args) {
        //File xmlFile = new File(fileName);
        //ArrayList<TrkPt> points = XmlReader.loadGpx(fileName);
        
        //connect();
    }
}