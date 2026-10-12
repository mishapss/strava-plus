public class ProfileData {
    public int userID;
    public String name;
    public String secondName;
    public int age;
    public String sex;
    public double weight;
    public double height;
    public int maxHR;
    public int restingHR;
    public double gesamteDistanzProjahr;
    public int maxDistanceTrainingID;
    public double maxDistance;
    public String maxDistanceFileName;
    public double maxSpeed;
    public String maxSpeedFileName;
    public double maxElevationGain;
    public String maxElevationGainFileName;
    public double maxCalories;
    public String maxCaloriesFileName;
    public int maxTrainingLoad;
    public String maxTrainingLoadFileName;
    public String profilePhoto;
    public String username;

    public ProfileData() {}

    public ProfileData(
        int userID,
        String name,
        String secondName,
        int age,
        String sex,
        double weight,
        double height,
        int maxHR,
        int restingHR,
        double gesamteDistanzProjahr,
        int maxDistanceTrainingID,
        double maxDistance,
        String maxDistanceFileName,
        double maxSpeed,
        String maxSpeedFileName,
        double maxElevationGain,
        String maxElevationGainFileName,
        double maxCalories,
        String maxCaloriesFileName,
        int maxTrainingLoad,
        String maxTrainingLoadFileName,
        String profilePhoto,
        String username
    ) {
        this.userID = userID;
        this.name = name;
        this.secondName = secondName;
        this.age = age;
        this.sex = sex;
        this.weight = weight;
        this.height = height;
        this.maxHR = maxHR;
        this.restingHR = restingHR;
        this.gesamteDistanzProjahr = gesamteDistanzProjahr;
        this.maxDistanceTrainingID = maxDistanceTrainingID;
        this.maxDistance = maxDistance;    
        this.maxDistanceFileName = maxDistanceFileName;    
        this.maxSpeed = maxSpeed;
        this.maxSpeedFileName = maxSpeedFileName; // Ergänzt
        this.maxElevationGain = maxElevationGain;
        this.maxElevationGainFileName = maxElevationGainFileName; // Ergänzt
        this.maxCalories = maxCalories;
        this.maxCaloriesFileName = maxCaloriesFileName; // Ergänzt
        this.maxTrainingLoad = maxTrainingLoad;
        this.maxTrainingLoadFileName = maxTrainingLoadFileName; // Ergänzt
        this.profilePhoto = profilePhoto;
        this.username = username;
    }

    public int getMaxDistanceTrainingID() {
        return maxDistanceTrainingID;
    }
}