package com.ccsidd.rtone.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.listeners.PhoneListener;

import java.util.ArrayList;

/**
 * Created by dung on 6/25/15.
 */
public class SingleChoiceDialogAdapter extends ArrayAdapter<String> {

    private ArrayList<String> functions;
    private Context mContext;
    private boolean showRadio = false;
    private PhoneListener listener;
    private int currentSelected;

    public void setListener(PhoneListener listener) {
        this.listener = listener;
    }

    public boolean isShowRadio() {
        return showRadio;
    }

    public void setShowRadio(boolean showRadio) {
        this.showRadio = showRadio;
    }

    public int getCurrentSelected() {
        return currentSelected;
    }

    public void setCurrentSelected(int currentSelected) {
        this.currentSelected = currentSelected;
    }

    public SingleChoiceDialogAdapter(Context context, int resource, ArrayList<String> functions) {
        super(context, resource, functions);
        this.functions = functions;
        this.mContext = context;
    }

    @Override
    public String getItem(int position) {
        return functions.get(position);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Holder holder;
        View view = convertView;
        if(view == null)
        {
            holder = new Holder();
            LayoutInflater layoutInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.dialog_row, null);
            holder.textItem = (TextView)view.findViewById(R.id.dl_row_tv);
            holder.radioItem = (RadioButton)view.findViewById(R.id.dl_row_rd);
            view.setTag(holder);
        }
        else {
            holder = (Holder) convertView.getTag();
        }
        final String item = getItem(position);
        holder.textItem.setText(item);
        if (isShowRadio())
        {
            holder.radioItem.setVisibility(View.VISIBLE);
            if (position == currentSelected)
                holder.radioItem.setChecked(true);
            else
                holder.radioItem.setChecked(false);
        }
        else
            holder.radioItem.setVisibility(View.GONE);

        if (listener != null)
        {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClickListener(position, holder);
                }
            });
            holder.radioItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClickListener(position, holder);
                }
            });
        }



        return view;
    }

    public class Holder
    {
        public TextView textItem;
        public RadioButton radioItem;
    }
}