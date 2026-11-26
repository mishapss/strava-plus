public class BestMetric {
    private double value;
    private String fileName;

    public BestMetric(double value, String fileName) {
        this.value = value;
        this.fileName = fileName;
    }

    public double getValue() {
        return value;
    }

    public String getFileName() {
        return fileName;
    }
}