import java.util.List;
//import java.io.File;                                                        //datei öfnen
//import java.io.IOException;
import java.time.Instant;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
//import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;

public class WorkoutAnalyzer { //klasse für die analyze des trainings
    //public static final String url = "jdbc:sqlite:C:/Users/MikhailLeshchenko/strava_plus/db/test.db";
    public static final String url = "jdbc:sqlite:C:\\Users\\MikhailLeshchenko\\strava_plus\\db\\test.db";
    
    public WorkoutResult analyzeWorkout(List<TrkPt> points) { //analysiert hr daten
        List<List<Integer>> heartZones = new ArrayList<>();       //liste für alle gps punkte
        //XmlReader reader = new XmlReader(); // erstellt ein neues XmlReader objekt, um die GPX-Datei zu lesen

        int[] hr = SQLite.getHRDaten();

        int maxHR = hr[0];
        int restingHR = hr[1];

        //debug
        System.out.println("maxHr: " + maxHR + "ruheHr: " + restingHR);
        
        for (int i = 0; i < 6; i ++) {
            heartZones.add(new ArrayList<>());
        }
        
        for (TrkPt point : points) {
            int currentHeartRate = point.getHeartRate(); // jetzige HR

            int zoneIndex = determineZone(currentHeartRate);
            heartZones.get(zoneIndex).add(currentHeartRate); // einfügt in die entsprechende zone den hr wert
        }        


        //ausrechnung der verbringenden zeit in jeder zone        
        int[] timeInZone = new int[6];

        for (int i = 0; i < points.size() - 1; i++) {
            TrkPt current = points.get(i);
            TrkPt next = points.get(i + 1);

            int zoneIndex = determineZone(current.getHeartRate());

            long seconds = calculateTimeDifferenceInSeconds(current.time, next.time);

            timeInZone[zoneIndex] += seconds;     
        }
        return new WorkoutResult(timeInZone, heartZones);
    }

    public long calculateTimeDifferenceInSeconds(String current, String next) { // berechnet die Zeitdifferenz zwischen zwei Zeitpunkten in Sekunden, brauche für ausrechnung der Zeit in jeder Herzfrequenzzone

        String startTimeString = current;
        String endTimeString = next;

        // Превращаем текстовые строки в объекты времени Instant
        Instant startToParse = Instant.parse(startTimeString);
        Instant endToParse = Instant.parse(endTimeString);

        //Считаем разницу между ними
        Duration duration = Duration.between(startToParse, endToParse);
        return duration.getSeconds();
    }

    public int determineZone(int heartRate) { // berechnet die herzfrequenzzone basierend auf der aktuellen herzfrequenz und der maximalen herzfrequenz
        List<List<Integer>> heartZones = new ArrayList<>();       
        
        for (int i = 0; i < 6; i ++) {
            heartZones.add(new ArrayList<>());
        }

        if (heartRate < 137) {
            heartZones.get(0).add(heartRate);
            return 0;
        }
        if (heartRate < 159) {
            heartZones.get(1).add(heartRate);
            return 1;
        }
        if (heartRate < 175) {
            heartZones.get(2).add(heartRate);
            return 2;
        }
        if (heartRate < 181) {
            heartZones.get(3).add(heartRate);
            return 3;
        }
        if (heartRate < 191) {
            heartZones.get(4).add(heartRate);
            return 4;
        }
        heartZones.get(5).add(heartRate);
        return 5;
    }

    public int getDurchschnittlicheHR(List<TrkPt> points) { // berechnet die durchschnittliche Herzfrequenz aus der Liste von TrkPt-Objekten
        int sum = 0;
        int countHR = 0;
        int durschnittlicheHR = 0;

        for (TrkPt point: points) {
            int currentHeartRate = point.getHeartRate();
            if (currentHeartRate > 0) {
                sum += currentHeartRate;
                countHR++;
            }
        }
        durschnittlicheHR = sum / countHR;
        return durschnittlicheHR;
    }

    public int getMaxHr(List<TrkPt> points) { //ausrechnung der max hr
        
        int maxHeartRate = 0;
        for (TrkPt point : points) {
            int currentHeartRate = point.getHeartRate(); // jetzige HR
            if (currentHeartRate > maxHeartRate) {
                maxHeartRate = currentHeartRate;
            }
        }
        return maxHeartRate;
    }

    public double getTrainingLoad(List<TrkPt> points) { //methode um die trainingsbelastung zu berechnen
        double e = Math.E;
        double trainingLoad = 0.0;
        double relativeHR = 0.0;    
        double intensityFaktor = 0.0;

        int[] hr = SQLite.getHRDaten();

        int maxHR = hr[0];
        int restingHR = hr[1];

        
        for (TrkPt point : points) {
            int currentHeartRate = point.getHeartRate(); // jetzige HR

            relativeHR = (currentHeartRate - restingHR) / (double)(maxHR - restingHR); //relative Herzfrequenz berechnen

            intensityFaktor = 0.64 * Math.pow(e, (1.92 * relativeHR))/53;
            
            trainingLoad += relativeHR * intensityFaktor;
        }
        return trainingLoad;
    }

