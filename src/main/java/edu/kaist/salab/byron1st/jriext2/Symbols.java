package edu.kaist.salab.byron1st.jriext2;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by util on 2017. 6. 20..
 */
public interface Symbols {
    String ENTER = "+E+";
    String EXIT = "+X+";
    String DDELIM = ",";
    String STATIC = "<static:";

    Path CACHE_ROOT = Paths.get(System.getProperty("user.dir"),"jriext_userdata", "cache");
    // 임시값.
    Path JRIEXTLOGGER_PATH = Paths.get("/Users/byron1st/Developer/Workspace/Java/jriext2/build/classes/main/java/util/JRiExtLogger.class");

    enum ProcessStatus {
        START, TERMINATED
    }

    default String getOutputFileName(String processKey) {
        return processKey + ".txt";
    }

    default String getErrorFileName(String processKey) {
        return processKey + "_error.txt";
    }
}
