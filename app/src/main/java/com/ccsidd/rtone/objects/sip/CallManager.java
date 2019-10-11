package com.ccsidd.rtone.objects.sip;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dung on 2/27/16.
 */
public class CallManager {
    private Map<String, CallInformation> callInformations = new HashMap<>();

    public void addCallInfo(String callID, CallInformation callInformation){
        callInformations.put(callID, callInformation);
    }

    public Map<String, CallInformation> getCallInformations() {
        return callInformations;
    }

    public int getCountCall (){
        if(callInformations == null)
            return 0;
        else
            return callInformations.size();
    }
}
