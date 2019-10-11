package com.ccsidd.rtone.activities;

import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.message.view.QKTextView;
import com.ccsidd.rtone.utilities.GlobalVars;
import com.ccsidd.rtone.utilities.ThemeUtils;
import com.ccsidd.rtone.utilities.Utility;

public class AboutActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private QKTextView mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String themes = Utility.getPref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_THEMES);
        if (themes.isEmpty())
            themes = "0";
        ThemeUtils.setsTheme(Integer.parseInt(themes));
        ThemeUtils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

//        getSupportActionBar().setCustomView(R.layout.actionbar);
//        getSupportActionBar().setDisplayShowCustomEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(false);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
//        getSupportActionBar().setDisplayUseLogoEnabled(false);
//        getSupportActionBar().setHomeButtonEnabled(false);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
////
//        LinearLayout btnBack = (LinearLayout) getSupportActionBar().getCustomView().findViewById(R.id.actinon_btnHome);
//        btnBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startApplication();
//            }
//        });

//        LinearLayout nagivationBack = (LinearLayout) findViewById(R.id.layout_back);
//        nagivationBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//            }
//        });
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar == null) {
            throw new RuntimeException("Toolbar not found in BaseActivity layout.");
        } else {
            mTitle = (QKTextView) mToolbar.findViewById(R.id.toolbar_title);
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (mTitle != null) {
            mTitle.setText("About");
        }

        showSomeField();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSomeField()
    {
        String version = "";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        }catch (Exception exception)
        {

        }

        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.splashScreen_ln);
        ((TextView)((LinearLayout) linearLayout.getChildAt(1)).getChildAt(2)).setText("Version " + version);
    }

}
