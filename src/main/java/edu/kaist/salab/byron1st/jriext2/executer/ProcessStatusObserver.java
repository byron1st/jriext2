package edu.kaist.salab.byron1st.jriext2.executer;

import edu.kaist.salab.byron1st.jriext2.Symbols;

/**
 * Created by byron1st on 2017. 6. 24..
 */
public interface ProcessStatusObserver {
    void observe(String processKey, Symbols.ProcessStatus processStatus);
}
