import java.io.File;                                                        //datei öfnen
import java.io.IOException; 
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
public class TimeSet {
    
    public int maxHr = 200;

    public void analyzeWorkout(List<TrkPt> points, int maxHr) {
        File xmlFile = new File("C:\\Users\\User\\projects_programming\\strava-plus-main\\route_uploaf.gpx");

        List<List<Integer>> heartZones = new ArrayList<>();       //liste für alle gps punkte
        
        for (int i = 0; i < 6; i ++) {
            heartZones.add(new ArrayList<>());
        }
        
        for (TrkPt point : points) {
            int currentHeartRate = point.getHeartRate(); // jetzige HR
            int zoneIndex = determineZone(currentHeartRate, maxHr);
        }
    }

    public int determineZone(int heartRate, int maxHr) {
        double maxHrDouble = maxHr;
        System.out.print(maxHrDouble);
        return maxHr;
        
    }



}