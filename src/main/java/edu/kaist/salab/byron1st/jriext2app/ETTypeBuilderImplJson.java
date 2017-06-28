package edu.kaist.salab.byron1st.jriext2app;

import edu.kaist.salab.byron1st.jriext2.ettype.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Created by byron1st on 2017. 6. 25..
 */
public class ETTypeBuilderImplJson implements ETTypeBuilder {
    public static final String KIND_FIELD = "field";
    public static final String KIND_METHOD = "method";
    public static final String KIND_PARAMETER = "parameter";
    public static final String KIND_RETURN = "return";

    @Override
    public ArrayList<ETType> buildETTypeList(Path storedFile) {
        ArrayList<ETType> ettypeList = new ArrayList<>();
        try {
            String jsonString = new String(Files.readAllBytes(storedFile), StandardCharsets.UTF_8);
            JSONArray ettypeInfoList = new JSONArray(jsonString);
            for (Object ettypeInfoObject : ettypeInfoList) {
                JSONObject ettypeInfo = (JSONObject) ettypeInfoObject;

                String typeName = ettypeInfo.getString("typeName");
                String className = ettypeInfo.getString("className");
                String methodName = ettypeInfo.getString("methodName");
                String methodDesc = ettypeInfo.getString("methodDesc");
                boolean isVirtual = ettypeInfo.getBoolean("isVirtual");
                boolean isEnter = ettypeInfo.getBoolean("isEnter");

                ArrayList<ETTAttribute> attributeList = new ArrayList<>();
                JSONArray attributeListInfo = ettypeInfo.getJSONArray("attributeList");
                for (Object attributeInfoObject : attributeListInfo) {
                    ETTAttribute attribute = null;
                    JSONObject attributeInfo = (JSONObject) attributeInfoObject;

                    String attributeName = attributeInfo.getString("attributeName");
                    String attrClassName = attributeInfo.getString("className");

                    String kind = attributeInfo.getString("kind");
                    switch (kind) {
                        case KIND_FIELD:
                            String fieldName = attributeInfo.getString("fieldName");
                            attribute = new ETTAttributeField(attributeName, fieldName, attrClassName);
                            break;
                        case KIND_PARAMETER:
                            int index = attributeInfo.getInt("index");
                            attribute = new ETTAttributeParameter(attributeName, attrClassName, index);
                            break;
                        case KIND_RETURN:
                            attribute = new ETTAttributeReturn(attributeName, attrClassName);
                            break;
                        case KIND_METHOD:
                            String attrMethodName = attributeInfo.getString("methodName");
                            String attrMethodDesc = attributeInfo.getString("methodDesc");
                            String attrMethodReturnType = attributeInfo.getString("returnType");
                            boolean attrIsVirtual = attributeInfo.getBoolean("isVirtual");
                            attribute = new ETTAttributeMethod(attributeName, attrClassName, attrMethodName, attrMethodDesc, attrMethodReturnType, attrIsVirtual);
                            break;
                    }

                    buildMethodChain(attributeInfo.getJSONArray("methodList"), attribute);

                    attributeList.add(attribute);
                }

                ETType ettype = new ETType(typeName, className, methodName, methodDesc, isEnter, isVirtual, attributeList);
                ettypeList.add(ettype);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ettypeList;
    }

    private void buildMethodChain(JSONArray methodList, ETTAttribute attribute) {
        if(methodList.length() != 0) {
            JSONObject methodInfo = methodList.getJSONObject(0);
            String attributeName = methodInfo.getString("attributeName");
            String className = methodInfo.getString("className");
            String methodName = methodInfo.getString("methodName");
            String methodDesc = methodInfo.getString("methodDesc");
            String returnType = methodInfo.getString("returnType");
            boolean isVirtual = methodInfo.getBoolean("isVirtual");

            ETTAttributeMethod attributeMethod = new ETTAttributeMethod(attributeName, className, methodName, methodDesc, returnType, isVirtual);
            attribute.setNextMethod(attributeMethod);

            methodList.remove(0);
            buildMethodChain(methodList, attributeMethod);
        }
    }
}
