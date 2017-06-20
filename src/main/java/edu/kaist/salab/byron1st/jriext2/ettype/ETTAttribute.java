package edu.kaist.salab.byron1st.jriext2.ettype;

/**
 * Created by byron1st on 2017. 6. 20..
 */
public class ETTAttribute {
    protected String attributeName;
    protected String className;
    protected ETTAttributeMethod nextMethod;

    public ETTAttribute(String attributeName, String className) {
        this.attributeName = attributeName;
        this.className = className;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public String getClassName() {
        return className;
    }

    public ETTAttributeMethod getNextMethod() {
        return nextMethod;
    }

    public void setNextMethod(ETTAttributeMethod nextMethod) {
        this.nextMethod = nextMethod;
    }
}
