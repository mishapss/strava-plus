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
    public int maxSpeedTrainingID;
    public double maxSpeed;
    public String maxSpeedFileName;
    public int maxElevationGainTrainingID;
    public double maxElevationGain;
    public String maxElevationGainFileName;
    public int maxCalorieBurnTrainingID;
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
        int maxSpeedTrainingID,
        double maxSpeed,
        String maxSpeedFileName,
        int maxElevationGainTrainingID,
        double maxElevationGain,
        String maxElevationGainFileName,
        int maxCalorieBurnTrainingID,
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
        this.maxSpeedTrainingID = maxSpeedTrainingID;
        this.maxSpeed = maxSpeed;
        this.maxSpeedFileName = maxSpeedFileName;  
        this.maxElevationGainTrainingID = maxElevationGainTrainingID;
        this.maxElevationGain = maxElevationGain;
        this.maxElevationGainFileName = maxElevationGainFileName;  
        this.maxCalorieBurnTrainingID = maxCalorieBurnTrainingID;
        this.maxCalories = maxCalories;
        this.maxCaloriesFileName = maxCaloriesFileName;  
        this.maxTrainingLoad = maxTrainingLoad;
        this.maxTrainingLoadFileName = maxTrainingLoadFileName;  
        this.profilePhoto = profilePhoto;
        this.username = username;
    }

    public int getMaxDistanceTrainingID() {
        return maxDistanceTrainingID;
    }
}