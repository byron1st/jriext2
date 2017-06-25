package edu.kaist.salab.byron1st.jriext2.ettype;

import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Created by byron1st on 2017. 6. 25..
 */
public interface ETTypeBuilder {
    ArrayList<ETType> buildETTypeList(Path storedFile);
}
