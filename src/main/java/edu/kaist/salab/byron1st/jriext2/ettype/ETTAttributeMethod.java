package edu.kaist.salab.byron1st.jriext2.ettype;

/**
 * Created by byron1st on 2017. 6. 21..
 */
public class ETTAttributeMethod extends ETTAttribute {
    private String methodName;
    private String methodDesc;
    private String returnType;
    private boolean isVirtual;

    public ETTAttributeMethod(boolean isVirtual, String attributeName, String className, String methodNameDesc) {
        super(attributeName, className);

        this.isVirtual = isVirtual;
        int index0 = methodNameDesc.indexOf("(");
        int index1 = methodNameDesc.indexOf(")");
        this.methodName = methodNameDesc.substring(0, index0);
        this.methodDesc = methodNameDesc.substring(index0);
        this.returnType = methodNameDesc.substring(index1 + 1);
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public String getReturnType() {
        return returnType;
    }

    public boolean isVirtual() {
        return isVirtual;
    }

    public boolean isFinal() {
        return this.nextMethod == null;
    }
}
