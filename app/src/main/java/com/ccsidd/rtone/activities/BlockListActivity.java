package com.ccsidd.rtone.activities;

import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.adapters.ContactAdapter;
import com.ccsidd.rtone.dialogs.FunctionDialog;
import com.ccsidd.rtone.fragments.BlockContactFragment;
import com.ccsidd.rtone.fragments.ContactFragment;
import com.ccsidd.rtone.listeners.ContactAdapterListener;
import com.ccsidd.rtone.listeners.DialogListener;
import com.ccsidd.rtone.message.ThemeManager;
import com.ccsidd.rtone.message.base.QKActivity;
import com.ccsidd.rtone.message.view.QKTextView;
import com.ccsidd.rtone.objects.ContactBlockList;
import com.ccsidd.rtone.searchview.SearchViewLayout;
import com.ccsidd.rtone.services.Logger;
import com.ccsidd.rtone.utilities.GlobalVars;
import com.ccsidd.rtone.utilities.ThemeUtils;
import com.ccsidd.rtone.utilities.Utility;
import com.innovattic.font.FontEditText;
import com.innovattic.font.TypefaceManager;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class BlockListActivity extends QKActivity implements AdapterView.OnItemClickListener, ContactAdapterListener, DialogListener {

    private String TAG = BlockListActivity.class.getName();
    private ListView listContact;
    private ContactAdapter<ContactBlockList> adapter;
    private Realm realm;
    private FunctionDialog functionDialog;
    private Toolbar mToolbar;
    private QKTextView mTitle;
    private ImageView mOverflowButton;
    private Menu mMenu;
    /*private boolean hasCallback;
    Runnable showMore = new Runnable() {
        public void run() {
            boolean noMoreToShow = adapter.showMore(); //show more views and find out if
            hasCallback = false;
        }
    };
    private Handler mHandler;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String themes = Utility.getPref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_THEMES);
        if (themes.isEmpty())
            themes = "0";
        ThemeUtils.setsTheme(Integer.parseInt(themes));
        ThemeUtils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);

        TypefaceManager.initialize(getApplicationContext(), R.xml.fonts);
        // AFTER SETTING THEME
        setContentView(R.layout.activity_blocklist);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar == null) {
            throw new RuntimeException("Toolbar not found in BaseActivity layout.");
        } else {
            mTitle = (QKTextView) mToolbar.findViewById(R.id.toolbar_title);
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (mTitle != null) {
            mTitle.setText(R.string.menu_block_list);
        }

        //Utility.configureRealm(this, Utility.getPref(this, GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_ACCOUNT_DEFAULT));

        listContact = (ListView) findViewById(R.id.fm_contact_lvContactBlockList);

//        mHandler = new Handler();

        realm = Realm.getDefaultInstance();
        adapter = new ContactAdapter(this, "ContactBlockList", realm.where(ContactBlockList.class).findAllSorted("displayName", Sort.ASCENDING), this);
        listContact.setAdapter(adapter);
        listContact.setOnItemClickListener(this);
        listContact.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                /*if (firstVisibleItem + visibleItemCount == totalItemCount && !adapter.endReached() && !hasCallback) { //check if we've reached the bottom
                    mHandler.postDelayed(showMore, 200);
                    hasCallback = true;
                }*/
            }
        });

        final SearchViewLayout searchViewLayout = (SearchViewLayout) findViewById(R.id.search_view_container);
        searchViewLayout.setExpandedContentSupportFragment(this, new ContactFragment());
        searchViewLayout.handleToolbarAnimation(mToolbar);
        searchViewLayout.setCollapsedHint("Add from contact");
        searchViewLayout.setExpandedHint("Search");

        ColorDrawable collapsed = new ColorDrawable(ContextCompat.getColor(this, R.color.grey_light_mega_ultra));
        ColorDrawable expanded = new ColorDrawable(ContextCompat.getColor(this, R.color.white));
        searchViewLayout.setTransitionDrawables(collapsed, expanded);
        searchViewLayout.setSearchListener(new SearchViewLayout.SearchListener() {
            @Override
            public void onFinished(String searchKeyword) {
                searchViewLayout.collapse();
            }
        });
        searchViewLayout.setOnToggleAnimationListener(new SearchViewLayout.OnToggleAnimationListener() {
            @Override
            public void onStart(boolean expanding) {

            }

            @Override
            public void onFinish(boolean expanded) {
            }
        });
        searchViewLayout.setSearchBoxListener(new SearchViewLayout.SearchBoxListener() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "beforeTextChanged: " + s + "," + start + "," + count + "," + after);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged: " + s + "," + start + "," + before + "," + count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged: " + s);
            }
        });
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public void onClickSubMenu(Object data) {
        ContactBlockList contactBlockList = (ContactBlockList) data;
        ArrayList<String> functions = new ArrayList<>();
        functions.add("Unblock");
        functionDialog = new FunctionDialog(this, contactBlockList, functions, this);
        functionDialog.setTitle(contactBlockList.getPhoneNumbers());
        functionDialog.show();
    }

    @Override
    public void doAction(String function, Object data) {
        if (data instanceof ContactBlockList) {
            final ContactBlockList contact = (ContactBlockList) data;
            switch (function) {
                case "Unblock":
                    try {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                RealmResults<ContactBlockList> result = realm.where(ContactBlockList.class).equalTo("phoneNumbers", contact.getPhoneNumbers()).findAll();
                                result.deleteAllFromRealm();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    functionDialog.dismiss();
                    break;
                default:
                    functionDialog.dismiss();
                    break;
            }
        }
    }


}
