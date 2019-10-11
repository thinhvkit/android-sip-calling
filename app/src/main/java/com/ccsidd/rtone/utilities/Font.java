package com.ccsidd.rtone.utilities;

import android.app.Activity;
import android.graphics.Typeface;

/**
 * Created by dung on 6/16/15.
 */
public class Font {

    private Activity mActivity;

    public Font(Activity activity)
    {
        this.mActivity = activity;
    }

    public Typeface getFont(String fontName)
    {
        Typeface tf = Typeface.createFromAsset(this.mActivity.getAssets(), fontName);
        return tf;
    }
}
