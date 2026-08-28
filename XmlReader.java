import com.fasterxml.jackson.databind.ObjectMapper;                         // wandelt daten json <-> java
import com.fasterxml.jackson.databind.JsonNode;                             //darstellung der daten als ein baum
import com.fasterxml.jackson.databind.ObjectWriter;                         //formatierung json schön
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.File;                                                        //datei öfnen
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.time.ZoneId;

public class XmlReader {

    public static String geoJsonData;                           //speicher für die fertige route
    public static double distanceBetweenPointsGerundet;
    public static String time;
    public static String dateString;
    public static double averageSpeed;
    public static double maxGeschwindigkeitKmh;
    public static double hoeheSummeGerundet;
    public static int averageHR;
    public static int maxHeartRate;
    public static String timeIn0HrZone;
    public static String timeIn1HrZone;
    public static String timeIn2HrZone;
    public static String timeIn3HrZone;
    public static String timeIn4HrZone;
    public static String timeIn5HrZone;
    public static String activeTimeFormatted;
    public static double averageSpeedGerundet;
    public static int trainingLoad;
    public static int aerobicTrainingLoad;  
    public static double calories;
    public static double aerobicTrainingEffect;
    public static double anaerobicTrainingEffect;

    public double getdistanceBetweenPointsGerundet() {
        return distanceBetweenPointsGerundet;
    }



