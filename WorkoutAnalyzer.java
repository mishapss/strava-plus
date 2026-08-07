import java.util.List;
//import java.io.File;                                                        //datei öfnen
//import java.io.IOException;
import java.time.Instant;
import java.time.Duration;
import java.util.ArrayList;

public class WorkoutAnalyzer { //klasse für die analyze des trainings
    public static final String url = "jdbc:sqlite:C:/Users/MikhailLeshchenko/strava_plus/db/test.db";
    
    public WorkoutResult analyzeWorkout(List<TrkPt> points) { //analysiert hr daten
        List<List<Integer>> heartZones = new ArrayList<>();       //liste für alle gps punkte
        //XmlReader reader = new XmlReader(); // erstellt ein neues XmlReader objekt, um die GPX-Datei zu lesen

        int[] hr = SQLite.getHRDaten();

        int maxHR = hr[0];
        int ruheHR = hr[1];

        //debug
        System.out.println("maxHr: " + maxHR + "ruheHr: " + ruheHR);
        
        for (int i = 0; i < 6; i ++) {
            heartZones.add(new ArrayList<>());
        }
        
        for (TrkPt point : points) {
            int currentHeartRate = point.getHeartRate(); // jetzige HR

            int zoneIndex = determineZone(currentHeartRate, maxHR);
            heartZones.get(zoneIndex).add(currentHeartRate); // einfügt in die entsprechende zone den hr wert
        }        


        //ausrechnung der verbringenden zeit in jeder zone        
        int[] timeInZone = new int[6];

        for (int i = 0; i < points.size() - 1; i++) {
            TrkPt current = points.get(i);
            TrkPt next = points.get(i + 1);

            int zoneIndex = determineZone(current.getHeartRate(), maxHR);

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

    public int determineZone(int heartRate, int maxHr) { // berechnet die herzfrequenzzone basierend auf der aktuellen herzfrequenz und der maximalen herzfrequenz
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

    public double getTrainingLoad(List<TrkPt> points, int maxHR, int ruheHR) { //methode um die trainingsbelastung zu berechnen
        double e = Math.E;
        double trainingLoad = 0.0;
        double relativeHR = 0.0;    
        double intensityFaktor = 0.0;
        
        for (TrkPt point : points) {
            int currentHeartRate = point.getHeartRate(); // jetzige HR

            relativeHR = (currentHeartRate - ruheHR) / (double)(maxHR - ruheHR); //relative Herzfrequenz berechnen

            intensityFaktor = 0.64 * Math.pow(e, (1.92 * relativeHR))/53;
            
            trainingLoad += relativeHR * intensityFaktor;
        }
        return trainingLoad;
    }

    public double getTrainingstatus() { //methode zur berechnung der trainingsstatus, die noch implementiert werden muss
        return 0.0; 
    }

    public double getAerobicTrainingEffect(List<TrkPt> points, int maxHR, int ruheHR) { //methode zur berechnung des aeroben trainingseffekt, anhand mehreren daten
        double e = Math.E;

        double trainingLoad = getTrainingLoad(points, maxHR, ruheHR); //bekommen trainingsbelsatung
        WorkoutResult result = analyzeWorkout(points);
        int[] zones = result.timeInZone;

        //faktoren benötigte zur berechnung        
        double faktor0 = 0.539;
        double faktor1 = 0.315;
        double faktor2 = 0.799;
        double faktor3 = 0.2;
        double faktor4 = 0.1;
        double faktor5 = 0.05;
        
        double aerobicWert = zones[0] * faktor0 + zones[1] * faktor1 + zones[2] * faktor2 + zones[3] * faktor3 + 
        zones[4] * faktor4 + zones[5] * faktor5; //zeit in den aeroben zonen
        
        double aerobicFraction = aerobicWert / (zones[0] + zones[1] + zones[2] + zones[3] + zones[4] + zones[5]); //anteil der aeroben zeit an der gesamten zeit

        double aerobicTrainingEffect = trainingLoad * aerobicFraction; //berechnung des aeroben trainingseffekt

        double aerobicTE = 5 * (1 - Math.pow(e, (-aerobicTrainingEffect / 60))); //berechnung des aeroben trainingseffekt auf einer skala von 0 bis 5     
        
        return Math.round(aerobicTE * 10.0) / 10.0;
    }
    
    public double getAnaerobicTrainingEffect(List<TrkPt> points, int maxHR, int ruheHR) { //methode zur berechnung des aeroben trainingseffekt, anhand mehreren daten
        double e = Math.E;

        double trainingLoad = getTrainingLoad(points, maxHR, ruheHR); //bekommen trainingsbelsatung
        WorkoutResult result = analyzeWorkout(points);
        int[] zones = result.timeInZone;

        //faktoren benötigte zur berechnung        
        double faktor0 = 0.0;
        double faktor1 = 0.005;
        double faktor2 = 0.06;
        double faktor3 = 0.2;
        double faktor4 = 0.45;
        double faktor5 = 0.6;
        
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

    public double getKalorienVerbrauch(List<TrkPt> points, int gewicht, int alter) { //methode zur berechnung des Kalorienverbrauchs anhand der Herzfrequenzdaten und des Gewichts
        double kcalGesamt = 0.0;
        for (TrkPt point: points)   {
            int currentHeartRate = point.getHeartRate();
            double kcalInMin = (-55.0969 + 0.6309 * currentHeartRate + 0.1988 * gewicht + 0.2017 * alter) / 8; //berechnung der Kalorien pro Minute

            kcalGesamt += kcalInMin / 60; //gesamtkalorienverbrauch
        }
        return Math.round(kcalGesamt * 100.0) / 100.0; 
        
    }
}