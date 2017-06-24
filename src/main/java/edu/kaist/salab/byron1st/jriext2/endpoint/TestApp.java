package edu.kaist.salab.byron1st.jriext2.endpoint;

import edu.kaist.salab.byron1st.jriext2.ettype.*;
import edu.kaist.salab.byron1st.jriext2.inst.ClassReaderNotConstructedException;
import edu.kaist.salab.byron1st.jriext2.inst.JRiExtLoggerClassFileCopyFailedException;
import edu.kaist.salab.byron1st.jriext2.inst.NotInstClassesCopyFailedException;
import edu.kaist.salab.byron1st.jriext2.inst.InstrumentedClassWriteFailedException;
import edu.kaist.salab.byron1st.jriext2.loggingtoolset.LogFilesCreationFailedException;
import edu.kaist.salab.byron1st.jriext2.loggingtoolset.RequiredFilesNotExistException;
import edu.kaist.salab.byron1st.jriext2.loggingtoolset.TargetSystemExecutionFailedException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Created by util on 2017. 6. 22..
 */
public class TestApp {
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
    public static ETType getThreadETType() {
        ETTAttributeMethod getClass = new ETTAttributeMethod("get-class", true, "java/lang/Object", "getClass()Ljava/lang/Class;");
        getClass.setNextMethod(new ETTAttributeMethod("get-class-name", true, "java/lang/Class", "getName()Ljava/lang/String;"));
        ETTAttributeField targetClass = new ETTAttributeField("target-class", "target", "Ljava/lang/Runnable;");
        targetClass.setNextMethod(getClass);

        ETTAttributeField targetObjectId = new ETTAttributeField("target-objectId", "target", "Ljava/lang/Runnable;");
        targetObjectId.setNextMethod(new ETTAttributeMethod("get-object-id", true, "java/lang/Object", "hashCode()I"));

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
                attributeList);
    }

    public static ETType getPipedStreamETType() {
        return new ETType(
                "ett-read",
                "java/io/PipedInputStream",
                "read",
                "()I",
                true,
                new ArrayList<>()
        );
    }
}
