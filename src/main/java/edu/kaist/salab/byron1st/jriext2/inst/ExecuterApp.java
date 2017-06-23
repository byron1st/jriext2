package edu.kaist.salab.byron1st.jriext2.inst;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by util on 2017. 6. 22..
 */
public class ExecuterApp implements Symbols {
    private static ExecuterApp executerApp = new ExecuterApp();

    public static ExecuterApp getInstance() {
        return executerApp;
    }

    private ArrayList<Process> processList = new ArrayList<>();

    public int execute(String mainClassName) throws RequiredFilesNotExistException, TargetSystemExecutionFailedException {
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
            processBuilder.redirectError(Files.createFile(CACHE_ROOT.resolve("error.txt")).toFile());
            processBuilder.redirectOutput(Files.createFile(CACHE_ROOT.resolve("output.txt")).toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Process process = processBuilder.start();
            InputStream inputStream = process.getInputStream();
            System.out.println(inputStream.getClass().getName());
            processList.add(process);

            return processList.size() - 1;
        } catch (IOException e) {
            throw new TargetSystemExecutionFailedException("Executing the target system has been failed.", e);
        }
    }

    private ExecuterApp() {
    }
}