    public static Training loadGpx(String fileName) throws Exception { //lesen gpx
        File xmlFile = new File(fileName);                       //datei-objekt erstellen, öffnen                                   
        XmlReader reader = new XmlReader();
        ArrayList<TrkPt> trackPoints = new ArrayList<>();       //liste für alle gps punkte 
        
        WorkoutAnalyzer analyzer = new WorkoutAnalyzer();         //objekt für die analyse der daten


        if (!xmlFile.exists()) {                                //überprüfung, ob die datei überhauprt existiert
            System.out.println("XML File existiert nicht");
            return null;
        }

        XmlMapper xmlMapper = new XmlMapper();                  //tool um aus xml in java zu gehen
        try {
            JsonNode root = xmlMapper.readTree(xmlFile);        //xml wird als baumstruktur geladen    

            JsonNode trkptNode = root.path("trk").path("trkseg").path("trkpt");

            for (JsonNode trkpt : trkptNode){
                double lat = trkpt.path("lat").asDouble();
                double lon = trkpt.path("lon").asDouble();
                double ele = trkpt.path("ele").asDouble();
                String time = trkpt.path("time").asText();

                double geschwindigkeitMps = trkpt.path("extensions").path("speed").asDouble();
                int hr = trkpt.path("extensions").path("hr").asInt();


                TrkPt punkt = new TrkPt(lat, lon, ele, time, geschwindigkeitMps, hr);    //jeder punkt als objekt gespeichert 
                trackPoints.add(punkt);                                                  //jeder punkt zur liste hinzugefügt             
            }

            //ausrechnung der Zeit
            TrkPt startTime = trackPoints.get(0);
            TrkPt endTime = trackPoints.get(trackPoints.size()-1);

            String startTimeString = startTime.time;
            String endTimeString = endTime.time;

            // Превращаем текстовые строки в объекты времени Instant
            Instant startToParse = Instant.parse(startTimeString);
            System.out.println(startToParse); //2026-06-29T16:33:35Z
            Instant endToParse = Instant.parse(endTimeString);

            //berechnung des datums
            LocalDate date = startToParse.atZone(ZoneId.systemDefault()).toLocalDate();
            String dateString = date.toString(); //2026-06-29
            int year = date.getYear();
            int month = date.getMonthValue();
            
            System.out.println("test zum monthausgabe");
            System.out.println(String.format("%02d", month));
            
            int day = date.getDayOfMonth();
            System.out.println("day: " + day + " month: " + month + " year: " + year);
            XmlReader.dateString = dateString;


            //Считаем разницу между ними
            Duration duration = Duration.between(startToParse, endToParse);
            long sekunden = duration.toSeconds();
            int sekundenInt = Math.toIntExact(sekunden);
            
            time = reader.stringFormatZoneTime(sekundenInt);

            //ausrechnung der distanz
            double distanceBetweenPoints = calculateTotalDistance(trackPoints);
            distanceBetweenPointsGerundet = Math.round(distanceBetweenPoints * 100.0) / 100.0;

            WorkoutResult result = analyzer.analyzeWorkout(trackPoints);
            int[] zones = result.timeInZone;
            
            trainingLoad = (int)analyzer.getTrainingLoad(trackPoints);
            aerobicTrainingLoad = (int)analyzer.getAerobicTrainingEffect(trackPoints);

            timeIn0HrZone = reader.stringFormatZoneTime(zones[0]);
            timeIn1HrZone = reader.stringFormatZoneTime(zones[1]);
            timeIn2HrZone = reader.stringFormatZoneTime(zones[2]);
            timeIn3HrZone = reader.stringFormatZoneTime(zones[3]);
            timeIn4HrZone = reader.stringFormatZoneTime(zones[4]);
            timeIn5HrZone = reader.stringFormatZoneTime(zones[5]);

            int activeTime = result.heartZones.get(0).size() + result.heartZones.get(1).size() + result.heartZones.get(2).size()
            + result.heartZones.get(3).size() +
            result.heartZones.get(4).size() + result.heartZones.get(5).size();

            
            activeTimeFormatted = reader.stringFormatZoneTime(activeTime);


            //ausrechnung der durschnittlichen Geschwindigkeit
            double averageSpeed = 0.0;
            int distanceInMeters = (int)(distanceBetweenPoints * 1000); //distanz in meter
            double geschwindigkeitInMps = distanceInMeters / (double) activeTime; //geschwindigkeit in m/s
            averageSpeed = geschwindigkeitInMps * 3.6; //geschwindigkeit in
            averageSpeedGerundet = Math.round(averageSpeed * 100.0) / 100.0;

            //ausrechnung der Kalorien
            double calories = analyzer.getKalorienVerbrauch(trackPoints);//ausrechnung der kalorien
            XmlReader.calories = calories;

            double aerobicTrainingEffect = analyzer.getAerobicTrainingEffect(trackPoints);
            XmlReader.aerobicTrainingEffect = aerobicTrainingEffect;

            double anaerobicTrainingEffect = analyzer.getAnaerobicTrainingEffect(trackPoints);
            XmlReader.anaerobicTrainingEffect = anaerobicTrainingEffect;

            //ausrechnung der max geschwindigkeit
            double maxGeschwindigkeit = 0.0;

            for (int i = 0; i < trackPoints.size(); i++) {
                double jetzigeGeschwindigkeit = trackPoints.get(i).geschwindigkeitMps; 

                if (jetzigeGeschwindigkeit > maxGeschwindigkeit) {
                    maxGeschwindigkeit = jetzigeGeschwindigkeit; 
                }
            }
            maxGeschwindigkeitKmh = Math.round(maxGeschwindigkeit * 3.6 * 100) / 100.0;


            //ausrechnung der höheanstieg
            double hoeheSumme = 0.0; //variable für höhesumme
            for (int i = 0; i < trackPoints.size() - 1; i++){
                double jetzigeHoehe = trackPoints.get(i).ele;
                double naechsteHoehe = trackPoints.get(i + 1).ele;

                if (naechsteHoehe > jetzigeHoehe){
                    double hoeheUnterschied = naechsteHoehe - jetzigeHoehe;
                    hoeheSumme += hoeheUnterschied;
                } 
            }
            hoeheSummeGerundet = Math.round(hoeheSumme * 100.0) / 100.0;


            //ausrechnung der durchsnittliche HR
            int summeHR = 0;
            for (int i = 0; i < trackPoints.size() - 1; i ++){
                int hrNow = trackPoints.get(i).hr;
                summeHR += hrNow;
            }
            averageHR = summeHR / trackPoints.size();

            maxHeartRate = analyzer.getMaxHr(trackPoints);

            //ausgabe
            System.out.print(" average Speed: " + averageSpeedGerundet + " trainingsbelsatung: " + trainingLoad +
            " distance: " + distanceBetweenPointsGerundet + " time: " + time + " maxHR: " + maxHeartRate
            + " Anstieg: " + hoeheSummeGerundet + " max speed: " + maxGeschwindigkeitKmh + " activeTimeFormatted: " + activeTimeFormatted +
            " average HR: " + averageHR + " calories: " + calories + "\n" );
            
            geoJsonData = buildGeoJsonForMap(trackPoints);       //wandelt die liste in geojson 
            System.out.println("JSON erzeugt!");

            //aktualisiert die daten des progresses in db
            SQLite.updateChallengeProgressKilometer(dateString, distanceBetweenPoints);


            SQLite.updateChallengeProgressMinutes(time, dateString, distanceBetweenPoints);           


            SQLite.updateChallengeProgressHoehenmeter(dateString, hoeheSumme, distanceBetweenPoints);


            SQLite.updateChallengeProgressTage(dateString, distanceBetweenPointsGerundet, distanceBetweenPoints);


            //einfügt die daten om training in datenbank
            int trainingID = SQLite.addTrainingDatenToDB(
                dateString, distanceBetweenPoints, time, activeTimeFormatted, averageSpeed, maxGeschwindigkeitKmh, 
                hoeheSumme, averageHR, maxHeartRate, trainingLoad, aerobicTrainingEffect, anaerobicTrainingEffect, calories, xmlFile.toString());            
            
            //aktualisiert distanz im jahr
            SQLite.addDistanzToJahr("Mischa", distanceBetweenPoints);


            //hr daten vom training in db einfügen
            SQLite.addHRDatenToDB(trainingID, timeIn0HrZone, timeIn1HrZone, timeIn2HrZone, timeIn3HrZone, timeIn4HrZone, timeIn5HrZone, averageHR, maxHeartRate);

            analyzer.checkNewMaxSpeed(trackPoints);

            analyzer.chenkNewMaxDistance(trackPoints);

            analyzer.checkNewElevationGain(trackPoints);

            analyzer.checkNewMaxCalorieBurn(trackPoints);
            analyzer.checkNewMaxTrainingLoad(trackPoints);  
            
            return new Training(trackPoints);
        
        } catch (IOException e ){
            System.err.println("Fehler beim Einlesen: " +e.getMessage());
            return null;
        }    
    }
    

