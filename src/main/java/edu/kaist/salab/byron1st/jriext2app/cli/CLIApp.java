package edu.kaist.salab.byron1st.jriext2app.cli;

import edu.kaist.salab.byron1st.jriext2.ettype.ETType;
import edu.kaist.salab.byron1st.jriext2.executer.ExecuterApp;
import edu.kaist.salab.byron1st.jriext2.inst.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by byron1st on 2017. 6. 28..
 */
public class CLIApp {
    public static final String KEY_ERROR = "error";
    public static final String KEY_DONE = "done";

    public static void main(String[] args) {
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
                    case "quit":
                        send(KEY_DONE, "Quitting JRiExt2.");
                        break label;
                    case "inst":
                        instrument(commandObject.getJSONArray("args"));
                        send(KEY_DONE, "Instrumentation has been completed.");
                        break;
                    case "execute":
                        execute(commandObject.getJSONArray("args"));
                        send(KEY_DONE, "The target program has been executed.");
                        break;
                    case "stop":
                        break;
                }
            } catch (JRiExtLoggerClassFileCopyFailedException | WrongArgumentsException | NotInstClassesCopyFailedException | InstrumentedClassWriteFailedException | ClassReaderNotConstructedException | JSONException e) {
                send(KEY_ERROR, e.getMessage());
            }
        }
    }

    private static void instrument(JSONArray args) throws JRiExtLoggerClassFileCopyFailedException, ClassReaderNotConstructedException, InstrumentedClassWriteFailedException, NotInstClassesCopyFailedException, WrongArgumentsException {
        Path targetClassPath;
        Path ettypeDefFilePath;
        try {
            targetClassPath = Paths.get(args.getString(0));
            ettypeDefFilePath = Paths.get(args.getString(1));

            ArrayList<ETType> ettypeList = (new ETTypeBuilderImplJson()).buildETTypeList(ettypeDefFilePath);
            InstApp.getInstance().instrument(targetClassPath, ettypeList);
        } catch (ArrayIndexOutOfBoundsException | InvalidPathException e) {
            throw new WrongArgumentsException("Arguments are wrong.", e);
        }
    }

    private static void execute(JSONArray args) {
//        ExecuterApp.getInstance().execute()
    }

    private static void send(String key, String message) {
        JSONObject messageObject = new JSONObject();
        messageObject.put("key", key);
        messageObject.put("body", message);
        System.out.println(messageObject.toString());
    }
}
