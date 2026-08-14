public class ChallengeData {
    public String challengeName;
    public String challengeDescription;
    public String challengeStartDate;
    public String challengeEndDate;
    public int status;
    public int ziel;
    public String imagePath;

    public ChallengeData(
                String challengeName, 
                String challengeDescription, 
                String challengeStartDate, 
                String challengeEndDate, 
                int status,
                int ziel,
                String imagePath
                ) {
        this.challengeName = challengeName;
        this.challengeDescription = challengeDescription;
        this.challengeStartDate = challengeStartDate;
        this.challengeEndDate = challengeEndDate;
        this.status = status;
        this.ziel = ziel;
        this.imagePath = imagePath;
    }

    public String getImagePath() {
        return imagePath;
    }
}