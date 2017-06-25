package edu.kaist.salab.byron1st.jriext2app;

import edu.kaist.salab.byron1st.jriext2.Endpoint;
import edu.kaist.salab.byron1st.jriext2.ettype.*;
import edu.kaist.salab.byron1st.jriext2.inst.ClassReaderNotConstructedException;
import edu.kaist.salab.byron1st.jriext2.inst.JRiExtLoggerClassFileCopyFailedException;
import edu.kaist.salab.byron1st.jriext2.inst.NotInstClassesCopyFailedException;
import edu.kaist.salab.byron1st.jriext2.inst.InstrumentedClassWriteFailedException;
import edu.kaist.salab.byron1st.jriext2.executer.LogFilesCreationFailedException;
import edu.kaist.salab.byron1st.jriext2.executer.RequiredFilesNotExistException;
import edu.kaist.salab.byron1st.jriext2.executer.TargetSystemExecutionFailedException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Created by util on 2017. 6. 22..
 */
public class BDPSApp {
    public static void main(String[] args) throws ClassReaderNotConstructedException {
        Path targetClassPath = Paths.get("/Users/byron1st/Developer/Workspace/IntelliJ/jriext2/src/test/resources/bin");

        ArrayList<ETType> ettypeList = new ArrayList<>();
        ettypeList.add(getThreadETType());
        ettypeList.add(getPipedStreamETType());

        String mainClassName = "framework/PFSystemMain";

        try {
            Endpoint.instrument(targetClassPath, ettypeList);
        } catch (InstrumentedClassWriteFailedException | NotInstClassesCopyFailedException | JRiExtLoggerClassFileCopyFailedException e) {
            e.printStackTrace();
        }

        try {
            String processKey = Endpoint.execute(mainClassName, null, null);
        } catch (RequiredFilesNotExistException | TargetSystemExecutionFailedException | LogFilesCreationFailedException e) {
            e.printStackTrace();
        }
    }

    // Test field type with method chains and parameter type.
    private static ETType getThreadETType() {
        ETTAttributeField targetClass = new ETTAttributeField("target-class", "target", "Ljava/lang/Runnable;");
        targetClass.setNextMethod(new ETTAttributeMethod("get-class", "java/lang/Object", "getClass", "()Ljava/lang/Class;", "Ljava/lang/Class;", true));
        targetClass.setNextMethod(new ETTAttributeMethod("get-class-name", "java/lang/Class", "getName", "()Ljava/lang/String;", "Ljava/lang/String;", true));

        ETTAttributeField targetObjectId = new ETTAttributeField("target-objectId", "target", "Ljava/lang/Runnable;");
        targetObjectId.setNextMethod(new ETTAttributeMethod("get-object-id", "java/lang/Object", "hashCode", "()I", "I", true));

        ETTAttributeParameter threadName = new ETTAttributeParameter("thread-name", "Ljava/lang/String;", 2);

        ArrayList<ETTAttribute> attributeList = new ArrayList<>();
        attributeList.add(targetClass);
        attributeList.add(targetObjectId);
        attributeList.add(threadName);

        return new ETType(
                "ett-thread",
                "java/lang/Thread",
                "init",
                "(Ljava/lang/ThreadGroup;Ljava/lang/Runnable;Ljava/lang/String;JLjava/security/AccessControlContext;)V",
                false,
                true,
                attributeList);
    }

    public static ETType getPipedStreamETType() {
        return new ETType(
                "ett-read",
                "java/io/PipedInputStream",
                "read",
                "()I",
                true,
                true,
                new ArrayList<>()
        );
    }
}
