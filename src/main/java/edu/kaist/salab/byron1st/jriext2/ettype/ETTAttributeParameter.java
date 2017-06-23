package edu.kaist.salab.byron1st.jriext2.ettype;

/**
 * Created by util on 2017. 6. 21..
 */
public class ETTAttributeParameter extends ETTAttribute {
    private int index;

    public ETTAttributeParameter(String attributeName, String className, int index) {
        super(attributeName, className);

        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
