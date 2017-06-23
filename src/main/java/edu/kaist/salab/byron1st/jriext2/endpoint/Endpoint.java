package edu.kaist.salab.byron1st.jriext2.endpoint;

import edu.kaist.salab.byron1st.jriext2.ettype.ETType;
import edu.kaist.salab.byron1st.jriext2.inst.*;

import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Created by util on 2017. 6. 22..
 */

public class Endpoint implements Symbols {
    public static void instrument(Path targetClassPath, ArrayList<ETType> ettypeList) throws ClassReaderNotConstructedException, CopyingNotInstClassesFailedException, WritingInstrumentedClassFailedException, CopyJRiExtLoggerClassFileFailedException {
        InstApp.getInstance().instrument(targetClassPath, ettypeList);
    }

    public static void execute() {

    }
}
