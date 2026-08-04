public class StartAll {
    public static void main(String[] args) {
        try {
            ProcessBuilder pb1 = new ProcessBuilder("java", "-cp", ".;lib/*", "SimplePostServer");
            pb1.inheritIO();
            Process server = pb1.start();

            Thread.sleep(2000);

            ProcessBuilder pb2 = new ProcessBuilder("java", "-cp", ".;lib/*", "FileUploader");
            pb2.inheritIO();
            Process uploader = pb2.start();

            uploader.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}