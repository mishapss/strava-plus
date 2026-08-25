import java.util.ArrayList;
import java.util.List;

public class createTrainingsList {
    public static int trainingLoad;
    public static int aerobicTrainingLoad;
    public static int totalTime;
 //18-1
    double[] corosWerte = {
        3.7, 2.3, 2.2, 3.0, 2.4,
        2.5, 3.9, 4.3, 2.8, 2.2,
        1.8, 3.6, 3.1, 3.9, 2.8,
        2.3, 2.8, 2.2
    };
    
    public static List<TrainingData> createTrainingList(ArrayList<Training> trainings, double[] corosWerte) {

        List<TrainingData> resuList = new ArrayList<>(); //erstellt neue liste
        WorkoutAnalyzer analyzer = new WorkoutAnalyzer(); 

        for (int i = 0; i < trainings.size(); i++) {
            Training t = trainings.get(i);
            List<TrkPt> points = t.getPoints(); //holt die trackpunkte
            WorkoutResult result = analyzer.analyzeWorkout(points); //auswertung der punkte
            int[] zones = result.timeInZone; //zonen-array speichern

            double[] zonesDouble = new double[6];

            for (int z = 0; z < zonesDouble.length; z++) {
                zonesDouble[z] = zones[z];
                totalTime += zonesDouble[z];
            }
            
            double trainingLoad = analyzer.getTrainingLoad(points); //bekommen trainingsbelastung
            
            double corosAE = corosWerte[i];

            TrainingData data = new TrainingData(zonesDouble, totalTime, trainingLoad, corosAE); //
            
            resuList.add(data);
        }

        return resuList;
    }
}