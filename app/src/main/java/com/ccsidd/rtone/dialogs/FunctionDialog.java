package com.ccsidd.rtone.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.adapters.DialogAdapter;
import com.ccsidd.rtone.adapters.DialogPhoneAdapter;
import com.ccsidd.rtone.listeners.DialogListener;
import com.ccsidd.rtone.listeners.PhoneListener;
import com.ccsidd.rtone.objects.CallLog;
import com.ccsidd.rtone.objects.Contact;
import com.ccsidd.rtone.objects.ContactBlockList;

import java.util.ArrayList;

/**
 * Created by dung on 6/25/15.
 */
public class FunctionDialog<T> extends Dialog implements AdapterView.OnItemClickListener {

    DialogListener dialogListener;
    private Context mContext;
    private ListView listView;
    private T contact;
    private ArrayList<String> functions;
    private boolean isPhoneDialog = false;
    private PhoneListener phoneListener;


    public FunctionDialog(Context context, T contact, ArrayList<String> functions, DialogListener dialogListener) {
        super(context);
        this.mContext = context;
        this.functions = functions;
        this.contact = contact;
        this.dialogListener = dialogListener;
    }

    public void setPhoneDialog(boolean isPhoneDialog) {
        this.isPhoneDialog = isPhoneDialog;
    }

    public void setDialogAdapterListenerr(PhoneListener phoneListener) {
        this.phoneListener = phoneListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_list);
        listView = (ListView) findViewById(R.id.dl_lv);
        EditText tv = (EditText) findViewById(R.id.dl_title);
        tv.setFocusable(false);

        String title = "";
        if (contact instanceof String)
            title = (String) contact;
        else if (contact instanceof Contact)
            title = ((Contact) contact).getDisplayName();
        else if (contact instanceof ContactBlockList)
            title = ((ContactBlockList) contact).getDisplayName();
        else if (contact instanceof CallLog)
            title = ((CallLog) contact).getName();

        if (title.length() == 0) {
            title = "Unknown";
        }
        tv.setText(title);

        if (isPhoneDialog) {
            DialogPhoneAdapter phoneAdapter = new DialogPhoneAdapter(mContext, R.layout.dialog_phone_row, functions);
            phoneAdapter.setListener(phoneListener);
            listView.setAdapter(phoneAdapter);
        } else {
            DialogAdapter adapter = new DialogAdapter(mContext, R.layout.dialog_row, functions);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(this);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        dialogListener.doAction(adapterView.getItemAtPosition(position).toString(), contact);
    }
}