    public String stringFormatZoneTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;

        int seconds = totalSeconds % 60;
        if (hours == 0) {
            return String.format("%02d:%02d", minutes, seconds);
        }
        else {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        
    }

    public static void main(String[] args) throws Exception {
        System.out.print("test");
        String[] gpxFiles = {
                "datenauswertung/radtraining/25.gpx",
                "datenauswertung/radtraining/24.gpx",
                "datenauswertung/radtraining/23.gpx",
                "datenauswertung/radtraining/22.gpx",
                "datenauswertung/radtraining/21.gpx",
                "datenauswertung/radtraining/20.gpx",
                "datenauswertung/radtraining/19.gpx",
                "datenauswertung/radtraining/18.gpx", 
                "datenauswertung/radtraining/17.gpx", 
                "datenauswertung/radtraining/16.gpx", 
                "datenauswertung/radtraining/15.gpx",
                "datenauswertung/radtraining/14.gpx", 
                "datenauswertung/radtraining/13.gpx", 
                "datenauswertung/radtraining/12.gpx", 
                "datenauswertung/radtraining/11.gpx", 
                "datenauswertung/radtraining/10.gpx", 
                "datenauswertung/radtraining/9.gpx", 
                "datenauswertung/radtraining/8.gpx", 
                "datenauswertung/radtraining/7.gpx", 
                "datenauswertung/radtraining/6.gpx",  
                "datenauswertung/radtraining/5.gpx",  
                "datenauswertung/radtraining/4.gpx", 
                "datenauswertung/radtraining/3.gpx", 
                "datenauswertung/radtraining/2.gpx", 
                "datenauswertung/radtraining/1.gpx"
            };

            //System.out.print(gpxFiles);

            //18-1
            double[] corosWerteAerob = {
                3.3, 1.2, 3.4, 4.0, 2.4, 
                2.6, 3.0, 3.7, 2.3, 2.2, 
                3.0, 2.4, 2.5, 3.9, 4.3, 2.8, 2.2,
                1.8, 3.6, 3.1, 3.9, 2.8,
                2.3, 2.8, 2.2
            };

            double[] corosWerteAnaerob = {
                0.2, 0.0, 0.2, 0.0, 0.0, 
                0.0, 0.0, 0.0/*18 - 22.08 */, 0.2, 0.0, 
                0.0, 0.6, 0.4, 0.0, 0.1, 0.3, 0.1,
                0.0, 0.0, 0.0, 1.2, 0.0,
                0.0, 0.0, 0.0
            };

            ArrayList<Training> meineTrainings = new ArrayList<>();

            for(String file : gpxFiles) {
                Training t = loadGpx(file);
                if (t != null && t.getTrackPoints() != null && !t.getTrackPoints().isEmpty()) {
                    meineTrainings.add(t);
                } else {
                    System.err.print("Warnung: die datei ungültig oder keine punkte gefunden in: " + file);
                }
            }

            //List<TrainingData> dataset = CreateTrainingsList.createTrainingList(meineTrainings, corosWerte);
            List<TrainingData> datasetAnaerob = CreateTrainingsList.createTrainingList(meineTrainings, corosWerteAnaerob);

            int iterationen = 9000000;
            double[] optimaleFactors = FaktorSucher.optimizeFactorsAnaerob(datasetAnaerob, iterationen);

            System.out.println("\n--- Gefundene optimale Gewichtungsfaktoren ---");
            for (int i = 0; i < optimaleFactors.length; i++) {
                System.out.printf("Faktor f%d (Zone %d): %.4f%n", i, i, optimaleFactors[i]);
            }

            System.out.println("\nvergleich tatsächlich und ausherechnete");
            double totalDifferenz = 0.0;
            for (int i = 0; i < datasetAnaerob.size(); i++) {
                TrainingData data = datasetAnaerob.get(i);

                double aerobicWert = 0.0;
                for (int z = 0; z < 6; z++) {
                    aerobicWert += data.zones[z] * optimaleFactors[z];
                }

                double aerobicFraction = aerobicWert / data.totalTimeMinuts;
                double aerobicTrainingEffect = data.trainingLoad * aerobicFraction;
                double calculatedAE = 5.0 * (1.0 - Math.pow(Math.E, (-aerobicTrainingEffect / 60.0)));

                double differenz = calculatedAE - data.corosAE;
                totalDifferenz += differenz;

                
                System.out.printf("Training %2d | COROS Ist: %.1f | Modell: %.1f | Differenz: %+.2f%n", 
                                    (i + 1), data.corosAE, calculatedAE, differenz);
                                                    
                System.out.println("totalDifferenz: " + totalDifferenz);
                System.out.println(totalDifferenz > -2.0999624073871628);
            }
    }
 
