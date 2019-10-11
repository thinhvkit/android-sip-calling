package com.ccsidd.rtone.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.listeners.PhoneListener;

import java.util.ArrayList;

/**
 * Created by dung on 6/25/15.
 */
public class DialogPhoneAdapter extends ArrayAdapter<String> {

    private ArrayList<String> items;
    private Context mContext;
    private PhoneListener listener;

    public DialogPhoneAdapter(Context context, int resource, ArrayList<String> functions) {
        super(context, resource, functions);
        this.items = functions;
        this.mContext = context;
    }

    public void setListener(PhoneListener listener) {
        this.listener = listener;
    }

    @Override
    public String getItem(int position) {
        return items.get(position);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Holder holder;
        View view = convertView;
        if (view == null) {
            holder = new Holder();
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.dialog_phone_row, null);
            holder.textItem = (TextView) view.findViewById(R.id.dl_row_tv);
            holder.imvCall = (ImageView) view.findViewById(R.id.imv_call);
            holder.imvMessage = (ImageView) view.findViewById(R.id.imv_message);
            view.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        final String item = getItem(position);
        holder.textItem.setText(item);

        if (listener != null) {
            if (listener != null) {
                holder.imvCall .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.onCall(item);
                    }
                });
                holder.imvMessage .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.onMessage(item);
                    }
                });
            }
        }

        return view;
    }

    public class Holder {
        public TextView textItem;
        public ImageView imvCall;
        public ImageView imvMessage;
    }
}
