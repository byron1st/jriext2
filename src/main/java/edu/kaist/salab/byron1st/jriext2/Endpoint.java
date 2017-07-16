package edu.kaist.salab.byron1st.jriext2;

import edu.kaist.salab.byron1st.jriext2.ettype.ETType;
import edu.kaist.salab.byron1st.jriext2.executer.*;
import edu.kaist.salab.byron1st.jriext2.inst.*;

import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Created by util on 2017. 6. 22..
 */

public class Endpoint implements Symbols {
    public static void instrument(Path targetClassPath, ArrayList<ETType> ettypeList) throws ClassReaderNotConstructedException, NotInstClassesCopyFailedException, InstrumentedClassWriteFailedException, JRiExtLoggerClassFileCopyFailedException {
        InstApp.getInstance().instrument(targetClassPath, ettypeList);
    }

    public static String execute(String mainClassName, Path outputFilePath) throws TargetSystemExecutionFailedException, RequiredFilesNotExistException, LogFilesCreationFailedException {
        return ExecuterApp.getInstance().execute(mainClassName, outputFilePath);
    }

    public static void stop(String processKey) throws ProcessNotExistException {
        ExecuterApp.getInstance().stopProcess(processKey);
    }

    public static void setProcessStatusObserver(ProcessStatusObserver observer) {
        ExecuterApp.getInstance().setProcessStatusObserver(observer);
    }
}
