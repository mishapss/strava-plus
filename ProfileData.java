public class ProfileData {
    public int userID;
    public String name;
    public String secondName;
    public int age;
    public String sex;
    public double weight;
    public double height;
    public int maxHr;
    public int ruheHr;
    public double gesamteDistanzProjahr;
    public double maxDistanse;
    public double maxSpeed;
    public double maxElevationGain;

    public ProfileData(
        int userID,
        String name,
        String secondName,
        int age,
        String sex,
        double weight,
        double height,
        int maxHr,
        int ruheHr,
        double gesamteDistanzProjahr,
        double maxDistanse,
        double maxSpeed,
        double maxElevationGain
    ) {
        this.userID = userID;
        this.name = name;
        this.secondName = secondName;
        this.age = age;
        this.sex = sex;
        this.weight = weight;
        this.height = height;
        this.maxHr = maxHr;
        this.ruheHr = ruheHr;
        this.gesamteDistanzProjahr = gesamteDistanzProjahr;
        this.maxDistanse = maxDistanse;
        this.maxSpeed = maxSpeed;
        this.maxElevationGain = maxElevationGain;
    }
}