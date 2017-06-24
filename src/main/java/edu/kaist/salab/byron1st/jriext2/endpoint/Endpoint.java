package edu.kaist.salab.byron1st.jriext2.endpoint;

import edu.kaist.salab.byron1st.jriext2.Symbols;
import edu.kaist.salab.byron1st.jriext2.ettype.ETType;
import edu.kaist.salab.byron1st.jriext2.inst.*;
import edu.kaist.salab.byron1st.jriext2.loggingtoolset.*;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by util on 2017. 6. 22..
 */

public class Endpoint implements Symbols {


    public static void instrument(Path targetClassPath, ArrayList<ETType> ettypeList) throws ClassReaderNotConstructedException, CopyingNotInstClassesFailedException, WritingInstrumentedClassFailedException, CopyJRiExtLoggerClassFileFailedException {
        InstApp.getInstance().instrument(targetClassPath, ettypeList);
    }

    public static String execute(String mainClassName, Path outputFilePath, Path errorFilePath) throws TargetSystemExecutionFailedException, RequiredFilesNotExistException, LogFilesCreationFailedException {
        return ExecuterApp.getInstance().execute(mainClassName, outputFilePath, errorFilePath);
    }

    public static HashMap<ParseType, Object> parse(File outputFile, ArrayList<ParseType> parseTypeList) {
        HashMap<ParseType, Object> parseResults = new HashMap<>();
        parseTypeList.forEach(parseType -> parseResults.put(parseType, runParserImpl(outputFile, parseType)));

        return parseResults;
    }

    private static Object runParserImpl(File outputFile, ParseType parseType) {
        switch (parseType) {
            case JSON: return (new ParserAppImplJSON()).parse(outputFile);
            default: return null;
        }
    }
}
