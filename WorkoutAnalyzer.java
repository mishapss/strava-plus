import java.util.List;
import java.io.File;                                                        //datei öfnen
import java.io.IOException;
import java.time.Instant;
import java.time.Duration;
import java.util.ArrayList;

public class WorkoutAnalyzer { //klasse für die analyze des trainings
    
    public WorkoutResult analyzeWorkout(List<TrkPt> points, int maxHr) { //analysiert hr daten
        List<List<Integer>> heartZones = new ArrayList<>();       //liste für alle gps punkte
        XmlReader reader = new XmlReader(); // erstellt ein neues XmlReader objekt, um die GPX-Datei zu lesen
        
        for (int i = 0; i < 6; i ++) {
            heartZones.add(new ArrayList<>());
        }
        
        for (TrkPt point : points) {
            int currentHeartRate = point.getHeartRate(); // jetzige HR

            int zoneIndex = determineZone(currentHeartRate, maxHr);
            heartZones.get(zoneIndex).add(currentHeartRate); // einfügt in die entsprechende zone den hr wert
            //System.out.println("Heart Rate: " + currentHeartRate + ", Zone: " + zoneIndex);
        }        


        //ausrechnung der verbringenden zeit in jeder zone        
        int[] timeInZone = new int[6];

        for (int i = 0; i < points.size() - 1; i++) {
            TrkPt current = points.get(i);
            TrkPt next = points.get(i + 1);

            int zoneIndex = determineZone(current.getHeartRate(), maxHr);

            long seconds = calculateTimeDifferenceInSeconds(current.time, next.time);

            timeInZone[zoneIndex] += seconds;     
        }

        //ausgabe
        System.out.println("Time in Zone " + 0 + ": " + timeInZone[0] + " seconds");
        System.out.println("Time in Zone " + 1 + ": " + timeInZone[1] + " seconds");
        System.out.println("Time in Zone " + 2 + ": " + timeInZone[2] + " seconds");
        System.out.println("Time in Zone " + 3 + ": " + timeInZone[3] + " seconds");
        System.out.println("Time in Zone " + 4 + ": " + timeInZone[4] + " seconds");
        System.out.println("Time in Zone " + 5 + ": " + timeInZone[5] + " seconds");
        //System.out.println(reader.activeTimeFormatted);

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

        if (heartRate < 142) {
            heartZones.get(0).add(heartRate);
            return 0;
        }
        if (heartRate < 159) {
            heartZones.get(1).add(heartRate);
            return 1;
        }
        if (heartRate < 168) {
            heartZones.get(2).add(heartRate);
            return 2;
        }
        if (heartRate < 181) {
            heartZones.get(3).add(heartRate);
            return 3;
        }
        if (heartRate < 188) {
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

    public int getMaxHr(List<TrkPt> points) {
        //ausrechnung der max hr
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

    public double getTrainingstatus() {
        return 0.0; //methode zur berechnung der trainingsstatus, die noch implementiert werden muss
    }



    //machdenken, wie man es realisiert, da die skala von 0 bis 6 geht, eine methode finden oder die daten analysieren
    public double getAerobicTrainingEffect(List<TrkPt> points, int maxHR, int ruheHR) { //methode zur berechnung des aeroben trainingseffekt, anhand mehreren daten
        double trainingLoad = getTrainingLoad(points, maxHR, ruheHR); //bekommen trainingsbelsatung
        WorkoutResult result = analyzeWorkout(points, 200);
        int[] zones = result.timeInZone;

        double aerobicWert = zones[1] * 0.3 + zones[2] * 1.0 + zones[3] * 1.5 + zones[4] * 1.8 + zones[5] * 0.5; //zeit in den aeroben zonen
        double aerobicFraction = aerobicWert / (zones[0] + zones[1] + zones[2] + zones[3] + zones[4] + zones[5]); //anteil der aeroben zeit an der gesamten zeit
        double aerobicTrainingEffect = trainingLoad * aerobicFraction / 8.57; //berechnung des aeroben trainingseffekt
        System.out.println("Aerobic Training Effect: " + aerobicTrainingEffect);
        return aerobicTrainingEffect;
    }
}