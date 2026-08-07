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
import java.util.List;
//import java.util.List;
import java.time.ZoneId;

public class FaktorSucher {
    public static double faktor0 = 0.539;
    public static double faktor1 = 0.315;
    public static double faktor2 = 0.799;
    public static double faktor3 = 0.2;
    public static double faktor4 = 0.1;
    public static double faktor5 = 0.05;

    public static void sucheFaktor(List<Training> trainings) throws Exception {
        WorkoutAnalyzer analyzer = new WorkoutAnalyzer();

        for (Training training: trainings) {
            double aerobicTrainingEffekt = analyzer.getAerobicTrainingEffect(training.getPoints(),200, 47);

            double fehler = aerobicTrainingEffekt - training.getAerobicTrainingEffektCoros();
            double quadratischFehler = fehler * fehler;

        }
        
    }
}