package com.ccsidd.rtone.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.adapters.SingleChoiceDialogAdapter;
import com.ccsidd.rtone.listeners.PhoneListener;
import com.ccsidd.rtone.listeners.SingleChoiceDialogListener;

import java.util.ArrayList;

/**
 * Created by dung on 7/17/15.
 */
public class SingleChoiceDialog extends Dialog implements AdapterView.OnItemClickListener {

    SingleChoiceDialogListener singleChoiceDialogListener;
    private Context mContext;
    private ListView listView;
    private String title;
    private ArrayList<String> items;
    private int currentSelected;

    public SingleChoiceDialog(Context context, ArrayList<String> items) {
        super(context);
        this.mContext = context;
        this.items = items;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCurrentSelected(int currentSelected) {
        this.currentSelected = currentSelected;
    }

    public void setDialogListener(SingleChoiceDialogListener singleChoiceDialogListener) {
        this.singleChoiceDialogListener = singleChoiceDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_list);
        listView = (ListView) findViewById(R.id.dl_lv);
        EditText tv = (EditText) findViewById(R.id.dl_title);
        tv.setFocusable(false);
        tv.setText(title);
        final SingleChoiceDialogAdapter adapter = new SingleChoiceDialogAdapter(mContext, R.layout.dialog_row, items);
        adapter.setCurrentSelected(currentSelected);
        adapter.setListener(new PhoneListener() {
            SingleChoiceDialogAdapter.Holder previousHolder = null;

            @Override
            public void onItemClickListener(int position, Object data) {
                SingleChoiceDialogAdapter.Holder holder = (SingleChoiceDialogAdapter.Holder) data;
                if (previousHolder == null)
                {
                    View view = listView.getChildAt(currentSelected);
                    RadioButton radioButton = (RadioButton)view.findViewById(R.id.dl_row_rd);
                    radioButton.setChecked(false);
                }
                else if (previousHolder != holder)
                    previousHolder.radioItem.setChecked(false);
                holder.radioItem.setChecked(true);
                previousHolder = holder;
                singleChoiceDialogListener.onClickItemListener(position, holder);
            }

            @Override
            public void onCall(String phoneNumber) {

            }

            @Override
            public void onMessage(String phoneNumber) {

            }
        });
        adapter.setShowRadio(true);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (singleChoiceDialogListener != null)
            singleChoiceDialogListener.onClickItemListener(position, position);
    }
}
