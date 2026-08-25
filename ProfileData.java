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
    public double maxDistance;
    public double maxSpeed;
    public double maxElevationGain;
    public String profilePhoto;
    public double maxCalorieBurn;
    public int maxTrainingLoad;

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
        double maxDistance,
        double maxSpeed,
        double maxElevationGain,
        String profilePhoto,
        double maxCalorieBurn,
        int maxTrainingLoad
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
        this.maxDistance = maxDistance;
        this.maxSpeed = maxSpeed;
        this.maxElevationGain = maxElevationGain;
        this.profilePhoto = profilePhoto;
        this.maxCalorieBurn = maxCalorieBurn;
        this.maxTrainingLoad = maxTrainingLoad;
    }
}