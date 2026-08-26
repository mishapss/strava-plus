import java.util.List;

public class Training {
    private List<TrkPt> points;
    private List<TrkPt> trackPoints;
    private double aerobicTrainingEffektCoros;

    public Training(List<TrkPt> trackPoints) {
        this.trackPoints = trackPoints;
    }
    public List<TrkPt> getTrackPoints() {
        return trackPoints;
    }

    public void setTrackPoints(List<TrkPt> trackPoints) {
        this.trackPoints = trackPoints;
    }

    // Getter für die Punkte
    public List<TrkPt> getPoints() {
        return this.points;
    }

    public double getAerobicTrainingEffektCoros() {
        return this.aerobicTrainingEffektCoros;
    }
}