    public double getTrainingstatus() { //methode zur berechnung der trainingsstatus, die noch implementiert werden muss
        return 0.0; 
    }

    public double getAerobicTrainingEffect(List<TrkPt> points) { //methode zur berechnung des aeroben trainingseffekt, anhand mehreren daten
        double e = Math.E;

        double trainingLoad = getTrainingLoad(points); //bekommen trainingsbelsatung
        WorkoutResult result = analyzeWorkout(points);
        int[] zones = result.timeInZone;

        //faktoren benötigte zur berechnung        
        double faktor0 = 0.4206;
        double faktor1 = 0.4206;
        double faktor2 = 0.7713;
        double faktor3 = 0.7713;
        double faktor4 = 0.7713;
        double faktor5 = 0.7713;
        
        double aerobicWert = zones[0] * faktor0 + zones[1] * faktor1 + zones[2] * faktor2 + zones[3] * faktor3 + 
        zones[4] * faktor4 + zones[5] * faktor5; //zeit in den aeroben zonen
        
        double aerobicFraction = aerobicWert / (zones[0] + zones[1] + zones[2] + zones[3] + zones[4] + zones[5]); //anteil der aeroben zeit an der gesamten zeit

        double aerobicTrainingEffect = trainingLoad * aerobicFraction; //berechnung des aeroben trainingseffekt

        double aerobicTE = 5 * (1 - Math.pow(e, (-aerobicTrainingEffect / 60))); //berechnung des aeroben trainingseffekt auf einer skala von 0 bis 5     
        
        return Math.round(aerobicTE * 10.0) / 10.0;
    }
    
    public double getAnaerobicTrainingEffect(List<TrkPt> points) { //methode zur berechnung des aeroben trainingseffekt, anhand mehreren daten
        double e = Math.E;

        double trainingLoad = getTrainingLoad(points); //bekommen trainingsbelsatung
        WorkoutResult result = analyzeWorkout(points);
        int[] zones = result.timeInZone;

        //faktoren benötigte zur berechnung        
        double faktor0 = 0.0;
        double faktor1 = 0.0;
        double faktor2 = 0.0;
        double faktor3 = 3.6264;
        double faktor4 = 3.7257;
        double faktor5 = 3.8252;
        
        double anaerobicWert = zones[0] * faktor0 + zones[1] * faktor1 + zones[2] * faktor2 + zones[3] * faktor3 + 
        zones[4] * faktor4 + zones[5] * faktor5; //zeit in den aeroben zonen

        System.out.println("anaerobicWert: " + anaerobicWert);
        
        double anaerobicFraction = anaerobicWert / (zones[0] + zones[1] + zones[2] + zones[3] + zones[4] + zones[5]); //anteil der aeroben zeit an der gesamten zeit
        System.out.println("anaerobicFraction: " + anaerobicFraction);

        double anaerobicTrainingEffect = trainingLoad * anaerobicFraction; //berechnung des aeroben trainingseffekt
        System.out.println("anaerobicTrainingEffect: " + anaerobicTrainingEffect);

        double anaerobicTE = 5 * (1 - Math.pow(e, (-anaerobicTrainingEffect / 60))); //berechnung des aeroben trainingseffekt auf einer skala von 0 bis 5     
        System.out.println("anaerobicTE: " + anaerobicTE);
        System.out.println("ate: " + Math.round(anaerobicTE * 10.0) / 10.0);
        return Math.round(anaerobicTE * 10.0) / 10.0;
    }

    public double getKalorienVerbrauch(List<TrkPt> points) { //methode zur berechnung des Kalorienverbrauchs anhand der Herzfrequenzdaten und des Gewichts
        double kcalGesamt = 0.0;
        int weight = 0;
        int age = 0;
        
        String sqlQuery = "SELECT weight, age FROM users WHERE userID = ?";

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {
                pstmt.setInt(1, 1);

                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    weight = rs.getInt("weight");
                    age = rs.getInt("age");
                };
            } catch (SQLException e) {
                e.printStackTrace();
            }


