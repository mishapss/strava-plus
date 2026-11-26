public class TrainingData {
    public double trainingLoad;
    public double[] zones; //zeit in den zonen
    public double totalTimeMinuts;
    public double corosAE;

    public TrainingData(double[] zones, double totalTimeMinuts, double trainingLoad, double corosAE){
        this.zones = zones;
        this.totalTimeMinuts = totalTimeMinuts;
        this.trainingLoad = trainingLoad;
        this.corosAE = corosAE;
    }
}