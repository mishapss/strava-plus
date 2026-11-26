public class DbConfig {
    public static String getUrl() {
        String envUrl = System.getenv("DB_URL");
        if (envUrl != null && !envUrl.isBlank()) {
            return envUrl.trim();
        }
        return "jdbc:sqlite:db/test.db";
    }
}