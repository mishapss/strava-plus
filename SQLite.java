//import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
//import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SQLite {
    public static int trainingLoad;

    //public static final String url = "jdbc:sqlite:C:\\Users\\User\\projects_programming\\strava-plus-main\\db\\test.db";
    public static final String url = "jdbc:sqlite:C:\\Users\\MikhailLeshchenko\\strava_plus\\db\\test.db";
    

    private static void setParameters(PreparedStatement ptsmt, Object... values) throws SQLException{ //kann so viele werten übergeben, wie ich will

        for (int i = 0; i < values.length; i++) { //
            ptsmt.setObject(i + 1, values[i]); //setzt selber wert und fügt in db
        }
    }    

    public static int[] getHRDaten(){
        String sqlAbfrage = "SELECT maxHR, restingHR FROM users WHERE userID = ?";

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlAbfrage)) {
                
                pstmt.setInt(1, 1);
                ResultSet rs = pstmt.executeQuery(); //führt die abfrage aus und liefert das ergebnis

                if (rs.next()) {
                    int maxHR = rs.getInt("maxHR");
                    int restingHR = rs.getInt("restingHR");
                    int[] hr = {maxHR, restingHR};

                    return hr;
                };
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
            return null;
    }

    public static int getTrainingID(double distanceBetweenPointsGerundet, String dateString) {
        String sqlAbfrage = "SELECT trainingID FROM training WHERE distance = ? AND date = ?";

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

    public static boolean checkTrainingID(int trainingID) {
        String sqlAbfrage = "SELECT trainingID FROM heart_zone WHERE trainingID = ?";

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement ptsmt = conn.prepareStatement(sqlAbfrage)) {

                ptsmt.setInt(1, trainingID);

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
        double calories,
        String fileName
        ) {

        int trainingID = -1;

        boolean trainingVorhanden = checkTraining(distanceBetweenPointsGerundet, dateString);

        if (trainingVorhanden) {
            trainingID = getTrainingID(distanceBetweenPointsGerundet, dateString);
            return trainingID;
        }

        try (var conn = DriverManager.getConnection(url); //verbindung zum DB
            PreparedStatement pstmt = conn.prepareStatement(
            """
            INSERT INTO training (
                date, 
                distance, 
                duration,
                activeTime,
                averageSpeed,
                maxSpeed, 
                elevationGain,
                averageHR,
                maxHR,
                trainingload,
                aerobicTrainingsEffekt,  
                anaeobicTrainingsEffekt,
                calories,
                fileName
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
                calories,
                fileName
            ); 

            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();

            if (rs.next()) {
                trainingID = rs.getInt(1);
            }

            System.out.println("Verbindung zum DB hergestellt + Daten gespeichert");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return trainingID;
    }

    public static void addHRDatenToDB(
        int trainingID,
        String timeIn0HrZone, 
        String timeIn1HrZone, 
        String timeIn2HrZone, 
        String timeIn3HrZone, 
        String timeIn4HrZone,
        String timeIn5HrZone,
        int averageHR,
        int maxHeartRate) {

        boolean trainingIDVorhanden = checkTrainingID(trainingID);

        if (trainingIDVorhanden) {
            return;
        }

        String sql = """
            INSERT INTO heart_zone (
                trainingID,
                zone0,
                zone1,
                zone2,
                zone3,
                zone4,
                zone5,
                averageHR,
                maxHR
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (var conn = DriverManager.getConnection(url); //verbindung zum DB
            PreparedStatement pstmt = conn.prepareStatement(sql)) { 

            setParameters(
                pstmt,
                trainingID,
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
        String sqlAbfrage = "SELECT * FROM training WHERE distance = ? and date = ?";

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
        String sqlAbfrage = "SELECT SUM(distance) FROM training WHERE date >= ? AND date < ?";

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
        String sqlAbfrage = "SELECT SUM(distance) FROM training WHERE date >= ? AND date < ?";

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
        String sqlAbfrage = "SELECT SUM(distance) FROM training WHERE date >= ? AND date < ?";

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
        String sqlAbfrageKilometer = "SELECT goal, progressValue, challengeID, challengeProgress FROM challenges WHERE status = '1' AND goalDataType = 'km'";

        class ChallengeData {
        int id;
        double progressValue;
        double goal;

        ChallengeData(int id, double progressValue, double goal) {
            this.id = id;
            this.progressValue = progressValue;
            this.goal = goal;
        }
    }

    List<ChallengeData> activeChallenges = new ArrayList<>();

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlAbfrageKilometer)) {

            ResultSet rs = pstmt.executeQuery(); //führt die abfrage aus und liefert das ergebnis

            //2. die daten ausrechnen
            while (rs.next()) {
                activeChallenges.add(new ChallengeData(
                    rs.getInt("challengeID"),
                    rs.getDouble("progressValue"),
                    rs.getInt("goal")
                ));
            }
        } catch (SQLException e ){
            e.printStackTrace(); 
            return;
        }

        int trainingID = getTrainingID(distanceBetweenPointsGerundet, dateString);

        for (ChallengeData challenge : activeChallenges) {
            if (checkTrainingGueltigeBereich(dateString, challenge.id) && trainingID == -1) { 
            
            double newDistanz = distanceBetweenPointsGerundet + challenge.progressValue;
            double newChallengeProgress = newDistanz / challenge.goal;

            //3. die daten speichern
            String sqlAbfrageSpeichern = "UPDATE challenges SET progressValue = ?, challengeProgress = ? WHERE challengeID = ?";

            try (var conn = DriverManager.getConnection(url); 
                PreparedStatement update = conn.prepareStatement(sqlAbfrageSpeichern)) {
                    update.setDouble(1, newDistanz);
                    update.setDouble(2, newChallengeProgress);
                    update.setInt(3, challenge.id);
                    update.executeUpdate();

                    checkChallengeStatus(challenge.id);
            } catch (SQLException e ){
                e.printStackTrace(); 
            }
        } 
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
        String sqlAbfrageMinuten = "SELECT goal, progressValue, challengeID, challengeProgress FROM challenges WHERE status = 1 AND goalDataType = 'minuten'";

        class ChallengeData {
            int id;
            double progressValue;
            int goal;

            ChallengeData (int id, double progressValue, int goal){
                this.id = id;
                this.progressValue = progressValue;
                this.goal = goal;
            }
        }

        List<ChallengeData> activeChallenges = new ArrayList<>();

        try (var conn =  DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlAbfrageMinuten)) {
                
            ResultSet rs = pstmt.executeQuery(); //ausführung der Abfrage

            while (rs.next()) {
                activeChallenges.add(new ChallengeData(
                    rs.getInt("challengeID"),
                    rs.getDouble("progressValue"),
                    rs.getInt("goal")
                ));
            }
                
            for (ChallengeData challenge : activeChallenges) {
                if (checkTrainingGueltigeBereich(dateString, challenge.id) && (getTrainingID(distanceBetweenPointsGerundet, dateString) == -1)) {
                    
                    long timeFormatted = formateTime(time);

                    double newMinuten = timeFormatted / 60.0;

                    double newZeit = challenge.progressValue + newMinuten;

                    double newChallengeProgress = newZeit / challenge.goal;

                    System.out.print("neue challengezeitprograss: " + newChallengeProgress);

                    String sqlAbfrageSpeichern = "UPDATE challenges SET progressValue = ?, challengeProgress = ? WHERE challengeID = ?";

                    try (PreparedStatement update = conn.prepareStatement(sqlAbfrageSpeichern)) {

                        update.setDouble(1, newZeit);
                        update.setDouble(2, newChallengeProgress);
                        update.setInt(3, challenge.id);
                        int rows = update.executeUpdate();

                        checkChallengeStatus(challenge.id);

                        System.out.println("anzahl der geänderten wors: " + rows);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
                
        } catch (SQLException e) {
            e.printStackTrace();
        }
    } 
      
    public static void updateChallengeProgressHoehenmeter(String dateString, double hoehenmeter, double distanceBetweenPointsGerundet) { //methode für die ausrechnung des neuen höhenmeters für challenge
        String sqlAbfrageHoehenmeter = "SELECT goal, progressValue, challengeID FROM challenges WHERE status = 1 AND goalDataType = 'höhenmeter'";

        class ChallengeData {
            int id;
            double progressValue;
            int goal;
        
            ChallengeData (int id, double progressValue, int goal) {
                this.id = id;
                this.progressValue = progressValue;
                this.goal = goal;
            }
        }

        List<ChallengeData> activeChallenges = new ArrayList<>();

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlAbfrageHoehenmeter)) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                activeChallenges.add(new ChallengeData(
                rs.getInt("challengeID"),
                rs.getDouble("progressValue"),
                rs.getInt("goal")
                ));
            }    
            
            for (ChallengeData challenge : activeChallenges) {
            
                if (checkTrainingGueltigeBereich(dateString, challenge.id) && (getTrainingID(distanceBetweenPointsGerundet, dateString) == -1)) {
                    double neuFortschrittwert = hoehenmeter + challenge.progressValue;
                    double neuChallengeProgress = neuFortschrittwert / challenge.goal;

                    String sqlAbfrageSpeichern = "UPDATE challenges SET challengeProgress = ?, progressValue = ? WHERE challengeID = ?";

                    try (PreparedStatement update = conn.prepareStatement(sqlAbfrageSpeichern)) {
                        update.setDouble(1, neuChallengeProgress);
                        update.setDouble(2, neuFortschrittwert);
                        update.setInt(3, challenge.id);
                        update.executeUpdate();

                        checkChallengeStatus(challenge.id);

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }                    
            }            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateChallengeProgressTage(String dateString, double distance, double distanceBetweenPointsGerundet) {
        String sqlAbfrageTage = "SELECT goal, progressValue, challengeProgress, challengeID FROM challenges WHERE status = 1 AND goalDataType = 'tage'";

        class ChallengeData {
        int id;
        double progressValue;
        double goal;
        double progress;

        ChallengeData(int id, double progressValue, double goal, double progress) {
            this.id = id;
            this.progressValue = progressValue;
            this.goal = goal;
            this.progress = progress;
        }
    }

        List<ChallengeData> activeChallenges = new ArrayList<>();

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlAbfrageTage)) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                activeChallenges.add(new ChallengeData(
                rs.getInt("challengeID"),
                rs.getDouble("progressValue"),
                rs.getInt("goal"),
                rs.getDouble("challengeProgress")
            ));
        }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (ChallengeData challenge : activeChallenges) {

            if (checkTrainingGueltigeBereich(dateString, challenge.id) && (getTrainingID(distanceBetweenPointsGerundet, dateString) == -1)) { //wenn true, dann ausführen
                int trainingId = getTrainingID(distance, dateString); //trainingID = -1 -> training wurde noch nicht hinzugefügt
            
                double newFortschrittwert;
                double newChallengeProgress;

                if (trainingId == -1){
                    newFortschrittwert = challenge.progressValue + 1;
                    newChallengeProgress = newFortschrittwert / challenge.goal;
                } else {
                    newFortschrittwert = challenge.progressValue;
                    newChallengeProgress = challenge.progress;
                }

                String sqlAbfageSpeichern = "UPDATE challenges SET challengeProgress = ?, progressValue = ? WHERE challengeID = ?";

                //checkTrainingGueltigeBereich(dateString, challengeID)

                try (var conn = DriverManager.getConnection(url);
                PreparedStatement update = conn.prepareStatement(sqlAbfageSpeichern)) {

                    update.setDouble(1, newChallengeProgress);
                    update.setDouble(2, newFortschrittwert);
                    update.setInt(3, challenge.id);
                    update.executeUpdate();

                    checkChallengeStatus(challenge.id);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }                    
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

        String sqlAbfrage = "UPDATE users SET totalDistancePerYear = ? WHERE name = ?";

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

    public static void checkChallengeStatus(int challengeID) {//prüft ob der user das ziel der herausforderung geschafft hat
        String sqlQueryToCatch = "SELECT challengeProgress, challengeName, status FROM challenges WHERE challengeID = ? AND status = 1";

            double challengeProgress = 0.0;
            String challengeName = "";
            //int status = 0;
            boolean found = false;

            try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlQueryToCatch)) {

                pstmt.setInt(1, challengeID);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        challengeProgress = rs.getDouble("challengeProgress");
                        challengeName = rs.getString("challengeName");
                        //status = rs.getInt("status");
                        found = true;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            } 

            if (found && challengeProgress >= 1.0) {
               System.out.println("Hurra, du hast den Challenge " + challengeName + " erfolgreich abgeschlossen");

                String sqlQuery = "UPDATE challenges SET status = 2 WHERE challengeID = ?";

                try (var conn = DriverManager.getConnection(url);
                    PreparedStatement pStatement = conn.prepareStatement(sqlQuery)) {
                    
                    pStatement.setInt(1, challengeID);
                    pStatement.executeUpdate(); // Jetzt läuft das Update, wenn keine Lese-Locks mehr aktiv sind
                    
                } catch (SQLException e) {
                    e.printStackTrace();
                } 
            }
    }

    public static void giveReward() { //gibt dem user seiner abzeichnung
        String konsoleString = "Hurra, du hast den Challenge erfolgreich abgeschollesen";
        System.out.println(konsoleString);
    }

    public static void saveNewUserToDatabase(
        String firstName,
        String secondName,
        int age,
        String sex, 
        int weight,
        int height,
        int maxHR,
        int restingHR,
        String username
    ) {
        String sqlQueryToSaveNewUser = """
            INSERT INTO users 
            (name, secondName, age, sex, weight, height, maxHR, restingHR, username)
            VALUES 
            (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        if (maxHR == 0) {
            maxHR = 220 - age; 
        }
        

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlQueryToSaveNewUser)) {
                pstmt.setString(1, firstName);
                pstmt.setString(2, secondName);
                pstmt.setInt(3, age);
                pstmt.setString(4, sex);
                pstmt.setInt(5, weight);
                pstmt.setInt(6, height);
                pstmt.setInt(7, maxHR);
                pstmt.setInt(8, restingHR);
                pstmt.setString(9, username);

                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }

    public static boolean checkUsernameInDB(String username) {
        String sqlQuery = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; //true, wenn counter > 0, es gab schon
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }



}