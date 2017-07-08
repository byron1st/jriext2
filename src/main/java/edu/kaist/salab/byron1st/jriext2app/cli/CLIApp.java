package edu.kaist.salab.byron1st.jriext2app.cli;

import edu.kaist.salab.byron1st.jriext2.Endpoint;
import edu.kaist.salab.byron1st.jriext2.Symbols;
import edu.kaist.salab.byron1st.jriext2.ettype.ETType;
import edu.kaist.salab.byron1st.jriext2.executer.LogFilesCreationFailedException;
import edu.kaist.salab.byron1st.jriext2.executer.ProcessNotExistException;
import edu.kaist.salab.byron1st.jriext2.executer.RequiredFilesNotExistException;
import edu.kaist.salab.byron1st.jriext2.executer.TargetSystemExecutionFailedException;
import edu.kaist.salab.byron1st.jriext2.inst.ClassReaderNotConstructedException;
import edu.kaist.salab.byron1st.jriext2.inst.InstrumentedClassWriteFailedException;
import edu.kaist.salab.byron1st.jriext2.inst.JRiExtLoggerClassFileCopyFailedException;
import edu.kaist.salab.byron1st.jriext2.inst.NotInstClassesCopyFailedException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by byron1st on 2017. 6. 28..
 */
public class CLIApp implements Symbols {
    public static final String KEY_ERROR = "error";
    public static final String KEY_DONE_INST = "done.inst";
    public static final String KEY_DONE_EXEC = "done.exec";
    public static final String KEY_TERM_EXEC = "term.exec";
    public static final String KEY_DONE_QUIT = "done.quit";
    public static final String CMD_QUIT = "cmd.quit";
    public static final String CMD_INST = "cmd.inst";
    public static final String CMD_EXEC = "cmd.exec";
    public static final String CMD_STOP = "cmd.stop";

    public static void send(String key, String... args) {
        JSONObject messageObject = new JSONObject();
        messageObject.put("key", key);
        if(args != null) {
            messageObject.put("body", args);
        }

        System.out.println(messageObject.toString());
    }

    public static void main(String[] args) {
        // ProcessKey로 나중에 UniqueName을 찾아야 한다. UniqueName은 JRiExt2ManagerApp에서 사용하는 키값.
        HashMap<String, String> mapProcessKeyToUniqueName = new HashMap<>();

        // ProcessObserver를 통해 sub-process의 종료를 감지해서 JRiExt2ManagerApp으로 메세지를 보내줘야 함.
        // mapProcessKeyToUniqueName을 넣어주어서, JRiExt2ManagerApp으로 UniqueName 값도 같이 보내주어야 함.
        ProcessObserver observer = new ProcessObserver(mapProcessKeyToUniqueName);
        // Endpoint를 통해 접근하는 ExecuterApp은 Singleton 패턴으로 생성되기 때문에,
        // 미리 Observer를 지정해주어도 괜찮다.
        Endpoint.setProcessStatusObserver(observer);
        /**
         * command:
         * {
         *  cmd: "inst",
         *  args: ["","",""]
         * }
         */
        label:
        while (true) {
            // 데몬 프로세스임.
            try {
                // 표준 input stream(System.in)을 사용함.
                Scanner in = new Scanner(System.in);

                // input은 무조건 JSON 형태로 오는 것을 가정함.
                // JSON으로 안 오면, JSONException 발생하고, 아래에서 캐치되서 ERROR 메세지 보냄.
                JSONObject commandObject = new JSONObject(in.nextLine());
                String command = commandObject.getString("cmd");
                switch (command) {
                    case CMD_QUIT:
                        send(KEY_DONE_QUIT, null);
                        break label;
                    case CMD_INST:
                        instrument(commandObject.getJSONArray("args"));
                        send(KEY_DONE_INST, CACHE_ROOT.toString());
                        break;
                    case CMD_EXEC:
                        JSONArray execargs = commandObject.getJSONArray("args");

                        // UniqueName은 미리 식별해둠.
                        String uniqueName = execargs.getString(1);

                        // 실행하면, ProcessKey를 반환받음.
                        String processKey = execute(execargs);

                        // 나중을 위해 mapProcessKeyToUniqueName에 추가해둠.
                        mapProcessKeyToUniqueName.put(processKey, uniqueName);
                        send(KEY_DONE_EXEC, uniqueName, processKey);
                        break;
                    case CMD_STOP:
                        stop(commandObject.getJSONArray("args"));
                        break;
                }
            } catch (JRiExtLoggerClassFileCopyFailedException | WrongArgumentsException | NotInstClassesCopyFailedException | InstrumentedClassWriteFailedException | ClassReaderNotConstructedException | JSONException | RequiredFilesNotExistException | LogFilesCreationFailedException | TargetSystemExecutionFailedException | ProcessNotExistException e) {
                send(KEY_ERROR, e.getMessage());
            }
        }
    }

    private static void instrument(JSONArray args) throws JRiExtLoggerClassFileCopyFailedException, ClassReaderNotConstructedException, InstrumentedClassWriteFailedException, NotInstClassesCopyFailedException, WrongArgumentsException {
        Path targetClassPath;
        JSONArray ettypeInfoList;
        try {
            targetClassPath = Paths.get(args.getString(0));
            ettypeInfoList = args.getJSONArray(1);

            ArrayList<ETType> ettypeList = (new ETTypeBuilderImplJson()).buildETTypeList(ettypeInfoList);
            Endpoint.instrument(targetClassPath, ettypeList);
        } catch (ArrayIndexOutOfBoundsException | JSONException e) {
            throw new WrongArgumentsException("Arguments are wrong.", e);
        } catch (InvalidPathException e) {
            throw new WrongArgumentsException("Classpath is wrong.", e);
        }
    }

    private static String execute(JSONArray args) throws RequiredFilesNotExistException, LogFilesCreationFailedException, TargetSystemExecutionFailedException, WrongArgumentsException {
        String mainClassName = args.getString(0);
        Path outputPath = null;
        try {
            if(!args.isNull(2)) {
                outputPath = Paths.get(args.getString(2));
            }
        } catch (InvalidPathException e) {
            throw new WrongArgumentsException("Output paths are wrong.", e);
        }

        return Endpoint.execute(mainClassName, outputPath);
    }

    private static void stop(JSONArray args) throws ProcessNotExistException {
        String processKey = args.getString(0);
        Endpoint.stop(processKey);
    }
}
