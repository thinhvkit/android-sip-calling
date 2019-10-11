package com.ccsidd.rtone.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.activities.MainActivity;
import com.ccsidd.rtone.activities.PaymentListActivity;
import com.ccsidd.rtone.objects.PaymentRow;
import com.ccsidd.rtone.util.IabHelper;
import com.ccsidd.rtone.util.IabResult;
import com.ccsidd.rtone.util.Inventory;
import com.ccsidd.rtone.util.Purchase;

import java.util.ArrayList;

/**
 * Created by ccsidd on 12/6/16.
 */

public class PaymentAdapter extends ArrayAdapter<PaymentRow> {
    private Context context;
    private ArrayList<PaymentRow> arrayList;
    private TextView mylbTextView;

    static final String SKU_PREMIUM = "premium";
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
    // Current amount of gas in tank, in units
    int mTank;
    //private Activity activity;

    // The helper object
    IabHelper mHelper;
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener;
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener;




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

    public void updateUi() {
        //
    }
    void setWaitScreen(boolean set) {
        //
    }
    void complain(String message) {
        Log.e(context.getClass().getName(), "**** Card Error: " + message);
        alert("Error: " + message);
    }
    void showAlert(String message){
        android.app.AlertDialog.Builder bld = new android.app.AlertDialog.Builder(context);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(context.getClass().getName(), "Showing alert dialog: " + message);
        bld.create().show();
    }
    void alert(String message) {
        android.app.AlertDialog.Builder bld = new android.app.AlertDialog.Builder(context);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(context.getClass().getName(), "Showing alert dialog: " + message);
        bld.create().show();
    }
    void showDeBug(String message){
        mylbTextView.setText(message);
    }


    public PaymentAdapter(Context context, ArrayList<PaymentRow> objects,  IabHelper mHelper, IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener, IabHelper.QueryInventoryFinishedListener mGotInventoryListener) {
        super(context, 0, objects);
        this.context = context;
        this.arrayList = objects;
        this.mHelper = mHelper;
        this.mPurchaseFinishedListener = mPurchaseFinishedListener;
        this.mGotInventoryListener = mGotInventoryListener;

        //mylbTextView.setText("My lv test-----");



    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = layoutInflater.inflate(R.layout.my_row, parent, false);

        TextView tvTitle = (TextView) row.findViewById(R.id.payment_list_row_title);
        TextView tvQuantily = (TextView) row.findViewById(R.id.payment_list_row_cost);
        TextView tvNote = (TextView) row.findViewById(R.id.payment_list_row_note);
       ImageView imageView = (ImageView) row.findViewById(R.id.payment_list_row_img);
        Button btnBuy = (Button) row.findViewById(R.id.btn_payment_buy);

           /// TextView myTv = (TextView) row.findViewById(R.id.my_test_textview);
        final PaymentRow paymentRow = arrayList.get(position);
        if(paymentRow != null){
          //  myTv.setText(paymentRow.getTitle());

            tvTitle.setText(paymentRow.getTitle());
            tvQuantily.setText(paymentRow.getCost());
            tvNote.setText(paymentRow.getNote());
            imageView.setImageResource(R.drawable.ic_apple);

        }
        Context context1 = this.context;
        btnBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(paymentRow != null){
                    Log.d(TAG, "Buy gas button clicked.");

                    if (mSubscribedToInfiniteCard) {
                        complain("No need! You're subscribed to infinite gas. Isn't that awesome?");
                        return;
                    }

//        if (mTank >= TANK_MAX) {
//            complain("Your tank is full. Drive around a bit!");
//            return;
//        }

                    // launch the gas purchase UI flow.
                    // We will be notified of completion via mPurchaseFinishedListener
                    setWaitScreen(true);
                    Log.d(TAG, "Launching purchase flow for gas.");

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
                    String payload = "";

                        mHelper.launchPurchaseFlow((Activity)context, paymentRow.getKey(), RC_REQUEST,
                                mPurchaseFinishedListener, payload);

                }
            }
        });
        return row;
    }

}
