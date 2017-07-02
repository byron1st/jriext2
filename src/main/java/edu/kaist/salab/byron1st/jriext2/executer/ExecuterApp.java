package edu.kaist.salab.byron1st.jriext2.executer;

import edu.kaist.salab.byron1st.jriext2.Symbols;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private ProcessStatusObserver processStatusObserver;

    /**
     * 이미 Instrumented 된 클래스들을 대상으로 시스템을 실행한다.
     * @param mainClassName 실행할 main class name으로, 예를 들어 'package/to/mainclass/Main' 이런 식으로 표현한다.
     * @param outputPath process.redirectOutput 함수를 호출할 대상 파일 경로 객체. null일 경우, cache 디렉토리로 설정된다.
     * @return 실행 중인 process의 key 값.
     * @throws RequiredFilesNotExistException cache 디렉토리가 없거나, mainClassName에 해당하는 클래스 파일이 없을 때 발생
     * @throws TargetSystemExecutionFailedException 대상 시스템 실행에 실패하였을 경우 발생
     * @throws LogFilesCreationFailedException 로깅을 위한 파일들을 생성하는데 실패하였을 경우 발생
     */
    public String execute(String mainClassName, Path outputPath) throws RequiredFilesNotExistException, TargetSystemExecutionFailedException, LogFilesCreationFailedException {
        // 이미 생성되어 있어야 하는 디렉토리(캐시)와 파일(메인 함수 클래스 파일)을 체크
        if(!Files.exists(CACHE_ROOT)) {
            throw new RequiredFilesNotExistException("Cache directory does not exist.");
        }

        if(!Files.exists(CACHE_ROOT.resolve(mainClassName + ".class"))) {
            throw new RequiredFilesNotExistException("Main Class does not exist.");
        }

        if (outputPath != null && Files.isDirectory(outputPath)) {
            throw new RequiredFilesNotExistException("Output path is not a directory.");
        }

        // 고유한 processKey를 생성
        String processKey = getProcessKey(mainClassName, System.currentTimeMillis());

        // 실행을 위한 ProcessBuilder 객체를 우선 생성.
        ProcessBuilder processBuilder = getProcessBuilder(processKey, mainClassName, outputPath);

        try {
            // 대상 시스템을 실행
            Process process = runProcess(processKey, processBuilder);

            // 생성된 프로세스를 저장.
            this.processMap.put(processKey, process);

            // Process Key 값을 반환.
            return processKey;
        } catch (IOException e) {
            throw new TargetSystemExecutionFailedException("Executing the target system has been failed.", e);
        }
    }

    private String getProcessKey(String mainClassName, long millis) {
        String name = mainClassName.substring(mainClassName.lastIndexOf("/") + 1);
        return name + millis;
    }

    public void setProcessStatusObserver(ProcessStatusObserver observer) {
        this.processStatusObserver = observer;
    }

    private ExecuterApp() {
    }

    /**
     * 실행을 위한 ProcessBuilder 객체를 생성하고 관련된 설정을 완료한 후 반환한다.
     *
     * @param processKey 실행할 프로세스의 고유한 Key 값.
     * @param mainClassName 실행할 main 함수가 있는 클래스 이름.
     * @param outputPath 실행할 프로세스의 출력값이 기록될 폴더 경로. default 값은 CACHE_ROOT.
     * @return 생성된 ProcessBuilder 객체.
     * @throws LogFilesCreationFailedException 실행할 프로세스의 출력값, 또는 에러값이 기록될 파일들 생성에 실패할 경우 발생.
     */
    private ProcessBuilder getProcessBuilder(String processKey, String mainClassName, Path outputPath) throws LogFilesCreationFailedException {
        // Java 프로세스 실행 커맨드를 이용하여 ProcessBuilder 클래스 생성.
        // Xbootclasspath/p:는 cache 루트의 java, javax 등과 같은 기본 클래스 파일들을 rt.jar보다 먼저 호출하도록 함.
        ProcessBuilder processBuilder = new ProcessBuilder(
                "java",
                "-Xbootclasspath/p:" + CACHE_ROOT.toString(),
                mainClassName.replaceAll("/", ".")
        );

        try {
            // 기록을 위한 파일들 생성.
            String outputFileString = processKey + ".txt";
            String errorFileString = processKey + "_error.txt";

            File outputLogFile = getLogFile(outputPath, outputFileString);
            File errorLogFile = getLogFile(outputPath, errorFileString);

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
        // 서브 프로세스를 시작함.
        Process process = processBuilder.start();

        // Observer를 이용해서 서브 프로세스의 시작을 알림.
        observe(processKey, ProcessStatus.START);

        // 실행된 프로세스가 종료되는 것을 감지하여, 종료됬을 때 processMap으로부터 해당 프로세스를 삭제.
        Runnable deathDetector = () -> {
            try {
                // 해당 process 객체가 나타내는 프로세스가 종료될 때까지 이 thread는 hold 상태.
                process.waitFor();

                // 해당 process 객체가 종료되면, processMap에서 제거.
                this.processMap.remove(processKey);

                // Observer를 이용해서 서브 프로세스의 종료를 알림.
                observe(processKey, ProcessStatus.TERMINATED);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        // 서브 프로세스 종료 감지 스레드 시작.
        (new Thread(deathDetector)).start();

        return process;
    }

    /**
     * Process key 값에 해당하는 서브 프로세스를 종료시킴.
     * @param processKey 종료 시키려는 프로세스 키
     * @throws ProcessNotExistException 프로세스 키 값에 해당하는 프로세스가 존재하지 않을 때 발생.
     */
    public void stopProcess(String processKey) throws ProcessNotExistException {
        // processKey가 없으면 예외 발생.
        if(!this.processMap.containsKey(processKey)) {
            throw new ProcessNotExistException(processKey + " process does not exist.");
        }

        // 저장된 Process 객체를 가져옴
        Process process = this.processMap.get(processKey);

        // TODO: 종료 여부 feedback 주기.
        process.destroy();

        // 종료 후 맵에서 제거.
        this.processMap.remove(processKey);
    }

    /**
     * Logging을 위한 파일 객체를 생성하고, 실제 파일을 생성한 후, 해당 파일 객체를 반환한다.
     * @param rootPath 로그 파일이 기록될 root directory. default 값은 CACHE_ROOT.
     * @param filePath 로그 파일.
     * @return 생성된 파일 객체
     * @throws IOException Files.createFile 함수가 실패할 경우 발생.
     */
    private File getLogFile(Path rootPath, String filePath) throws IOException {
        if(rootPath == null) {
            return Files.createFile(CACHE_ROOT.resolve(filePath)).toFile();
        } else {
            return Files.createFile(rootPath.resolve(filePath)).toFile();
        }
    }

    private void observe(String processKey, ProcessStatus status) {
        if(this.processStatusObserver != null) {
            this.processStatusObserver.observe(processKey, status);
        }
    }
}
