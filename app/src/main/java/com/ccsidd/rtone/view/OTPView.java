package com.ccsidd.rtone.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.ccsidd.rtone.R;

/**
 * Created by dung on 1/25/16.
 */
public class OTPView extends LinearLayout {
    private Context context;

    public OTPView(Context context) {
        super(context, null);
    }

    public OTPView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.otp_view, this, true);
    }
}
