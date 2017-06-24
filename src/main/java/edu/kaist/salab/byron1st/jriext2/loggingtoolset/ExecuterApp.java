package edu.kaist.salab.byron1st.jriext2.loggingtoolset;

import edu.kaist.salab.byron1st.jriext2.Symbols;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Created by util on 2017. 6. 22..
 */
public class ExecuterApp implements Symbols {
    private static ExecuterApp executerApp = new ExecuterApp();

    public static ExecuterApp getInstance() {
        return executerApp;
    }

    private ArrayList<Process> processList = new ArrayList<>();
    private Path DEFAULT_ERROR_FILE = CACHE_ROOT.resolve("error.txt");
    private Path DEFAULT_OUTPUT_FILE = CACHE_ROOT.resolve("output.txt");

    public int execute(String mainClassName, String outputFilePathString, String errorFilePathString) throws RequiredFilesNotExistException, TargetSystemExecutionFailedException {
        if(!Files.exists(CACHE_ROOT)) {
            throw new RequiredFilesNotExistException("Cache directory does not exist.");
        }

        if(!Files.exists(CACHE_ROOT.resolve(mainClassName + ".class"))) {
            throw new RequiredFilesNotExistException("Main Class does not exist.");
        }

        ProcessBuilder processBuilder = new ProcessBuilder("java",
                "-Xbootclasspath/p:" + CACHE_ROOT.toString(),
                mainClassName.replaceAll("/", "."));
        processBuilder.directory(CACHE_ROOT.toFile());
        try {
            File outputLogFile = getLogFile(outputFilePathString, DEFAULT_OUTPUT_FILE);
            File errorLogFile = getLogFile(errorFilePathString, DEFAULT_ERROR_FILE);

            processBuilder.redirectOutput(outputLogFile);
            processBuilder.redirectError(errorLogFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Process process = processBuilder.start();
//            InputStream inputStream = process.getInputStream();
//            LogListener logListener = new LogListener(inputStream, ettypeList);
//            logListener.start();
            processList.add(process);

            return processList.size() - 1;
        } catch (IOException e) {
            throw new TargetSystemExecutionFailedException("Executing the target system has been failed.", e);
        }
    }

    private ExecuterApp() {
    }

    private File getLogFile(String filePathString, Path defaultPath) throws IOException {
        File logFile;

        if (filePathString == null) {
            logFile = Files.createFile(defaultPath).toFile();
        } else {
            logFile = Files.createFile(Paths.get(filePathString)).toFile();
        }

        return logFile;
    }
}
