package com.ccsidd.rtone.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.ccsidd.rtone.R;

/**
 * Created by dung on 1/25/16.
 */
public class LoginView extends LinearLayout {
    public LoginView(Context context) {
        super(context, null);
    }

    public LoginView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.login_component, this, true);
    }
}
