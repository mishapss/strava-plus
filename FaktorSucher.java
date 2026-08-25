import java.util.List;
import java.util.Random;

public class FaktorSucher {
    public static double faktor0 = 0.539;
    public static double faktor1 = 0.315;
    public static double faktor2 = 0.799;
    public static double faktor3 = 0.2;
    public static double faktor4 = 0.1;
    public static double faktor5 = 0.05;

    public static double calculateMSE(List<TrainingData> dataset, double[] factors) {
        double e = Math.E;
        double totalSquaredError = 0.0;

        for (TrainingData data: dataset) {
            double aerobicWert = data.zones[0] * factors[0] + 
                                 data.zones[1] * factors[1] + 
                                 data.zones[2] * factors[2] + 
                                 data.zones[3] * factors[3] + 
                                 data.zones[4] * factors[4] + 
                                 data.zones[5] * factors[5]; //zeit in den aeroben zonen
            
            double aerobicFraction = aerobicWert / (data.totalTime); //anteil der aeroben zeit an der gesamten zeit
            double aerobicTrainingEffect = data.trainingLoad * aerobicFraction; //berechnung des aeroben trainingseffekt
            double calculatedAE = 5.0 * (1.0 - Math.pow(e, (-aerobicTrainingEffect / 60))); //berechnung des aeroben trainingseffekt auf einer skala von 0 bis 5     

            double error = calculatedAE - data.corosAE;
            totalSquaredError = error * error;
        }

        return totalSquaredError / dataset.size();// mittleren quadratischen Fehler berechnen       
    }

    public static double[] optimizeFactors(List<TrainingData> dataset, int totalIterations) {
        Random random = new Random(); //random-objekt instanziieren
        double[] currentFactors = {0.5, 0.5, 0.5, 0.5, 0.5, 0.5}; //array erstellen
        double[] bestFactors = currentFactors.clone(); //start-array klonen 

    }
}