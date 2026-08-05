import com.fasterxml.jackson.databind.ObjectMapper;                         // wandelt daten json <-> java
import com.fasterxml.jackson.databind.JsonNode;                             //darstellung der daten als ein baum
import com.fasterxml.jackson.databind.ObjectWriter;                         //formatierung json schön
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;


import com.fasterxml.jackson.dataformat.xml.XmlMapper;
//import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.io.File;                                                        //datei öfnen
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.ArrayList;
//import java.util.List;
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
    public static double kalorien;
    public static double aerobicTrainingEffect;


    public static void loadGpx(String fileName) throws Exception { //lesen gpx
        File xmlFile = new File(fileName);                       //datei-objekt erstellen, öffnen                                   
        XmlReader reader = new XmlReader();
        ArrayList<TrkPt> trackPoints = new ArrayList<>();       //liste für alle gps punkte 
        
        WorkoutAnalyzer analyzer = new WorkoutAnalyzer();         //objekt für die analyse der daten

        if (!xmlFile.exists()) {                                //überprüfung, ob die datei überhauprt existiert
            System.out.println("XML File existiert nicht");
            return;
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
                //System.out.println(" HR: " + hr);


                TrkPt punkt = new TrkPt(lat, lon, ele, time, geschwindigkeitMps, hr);    //jeder punkt als objekt gespeichert 
                trackPoints.add(punkt);                          //jeder punkt zur liste hinzugefügt             
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
            String dateString = date.toString();
            XmlReader.dateString = dateString;


            //Считаем разницу между ними
            Duration duration = Duration.between(startToParse, endToParse);
            long sekunden = duration.toSeconds();
            int sekundenInt = Math.toIntExact(sekunden);
            long minutes = duration.toMinutes();
            int minutesInt = Math.toIntExact(minutes);

            int restHours = minutesInt/60;

            time = reader.stringFormatZoneTime(sekundenInt);

            //ausrechnung der distanz
            double distanceBetweenPoints = calculateTotalDistance(trackPoints);
            distanceBetweenPointsGerundet = Math.round(distanceBetweenPoints * 100.0) / 100.0;

            WorkoutResult result = analyzer.analyzeWorkout(trackPoints, 200);
            int[] zones = result.timeInZone;
            
            trainingLoad = (int)analyzer.getTrainingLoad(trackPoints, 200, 49);
            aerobicTrainingLoad = (int)analyzer.getAerobicTrainingEffect(trackPoints, 200, 49);

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
            double kalorien = analyzer.getKalorienVerbrauch(trackPoints, 200, 19);//ausrechnung der kalorien
            XmlReader.kalorien = kalorien;

            double aerobicTrainingEffect = analyzer.getAerobicTrainingEffect(trackPoints, 200, 47);
            XmlReader.aerobicTrainingEffect = aerobicTrainingEffect;

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
            " average HR: " + averageHR + " kalorien: " + kalorien + "\n" );
            
            geoJsonData = buildGeoJsonForMap(trackPoints);       //wandelt die liste in geojson 
            System.out.println("JSON erzeugt!");
            
        } catch (IOException e) {
            System.err.println("Fehler beim Einlesen: " + e.getMessage());
            e.printStackTrace();
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
        //loadGpx("ride.gpx");
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
            properties.put("color", "#a03a3a");
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
            properties.put("kalorien", kalorien);
            properties.put("aerobicTrainingEffect", aerobicTrainingEffect);

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