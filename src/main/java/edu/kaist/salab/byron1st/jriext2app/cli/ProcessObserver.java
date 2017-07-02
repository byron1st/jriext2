package edu.kaist.salab.byron1st.jriext2app.cli;

import edu.kaist.salab.byron1st.jriext2.Symbols;
import edu.kaist.salab.byron1st.jriext2.executer.ProcessStatusObserver;

import java.util.HashMap;

/**
 * Created by byron1st on 2017. 6. 28..
 */
public class ProcessObserver implements ProcessStatusObserver, Symbols {
    public HashMap<String, String> mapProcessKeyToUniqueName = new HashMap<>();

    public ProcessObserver (HashMap<String, String> mapProcessKeyToUniqueName) {
        this.mapProcessKeyToUniqueName = mapProcessKeyToUniqueName;
    }

    @Override
    public void observe(String processKey, ProcessStatus processStatus) {
        if(processStatus == ProcessStatus.TERMINATED) {
            String uniqueName = mapProcessKeyToUniqueName.get(processKey);
            CLIApp.send(CLIApp.KEY_TERM_EXEC, uniqueName, processKey);
        }
    }
}
