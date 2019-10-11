package com.ccsidd.rtone.utilities;

/**
 * Created by thinhvo on 5/18/16.
 */

import android.app.Activity;
import android.content.Intent;

import com.ccsidd.rtone.R;

public class ThemeUtils {
    private static int sTheme;

    public static void changeToTheme(Activity activity, int theme) {
        sTheme = theme;
        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
        activity.overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
    }

    public static void onActivityCreateSetTheme(Activity activity) {
        switch (sTheme) {
            default:
            case 0:
                activity.setTheme(R.style.AppTheme_Light);
                break;
            case 1:
                activity.setTheme(R.style.AppTheme_Dark);
                break;
            case 2:
                activity.setTheme(R.style.AppTheme_Green);
                break;
            case 3:
                activity.setTheme(R.style.AppTheme_Pink);
                break;
            case 4:
                activity.setTheme(R.style.AppTheme_5);
                break;
            case 5:
                activity.setTheme(R.style.AppTheme_6);
                break;
        }
    }

    public static void setsTheme(int sTheme) {
        ThemeUtils.sTheme = sTheme;
    }
}
