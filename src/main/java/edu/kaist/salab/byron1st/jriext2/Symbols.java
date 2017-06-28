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
    Path JRIEXTLOGGER_PATH = Paths.get("/Users/byron1st/Developer/Workspace/IntelliJ/jriext2/build/classes/main/java/util/JRiExtLogger.class");
//    Path DEFAULT_OUTPUT_FILE = CACHE_ROOT.resolve("output.txt");
//    Path DEFAULT_ERROR_FILE = CACHE_ROOT.resolve("error.txt");

    enum ParseType {
        JSON
    }

    enum ProcessStatus {
        START, TERMINATED
    }
}
