package com.ccsidd.rtone.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.Html;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.listeners.ContactAdapterListener;
import com.ccsidd.rtone.objects.CallLog;
import com.ccsidd.rtone.objects.Contact;
import com.ccsidd.rtone.objects.ContactBlockList;
import com.ccsidd.rtone.utilities.Utility;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.realm.Case;
import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;
import io.realm.RealmModel;
import io.realm.RealmResults;

/**
 * Created by dung on 3/26/15.
 */
public class ContactAdapter<T extends RealmModel> extends RealmBaseAdapter<T> implements ListAdapter, Filterable {
    private Context mContext;
    private String fragmentName = "";

    private int count;
    private int stepNumber = 50;
    private int startCount = 50;

    private ContactAdapterListener contactAdapterListener;
    private SparseBooleanArray mSelectedItemsIds;
    private OrderedRealmCollection<T> mRealmObjectList;

    public ContactAdapter(Context context, String fragmentName, OrderedRealmCollection<T> realmResult) {
        super(context, realmResult);
        mSelectedItemsIds = new SparseBooleanArray();
        this.mContext = context;
        this.fragmentName = fragmentName;
        this.contactAdapterListener = null;
        this.mRealmObjectList = realmResult;

        this.startCount = Math.min(startCount, adapterData.size()); //don't try to show more views than we have
        this.count = this.startCount;
    }

    public ContactAdapter(Context context, String fragmentName, OrderedRealmCollection<T> realmResult, ContactAdapterListener contactAdapterListener) {
        super(context, realmResult);
        mSelectedItemsIds = new SparseBooleanArray();
        this.mContext = context;
        this.fragmentName = fragmentName;
        this.contactAdapterListener = contactAdapterListener;
        this.mRealmObjectList = realmResult;

        this.startCount = Math.min(startCount, adapterData.size()); //don't try to show more views than we have
        this.count = this.startCount;
    }

    @Override
    public int getCount() {
        return super.getCount();
        //return count;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            viewHolder = new ViewHolder();
            view = inflater.inflate(R.layout.fragment_contact_row, null);
            viewHolder.tvName = (TextView) view.findViewById(R.id.fm_contact_row_name);
            viewHolder.tvPhone = (TextView) view.findViewById(R.id.fm_contact_row_phone);
            viewHolder.tvDataUsage = (TextView) view.findViewById(R.id.fm_contact_row_datausage);
            viewHolder.tvDate = (TextView) view.findViewById(R.id.fm_contact_row_date);
            viewHolder.imgvAvatar = (ImageView) view.findViewById(R.id.fm_contact_row_imgv_avatar);
            viewHolder.imgvStatus = (ImageView) view.findViewById(R.id.fm_contact_row_imgv_status);
            viewHolder.imgMore = (LinearLayout) view.findViewById(R.id.fm_contact_row_imgv_more);
            view.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) view.getTag();