    public static double distance(TrkPt a, TrkPt b) { //distanz ausrechnen mit Haversine-Formel 
        //double fullDist = 0;

        double lat1Rad = Math.toRadians(a.lat);
        double lat2Rad = Math.toRadians(b.lat);
        double lon1Rad = Math.toRadians(a.lon);
        double lon2Rad = Math.toRadians(b.lon);

        double divLat = lat2Rad - lat1Rad;  //dist = 2R*arcsin(sqrt ( sin^2( (lat2 - lat1) /2 ) ) + cos(lat1) * cos(lat2) * sin^2 ( (lon2 - lon1) / 2))
        double divLon = lon2Rad - lon1Rad;

        double produkt1 = 2*6371;           //2R
        double produkt2 = Math.pow(Math.sin(divLat / 2), 2); // sin^2( (lat2 - lat1) /2 )
        double produkt3 = Math.cos(lat1Rad);                 //cos(lat1)   
        double produkt4 = Math.cos(lat2Rad);                 //cos(lat2)
        double produkt5 = Math.pow(Math.sin(divLon / 2), 2); //sin^2 ( (lon2 - lon1) / 2)
        double produkt6 = Math.asin(Math.sqrt(produkt2 + produkt3 * produkt4 * produkt5)); //arcsin(sqrt ( sin^2( (lat2 - lat1) /2 ) ) + cos(lat1) * cos(lat2) * sin^2 ( (lon2 - lon1) / 2))
        double dist = produkt1 * produkt6;                   // 2R*arcsin(...)
                
        return dist;
    }
        
