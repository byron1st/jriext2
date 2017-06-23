package edu.kaist.salab.byron1st.jriext2.ettype;

/**
 * Created by util on 2017. 6. 21..
 */
public class ETTAttributeField extends ETTAttribute {
    private String fieldName;

    public ETTAttributeField(String attributeName, String fieldName, String typeClassName) {
        super(attributeName, typeClassName);

        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
