package edu.kaist.salab.byron1st.jriext2.loggingtoolset;

import edu.kaist.salab.byron1st.jriext2.ettype.ETType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by byron1st on 2017. 6. 24..
 */
public class LogListener extends Thread {
    private BufferedReader reader;
    private HashMap<String, ETType> ettypeMap;

    public LogListener(InputStream processInputStream, ArrayList<ETType> ettypeList) {
        reader = new BufferedReader(new InputStreamReader(processInputStream));
        ettypeMap = getHashMapFromETTypeList(ettypeList);
    }

    @Override
    public void run() {
        String line;
        try {
            while((line = reader.readLine()) != null) {
//                +E+,ett-read,204825040451026,1806629833
                String[] lineElements = line.split(",");
//                메소드 도입부 기록 여부, Execution trace type 이름, 실행 시간, 객체 hash code 값
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, ETType> getHashMapFromETTypeList(ArrayList<ETType> ettypeList) {
        HashMap<String, ETType> hashMapFromETTypeList = new HashMap<>();
        ettypeList.forEach(ettype -> hashMapFromETTypeList.put(ettype.getTypeName(), ettype));

        return hashMapFromETTypeList;
    }
}
