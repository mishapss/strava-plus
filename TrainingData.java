public class TrainingData {
    public double trainingLoad;
    public double[] zones; //zeit in den zonen
    public double totalTime;
    public double corosAE;

    public TrainingData(double[] zones, double totalTime, double trainingLoad, double corosAE){
        this.zones = zones;
        this.totalTime = totalTime;
        this.trainingLoad = trainingLoad;
        this.corosAE = corosAE;
    }
}