        T item = getItem(position);
        if (item != null)
            switch (this.fragmentName) {
                case "ContactFragment":
                    Contact contact = (Contact) getItem(position);
                    setDataWithContactFragment(contact, viewHolder);
                    break;
                case "ContactFragment-Block":
                    Contact contactBlock = (Contact) getItem(position);
                    setDataWithContactFragment(contactBlock, viewHolder);
                    break;
                case "ContactBlockList":
                    ContactBlockList contactBlockList = (ContactBlockList) getItem(position);
                    setDataWithContactBlockListFragment(contactBlockList, viewHolder);
                    break;
                case "RecentFragment":
                    CallLog callLog = (CallLog) getItem(position);
                    Contact o_contact = Utility.getContactDisplayNameByNumber(callLog.getNumber());
                    CallLog cal = new CallLog();
                    cal.setId(callLog.getId());
                    cal.setName(o_contact.getDisplayName());
                    cal.setImageUri(callLog.getImageUri());
                    cal.setNumber(callLog.getNumber());
                    cal.setDataUsage(callLog.getDataUsage());
                    cal.setNumberLabel(callLog.getNumberLabel());
                    cal.setNumberType(callLog.getNumberType());
                    cal.setType(callLog.getType());
                    cal.setImageUri(o_contact.getImageUri());
                    cal.setDate(callLog.getDate());
//                    if (callLog.getType() == android.provider.CallLog.Calls.MISSED_TYPE) {
//                        RealmResults<T> callLogMiss = mRealmObjectList.where().equalTo("number", callLog.getNumber())
//                                .equalTo("type", android.provider.CallLog.Calls.MISSED_TYPE).findAll();
//                        Calendar beginDay = Calendar.getInstance();
//                        beginDay.setTimeInMillis(callLog.getDate());
//                        beginDay.set(Calendar.HOUR_OF_DAY, 0);
//                        beginDay.set(Calendar.MINUTE, 0);
//                        beginDay.set(Calendar.SECOND, 0);
//                        beginDay.set(Calendar.MILLISECOND, 0);
//
//                        Calendar endDay = Calendar.getInstance();
//                        endDay.setTimeInMillis(callLog.getDate());
//                        endDay.set(Calendar.HOUR_OF_DAY, 23);
//                        endDay.set(Calendar.MINUTE, 59);
//                        endDay.set(Calendar.SECOND, 59);
//                        endDay.set(Calendar.MILLISECOND, 999);
//
//                        if (callLogMiss.where().between("date", callLog.getDate(), endDay.getTimeInMillis()).findAll().size() > 1) {
//                            return null;
//                        }
//                        cal.setDataUsage("(" + callLogMiss.where().between("date", beginDay.getTimeInMillis(), endDay.getTimeInMillis()).findAll().size() + ")");
//                    }
                    setDataWithRecentFragment(cal, viewHolder);
                    break;
            }
        return view;
    }

    private void setDataWithContactFragment(final Contact contact, final ViewHolder viewHolder) {
        viewHolder.tvName.setText(contact.getDisplayName());
        viewHolder.imgvStatus.setVisibility(View.INVISIBLE);
        viewHolder.tvDate.setVisibility(View.GONE);
        viewHolder.tvPhone.setVisibility(View.GONE);
        viewHolder.tvDataUsage.setVisibility(View.GONE);

        viewHolder.imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contactAdapterListener.onClickSubMenu(contact);
            }
        });

        Utility.loadAvatar(mContext, viewHolder.imgvAvatar, contact);
    }

    private void setDataWithContactBlockListFragment(final ContactBlockList contactBlock, ViewHolder viewHolder) {
        viewHolder.tvPhone.setVisibility(View.VISIBLE);
        viewHolder.imgvStatus.setVisibility(View.INVISIBLE);
        viewHolder.tvDate.setVisibility(View.GONE);
        viewHolder.tvDataUsage.setVisibility(View.GONE);

        viewHolder.tvName.setText(contactBlock.getDisplayName());
        viewHolder.tvPhone.setText(contactBlock.getPhoneNumbers());
        viewHolder.imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contactAdapterListener.onClickSubMenu(contactBlock);
            }
        });

        Contact contact = Utility.getContactDisplayNameByNumber(contactBlock.getPhoneNumbers());
        Utility.loadAvatar(mContext, viewHolder.imgvAvatar, contact);
    }

    private void setDataWithRecentFragment(final CallLog callLog, final ViewHolder viewHolder) {

        viewHolder.imgvStatus.setVisibility(View.VISIBLE);
        viewHolder.tvDate.setVisibility(View.VISIBLE);
        viewHolder.tvPhone.setVisibility(View.VISIBLE);
        viewHolder.tvDataUsage.setVisibility(View.VISIBLE);

        Contact contact = Utility.getContactDisplayNameByNumber(callLog.getNumber());
        Utility.loadAvatar(mContext, viewHolder.imgvAvatar, contact);

        switch (callLog.getType()) {
            case android.provider.CallLog.Calls.INCOMING_TYPE:
                viewHolder.imgvStatus.setImageResource(R.drawable.incoming);
                break;
            case android.provider.CallLog.Calls.OUTGOING_TYPE:
                viewHolder.imgvStatus.setImageResource(R.drawable.outgoing);
                break;
            case android.provider.CallLog.Calls.MISSED_TYPE:
                viewHolder.imgvStatus.setImageResource(R.drawable.missed);
                break;
        }

        if (callLog.getName().length() > 0) {

            viewHolder.tvName.setText(callLog.getName());
            viewHolder.tvPhone.setText(Html.fromHtml("<b>" + Utility.getNumberLabel(callLog.getNumberType(), callLog.getNumberLabel()) + "</b> " + callLog.getNumber()));
        } else {
            viewHolder.tvName.setText(callLog.getNumber());
            viewHolder.tvPhone.setText(callLog.getCountry());
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        String formattedDate = sdf.format(callLog.getDate());
        viewHolder.tvDate.setText(formattedDate);

        viewHolder.tvDataUsage.setText(callLog.getDataUsage());

        viewHolder.imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contactAdapterListener.onClickSubMenu(callLog);
            }
        });
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);

        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

    @Override
    public Filter getFilter() {

        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results == null)
                    return;

                if (fragmentName.contains("ContactFragment")) {
                    RealmResults<T> realmResults = mRealmObjectList.where()
                            .contains("displayName", constraint.toString(), Case.INSENSITIVE)
                            .or()
                            .contains("phoneNumbers.number", constraint.toString())
                            .findAll();
                    adapterData = realmResults;
                    startCount = Math.min(startCount, adapterData.size()); //don't try to show more views than we have
                    count = startCount;
                } else if (fragmentName.contains("ContactBlockList")) {
                    RealmResults<T> realmResults = mRealmObjectList.where()
                            .contains("displayName", constraint.toString(), Case.INSENSITIVE)
                            .or()
                            .contains("phoneNumbers", constraint.toString())
                            .findAll();
                    adapterData = realmResults;
                    startCount = Math.min(startCount, adapterData.size()); //don't try to show more views than we have
                    count = startCount;
                }

                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();
                return results;
            }
        };

        return filter;
    }

    public boolean showMore() {
        if (count == adapterData.size()) {
            return true;
        } else {
            count = Math.min(count + stepNumber, adapterData.size()); //don't go past the end
            notifyDataSetChanged(); //the count size has changed, so notify the super of the change
            return endReached();
        }
    }

    /**
     * @return true if then entire data set is being displayed, false otherwise
     */
    public boolean endReached() {
        return count == adapterData.size();
    }

    /**
     * Sets the ListView back to its initial count number
     */
    public void reset() {
        count = startCount;
        notifyDataSetChanged();
    }

    static class ViewHolder {
        private ImageView imgvAvatar;
        private TextView tvName;
        private TextView tvPhone;
        private TextView tvDataUsage;
        private TextView tvDate;
        private ImageView imgvStatus;
        private LinearLayout imgMore;
    }

}
