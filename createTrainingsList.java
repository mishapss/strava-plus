import java.util.ArrayList;
import java.util.List;

public class CreateTrainingsList {
    public static int trainingLoad;
    public static int aerobicTrainingLoad;
    public static int totalTime;
    public static double totalTimeMinuts;
   
    
    public static List<TrainingData> createTrainingList(ArrayList<Training> trainings, double[] corosWerte) {

        List<TrainingData> resuList = new ArrayList<>(); //erstellt neue liste
        WorkoutAnalyzer analyzer = new WorkoutAnalyzer(); 

        for (int i = 0; i < trainings.size(); i++) {
            Training t = trainings.get(i);
            List<TrkPt> points = t.getTrackPoints(); //holt die trackpunkte

            if (points == null) { //sicherung vorm fehler
                System.err.print("training an index " + i + " hat keine Punkte");
                continue;
            }

            WorkoutResult result = analyzer.analyzeWorkout(points); //auswertung der punkte, bekommen die hr-zonen
            int[] zones = result.timeInZone; //zonen-array speichern

            double[] zonesInMinutes = new double[6]; //die zeit in jeder zone bekommen
            double totalTimeMinuts = 0.0;

            for (int z = 0; z < 6; z++) {
                zonesInMinutes[z] = zones[z] /60.0; //sekunden in minuten
                totalTimeMinuts += zonesInMinutes[z]; //gesamte trainingszeit

            }
            
            double trainingLoad = analyzer.getTrainingLoad(points); //bekommen trainingsbelastung
            
            double corosAE = corosWerte[i]; //bekommen die werte von coros

            TrainingData data = new TrainingData(zonesInMinutes, totalTimeMinuts, trainingLoad, corosAE); //
            
            resuList.add(data);
        }

        return resuList;
    }
}