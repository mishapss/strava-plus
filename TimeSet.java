import java.io.File;                                                        //datei öfnen
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
public class TimeSet {
    
    public int maxHr = 200;

    public static void main(String[] args) throws IOException {

        File xmlFile = new File("C:\\Users\\MikhailLeshchenko\\strava_plus\\route_uploaf.gpx");  //C:\\Users\\User\\projects_programming\\strava-plus-main\\route_uploaf.gpx
        XmlMapper xmlMapper = new XmlMapper();                  //tool um aus xml in java zu gehen
        List<TrkPt> points = new ArrayList<>();

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

                TrkPt point = new TrkPt(lat, lon, ele, time, 0.0, hr);
                points.add(point);                         //jeder punkt zur liste hinzugefügt             
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        TimeSet timeSet = new TimeSet();
        timeSet.analyzeWorkout(points, 200);
        int durchschnittlicheHR = timeSet.getDurchschnittlicheHR(points);
        int maxHR = timeSet.getMaxHr(points);
        System.out.println("Durchschnittliche Herzfrequenz: " + durchschnittlicheHR);
        System.out.println("Maximale Herzfrequenz: " + maxHR);
    }

    public void analyzeWorkout(List<TrkPt> points, int maxHr) {
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
    }

    public long calculateTimeDifferenceInSeconds(String current, String next) {

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
}