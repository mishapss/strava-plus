public class ChallengeData {
    public int challengeID;
    public String challengeName;
    public String challengeDescription;
    public String challengeStartDate;
    public String challengeEndDate;
    public int status;
    public int ziel;
    public String imagePath;
    public String imagePathReward;

    public ChallengeData(
                int challengeID,
                String challengeName, 
                String challengeDescription, 
                String challengeStartDate, 
                String challengeEndDate, 
                int status,
                int ziel,
                String imagePath,
                String imagePathReward
                ) {
                    
        this.challengeID = challengeID;                  
        this.challengeName = challengeName;
        this.challengeDescription = challengeDescription;
        this.challengeStartDate = challengeStartDate;
        this.challengeEndDate = challengeEndDate;
        this.status = status;
        this.ziel = ziel;
        this.imagePath = imagePath;
        this.imagePathReward = imagePathReward;
    }

    public String getImagePath() {
        return imagePath;
    }
}