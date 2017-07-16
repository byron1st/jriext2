package edu.kaist.salab.byron1st.jriext2app.cli;

import edu.kaist.salab.byron1st.jriext2.Symbols;
import edu.kaist.salab.byron1st.jriext2.executer.ProcessStatusObserver;

import java.util.HashMap;

/**
 * Created by byron1st on 2017. 6. 28..
 */
public class ProcessObserver implements ProcessStatusObserver, Symbols {
    public HashMap<String, String> mapProcessKeyToUniqueName;

    public ProcessObserver (HashMap<String, String> mapProcessKeyToUniqueName) {
        this.mapProcessKeyToUniqueName = mapProcessKeyToUniqueName;
    }

    @Override
    public void observe(String processKey, ProcessStatus processStatus) {
        // 종료만 감지.
        // 시작은 UniqueName을 식별하기 전에 이 observe 함수가 먼저 호출됨.
        // CLIApp에서 식별한 UniqueName을 넘겨주는 방법을 하기 위해선 별 지랄염병을 해야 함.
        // => 쓸데없이 여기에 시간 쓰지 말것.
        if(processStatus == ProcessStatus.TERMINATED) {
            String uniqueName = mapProcessKeyToUniqueName.get(processKey);
            String outputFileName = getOutputFileName(processKey);
            CLIApp.send(CLIApp.KEY_TERM_EXEC, uniqueName, outputFileName);
        }
    }
}
