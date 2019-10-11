package com.ccsidd.rtone.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.adapters.ContactAdapter;
import com.ccsidd.rtone.adapters.MultiChoiceDialogAdapter;
import com.ccsidd.rtone.adapters.PaymentAdapter;
import com.ccsidd.rtone.dialogs.FunctionDialog;
import com.ccsidd.rtone.listeners.ContactAdapterListener;
import com.ccsidd.rtone.listeners.DialogListener;
import com.ccsidd.rtone.message.view.QKTextView;
import com.ccsidd.rtone.objects.Contact;
import com.ccsidd.rtone.objects.ContactBlockList;
import com.ccsidd.rtone.objects.PaymentRow;
import com.ccsidd.rtone.objects.Phone;
import com.ccsidd.rtone.services.Logger;
import com.ccsidd.rtone.util.IabHelper;
import com.ccsidd.rtone.util.IabResult;
import com.ccsidd.rtone.util.Inventory;
import com.ccsidd.rtone.util.Purchase;
import com.ccsidd.rtone.utilities.GlobalVars;
import com.ccsidd.rtone.utilities.ThemeUtils;
import com.ccsidd.rtone.utilities.Utility;
import com.innovattic.font.FontEditText;
import com.innovattic.font.TypefaceManager;
import com.konifar.fab_transformation.FabTransformation;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class PaymentListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, ContactAdapterListener, DialogListener {

    private ListView listPayment;
    private FontEditText searchcontacts;
    private ContactAdapter<ContactBlockList> adapter;
    private Realm realm;
    private FunctionDialog functionDialog;
    private String themes;
    private Toolbar mToolbar;
    private QKTextView mTitle;
    private ArrayList<PaymentRow> arrPayment = new ArrayList<PaymentRow>();
    private PaymentAdapter myadapter;
    private boolean hasCallback;
    private String[] name = {"Thanh", "Dung", "Thinh"};
    private TextView mylbTextView;
//    Runnable showMore = new Runnable() {
//        public void run() {
//            boolean noMoreToShow = adapter.showMore(); //show more views and find out if
//            hasCallback = false;
//        }
//    };
//    private Handler mHandler;

    static final String SKU_CARD = "card";

    // SKU for our subscription (infinite card)
    static final String SKU_INFINITE_CARD = "infinite_card";
    // (arbitrary) request code for the purchase flow

    static final int RC_REQUEST = 10001;
    public static Activity mainContext;
    final int REQUEST_CODE_ASK_CONTACT_PERMISSIONS = 123;
    final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    boolean mIsPremium = false;
    boolean mSubscribedToInfiniteCard = false;
    static final int TANK_MAX = 4;
    String TAG = "PaymentActivity";

    IabHelper mHelper;


    // Provides purchase notification while this app is running
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            showAlert("On consumeFinished: "+ purchase + ", result: " + result);
        }
    };
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                showDeBug("Failed to query inventory: " + result);
                return;
            }
            showAlert("Query inventory was successful -- " + result + "Inventory : " + inventory.getAllSkuDetails().size() + " ," + inventory.getAllPurchases().size());
            showDeBug("Query inventory was successful -- " + result + "Inventory : " + inventory);

        }
    };
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
            showAlert("Purchase finished: " + result + ", purchase: " + purchase);
            mylbTextView.setText("Purchase finished: " + result + ", purchase: " + purchase);
            showDeBug("Purchase finished: " + result + ", purchase: " + purchase);

        }
    };

    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    void showDeBug(String message){
        mylbTextView.setText(message);
    }
    void showAlert(String message){
        android.app.AlertDialog.Builder bld = new android.app.AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(this.getPackageName(), "Showing alert dialog: " + message);
        bld.create().show();
    }

    private void initArrPayment(){

        PaymentRow row1 = new PaymentRow();
        row1.setKey("card");
        row1.setTitle("Card");
        row1.setCost("Cost: 0.99$");
        row1.setNote("Action, Drama, Sci-Fi");
        arrPayment.add(row1);

        PaymentRow row2 = new PaymentRow();
        row2.setKey("banana");
        row2.setTitle("Banana");
        row2.setCost("Cost: 1.0$");
        row2.setNote("Action, Sci-Fi, Thriller");
        arrPayment.add(row2);

        PaymentRow row3 = new PaymentRow();
        row3.setKey("apple pen");
        row3.setTitle("Apple pen");
        row3.setCost("Cost: 2.0$");
        row3.setNote("Action, Adventure, Sci-Fi");
        arrPayment.add(row3);

        PaymentRow row4 = new PaymentRow();
        row4.setKey("pinaple pen");
        row4.setTitle("Pinaple pen");
        row4.setCost("Cost: 3.0$");
        row4.setNote("Action, Sci-Fi, Thriller ");
        arrPayment.add(row4);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        themes = Utility.getPref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_THEMES);
        if (themes.isEmpty())
            themes = "0";
        ThemeUtils.setsTheme(Integer.parseInt(themes));
        ThemeUtils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);

