package edu.kaist.salab.byron1st.jriext2.ettype;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by util on 2017. 6. 20..
 */
public class ETType {
    private String typeName;
    private String className;
    private String methodName;
    private String methodDesc;
    private boolean isEnter;
    private ArrayList<ETTAttribute> attributeList = new ArrayList<>();

    public ETType(String typeName, String className, String methodName, String methodDesc, boolean isEnter, ArrayList<ETTAttribute> attributeList) {
        this.typeName = typeName;
        this.className = className;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
        this.isEnter = isEnter;
        this.attributeList = attributeList;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodSignature() {
        return methodName + '/' + methodDesc;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public ArrayList<ETTAttribute> getAttributeList() {
        return attributeList;
    }

    public boolean isEnter() {
        return isEnter;
    }
}
