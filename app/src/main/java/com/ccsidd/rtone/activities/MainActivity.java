package com.ccsidd.rtone.activities;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ccsidd.rtone.Foreground;
import com.ccsidd.rtone.R;
import com.ccsidd.rtone.adapters.CacheFragmentStatePagerAdapter;
import com.ccsidd.rtone.dialogs.AlertDialog;
import com.ccsidd.rtone.fragments.CallFragment;
import com.ccsidd.rtone.fragments.ContactFragment;
import com.ccsidd.rtone.fragments.ConversationFragment;
import com.ccsidd.rtone.fragments.RecentFragment;
import com.ccsidd.rtone.listeners.AlertDialogListener;
import com.ccsidd.rtone.message.ThemeManager;
import com.ccsidd.rtone.message.view.QKTextView;
import com.ccsidd.rtone.objects.ContactList;
import com.ccsidd.rtone.objects.Conversation;
import com.ccsidd.rtone.objects.User;
import com.ccsidd.rtone.observers.ObservableListView;
import com.ccsidd.rtone.observers.ObservableRecyclerView;
import com.ccsidd.rtone.observers.ObservableScrollViewCallbacks;
import com.ccsidd.rtone.observers.ScrollState;
import com.ccsidd.rtone.observers.ScrollUtils;
import com.ccsidd.rtone.services.Logger;
import com.ccsidd.rtone.services.SipServiceCommand;
import com.ccsidd.rtone.util.IabHelper;
import com.ccsidd.rtone.util.IabResult;
import com.ccsidd.rtone.util.Inventory;
import com.ccsidd.rtone.util.Purchase;
import com.ccsidd.rtone.util.SkuDetails;
import com.ccsidd.rtone.utilities.GlobalVars;
import com.ccsidd.rtone.utilities.ThemeUtils;
import com.ccsidd.rtone.utilities.Utility;
import com.ccsidd.rtone.view.BadgeView;
import com.ccsidd.rtone.view.PagerSlidingTabStrip;
import com.innovattic.font.TypefaceManager;
import com.konifar.fab_transformation.FabTransformation;
import com.melnykov.fab.FloatingActionButton;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.nineoldandroids.view.ViewHelper;

import java.util.List;
import java.util.Timer;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import me.leolin.shortcutbadger.ShortcutBadger;

public class MainActivity extends RuntimePermissionsActivity implements Foreground.Listener, ObservableScrollViewCallbacks {

    public static Activity mainContext;
    final int REQUEST_CODE_ASK_CONTACT_PERMISSIONS = 123;
    final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    String TAG = MainActivity.class.getName();
    int[] iconIntArray = {-1, -1, R.drawable.ic_action_add_person, R.drawable.ic_add};

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private Menu mainMenu;
    private Timer timerStartService = new Timer();
    private Realm realm;
    private RealmResults<Conversation> realmResults;
    private MaterialSearchView searchView;
    private BroadcastReceiver callActivityStateReceiver;
    private BroadcastReceiver badgeReceiver;
    private BadgeView badgeRecent;
    private BadgeView badgeMessage;
    private Foreground.Binding listenerBinding;
    private View mHeaderView;
    private View mToolbarView;
    private QKTextView mTitle;
    private int mBaseTranslationY;

