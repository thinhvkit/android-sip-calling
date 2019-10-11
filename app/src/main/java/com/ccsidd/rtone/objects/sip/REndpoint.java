package com.ccsidd.rtone.objects.sip;

import android.util.Log;

import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.OnSelectAccountParam;

/**
 * Created by dung on 12/8/15.
 */
public class REndpoint extends Endpoint {
    @Override
    public void onSelectAccount(OnSelectAccountParam prm) {
        super.onSelectAccount(prm);
        Log.e("account Index", prm.getAccountIndex() + "");
    }
}
