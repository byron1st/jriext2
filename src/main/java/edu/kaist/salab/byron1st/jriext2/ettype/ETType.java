package edu.kaist.salab.byron1st.jriext2.ettype;

import java.util.HashMap;

/**
 * Created by byron1st on 2017. 6. 20..
 */
public class ETType {
    private String className;
    private String methodSignature;
    private HashMap<String, ETTAttribute> attributes = new HashMap<>();

    public ETType(String className, String methodSignature, HashMap<String, ETTAttribute> attributes) {
        this.className = className;
        this.methodSignature = methodSignature;
        this.attributes = attributes;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodSignature() {
        return methodSignature;
    }

    public HashMap<String, ETTAttribute> getAttributes() {
        return attributes;
    }
}
