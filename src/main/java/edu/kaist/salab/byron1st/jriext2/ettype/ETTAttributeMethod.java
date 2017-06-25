package edu.kaist.salab.byron1st.jriext2.ettype;

/**
 * Created by util on 2017. 6. 21..
 */
public class ETTAttributeMethod extends ETTAttribute {
    private String methodName;
    private String methodDesc;
    private String returnType;
    private boolean isVirtual;

    public ETTAttributeMethod(String attributeName, String className, String methodName, String methodDesc, String returnType, boolean isVirtual) {
        super(attributeName, className);

        this.isVirtual = isVirtual;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
        this.returnType = returnType;
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
