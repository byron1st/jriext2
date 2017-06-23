package java.util;

/**
 * Created by util on 2017. 6. 22..
 */
public class JRiExtLogger {
    static JRiExtLogger logger = new JRiExtLogger();

    public static void recordExecutionTrace(String executionTrace) {
        if(logger == null) {
            launchLogger();
        }

        logger.send(executionTrace);
    }

    private static void launchLogger() {
        logger = new JRiExtLogger();
        logger.setUpConnection();
    }

    private ArrayList<String> buffer = new ArrayList<>();

    private void setUpConnection() {

    }

    private void send(String executionTrace) {
        if(System.out != null) {
            System.out.println(executionTrace);
        }
    }
}
