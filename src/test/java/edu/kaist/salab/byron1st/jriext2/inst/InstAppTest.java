package edu.kaist.salab.byron1st.jriext2.inst;

import edu.kaist.salab.byron1st.jriext2.ettype.ETTAttribute;
import edu.kaist.salab.byron1st.jriext2.ettype.ETTAttributeField;
import edu.kaist.salab.byron1st.jriext2.ettype.ETTAttributeMethod;
import edu.kaist.salab.byron1st.jriext2.ettype.ETType;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Array;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by byron1st on 2017. 6. 21..
 */
public class InstAppTest {
    private ETType threadETType;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void instrument() throws Exception, CopyingNotInstClassesFailedException {
        InstApp app = InstApp.getInstance();
        Path targetClassPath = Paths.get("/Users/byron1st/Downloads");
        ArrayList<ETType> ettypeList = new ArrayList<>();
        ettypeList.add(getThreadETType());

        app.instrument(targetClassPath, ettypeList);
    }

    public ETType getThreadETType() {
        ETTAttributeField target1 = new ETTAttributeField("target", "target", "Ljava/lang/Runnable;");
        target1.setNextMethod(new ETTAttributeMethod("get-class", true, "java/lang/Object", "getClass()Ljava/lang/Class;"));
        target1.setNextMethod(new ETTAttributeMethod("get-class-name", true, "java/lang/Class", "getName()Ljava/lang/String;"));

        ETTAttributeField target2 = new ETTAttributeField("target", "target", "Ljava/lang/Runnable;");
        target2.setNextMethod(new ETTAttributeMethod("get-object-id", true, "java/lang/Object", "hashCode()I"));

        ArrayList<ETTAttribute> attributeList = new ArrayList<>();
        attributeList.add(target1);
        attributeList.add(target2);

        return new ETType(
                "ett-thread",
                "java/lang/Thread",
                "init",
                "(Ljava/lang/ThreadGroup;Ljava/lang/Runnable;Ljava/lang/String;JLjava/security/AccessControlContext;)V",
                false,
                attributeList);
    }
}