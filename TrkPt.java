public class TrkPt {

    public double lat; //Attribut in xml
    public double lon;
    public double ele; //Unterelement <xml>
    public String time;
    public double geschwindigkeitMps;
    public int hr;

    public TrkPt (double lat, double lon, double ele, String time, double geschwindigkeitMps, int hr) {                //voller Konsktruktor
        this.lat = lat;
        this.lon = lon;
        this.ele = ele;
        this.time = time;
        this.geschwindigkeitMps = geschwindigkeitMps;
        this.hr = hr;
    }

    public TrkPt (double lat, double lon) {                                         //konstruktor für die karte 
        this.lat = lat;
        this.lon = lon;
        this.ele = 0;
        this.time = null;        
    }

    public TrkPt (String time) {
        this.time = time;
    }


    public int getHeartRate() {
        return hr;
    }

    @Override
    public String toString() {                                                      //debug, um auszugeben, dass die daten da sind
        return "[" + lat + " ,"+ lon + "],";
    }
}