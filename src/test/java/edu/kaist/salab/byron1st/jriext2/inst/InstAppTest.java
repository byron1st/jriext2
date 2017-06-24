package edu.kaist.salab.byron1st.jriext2.inst;

import edu.kaist.salab.byron1st.jriext2.ettype.*;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Created by util on 2017. 6. 21..
 */
public class InstAppTest {
    private ETType threadETType;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void instrument() throws Exception, NotInstClassesCopyFailedException {
        InstApp app = InstApp.getInstance();
        Path targetClassPath = Paths.get("/Users/util/Downloads");
        ArrayList<ETType> ettypeList = new ArrayList<>();
        ettypeList.add(getThreadETType());
        ettypeList.add(getSocketETType());

        app.instrument(targetClassPath, ettypeList);
    }

    // Test field type with method chains and parameter type.
    private ETType getThreadETType() {
        ETTAttributeField targetClass = new ETTAttributeField("target-class", "target", "Ljava/lang/Runnable;");
        targetClass.setNextMethod(new ETTAttributeMethod("get-class", true, "java/lang/Object", "getClass()Ljava/lang/Class;"));
        targetClass.setNextMethod(new ETTAttributeMethod("get-class-name", true, "java/lang/Class", "getName()Ljava/lang/String;"));

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

    // Test return type with method chains
    // Return type은 반드시 Return type 1개만 가져야 함.
    // Return type은 반드시 method 말미에 체크.
    private ETType getSocketETType() {
        ETTAttributeReturn returnValue = new ETTAttributeReturn("return-value", "Ljava/net/SocketAddress;");
        returnValue.setNextMethod(new ETTAttributeMethod("get-inet-socket-address", true, "java/net/InetSocketAddress", "getAddress()Ljava/net/InetAddress;"));
        returnValue.setNextMethod(new ETTAttributeMethod("get-inet-address-string", true, "java/net/InetAddress", "toString()Ljava/lang/String;"));

        ArrayList<ETTAttribute> attributeList = new ArrayList<>();
        attributeList.add(returnValue);

        return new ETType(
                "ett-socket",
                "java/net/Socket",
                "getLocalSocketAddress",
                "()Ljava/net/SocketAddress;",
                false,
                attributeList
        );
    }
}