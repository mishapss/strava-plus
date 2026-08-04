import java.util.List;

public class WorkoutResult {
    public int[] timeInZone;
    public List<List<Integer>> heartZones;

    public WorkoutResult(int[] timeInZone, List<List<Integer>> heartZones) {
        this.timeInZone = timeInZone;
        this.heartZones = heartZones;
    }
}