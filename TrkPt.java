import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.ArrayList;

public class TrkPt {

    public double lat; //Attribut in xml
    public double lon;
    public double ele; //Unterelement <xml>
    public String time;

    public TrkPt (double lat, double lon, double ele, String time) {                //voller Konsktruktor
        this.lat = lat;
        this.lon = lon;
        this.ele = ele;
        this.time = time;
    }

    public TrkPt (double lat, double lon) {                                         //konstruktor für die karte 
        this.lat = lat;
        this.lon = lon;
        this.ele = 0;
        this.time = null;        
    }

    @Override
    public String toString() {                                                      //debug, um auszugeben, dass die daten da sind
        return "[" + lat + " ,"+ lon + "],";
    }
}