    private RealmChangeListener listener;
    private FloatingActionButton mFab;
    private View overlay;
    private CardView sheet;
    private ContactList contactList;

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);

        if (mTitle != null) {
            mTitle.setText(title);
        }
    }

    @Override
    public void onPermissionsGranted(final int requestCode) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_CONTACT_PERMISSIONS:
                FabTransformation.with(mFab).setOverlay(overlay).transformTo(sheet);
                break;
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TypefaceManager.initialize(this, R.xml.fonts);
        String themes = Utility.getPref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_THEMES);
        if (themes.isEmpty())
            themes = "0";
        ThemeUtils.setsTheme(Integer.parseInt(themes));
        ThemeUtils.onActivityCreateSetTheme(this);

        int[] attrs = new int[]{R.attr.textColorNumberPad};
        TypedArray ta = obtainStyledAttributes(attrs);
        int color = ta.getColor(0, 0xFF101010);
        ta.recycle();

        ThemeManager.setActiveColor(color);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Utility.setupTones();

        Utility.configureRealm(getApplicationContext(), Utility.getPref(getApplicationContext(),
                GlobalVars.PREFERENCES_DATA_FILE_NAME,
                GlobalVars.PREFERENCES_DATA_ACCOUNT_DEFAULT));

        MainActivity.super.requestAppPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.WRITE_CONTACTS,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA
                }, R.string.runtime_permissions_txt
                , REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);

        listenerBinding = Foreground.get(getApplication()).addListener(this);

        mainContext = this;
        contactList = new ContactList(this);

        if (callActivityStateReceiver == null) {
            callActivityStateReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    //mViewPager.setCurrentItem(0);
                }
            };
            registerReceiver(callActivityStateReceiver, new IntentFilter(GlobalVars.BROADCAST_ACTION_SIP_CALL_END));
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mToolbarView = findViewById(R.id.toolbar);
        mHeaderView = findViewById(R.id.header);

        mTitle = (QKTextView) mToolbarView.findViewById(R.id.toolbar_title);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.hide(false);
        overlay = findViewById(R.id.overlay);
        sheet = (CardView) findViewById(R.id.sheet);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mViewPager.getCurrentItem()) {
                    case 2:
                        if (mFab.getVisibility() == View.VISIBLE) {

                            MainActivity.super.requestAppPermissions(new String[]{
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                            Manifest.permission.READ_CONTACTS
                                    }, R.string.runtime_permissions_txt
                                    , REQUEST_CODE_ASK_CONTACT_PERMISSIONS);
                        }
                        break;
                    case 3:
                        ((ConversationFragment) getCurrentFragment()).onFabClick(v);
                        break;
                    default:
                        break;
                }

            }
        });
        overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFab.getVisibility() != View.VISIBLE) {
                    FabTransformation.with(mFab).setOverlay(overlay).transformFrom(sheet);
                }
            }
        });

        ListView listView = (ListView) findViewById(R.id.list_contact_option);
        String[] values = new String[]{"Add new", "Sync contact"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.contact_option_row, R.id.txt_contact_option_row, values);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (mFab.getVisibility() != View.VISIBLE) {
                    FabTransformation.with(mFab).setOverlay(overlay).transformFrom(sheet);
                }

                switch (position) {
                    case 0:
                        startActivity(new Intent(MainActivity.this, ContactActivity.class));
                        break;
                    case 1:
                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... params) {
                                contactList.fetchAll();
                                return null;
                            }

                        }.execute();
                        break;
                }
            }
        });

        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setVoiceSearch(true);
        searchView.setHintTextColor(R.color.hintColorPhoneNumber);
        searchView.setCursorDrawable(R.drawable.cursor_drawable);

        ViewCompat.setElevation(mHeaderView, getResources().getDimension(R.dimen.toolbar_elevation));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setIcon(R.drawable.ic_launcher_small);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                showToolbar();
                Utility.savePref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_LAST_TAB, String.valueOf(mViewPager.getCurrentItem()));
                switch (position) {
                    case 0:
                        mFab.setVisibility(View.GONE);
                        break;
                    case 1:
                        if (badgeRecent != null && badgeRecent.isShown())
                            badgeRecent.setText("0");
                        badgeRecent.hide();
                        mFab.hide(false);
                        mFab.setVisibility(View.GONE);
                        break;
                    case 2:
                        animateFab(position);
                        mFab.setVisibility(View.VISIBLE);
                        mFab.show();
                        break;
                    case 3:
                        animateFab(position);
                        mFab.setVisibility(View.VISIBLE);
                        mFab.show();
                        break;
                    default:
                        mFab.hide(false);
                        mFab.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        final PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(mViewPager);

        badgeRecent = new BadgeView(this, tabs, 1);
        badgeRecent.setText("0");
        if (badgeReceiver == null) {
            badgeReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (mViewPager.getFocusedChild() != null && mViewPager.getCurrentItem() == 1) {
                        return;
                    }
                    Integer a = Integer.parseInt(badgeRecent.getText().toString());
                    Integer b = intent.getIntExtra("missCall", 0);
                    badgeRecent.setText(a + b > 999 ? "999+" : String.valueOf(a + b));
                    badgeRecent.show();
                }
            };
            registerReceiver(badgeReceiver, new IntentFilter(GlobalVars.BROADCAST_ACTION_BADGE));
        }

        badgeMessage = new BadgeView(this, tabs, 3);
        realm = Realm.getDefaultInstance();
        realmResults = realm.where(Conversation.class).equalTo("unRead", true).findAllAsync();
        listener = new RealmChangeListener<RealmResults<Conversation>>() {
            @Override
            public void onChange(RealmResults<Conversation> s) {
                if (s.size() > 0) {
                    badgeMessage.setText(s.size() > 999 ? "999+" : s.size() + "");
                    badgeMessage.show();
                    ShortcutBadger.applyCount(MainActivity.this, s.size() > 999 ? 999 : s.size());
                } else {
                    badgeMessage.hide();
                    ShortcutBadger.removeCount(MainActivity.this);
                }
            }
        };
        realmResults.addChangeListener(listener);

        tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mViewPager.setCurrentItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        String lt = Utility.getPref(this, GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_LAST_TAB);
        int lastTab = 0;
        if (lt.length() > 0) {
            lastTab = Integer.parseInt(lt);
        }

        mViewPager.setCurrentItem(lastTab, false);

        propagateToolbarState(toolbarIsShown());

        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        phoneNumber = phoneNumber == null ? "" : phoneNumber;
        if(!phoneNumber.isEmpty()){
            startActivity(new Intent(this, MessageActivity.class)
                    .putExtra("phoneNumber", phoneNumber));
            Logger.info(TAG, phoneNumber);
        }

    }

    protected void animateFab(final int position) {
        mFab.clearAnimation();

        // Scale down animation
        ScaleAnimation shrink = new ScaleAnimation(1f, 0.1f, 1f, 0.1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        shrink.setDuration(100);     // animation duration in milliseconds
        shrink.setInterpolator(new AccelerateInterpolator());
        shrink.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Change FAB color and icon
                mFab.setImageResource(iconIntArray[position]);

                // Rotate Animation
                Animation rotate = new RotateAnimation(60.0f, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                        0.5f);
                rotate.setDuration(150);
                rotate.setInterpolator(new DecelerateInterpolator());

                // Scale up animation
                ScaleAnimation expand = new ScaleAnimation(0.1f, 1f, 0.1f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                expand.setDuration(150);     // animation duration in milliseconds
                expand.setInterpolator(new DecelerateInterpolator());

                // Add both animations to animation state
                AnimationSet s = new AnimationSet(false); //false means don't share interpolators
                s.addAnimation(rotate);
                s.addAnimation(expand);
                mFab.startAnimation(s);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mFab.startAnimation(shrink);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Utility.checkPlayServices(this);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        Utility.removePref(this, GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.KEY_PHONE_CHATTING);
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(callActivityStateReceiver);
        } catch (Exception exception) {
        }
        try {
            unregisterReceiver(badgeReceiver);
        } catch (Exception ex) {
        }
        timerStartService.cancel();
        timerStartService.purge();
        timerStartService = null;
        listenerBinding.unbind();

        realmResults.removeChangeListener(listener);

    }

    @Override
    public void onBecameForeground() {
        Log.i(Foreground.TAG, getClass().getName() + " became foreground");
    }

    @Override
    public void onBecameBackground() {
        Log.i(Foreground.TAG, getClass().getName() + " went background");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        mainMenu = menu;
        MenuItem clearLogMenuItem = menu.findItem(R.id.menu_clear_log);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);

        searchView.closeSearch();
        switch (mViewPager.getCurrentItem()) {
            case 0:
                clearLogMenuItem.setVisible(false);
                searchMenuItem.setVisible(false);
                break;
            case 1:
                clearLogMenuItem.setVisible(true);
                searchMenuItem.setVisible(false);
                break;
            case 2:
                clearLogMenuItem.setVisible(false);
                searchMenuItem.setVisible(true);
                searchView.setMenuItem(searchMenuItem);
                break;
            case 3:
                clearLogMenuItem.setVisible(false);
                searchMenuItem.setVisible(false);
//                searchView.setMenuItem(searchMenuItem);
                break;
        }

        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            Drawable newIcon = menuItem.getIcon();
            if (newIcon != null) {
                int[] attrs = new int[]{R.attr.numberPadStyle};
                TypedArray ta = obtainStyledAttributes(attrs);
                newIcon.setColorFilter(ta.getColor(0, 0xFF1565C0), PorterDuff.Mode.SRC_ATOP);
                menuItem.setIcon(newIcon);
                ta.recycle();
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.menu_settings_screen:
                Intent intentSetting = new Intent(this, SettingActivity.class);
                startActivity(intentSetting);
                break;
            case R.id.menu_block_list:
                Intent intentBlockList = new Intent(this, BlockListActivity.class);
                startActivity(intentBlockList);
                break;
            case R.id.menu_clear_log:
                AlertDialog alertDialog = new AlertDialog(this, "Alert", "Do you want to clear all logs ?");
                alertDialog.setAlertDialogListener(new AlertDialogListener() {
                    @Override
                    public void btnYesPressed(AlertDialog alertDialog) {
                        alertDialog.dismiss();
                        deleteAllLogs();
                    }

                    @Override
                    public void btnNoPressed(AlertDialog alertDialog) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
                break;
            case R.id.menu_about:
                Intent intentAbout = new Intent(this, AboutActivity.class);
                startActivity(intentAbout);
                break;
            case R.id.menu_sign_out:
                Realm realm = Realm.getDefaultInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmResults<User> results = realm.where(User.class).findAll();
                        if (results.size() < 1)
                            return;
                        else
                            results.first().setPassword("");
                    }
                });
                realm.close();

                Utility.removePref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_LAST_TAB);
                Utility.removePref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_SYNCHRONIZED_DB);

                String defaultAccount = Utility.getPref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_ACCOUNT_DEFAULT);
                SipServiceCommand.removeAccount(this, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls");

                Intent splashScreen = new Intent(this, SplashScreenActivity.class);
                startActivity(splashScreen);

                finish();
                break;
            case R.id.menu_quit:
                System.exit(0);
                break;
//            case R.id.menu_payment_list:
//                Intent intentPayment = new Intent(this, PaymentListActivity.class);
//                startActivity(intentPayment);
//                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private boolean deleteAllLogs() {

        try {
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.where(com.ccsidd.rtone.objects.CallLog.class).findAll().deleteAllFromRealm();
                }
            });
            realm.close();

        } catch (Exception exception) {
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        if (dragging) {
            int toolbarHeight = mToolbarView.getHeight();
            float currentHeaderTranslationY = ViewHelper.getTranslationY(mHeaderView);
            if (firstScroll) {
                if (-toolbarHeight < currentHeaderTranslationY) {
                    mBaseTranslationY = scrollY;
                }
            }
            float headerTranslationY = ScrollUtils.getFloat(-(scrollY - mBaseTranslationY), -toolbarHeight, 0);
            com.nineoldandroids.view.ViewPropertyAnimator.animate(mHeaderView).cancel();
            ViewHelper.setTranslationY(mHeaderView, headerTranslationY);
        }
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

        Fragment fragment = getCurrentFragment();
        if (fragment == null) {
            return;
        }
        View view = fragment.getView();
        if (view == null) {
            return;
        }

        int toolbarHeight = mToolbarView.getHeight();
        int scrollY;
        final ObservableListView listView = (ObservableListView) view.findViewById(R.id.list_fragment);
        if (listView == null) {
            ObservableRecyclerView recyclerView = (ObservableRecyclerView) view.findViewById(R.id.conversations_list);
            if (recyclerView == null)
                return;
            scrollY = recyclerView.getCurrentScrollY();
        } else
            scrollY = listView.getCurrentScrollY();

        if (scrollState == ScrollState.DOWN) {
            showToolbar();
        } else if (scrollState == ScrollState.UP) {
            if (toolbarHeight <= scrollY) {
                hideToolbar();
            } else {
                showToolbar();
            }
        } else {
            // Even if onScrollChanged occurs without scrollY changing, toolbar should be adjusted
            if (toolbarIsShown() || toolbarIsHidden()) {
                // Toolbar is completely moved, so just keep its state
                // and propagate it to other pages
                propagateToolbarState(toolbarIsShown());
            } else {
                // Toolbar is moving but doesn't know which to move:
                // you can change this to hideToolbar()
                showToolbar();
            }
        }
    }

    private Fragment getCurrentFragment() {
        return mSectionsPagerAdapter.getItemAt(mViewPager.getCurrentItem());
    }

    private void propagateToolbarState(boolean isShown) {
        int toolbarHeight = mToolbarView.getHeight();

        // Set scrollY for the fragments that are not created yet
        mSectionsPagerAdapter.setScrollY(isShown ? 0 : toolbarHeight);

        // Set scrollY for the active fragments
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Skip current item
            if (i == mViewPager.getCurrentItem()) {
                continue;
            }

            // Skip destroyed or not created item
            Fragment f = mSectionsPagerAdapter.getItemAt(i);
            if (f == null) {
                continue;
            }

            View view = f.getView();
            if (view == null) {
                continue;
            }

            if (i == 0)
                continue;

            if (i == 1 || i == 2) {
                ObservableListView listView = (ObservableListView) view.findViewById(R.id.list_fragment);
                if (listView == null) {
                    return;
                }
                if (isShown) {
                    // Scroll up
                    if (0 < listView.getCurrentScrollY()) {
                        listView.setSelection(0);
                    }
                } else {
                    // Scroll down (to hide padding)
                    if (listView.getCurrentScrollY() < toolbarHeight) {
                        listView.setSelection(1);
                    }
                }

            }
            if (i == 3) {
                ObservableRecyclerView recyclerView = (ObservableRecyclerView) view.findViewById(R.id.conversations_list);
                if (recyclerView == null)
                    return;
                if (isShown) {
                    // Scroll up
                    if (0 < recyclerView.getCurrentScrollY()) {
                        recyclerView.scrollVerticallyToPosition(0);
                    }
                } else {
                    // Scroll down (to hide padding)
                    if (recyclerView.getCurrentScrollY() < toolbarHeight) {
                        recyclerView.scrollVerticallyToPosition(1);
                    }
                }
            }
        }
    }

    private boolean toolbarIsShown() {
        return ViewHelper.getTranslationY(mHeaderView) == 0;
    }

    private boolean toolbarIsHidden() {
        return ViewHelper.getTranslationY(mHeaderView) == -mToolbarView.getHeight();
    }

    private void showToolbar() {
        float headerTranslationY = ViewHelper.getTranslationY(mHeaderView);
        if (headerTranslationY != 0) {
            com.nineoldandroids.view.ViewPropertyAnimator.animate(mHeaderView).cancel();
            com.nineoldandroids.view.ViewPropertyAnimator.animate(mHeaderView).translationY(0).setDuration(200).start();
        }
        propagateToolbarState(true);
    }

    private void hideToolbar() {
        float headerTranslationY = ViewHelper.getTranslationY(mHeaderView);
        int toolbarHeight = mToolbarView.getHeight();
        if (headerTranslationY != -toolbarHeight) {
            com.nineoldandroids.view.ViewPropertyAnimator.animate(mHeaderView).cancel();
            com.nineoldandroids.view.ViewPropertyAnimator.animate(mHeaderView).translationY(-toolbarHeight).setDuration(200).start();
        }
        propagateToolbarState(false);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends android.support.v4.app.Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_INITIAL_POSITION = "ARG_INITIAL_POSITION";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_INITIAL_POSITION, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            TypefaceManager.initialize(getActivity(), R.xml.fonts);
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView tv = (TextView) rootView.findViewById(R.id.section_label);
            tv.setText("");
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends CacheFragmentStatePagerAdapter implements PagerSlidingTabStrip.IconTabProvider {

        private int mScrollY;

        public SectionsPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        public void setScrollY(int scrollY) {
            mScrollY = scrollY;
        }

        @Override
        protected Fragment createItem(int position) {
            Fragment f;
            switch (position + 1) {
                case 1:
                    f = new CallFragment();
                    break;
                case 2:
                    f = new RecentFragment();
                    break;
                case 3:
                    f = new ContactFragment();
                    break;
                case 4:
                    f = new ConversationFragment();
                    break;
                default:
                    f = new PlaceholderFragment();
                    break;
            }
            String ARG_INITIAL_POSITION = "ARG_INITIAL_POSITION";
            if (0 < mScrollY) {
                Bundle args = new Bundle();
                args.putInt(ARG_INITIAL_POSITION, 1);
                f.setArguments(args);
            }
            return f;
        }

        @Override
        public int getCount() {
            return 4
                    ;
        }

        @Override
        public CharSequence getPageTitle(int position) {
//            Locale l = Locale.getDefault();
//            switch (position + 1) {
//                case 1:
//                    return getString(R.string.title_call);
//                case 2:
//                    return getString(R.string.title_recents);
//                case 3:
//                    return getString(R.string.title_contacts);
//                case 4:
//                    return getString(R.string.title_messages);
//            }
            return "";
        }

        @Override
        public Drawable getPageIconResId(int position) {
            TypedArray ta;
            int[] attrs;
            Drawable drawable;
            switch (position + 1) {
                case 1:
                    attrs = new int[]{R.attr.iconCall};
                    ta = obtainStyledAttributes(attrs);
                    drawable = ta.getDrawable(0);
                    ta.recycle();
                    return drawable;
                case 2:
                    attrs = new int[]{R.attr.iconRecent};
                    ta = obtainStyledAttributes(attrs);
                    drawable = ta.getDrawable(0);
                    ta.recycle();
                    return drawable;
                case 3:
                    attrs = new int[]{R.attr.iconContact};
                    ta = obtainStyledAttributes(attrs);
                    drawable = ta.getDrawable(0);
                    ta.recycle();
                    return drawable;
                case 4:
                    attrs = new int[]{R.attr.iconMessage};
                    ta = obtainStyledAttributes(attrs);
                    drawable = ta.getDrawable(0);
                    ta.recycle();
                    return drawable;
                default:
                    return null;
            }
        }
    }
}
