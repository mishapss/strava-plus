import java.util.List;
import java.util.Random;

public class FaktorSucher {


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
            
            double aerobicFraction = aerobicWert / (data.totalTimeMinuts); //anteil der aeroben zeit an der gesamten zeit
            double aerobicTrainingEffect = data.trainingLoad * aerobicFraction; //berechnung des aeroben trainingseffekt
            double calculatedAE = 5.0 * (1.0 - Math.pow(e, (-aerobicTrainingEffect / 60))); //berechnung des aeroben trainingseffekt auf einer skala von 0 bis 5     

            double error = calculatedAE - data.corosAE;
            totalSquaredError += error * error;
        }

        return totalSquaredError / dataset.size();// mittleren quadratischen Fehler berechnen       
    }

    public static double calculateMSEAnaerob(List<TrainingData> dataset, double[] factors) {
        double e = Math.E;
        double totalSquaredError = 0.0;

        for (TrainingData data: dataset) { //zone 0-2 sind weg, weil sie kein einfluss auf anaerob bereich haben
            double anaerobicWert = data.zones[3] * factors[3] + 
                                 data.zones[4] * factors[4] + 
                                 data.zones[5] * factors[5]; // anaerobic-wert ausrechnen
            
            double anaerobicFraction = anaerobicWert / (data.totalTimeMinuts); //anteil der aaneroben zeit an der gesamten zeit
            double anaerobicTrainingEffect = data.trainingLoad * anaerobicFraction; //berechnung des anaeroben trainingseffekt
            double calculatedAE = 5.0 * (1.0 - Math.pow(e, (-anaerobicTrainingEffect / 60))); //berechnung des aeroben trainingseffekt auf einer skala von 0 bis 5     

            double error = calculatedAE - data.corosAE; //ausrechnung des fehlers
            totalSquaredError += error * error; //quadratische fehler
        }

        return totalSquaredError / dataset.size();// mittleren quadratischen Fehler berechnen       
    }

    public static double[] optimizeFactors(List<TrainingData> dataset, int totalIterations) {
        Random random = new Random(); //random-objekt instanziieren
        
        double[] currentFactors = {0.01, 0.01, 0.01, 0.01, 0.01, 0.01}; //array mit anfangswerten erstellen
        double[] bestFactors = currentFactors.clone(); //start-array klonen 

        double bestMSE = calculateMSE(dataset, currentFactors); //start mse-wert finden
        System.out.println("ausgabe: bestMSE: " + bestMSE);
        int noImporovementCounter = 0;

        //schritt2
        for (int i = 0; i < totalIterations; i++) {
            double stepSize = 0.2 * (1.0 - ((double) i / totalIterations));
            int index = random.nextInt(6); //zufälliger index von 0 bis 5
            double oldFaktor = currentFactors[index]; //alten faktor speichern
            
            double change = (random.nextDouble() - 0.5) * stepSize; //zufallszahl von -0.5 bis 0.5

            currentFactors[index] += change;

            if (currentFactors[index] < 0.0) {
                currentFactors[index] = 0;
            }

            for (int z = 1; z < currentFactors.length; z++) {
                if (currentFactors[z] < currentFactors[z-1]) {
                    currentFactors[z] = currentFactors[z-1];
                }
            }

            //schritt 3
            double newMSE = calculateMSE(dataset, currentFactors);
            if (newMSE < bestMSE) {
                bestMSE = newMSE;
                bestFactors = currentFactors.clone(); //alten array in neuen kopieren
                noImporovementCounter = 0;
            } else {
                currentFactors[index] = oldFaktor;
                noImporovementCounter++; //counter erhöhen
            }

            //schritt 4
            if (noImporovementCounter > 5000) {
                for (int k = 0; k < currentFactors.length; k++) { //neue werte für faktoren
                    currentFactors[k] = random.nextDouble();
                }
                noImporovementCounter = 0;
            }
        }
        //schritt 5
        System.out.println(bestMSE);
        return bestFactors;

    }
    public static double[] optimizeFactorsAnaerob(List<TrainingData> dataset, int totalIterations) {
        Random random = new Random(); //random-objekt instanziieren
        
        double[] currentFactors = {0.0, 0.0, 0.0, 0.001, 0.001, 0.001}; //array mit start werten erstellen

        double[] bestFactors = currentFactors.clone(); //start-array klonen 
        
        double bestMSE = calculateMSEAnaerob(dataset, currentFactors); //start mse-wert finden
        
        int noImporovementCounter = 0;

        //schritt2
        for (int i = 0; i < totalIterations; i++) {
            double stepSize = 0.2 * (1.0 - ((double) i / totalIterations));

            int index = 3 + random.nextInt(3); //zufällige zone von 3 bis 5
            double oldFaktor = currentFactors[index]; //alten faktor speichern
            
            double change = (random.nextDouble() - 0.5) * stepSize; //zufallige abweichnung von -0.5 bis 0.5 generieren

            currentFactors[index] += change; //faktor ändern

            if (currentFactors[index] < 0.0) {
                currentFactors[index] = 0; //faktor darf nicht negativ sein
            }

            if (currentFactors[index] > 5.0) {
                currentFactors[index] = 5.0; //faktor darf nicht größer 5 sein
            }

            for (int z = 4; z < currentFactors.length; z++) { //die höhere zone darf nicht kleineren wert als die untere zone haben
                if (currentFactors[z] < currentFactors[z-1]) {
                    currentFactors[z] = currentFactors[z-1];
                }
            }

            //schritt 3
            double newMSE = calculateMSEAnaerob(dataset, currentFactors); //neue fehler ausrechnen

            if (newMSE < bestMSE) {
                bestMSE = newMSE; //neues fehler speichern,wenn es kleiner ist
                bestFactors = currentFactors.clone(); //alten array in neuen kopieren
                noImporovementCounter = 0; //counter zurücksetzen
            } else {
                currentFactors[index] = oldFaktor; //den wert zurücksetzen, wenn fehler größer ist
                
                for (int z = 4; z < currentFactors.length; z++) {//sichert, dass array monoton bleibt nach dem zurücksetzen
                    if (currentFactors[z] < currentFactors[z - 1]) {
                        currentFactors[z] = currentFactors[z - 1];
                    }
                }

                noImporovementCounter++; //counter erhöhen
            }

            //schritt 4
            if (noImporovementCounter > 5000) {
                currentFactors[0] = 0.0;
                currentFactors[1] = 0.0;
                currentFactors[2] = 0.0;
                
                // nur zone 3-5 neu würfeln, wenn über 5k erfolglose versuche
                currentFactors[3] = random.nextDouble();
                currentFactors[4] = currentFactors[3] + random.nextDouble();
                currentFactors[5] = currentFactors[4] + random.nextDouble();
                noImporovementCounter = 0;
            }
        }
        //schritt 5
        System.out.println("best anaerob faktors: " + bestMSE);
        return bestFactors;

    }
}

/*
Faktor f0 (Zone 0): 0,4206
Faktor f1 (Zone 1): 0,4206
Faktor f2 (Zone 2): 0,7713
Faktor f3 (Zone 3): 0,7713
Faktor f4 (Zone 4): 0,7713
Faktor f5 (Zone 5): 0,7713
0.01157670951257006 


Faktor f0 (Zone 0): 0,0000
Faktor f1 (Zone 1): 0,0000
Faktor f2 (Zone 2): 0,0000
Faktor f3 (Zone 3): 3,6264
Faktor f4 (Zone 4): 3,7257
Faktor f5 (Zone 5): 3,8252
-2.0999624073871628

*/