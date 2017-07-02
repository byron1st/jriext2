package edu.kaist.salab.byron1st.jriext2app.cli;

import edu.kaist.salab.byron1st.jriext2.Endpoint;
import edu.kaist.salab.byron1st.jriext2.ettype.ETType;
import edu.kaist.salab.byron1st.jriext2.executer.LogFilesCreationFailedException;
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
public class CLIApp {
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
        HashMap<String, String> mapProcessKeyToUniqueName = new HashMap<>();
        ProcessObserver observer = new ProcessObserver(mapProcessKeyToUniqueName);
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
            try {
                Scanner in = new Scanner(System.in);
                JSONObject commandObject = new JSONObject(in.nextLine());
                String command = commandObject.getString("cmd");
                switch (command) {
                    case CMD_QUIT:
                        send(KEY_DONE_QUIT, null);
                        break label;
                    case CMD_INST:
                        instrument(commandObject.getJSONArray("args"));
                        send(KEY_DONE_INST, null);
                        break;
                    case CMD_EXEC:
                        JSONArray execargs = commandObject.getJSONArray("args");
                        String uniqueName = execargs.getString(1);
                        String processKey = execute(execargs);
                        mapProcessKeyToUniqueName.put(processKey, uniqueName);
                        send(KEY_DONE_EXEC, uniqueName, processKey);
                        break;
                    case CMD_STOP:
                        break;
                }
            } catch (JRiExtLoggerClassFileCopyFailedException | WrongArgumentsException | NotInstClassesCopyFailedException | InstrumentedClassWriteFailedException | ClassReaderNotConstructedException | JSONException | RequiredFilesNotExistException | LogFilesCreationFailedException | TargetSystemExecutionFailedException e) {
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
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new WrongArgumentsException("Arguments are wrong.", e);
        } catch (InvalidPathException e) {
            throw new WrongArgumentsException("Classpath is wrong.", e);
        }
    }

    private static String execute(JSONArray args) throws RequiredFilesNotExistException, LogFilesCreationFailedException, TargetSystemExecutionFailedException, WrongArgumentsException {
        String mainClassName = args.getString(0);
        Path outputFilePath = null;
        Path errorFilePath = null;
        try {
            if(!args.isNull(2)) {
                outputFilePath = Paths.get(args.getString(1));
            }

            if(!args.isNull(3)) {
                errorFilePath = Paths.get(args.getString(2));
            }
        } catch (InvalidPathException e) {
            throw new WrongArgumentsException("Output paths are wrong.", e);
        }

        return Endpoint.execute(mainClassName, outputFilePath, errorFilePath);
    }
}
