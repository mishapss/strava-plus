import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

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
                getDistanzJahrAusDB(2026);

                return rs.next();

            } catch (SQLException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
                return false;
            }
    }

    public static double getDistanzJahrAusDB(int jahr) { //methode, die das distanz in dem monat zurückgibt
        String sqlAbfrage = "SELECT SUM(distanz) FROM training WHERE datum >= ? AND datum < ?";

        String start = jahr + "-01-01";
        String end = (jahr + 1) + "-01-01";

        System.out.println("start: " + start + " end: " + end);

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlAbfrage)) {

                pstmt.setString(1, start);
                pstmt.setString(2, end);

                ResultSet rs = pstmt.executeQuery(); //führt die abfrage aus und liefert das ergebnis
                
                if (rs.next()) {

                    return rs.getDouble(1);
                }

            } catch (SQLException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
            return 0.0;
    }

    public static double getDistanzMonatAusDB(int year, int gegebenerMonat) { //methode, die das distanz in dem monat zurückgibt
        String sqlAbfrage = "SELECT SUM(distanz) FROM training WHERE datum >= ? AND datum < ?";

        YearMonth monat = YearMonth.of(year, gegebenerMonat);
        
        String start = monat.atDay(1).toString();
        String end = monat.plusMonths(1).toString();

        System.out.println("start: " + start + " end: " + end);

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlAbfrage)) {

                pstmt.setString(1, start);
                pstmt.setString(2, end);

                ResultSet rs = pstmt.executeQuery(); //führt die abfrage aus und liefert das ergebnis
                
                if (rs.next()) {

                    return rs.getDouble(1);
                }

            } catch (SQLException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
            return 0.0;
    }

    public static double getDistanzWoche(String startWoche, String endWoche) { //methode, die das distanz in der woche zurückgibt
        String sqlAbfrage = "SELECT SUM(distanz) FROM training WHERE datum >= ? AND datum < ?";

        System.out.println("startwoche: " + startWoche + " endwoche: " + endWoche);

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlAbfrage)) {

                pstmt.setString(1, startWoche);
                pstmt.setString(2, endWoche);

                ResultSet rs = pstmt.executeQuery(); //führt die abfrage aus und liefert das ergebnis
                
                if (rs.next()) {

                    return rs.getDouble(1);
                }

            } catch (SQLException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
            return 0.0;
    }

    public static void updateChallengeProgressKilometer(String dateString, double distanceBetweenPointsGerundet) { //Methode für die ausrechnung des neuen progress
        //1. challenge prüfen, ob er gemacht wird
        String sqlAbfrageKilometer = "SELECT ziel, fortschrittwert, challengeID, challengeProgress FROM challenges WHERE status = '1' AND zielDatentyp = 'km'";

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlAbfrageKilometer)) {

                ResultSet rs = pstmt.executeQuery(); //führt die abfrage aus und liefert das ergebnis

                //2. die daten ausrechnen
                while (rs.next()) {
                    double alteDistanz = rs.getDouble("fortschrittwert");
                    double ziel = rs.getInt("ziel");
                    int challengeID = rs.getInt("challengeID");
                    System.out.println("gettrainingID: " + getTrainingID(distanceBetweenPointsGerundet, dateString));
                    if (checkTrainingGueltigeBereich(dateString, challengeID) && (getTrainingID(distanceBetweenPointsGerundet, dateString) == -1)) {
                        
                        double newDistanz = distanceBetweenPointsGerundet + alteDistanz;

                        double newChallengeProgress = newDistanz / ziel;

                        //3. die daten speichern
                        String sqlAbfrageSpeichern = "UPDATE challenges SET fortschrittwert = ?, challengeProgress = ? WHERE challengeID = ?";
                        
                        try (PreparedStatement update = conn.prepareStatement(sqlAbfrageSpeichern)) {

                            update.setDouble(1, newDistanz);
                            update.setDouble(2, newChallengeProgress);
                            update.setInt(3, challengeID);
                            update.executeUpdate();
                        }
                    }                  
                }
            } catch (SQLException e ){
            e.printStackTrace();
        }
    }

    public static long formateTime(String time) { //schreibt die zeit in sekunden
        String[] teile = time.split(":");

        if (teile.length == 3) {
            return Duration.ofHours(Long.parseLong(teile[0]))
                           .plusMinutes(Long.parseLong(teile[1]))
                           .plusSeconds(Long.parseLong(teile[2]))
                           .getSeconds();
        } else if (teile.length == 2) {
            return Duration.ofMinutes(Long.parseLong(teile[0]))
                           .plusSeconds(Long.parseLong(teile[1]))
                           .getSeconds();
        } else if (teile.length == 1) {
            return Duration.ofSeconds(Long.parseLong(teile[0])).getSeconds();
        }

        throw new IllegalArgumentException("ungültiges Zeitformat: " + time);   
    }

    public static void updateChallengeProgressMinutes(String time, String dateString, double distanceBetweenPointsGerundet) { //methode für die ausrechnung des neuen zeitlichen progress 
        String sqlAbfrageMinuten = "SELECT ziel, fortschrittwert, challengeID, challengeProgress FROM challenges WHERE status = 1 AND zielDatentyp = 'minuten'";

        try (var conn =  DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlAbfrageMinuten)) {
                
                ResultSet rs = pstmt.executeQuery(); //ausführung der Abfrage

                while (rs.next()) {
                    double alteZeit = rs.getDouble("fortschrittwert");
                    
                    int ziel = rs.getInt("ziel");
                    int challengeID = rs.getInt("challengeID");

                    if (checkTrainingGueltigeBereich(dateString, challengeID) && (getTrainingID(distanceBetweenPointsGerundet, dateString) == -1)) {
                        
                        long timeFormatted = formateTime(time);

                        double newMinuten = timeFormatted / 60.0;

                        double newZeit = alteZeit + newMinuten;

                        double newChallengeProgress = newZeit / ziel;

                        System.out.print("neue challengezeitprograss: " + newChallengeProgress);

                        String sqlAbfrageSpeichern = "UPDATE challenges SET fortschrittwert = ?, challengeProgress = ? WHERE challengeID = ?";

                        try (PreparedStatement update = conn.prepareStatement(sqlAbfrageSpeichern)) {

                            update.setDouble(1, newZeit);
                            update.setDouble(2, newChallengeProgress);
                            update.setInt(3, challengeID);
                            int rows = update.executeUpdate();

                            System.out.println("anzahl der geänderten wors: " + rows);
                        }
                    }

                    
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }
     
    public static void updateChallengeProgressHoehenmeter(String dateString, double hoehenmeter, double distanceBetweenPointsGerundet) { //methode für die ausrechnung des neuen höhenmeters für challenge
        String sqlAbfrageHoehenmeter = "SELECT ziel, fortschrittwert, challengeID FROM challenges WHERE status = 1 AND zielDatentyp = 'höhenmeter'";

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlAbfrageHoehenmeter)) {

                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    int ziel = rs.getInt("ziel");
                    double fortschrittwert = rs.getDouble("fortschrittwert");
                    int challengeID = rs.getInt("challengeID");

                    if (checkTrainingGueltigeBereich(dateString, challengeID) && (getTrainingID(distanceBetweenPointsGerundet, dateString) == -1)) {
                        double neuFortschrittwert = hoehenmeter + fortschrittwert;
                        double neuChallengeProgress = neuFortschrittwert / ziel;

                        String sqlAbfrageSpeichern = "UPDATE challenges SET challengeProgress = ?, fortschrittwert = ? WHERE challengeID = ?";

                        try (PreparedStatement update = conn.prepareStatement(sqlAbfrageSpeichern)) {
                            update.setDouble(1, neuChallengeProgress);
                            update.setDouble(2, neuFortschrittwert);
                            update.setInt(3, challengeID);
                            update.executeUpdate();
                        }
                    }                    
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }

    public static void updateChallengeProgressTage(String dateString, double distanz, double distanceBetweenPointsGerundet) {
        String sqlAbfrageTage = "SELECT ziel, fortschrittwert, challengeProgress, challengeID FROM challenges WHERE status = 1 AND zielDatentyp = 'tage'";

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlAbfrageTage)) {

                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    int ziel = rs.getInt("ziel");
                    double fortschrittwert = rs.getDouble("fortschrittwert");
                    int challengeID = rs.getInt("challengeID");
                    double challengeProgress = rs.getDouble("challengeProgress");

                    if (checkTrainingGueltigeBereich(dateString, challengeID) && (getTrainingID(distanceBetweenPointsGerundet, dateString) == -1)) { //wenn true, dann ausführen
                        int trainingId = getTrainingID(distanz, dateString); //trainingID = -1 -> training wurde noch nicht hinzugefügt
                    
                        double newFortschrittwert;
                        double newChallengeProgress;

                        if (trainingId == -1){
                            newFortschrittwert = fortschrittwert + 1;
                            newChallengeProgress = newFortschrittwert / ziel;
                        } else {
                            newFortschrittwert = fortschrittwert;
                            newChallengeProgress = challengeProgress;
                        }

                        String sqlAbfageSpeichern = "UPDATE challenges SET challengeProgress = ?, fortschrittwert = ? WHERE challengeID = ?";

                        //checkTrainingGueltigeBereich(dateString, challengeID)

                        try (PreparedStatement update = conn.prepareStatement(sqlAbfageSpeichern)) {
                            update.setDouble(1, newChallengeProgress);
                            update.setDouble(2, newFortschrittwert);
                            update.setInt(3, challengeID);
                            update.executeUpdate();
                        }
                    }                    
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }

    public static boolean checkTrainingGueltigeBereich(String dateString, int challengeID) {
        String sqlAbfrage = "SELECT challengeStartDate, challengeEndDate FROM challenges WHERE challengeID = ?";
        
        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlAbfrage)) {
                
                pstmt.setInt(1, challengeID);

                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    String challengeStartDate = rs.getString("challengeStartDate");
                    String challengeEndDate = rs.getString("challengeEndDate");


                    LocalDate datumTraining = LocalDate.parse(dateString); //parse von 08-08-2026
                    LocalDate datumStart = LocalDate.parse(challengeStartDate); //parse von challengeStartDate
                    LocalDate datumEnd = LocalDate.parse(challengeEndDate); //parse von challengeEndDate

                    System.out.println(!datumTraining.isBefore(datumStart) && !datumTraining.isAfter(datumEnd));
                    return !datumTraining.isBefore(datumStart) && !datumTraining.isAfter(datumEnd);
                    
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;

    }

    public static void addDistanzToJahr(String name, double distanceBetweenPointsGerundet) { //methode um die distanz zu datenbank distanzProJahr hinzufügen
        double fruehereDistanz = getDistanzJahrAusDB(2026);

        double newDistanzProJahr = fruehereDistanz + distanceBetweenPointsGerundet;

        String sqlAbfrage = "UPDATE users SET gesamteDistanzProJahr = ? WHERE name = ?";

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlAbfrage)) {

                pstmt.setDouble(1, newDistanzProJahr);
                pstmt.setString(2, name);

                System.out.println("newDistanzProJahr: " + newDistanzProJahr);

                pstmt.executeUpdate(); //führt die abfrage aus und liefert das ergebnis
                
                System.out.println("gesamte Distanz pro Jahr: " + newDistanzProJahr);
                
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
    }    

    public static void main(String[] args) {
        //File xmlFile = new File(fileName);
        //ArrayList<TrkPt> points = XmlReader.loadGpx(fileName);
        
        //connect();
    }
}