        for (TrkPt point: points)   {
            int currentHeartRate = point.getHeartRate();
            double kcalInMin = (-55.0969 + 0.6309 * currentHeartRate + 0.1988 * weight + 0.2017 * age) / 8; //berechnung der Kalorien pro Minute

            kcalGesamt += kcalInMin / 60; //gesamtkalorienverbrauch
        }
        return Math.round(kcalGesamt * 100.0) / 100.0; 
        
    }

    public void checkNewMaxSpeed(List<TrkPt> points) {
        double maxGeschwindigkeit = 0.0;

        for (int i = 0; i < points.size(); i++) {
            double jetzigeGeschwindigkeit = points.get(i).geschwindigkeitMps; 

            if (jetzigeGeschwindigkeit > maxGeschwindigkeit) {
                maxGeschwindigkeit = jetzigeGeschwindigkeit; 
            }
        }
        double maxGeschwindigkeitKmh = Math.round(maxGeschwindigkeit * 3.6 * 100) / 100.0;

        String sqlQuery = "SELECT maxSpeed FROM users WHERE userID = ?";

        double maxSpeed = 0.0;

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {
                
                pstmt.setInt(1,1);

                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    maxSpeed = rs.getDouble("maxSpeed");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }

        if (maxSpeed < maxGeschwindigkeitKmh) {

            String sqlQueryInsert = "UPDATE users SET maxSpeed = ? WHERE userID = ?";

            try (var conn = DriverManager.getConnection(url); 
                PreparedStatement update = conn.prepareStatement(sqlQueryInsert)) {

                    update.setDouble(1, maxGeschwindigkeitKmh);
                    update.setInt(2, 1);
                    update.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void chenkNewMaxDistance(List<TrkPt> points) {
        double currentDistance = XmlReader.distanceBetweenPointsGerundet;

        String sqlQueryGetDistance = "SELECT maxDistance FROM users WHERE userID = ?";
        double maxDistance = 0.0;

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlQueryGetDistance)) {
                pstmt.setInt(1,1);
                
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    maxDistance = rs.getDouble("maxDistance");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        
            if (currentDistance > maxDistance) {

                String sqlQuerySaveNewDistance = "UPDATE users SET maxDistance = ? WHERE userID = ?";

                try (var conn = DriverManager.getConnection(url);
                    PreparedStatement pstmt = conn.prepareStatement(sqlQuerySaveNewDistance)) {
                    
                    pstmt.setDouble(1, currentDistance);
                    pstmt.setInt(2, 1);
                    pstmt.executeUpdate();                    
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
    }

    public void checkNewMaxCalorieBurn(List<TrkPt> points) { //checkt ob es beim Training neue Kalorienverbraucht-Rekord gibt
        double currentCalorieBurn = XmlReader.calories; //kalorien von training

        String sqlQueryGetCalorie = "SELECT calorieBurn FROM users WHERE userID = ?";

        double maxCalorieBurn = 0.0;

        //alte daten bekommen
        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlQueryGetCalorie)) {

                pstmt.setInt(1,1);
                
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    maxCalorieBurn = rs.getDouble("calorieBurn");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        //vergleichen und speichern    
        if (currentCalorieBurn > maxCalorieBurn) {
            String sqlQueryUpdateCalorie = "UPDATE users SET calorieBurn = ? WHERE userID = ?";

            try (var conn = DriverManager.getConnection(url);
                PreparedStatement pstmtUpdate = conn.prepareStatement(sqlQueryUpdateCalorie)) {

                    pstmtUpdate.setDouble(1, currentCalorieBurn);
                    pstmtUpdate.setInt(2, 1); //1 = userId, bis jetzt nur 1 User

                    pstmtUpdate.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
        }
    }

    public void checkNewMaxTrainingLoad(List<TrkPt> points) {
        int currentTrainingLoad = XmlReader.trainingLoad;

        String sqlQueryGetTraningLoad = "SELECT trainingLoad FROM users WHERE userID = ?";

        int maxTrainingLoad = 0;

        //daten aus db bekommen
        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmtGet = conn.prepareStatement(sqlQueryGetTraningLoad)) {

                pstmtGet.setInt(1, 1);
                
                ResultSet rs = pstmtGet.executeQuery();

                if (rs.next()) {
                    maxTrainingLoad = rs.getInt("trainingLoad");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        //vergleichen und speichern
        if (currentTrainingLoad > maxTrainingLoad) {
            String sqlQuerySaveNewTrainingLoad = "UPDATE users SET trainingLoad = ? WHERE userID = ?";

            try (var conn = DriverManager.getConnection(url);
                PreparedStatement pstmtUpdate = conn.prepareStatement(sqlQuerySaveNewTrainingLoad)) {

                    pstmtUpdate.setInt(1, currentTrainingLoad);
                    pstmtUpdate.setInt(2,1);
                    
                    pstmtUpdate.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void checkNewElevationGain(List<TrkPt> points) {
        double currentElevation = XmlReader.hoeheSummeGerundet;

        String sqlQueryGetElevation = "SELECT maxElevationGain FROM users WHERE userID = ?";

        double maxElevationGain = 0.0;

        try (var conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sqlQueryGetElevation)) {
            
            pstmt.setInt(1,1);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                maxElevationGain = rs.getDouble("maxElevationGain");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (currentElevation > maxElevationGain) {
            String sqlQuerySaveNewElevation = "UPDATE users SET maxElevationGain = ? WHERE userID = ?";

            try (var conn = DriverManager.getConnection(url);
                PreparedStatement pstmt = conn.prepareStatement(sqlQuerySaveNewElevation)) {

                pstmt.setDouble(1, currentElevation);
                pstmt.setInt(2,1);
                pstmt.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}