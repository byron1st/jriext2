package edu.kaist.salab.byron1st.jriext2.loggingtoolset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by byron1st on 2017. 6. 22..
 */
public class JRiExtLogger {
    static JRiExtLogger logger;

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
        System.out.println(executionTrace);
    }
}
