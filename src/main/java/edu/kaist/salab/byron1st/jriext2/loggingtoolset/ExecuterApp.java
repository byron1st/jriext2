package edu.kaist.salab.byron1st.jriext2.loggingtoolset;

import edu.kaist.salab.byron1st.jriext2.Symbols;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * Created by util on 2017. 6. 22..
 */
public class ExecuterApp implements Symbols {
    private static ExecuterApp executerApp = new ExecuterApp();

    public static ExecuterApp getInstance() {
        return executerApp;
    }

    private HashMap<String, Process> processMap = new HashMap<>();
    private int processCount = 0;

    /**
     * 이미 Instrumented 된 클래스들을 대상으로 시스템을 실행한다.
     * @param mainClassName 실행할 main class name으로, 예를 들어 'package/to/mainclass/Main' 이런 식으로 표현한다.
     * @param outputFilePath process.redirectOutput 함수를 호출할 대상 파일 경로 객체. null일 경우, cache 디렉토리 내의 output.txt 파일로 설정된다.
     * @param errorFilePath process.redirectError 함수를 호출할 대상 파일 경로 객체. null일 경우, cache 디렉토리 내의 error.txt 파일로 설정된다.
     * @return 실행 중인 process의 key 값.
     * @throws RequiredFilesNotExistException cache 디렉토리가 없거나, mainClassName에 해당하는 클래스 파일이 없을 때 발생
     * @throws TargetSystemExecutionFailedException 대상 시스템 실행에 실패하였을 경우 발생
     * @throws LogFilesCreationFailedException 로깅을 위한 파일들을 생성하는데 실패하였을 경우 발생
     */
    public String execute(String mainClassName, Path outputFilePath, Path errorFilePath) throws RequiredFilesNotExistException, TargetSystemExecutionFailedException, LogFilesCreationFailedException {
        // 이미 생성되어 있어야 하는 디렉토리(캐시)와 파일(메인 함수 클래스 파일)을 체크
        if(!Files.exists(CACHE_ROOT)) {
            throw new RequiredFilesNotExistException("Cache directory does not exist.");
        }

        if(!Files.exists(CACHE_ROOT.resolve(mainClassName + ".class"))) {
            throw new RequiredFilesNotExistException("Main Class does not exist.");
        }

        // 실행을 위한 ProcessBuilder 객체를 우선 생성.
        ProcessBuilder processBuilder = getProcessBuilder(mainClassName, outputFilePath, errorFilePath);

        try {
            // 대상 시스템을 실행
            String processKey = mainClassName + processCount;
            Process process = runProcess(processKey, processBuilder);

            // 대상 시스템 실행과 관련된 전역 변수들 값 변경
            processCount++;
            processMap.put(processKey, process);

            // Process Key 값을 반환.
            return processKey;
        } catch (IOException e) {
            throw new TargetSystemExecutionFailedException("Executing the target system has been failed.", e);
        }
    }

    private ExecuterApp() {
    }

    /**
     * 실행을 위한 ProcessBuilder 객체를 생성하고 관련된 설정을 완료한 후 반환한다.
     * @param mainClassName 실행할 main 함수가 있는 클래스 이름.
     * @param outputFilePath 실행할 프로세스의 출력값이 기록될 파일 경로.
     * @param errorFilePath 실행할 프로세스의 에러값이 기록될 파일 경로.
     * @return 생성된 ProcessBuilder 객체.
     * @throws LogFilesCreationFailedException 실행할 프로세스의 출력값, 또는 에러값이 기록될 파일들 생성에 실패할 경우 발생.
     */
    private ProcessBuilder getProcessBuilder(String mainClassName, Path outputFilePath, Path errorFilePath) throws LogFilesCreationFailedException {
        // Java 프로세스 실행 커맨드를 이용하여 ProcessBuilder 클래스 생성.
        // Xbootclasspath/p:는 cache 루트의 java, javax 등과 같은 기본 클래스 파일들을 rt.jar보다 먼저 호출하도록 함.
        ProcessBuilder processBuilder = new ProcessBuilder(
                "java",
                "-Xbootclasspath/p:" + CACHE_ROOT.toString(),
                mainClassName.replaceAll("/", ".")
        );

        try {
            // 기록을 위한 파일들 생성.
            File outputLogFile = getLogFile(outputFilePath, DEFAULT_OUTPUT_FILE);
            File errorLogFile = getLogFile(errorFilePath, DEFAULT_ERROR_FILE);

            // ProcessBuilder 설정 진행. 출력값 스트림과 에러값 스트림을 각각 파일객체로 redirect
            processBuilder.redirectOutput(outputLogFile);
            processBuilder.redirectError(errorLogFile);

            // 서브 프로세스가 실행되는 기준 폴더를 지정.
            processBuilder.directory(CACHE_ROOT.toFile());
        } catch (IOException e) {
            throw new LogFilesCreationFailedException("Creating log files has been failed.", e);
        }

        return processBuilder;
    }

    /**
     * ProcessBuilder 객체를 실행한다.
     * @param processKey 실행할 프로세스의 Key 값
     * @param processBuilder 실행할 프로세스의 ProcessBuilder 객체
     * @return 실행 중인 Process 객체
     * @throws IOException ProcessBuilder.start 실행 실패 시 발생.
     */
    private Process runProcess(String processKey, ProcessBuilder processBuilder) throws IOException {
        Process process = processBuilder.start();

        // 실행된 프로세스가 종료되는 것을 감지하여, 종료됬을 때 processMap으로부터 해당 프로세스를 삭제.
        Runnable deathDetector = () -> {
            try {
                // 해당 process 객체가 나타내는 프로세스가 종료될 때까지 이 thread는 hold 상태.
                process.waitFor();

                // 해당 process 객체가 종료되면, processMap에서 제거.
                processMap.remove(processKey);
                System.out.println("Terminated: " + processKey);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        // 서브 프로세스 종료 감지 스레드 시작.
        (new Thread(deathDetector)).start();

        return process;
    }

    /**
     * Logging을 위한 파일 객체를 생성하고, 실제 파일을 생성한 후, 해당 파일 객체를 반환한다.
     * @param filePath 사용자가 지정한 커스텀 경로. null일 경우 defaultPath 값을 사용.
     * @param defaultPath 사용자가 지정한 경로가 null일 경우 사용.
     * @return 생성된 파일 객체
     * @throws IOException Files.createFile 함수가 실패할 경우 발생.
     */
    private File getLogFile(Path filePath, Path defaultPath) throws IOException {
        File logFile;

        if (filePath == null) {
            logFile = Files.createFile(defaultPath).toFile();
        } else {
            logFile = Files.createFile(filePath).toFile();
        }

        return logFile;
    }
}