    public static double calculateTotalDistance(ArrayList<TrkPt> trackPoints) {
        double fullDist = 0;

        for (int i = 0; i < trackPoints.size() - 1; i++) {                  //ganze distanz ausrechnen           
            
            TrkPt b = trackPoints.get(i);
            TrkPt a = trackPoints.get(i+1);

            double dist = distance(a, b);

            fullDist += dist;   
        }
        
        return fullDist;
    }       

    public static void jsonFormatter(ArrayList<TrkPt> list) throws Exception { //was komt hier rein?
        ObjectMapper mapper = new ObjectMapper();

        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();         //schön formatiertes json 
        String formattedJson = writer.writeValueAsString(list);                //java -> json string 

        System.out.println(formattedJson);
    }

    public static String buildGeoJsonForMap(ArrayList<TrkPt> trackPoints) throws Exception {
        ObjectMapper mapper = new ObjectMapper();                              //tool um json zu bauen, lesen, erstellen, formatieren

        ArrayNode coordinatesNode = mapper.createArrayNode();                  //liste für koordinaten
        for (TrkPt pt: trackPoints) {                                          //lon lat statt lat lon, da maplibre so will 
            ArrayNode point = mapper.createArrayNode();
            point.add(pt.lon);
            point.add(pt.lat);                                                  
            coordinatesNode.add(point);
        }

            ObjectNode feature = mapper.createObjectNode();                    //erstellt einen leeren objekt 
            feature.put("type", "Feature");                                    //mit "type", "Feature" einfühlen ->  {"type": "Feature"} = json
            ObjectNode geometry = mapper.createObjectNode();
            geometry.put("type", "LineString");                                //sagt der karte, es ist eine linie
            geometry.set("coordinates", coordinatesNode);                      //koordinaten einfügen
            feature.set("geometry", geometry);                                 //alles zusammenbauen
            

            ObjectNode properties = mapper.createObjectNode();
            properties.put("color", "#004aad");
            properties.put("date", dateString);
            properties.put("distanceBetweenPointsGerundet", distanceBetweenPointsGerundet); // kommt alles aus loadGpx
            properties.put("time", time);
            properties.put("activeTimeFormatted", activeTimeFormatted);
            properties.put("averageSpeedGerundet", averageSpeedGerundet );
            properties.put("maxGeschwindigkeitKmh", maxGeschwindigkeitKmh );
            properties.put("hoeheSummeGerundet", hoeheSummeGerundet );
            properties.put("maxHeartRate", maxHeartRate);
            properties.put("averageHR", averageHR);
            properties.put("timeIn0HrZone", timeIn0HrZone);
            properties.put("timeIn1HrZone", timeIn1HrZone);
            properties.put("timeIn2HrZone", timeIn2HrZone);
            properties.put("timeIn3HrZone", timeIn3HrZone);
            properties.put("timeIn4HrZone", timeIn4HrZone);
            properties.put("timeIn5HrZone", timeIn5HrZone);
            properties.put("trainingLoad", trainingLoad);
            properties.put("calories", calories);
            properties.put("aerobicTrainingEffect", aerobicTrainingEffect);
            properties.put("anaerobicTrainingEffect", anaerobicTrainingEffect);

            properties.put("comment", "test");
            feature.set("properties", properties);

            ArrayNode features = mapper.createArrayNode(); 
            features.add(feature);

            ObjectNode featureCollection = mapper.createObjectNode();          
            featureCollection.put("type", "FeatureCollection");
            featureCollection.set("features", features);                       //bauet fertiger stuktur

            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
            return writer.writeValueAsString(featureCollection);               //fertiger json-string
    }
    
}