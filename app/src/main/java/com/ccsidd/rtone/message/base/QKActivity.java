package com.ccsidd.rtone.message.base;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.fragments.BlockContactFragment;
import com.ccsidd.rtone.services.Logger;
import com.ccsidd.rtone.message.ThemeManager;
import com.ccsidd.rtone.message.view.QKTextView;
import com.ccsidd.rtone.utilities.GlobalVars;
import com.ccsidd.rtone.utilities.ThemeUtils;
import com.ccsidd.rtone.utilities.Utility;

import java.util.ArrayList;

import com.ccsidd.rtone.searchview.SearchViewLayout;

public abstract class QKActivity extends AppCompatActivity {
    private final String TAG = "QKActivity";

    private Toolbar mToolbar;
    private QKTextView mTitle;
    private ImageView mOverflowButton;
    private Menu mMenu;
    private ProgressDialog mProgressDialog;
    private Bitmap mRecentsIcon;

    protected Resources mRes;
    protected SharedPreferences mPrefs;

    private static boolean mStatusTintEnabled = true;
    private static boolean mNavigationTintEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String themes = Utility.getPref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_THEMES);
        if (themes.isEmpty())
            themes = "0";
        ThemeUtils.setsTheme(Integer.parseInt(themes));
        ThemeUtils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        mRes = getResources();
        getPrefs(); // set the preferences if they haven't been set. this method takes care of that logic for us

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);

        /*LiveViewManager.registerView(QKPreference.TINTED_STATUS, this, key -> {
            mStatusTintEnabled = QKPreferences.getBoolean(QKPreference.TINTED_STATUS) &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
        });

        LiveViewManager.registerView(QKPreference.TINTED_NAV, this, key -> {
            mNavigationTintEnabled = QKPreferences.getBoolean(QKPreference.TINTED_NAV) &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
        });

        if (Build.VERSION.SDK_INT >= 21) {
            mRecentsIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            LiveViewManager.registerView(QKPreference.THEME, this, key -> {
                ActivityManager.TaskDescription taskDesc = new ActivityManager.TaskDescription(getString(R.string.app_name), mRecentsIcon, ThemeManager.getColor());
                setTaskDescription(taskDesc);
            });
        }*/
    }

    /**
     * Reloads the toolbar and it's view references.
     * <p>
     * This is called every time the content view of the activity is set, since the
     * toolbar is now a part of the activity layout.
     * <p>
     * TODO: If someone ever wants to manage the Toolbar dynamically instead of keeping it in their
     * TODO  layout file, we can add an alternate way of setting the toolbar programmatically.
     */
    private void reloadToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        if (mToolbar == null) {
            throw new RuntimeException("Toolbar not found in BaseActivity layout.");
        } else {
            mTitle = (QKTextView) mToolbar.findViewById(R.id.toolbar_title);
            setSupportActionBar(mToolbar);
        }

        /*LiveViewManager.registerView(QKPreference.THEME, this, key -> {
            mToolbar.setBackgroundColor(ThemeManager.getColor());

            if (mStatusTintEnabled) {
                getWindow().setStatusBarColor(ColorUtils.darken(ThemeManager.getColor()));
            }
            if (mNavigationTintEnabled) {
                getWindow().setNavigationBarColor(ColorUtils.darken(ThemeManager.getColor()));
            }
        });

        LiveViewManager.registerView(QKPreference.BACKGROUND, this, key -> {
            setTheme(getThemeRes());
            switch (ThemeManager.getTheme()) {
                case LIGHT:
                    mToolbar.setPopupTheme(R.style.PopupThemeLight);
                    break;

                case DARK:
                case BLACK:
                    mToolbar.setPopupTheme(R.style.PopupTheme);
                    break;
            }
            ((QKTextView) findViewById(R.id.toolbar_title)).setTextColor(ThemeManager.getTextOnColorPrimary());
        });*/
    }

    protected void showBackButton(boolean show) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(show);
    }

    public void showProgressDialog() {
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        mProgressDialog.hide();
    }

    public SharedPreferences getPrefs() {
        if (mPrefs == null) {
            mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        }
        return mPrefs;
    }

    public void colorMenuIcons(Menu menu, int color) {

        // Toolbar navigation icon
        Drawable navigationIcon = mToolbar.getNavigationIcon();
        if (navigationIcon != null) {
            navigationIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            mToolbar.setNavigationIcon(navigationIcon);
        }

        // Overflow icon
        colorOverflowButtonWhenReady(color);

        // Other icons
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
//            SpannableString spanString = new SpannableString(menu.getItem(i).getTitle().toString());
//            spanString.setSpan(new ForegroundColorSpan(color), 0, spanString.length(), 0); //fix the color to white
//            menuItem.setTitle(spanString);

            Drawable newIcon = menuItem.getIcon();
            if (newIcon != null) {
                newIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                menuItem.setIcon(newIcon);
            }
        }
    }

    private void colorOverflowButtonWhenReady(final int color) {
        if (mOverflowButton != null) {
            // We already have the overflow button, so just color it.
            Drawable icon = mOverflowButton.getDrawable();
            icon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            // Have to clear the image drawable first or else it won't take effect
            mOverflowButton.setImageDrawable(null);
            mOverflowButton.setImageDrawable(icon);

        } else {
            // Otherwise, find the overflow button by searching for the content description.
            final String overflowDesc = getString(R.string.abc_action_menu_overflow_description);
            final ViewGroup decor = (ViewGroup) getWindow().getDecorView();
            decor.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    decor.getViewTreeObserver().removeOnPreDrawListener(this);

                    final ArrayList<View> views = new ArrayList<>();
                    decor.findViewsWithText(views, overflowDesc,
                            View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);

                    if (views.isEmpty()) {
                        Logger.debug(TAG, "no views");
                    } else {
                        if (views.get(0) instanceof ImageView) {
                            mOverflowButton = (ImageView) views.get(0);
                            colorOverflowButtonWhenReady(color);
                        } else {
                            Log.w(TAG, "overflow button isn't an imageview");
                        }
                    }
                    return false;
                }
            });
        }
    }

    public Menu getMenu() {
        return mMenu;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Save a reference to the menu so that we can quickly access menu icons later.
        mMenu = menu;
        //colorMenuIcons(mMenu, ThemeManager.getTextOnColorPrimary());
        return true;
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        reloadToolbar();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        reloadToolbar();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        reloadToolbar();
    }

    /**
     * Sets the title of the activity, displayed on the toolbar
     * <p>
     * Make sure this is only called AFTER setContentView, or else the Toolbar
     * is likely not initialized yet and this method will do nothing
     *
     * @param title title of activity
     */
    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);

        if (mTitle != null) {
            mTitle.setText(title);
        }
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

    public void startActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }

    public boolean isScreenOn() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            return powerManager.isInteractive();
        } else {
            return powerManager.isScreenOn();
        }
    }

    public void makeToast(@StringRes int message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