//        TypefaceManager.initialize(getApplicationContext(), R.xml.fonts);
//        // AFTER SETTING THEME
        setContentView(R.layout.activity_payment);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar == null) {
            throw new RuntimeException("Toolbar not found in BaseActivity layout.");
        } else {
            mTitle = (QKTextView) mToolbar.findViewById(R.id.toolbar_title);
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (mTitle != null) {
            mTitle.setText("Payment list");
        }

        //Toast.makeText(this,"----On click------", Toast.LENGTH_LONG).show();
//
//        LinearLayout layout_back = (LinearLayout) findViewById(R.id.layout_back);
//        layout_back.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//            }
//        });
//        TextView tv = (TextView) findViewById(R.id.navigation_title);
//        tv.setText("Payment List");
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgyEgNuSvvZRvAqwpvhape12Rp0aoQR0b9BLDKaLTUEJnttxpxH7GtkeqRLO15lIwFPBOUqpg3gcqRioJjPYQkRu0Uet6NjmGlUACaS5uJImuN6EPTshi7VGk9uJOYNO6be3T1g1FV2JiXRoRns8ah+XJxq4aTt5rv6vTQUdWvrvUtvg3HKek9VcarHyj+6F1dmAG92tiqXJSC+jr1a/w5fDdV+/m7Ie3U0dF/djz2gXlfdUAwWxsKfjBzwnNK2Tsl2EgXK3fAW//TgKZeyFx2SoJ4PLG15wZN5x46GA2BUUXET5Y1/OA0vc7kFsiHCWwK8ZOHhCu9dnPi/iD6RTdlwIDAQAB";


        // Some sanity checks to see if the developer (that's you!) really followed the
        // instructions to run this sample (don't put these checks on your app!)
        if (base64EncodedPublicKey.contains("CONSTRUCT_YOUR")) {
            throw new RuntimeException("Please put your app's public key in MainActivity.java. See README.");
        }
        if (this.getPackageName().startsWith("com.example")) {
            throw new RuntimeException("Please change the sample's package name! See README.");
        }

        // Create the helper, passing it our context and the public key to verify signatures with
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(true);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    showDeBug("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;
                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });




        Utility.configureRealm(this, Utility.getPref(this, GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_ACCOUNT_DEFAULT));

        listPayment = (ListView) findViewById(R.id.fm_payment_lvPayment);
        mylbTextView = (TextView) findViewById(R.id.mylbTest);

        //mylbTextView.setText("heello 233");
        realm = Realm.getDefaultInstance();

        initArrPayment();
        myadapter = new PaymentAdapter(PaymentListActivity.this,arrPayment,mHelper, mPurchaseFinishedListener,mGotInventoryListener);

        ArrayAdapter<String> adap = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,name);

        listPayment.setAdapter(myadapter);
        //listPayment.setAdapter(adap);
        //listPayment.setOnItemClickListener(this);
        listPayment.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Toast.makeText(PaymentListActivity.this,"----Here click------", Toast.LENGTH_LONG).show();
            }
        });

//        searchcontacts = (FontEditText) findViewById(R.id.fm_contact_edSearchTextBlockList);

        //Button btnAddPerson = (Button) findViewById(R.id.btnAdd_BlockList);
        //btnAddPerson.setOnClickListener(this);

//        searchcontacts.requestFocus();
//        searchcontacts.addTextChangedListener(new TextWatcher() {
//
//            public void onTextChanged(CharSequence s, int start, int before,
//                                      int count) {
//                adapter.getFilter().filter(s);
//            }
//
//            public void beforeTextChanged(CharSequence s, int start, int count,
//                                          int after) {
//
//            }
//
//            public void afterTextChanged(Editable s) {
//                //Toast.makeText(view.getContext(), s+"",Toast.LENGTH_SHORT);
//            }
//        });

//        mHandler = new Handler();
//

//        adapter = new ContactAdapter(this, "ContactBlockList", realm.where(ContactBlockList.class).findAllSorted("displayName", Sort.ASCENDING), this);
//        listPayment.setAdapter(adapter);
//        listPayment.setOnItemClickListener(this);
//        listPayment.setOnScrollListener(new AbsListView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//
//            }
//
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                if (firstVisibleItem + visibleItemCount == totalItemCount && !adapter.endReached() && !hasCallback) { //check if we've reached the bottom
//                    mHandler.postDelayed(showMore, 200);
//                    hasCallback = true;
//                }
//            }
 //       });
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

    @Override
    protected void onResume() {
        super.onResume();
    }

//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        Toast.makeText(PaymentListActivity.this,"----Here click------", Toast.LENGTH_LONG).show();
//    }
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

    }


    @Override
    public void doAction(String function, Object data) {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Toast.makeText(PaymentListActivity.this,"----Here click------", Toast.LENGTH_LONG).show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

}
