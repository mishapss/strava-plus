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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class XmlReader {

    public static String geoJsonData;                           //speicher für die fertige route
    public static double distanceBetweenPointsGerundet;
    public static String time;
    public static double averageSpeed;
    public static double maxGeschwindigkeitKmh;
    public static double hoeheSummeGerundet;
    public static int averageHR;
    public static double averageSpeedGerundet;


    public static void loadGpx(String fileName) throws Exception { //lesen gpx
        File xmlFile = new File(fileName);                       //datei-objekt erstellen, öffnen                                   
        XmlReader reader = new XmlReader();
        ArrayList<TrkPt> trackPoints = new ArrayList<>();       //liste für alle gps punkte 

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
            Instant endToParse = Instant.parse(endTimeString);

            //Считаем разницу между ними
            Duration duration = Duration.between(startToParse, endToParse);
            long sekunden = duration.toSeconds();
            int sekundenInt = Math.toIntExact(sekunden);
            long minutes = duration.toMinutes();
            int minutesInt = Math.toIntExact(minutes);

            double restHoursDouble = minutesInt/60.0;
            int restHours = minutesInt/60;
            int restMinutes = minutesInt - restHours * 60;
            int restSekunden = sekundenInt - minutesInt * 60;



            time = restHours + "h " + restMinutes + "min " + restSekunden + "sek" + "\n";



            //ausrechnung der distanz
            double distanceBetweenPoints = calculateTotalDistance(trackPoints);
            distanceBetweenPointsGerundet = Math.round(distanceBetweenPoints * 100.0) / 100.0;



            //ausrechnung der durschnittlichen Geschwindigkeit
            double averageSpeed = 0.0;
            averageSpeed = distanceBetweenPoints/restHoursDouble;
            System.out.print("distanceBetweenPoints: " + distanceBetweenPoints + " restHours: " + restHours);
            averageSpeedGerundet = Math.round(averageSpeed * 100.0) / 100.0;


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



            //ausgabe
            System.out.print(" average Speed: " + averageSpeedGerundet + 
            " distance: " + distanceBetweenPointsGerundet + " time: " + time
            + " Anstieg: " + hoeheSummeGerundet + " max speed: " + maxGeschwindigkeitKmh + 
            " average HR: " + averageHR + "\n" );

            //ausgabe hr daten
            int[] zones = reader.analyzeWorkout(trackPoints, 200); 
            
            geoJsonData = buildGeoJsonForMap(trackPoints);       //wandelt die liste in geojson 
            System.out.println("JSON erzeugt!");
            
        } catch (IOException e) {
            System.err.println("Fehler beim Einlesen: " + e.getMessage());
            e.printStackTrace();
        }
    }

        public int[] analyzeWorkout(List<TrkPt> points, int maxHr) { //analysiert hr daten
        List<List<Integer>> heartZones = new ArrayList<>();       //liste für alle gps punkte
        
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

        int sumTotalTime = timeInZone[0] + timeInZone[1] + timeInZone[2] + timeInZone[3] + timeInZone[4] + timeInZone[5];
        int sumActiveTime = (heartZones.get(0).size() + heartZones.get(1).size() + heartZones.get(2).size() + heartZones.get(3).size() + heartZones.get(4).size() + heartZones.get(5).size());

        System.out.println("sumTotalTime in Zones 0-5: " + sumTotalTime + " seconds");
        System.out.println("sumActiveTime in Zones 0-5: " + sumActiveTime + " seconds");

        return timeInZone;
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
        
        double percentage = (double) heartRate / maxHr * 100;

        if (percentage < 50) {
            heartZones.get(0).add(heartRate);
            System.out.println(heartZones.get(0));
            return 0;
        }
        if (percentage < 60) {
            heartZones.get(1).add(heartRate);
            return 1;
        }
        if (percentage < 70) {
            heartZones.get(2).add(heartRate);
            return 2;
        }
        if (percentage < 80) {
            heartZones.get(3).add(heartRate);
            return 3;
        }
        if (percentage < 90) {
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
            properties.put("distanceBetweenPointsGerundet", distanceBetweenPointsGerundet);
            properties.put("time", time);
            properties.put("averageSpeedGerundet", averageSpeedGerundet );
            properties.put("maxGeschwindigkeitKmh", maxGeschwindigkeitKmh );
            properties.put("hoeheSummeGerundet", hoeheSummeGerundet );
            properties.put("averageHR", averageHR);
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