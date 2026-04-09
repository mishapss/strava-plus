import com.fasterxml.jackson.databind.ObjectMapper;                         // wandelt daten json <-> java
import com.fasterxml.jackson.databind.JsonNode;                             //darstellung der daten als ein baum
import com.fasterxml.jackson.databind.ObjectWriter;                         //formatierung json schön
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;


import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.io.File;                                                        //datei öfnen
import java.io.IOException; 
import java.util.ArrayList;

public class XmlReader {

    public static String geoJsonData;

    public static void loadGpx(String fileName) throws Exception {
        File xmlFile = new File(fileName);                      //datei-objekt erstellen
        double fullDist = 0;

        ArrayList<TrkPt> trackPoints = new ArrayList<>();       //alle gps punkte seichern

        if (!xmlFile.exists()) {
            System.out.println("XML File existiert nicht");
            return;
        }

        XmlMapper xmlMapper = new XmlMapper();                  //jackson tool, um sml zu lesen
        try {
            JsonNode root = xmlMapper.readTree(xmlFile);        //xml wird in baumstruktur geladen    

            ObjectMapper jsonMapper = new ObjectMapper();       //
            JsonNode trkptNode = root.path("trk").path("trkseg").path("trkpt");

            for (JsonNode trkpt : trkptNode){
                double lat = trkpt.path("lat").asDouble();
                double lon = trkpt.path("lon").asDouble();
                double ele = trkpt.path("ele").asDouble();
                String time = trkpt.path("time").asText();

                TrkPt punkt = new TrkPt(lat, lon, ele, time);    //jeder punkt als objekt gespeichert 
                trackPoints.add(punkt);                          //jeder punkt zur liste hinzugefügt                
            }

            geoJsonData = buildGeoJsonForMap(trackPoints);
            System.out.println("JSON erzeugt!");
            
        } catch (IOException e) {
            System.err.println("Fehler beim Einlesen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        loadGpx("ride.gpx");
    }
 
    public static double distance(TrkPt a, TrkPt b) {
        double fullDist = 0;

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
        /*
        for (int i = 0; i < trackPoints.size() - 1; i++) {                  //distanz ausrechnen           
            
            TrkPt b = trackPoints.get(i);
            TrkPt a = trackPoints.get(i+1);
            
            System.out.println("A: " + a.lat + " " + a.lon);
            System.out.println("B: " + b.lat + " " + b.lon);
            
            double dist = distance(a, b);
            System.out.println(dist);
            fullDist += dist;   
        }
        
        System.out.println("fulldist: " + fullDist + " km");
        jsonFormatter(trackPoints);   */                                       //liste als json anzeigen
    } 

    public static void jsonFormatter(ArrayList<TrkPt> list) throws Exception { //was komt hier rein?
        ObjectMapper mapper = new ObjectMapper();

        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();         //schön formatiertes json 
        String formattedJson = writer.writeValueAsString(list);                //java -> json string 

        System.out.println(formattedJson);
    }


    public static String buildGeoJsonForMap(ArrayList<TrkPt> trackPoints) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        ArrayNode coordinatesNode = mapper.createArrayNode();
        for (TrkPt pt: trackPoints) {
            ArrayNode point = mapper.createArrayNode();
            point.add(pt.lon);
            point.add(pt.lat);
            coordinatesNode.add(point);
        }

            ObjectNode feature = mapper.createObjectNode();
            feature.put("type", "Feature");
            ObjectNode geometry = mapper.createObjectNode();
            geometry.put("type", "LineString");
            geometry.set("coordinates", coordinatesNode);
            feature.set("geometry", geometry);

            ObjectNode properties = mapper.createObjectNode();
            properties.put("color", "#FC4C02");
            feature.set("properties", properties);

            // FeatureCollection
            ArrayNode features = mapper.createArrayNode();
            features.add(feature);

            ObjectNode featureCollection = mapper.createObjectNode();
            featureCollection.put("type", "FeatureCollection");
            featureCollection.set("features", features);

            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
            return writer.writeValueAsString(featureCollection);
    }
    
}