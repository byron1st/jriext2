package edu.kaist.salab.byron1st.jriext2.loggingtoolset;

import java.io.File;

/**
 * Created by byron1st on 2017. 6. 24..
 */
public interface ParserApp<T> {
    T parse(File outputFile);
}
