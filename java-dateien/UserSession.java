public class UserSession {
    private static int currentUserID = -1; //nicht angemeldet

    public static void setCurrentUserID(int userID) {
        currentUserID = userID;
    }
    public static int getCurrentUserID() {
        return currentUserID;
    }

    public static boolean isLoggedIn(int userID) {
        return currentUserID != -1;
    }

    public static void logOut(int userID) {
        currentUserID = -1;
    }
}