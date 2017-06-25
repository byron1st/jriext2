package edu.kaist.salab.byron1st.jriext2app;

import edu.kaist.salab.byron1st.jriext2.ettype.ETType;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by byron1st on 2017. 6. 25..
 */
public class ETTypeBuilderImplJsonTest {
    @Test
    public void buildETTypeList() throws Exception {
        Path path = Paths.get("/Users/byron1st/Developer/Workspace/IntelliJ/jriext2/src/test/resources/monitoringUnits_banking.json");
        ETTypeBuilderImplJson builder = new ETTypeBuilderImplJson();
        ArrayList<ETType> ettypeList = builder.buildETTypeList(path);

        System.out.println("Hello");
    }

}