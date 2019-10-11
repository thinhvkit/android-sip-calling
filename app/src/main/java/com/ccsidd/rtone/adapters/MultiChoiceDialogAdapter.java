package com.ccsidd.rtone.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.objects.Contact;
import com.ccsidd.rtone.objects.ContactBlockList;
import com.ccsidd.rtone.utilities.Utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import io.realm.RealmModel;

/**
 * Created by thinhvo on 7/22/16.
 */
public class MultiChoiceDialogAdapter<T extends RealmModel> extends BaseAdapter implements Filterable {
    protected MultiChoiceDialogAdapter.MultiSelectListener mMultiSelectListener;
    Map<T, Boolean> checkBoxState;
    private Context ctx;
    private ArrayList<T> selection;
    private ArrayList<T> mRealmObjectList;
    private ArrayList<T> originalSource;
    private int count;
    private int stepNumber = 10;
    private int startCount = 20;

    public MultiChoiceDialogAdapter(Context context, int resource, ArrayList<T> objects) {
//        super(context, resource, objects);
        this.ctx = context;

        this.selection = new ArrayList<>();
        this.mRealmObjectList = objects;
        originalSource = new ArrayList<>();
        checkBoxState = new HashMap<>();
        for (T key : objects) {
            checkBoxState.put(key, false);
        }

        this.startCount = Math.min(startCount, objects.size()); //don't try to show more views than we have
        this.count = this.startCount;
    }

    @Override
    public int getCount() {
//        return count;
        return mRealmObjectList.size();
    }


    @Nullable
    @Override
    public T getItem(int position) {
        return mRealmObjectList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public ArrayList<T> getSelection() {
        return selection;
    }

    public void setMultiSelectListener(MultiChoiceDialogAdapter.MultiSelectListener multiSelectListener) {
        mMultiSelectListener = multiSelectListener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = inflater.inflate(R.layout.fragment_contact_search_row, null);
            viewHolder.tvName = (TextView) view.findViewById(R.id.dialog_contact_row_name);
            viewHolder.tvPhone = (TextView) view.findViewById(R.id.dialog_contact_row_phone);
            viewHolder.tvType = (TextView) view.findViewById(R.id.dialog_contact_row_type);
            viewHolder.imgvAvatar = (ImageView) view.findViewById(R.id.dialog_contact_row_imgv);
            viewHolder.checkBox = (CheckBox) view.findViewById(R.id.dialog_contact_row_chk);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        final T item = getItem(position);
        if (item instanceof Contact) {
            Contact contact = (Contact) item;
            viewHolder.tvName.setText(contact.getDisplayName());
            viewHolder.tvPhone.setText(contact.getPhoneNumbers().isEmpty() ? "" : contact.getPhoneNumbers().first().getNumber());
            viewHolder.tvType.setText(contact.getPhoneNumbers().isEmpty() ? "" : contact.getPhoneNumbers().first().getLabel());

            if (contact.getImageUri() != null && contact.getImageUri().length() > 0) {
                Bitmap bitmap = Utility.loadBitmapFromPath(ctx, contact.getImageUri());
                if (bitmap == null)
                    viewHolder.imgvAvatar.setImageURI(Uri.parse(contact.getImageUri()));
                else
                    viewHolder.imgvAvatar.setImageBitmap(bitmap);

            } else
                viewHolder.imgvAvatar.setImageResource(R.drawable.unknown_contact);
        }
        if (item instanceof ContactBlockList) {
            ContactBlockList contact = (ContactBlockList) item;
            viewHolder.tvName.setText(contact.getDisplayName());
            viewHolder.tvPhone.setText(contact.getPhoneNumbers());

            if (contact.getImageUri() != null && contact.getImageUri().length() > 0) {
                Bitmap bitmap = Utility.loadBitmapFromPath(ctx, contact.getImageUri());
                if (bitmap == null)
                    viewHolder.imgvAvatar.setImageURI(Uri.parse(contact.getImageUri()));
                else
                    viewHolder.imgvAvatar.setImageBitmap(bitmap);
            } else
                viewHolder.imgvAvatar.setImageResource(R.drawable.unknown_contact);
        }
        viewHolder.checkBox.setChecked(checkBoxState.get(item));
        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    checkBoxState.put(item, true);
                    selection.add(item);
                    if (selection.size() == 1) {
                        mMultiSelectListener.onMultiSelectStateChanged(true);
                    }
                } else {
                    checkBoxState.put(item, false);
                    selection.remove(item);
                    if (selection.size() == 1) {
                        mMultiSelectListener.onMultiSelectStateChanged(true);
                    }
                }
            }
        });
        return view;
    }

    public void checkAll(boolean check) {
        selection.clear();
        if (check) {
            selection.addAll(mRealmObjectList);
        }
        for (T key : mRealmObjectList) {
            checkBoxState.put(key, check);
        }

        notifyDataSetChanged();
    }

    public void setOriginalSource(ArrayList<T> originalSource) {
        this.originalSource.clear();
        this.originalSource.addAll(originalSource);
    }

    void filterCollection(Collection<T> col, Predicate<T> predicate) {
        for (Iterator i = col.iterator(); i.hasNext(); ) {
            T obj = (T) i.next();
            if (!predicate.filter(obj)) {
                i.remove();
            }
        }
    }

    @Override
    public Filter getFilter() {

        Filter filter = new Filter() {
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                results.count = originalSource.size();
                results.values = originalSource;

                return results;
            }

            @Override
            protected void publishResults(final CharSequence constraint, FilterResults results) {
                if (results.count <= 0)
                    return;
                mRealmObjectList.clear();
                mRealmObjectList.addAll((ArrayList<T>) results.values);

                if (constraint.length() != 0) {
                    filterCollection(mRealmObjectList, new Predicate<T>() {
                        public boolean filter(T obj) {
                            if (obj instanceof ContactBlockList)
                                return ((ContactBlockList) obj).getDisplayName().toUpperCase(Locale.getDefault()).contains(constraint.toString().toUpperCase(Locale.getDefault()))
                                        || ((ContactBlockList) obj).getPhoneNumbers().contains(constraint.toString().toUpperCase(Locale.getDefault()));
                            else return false;
                        }
                    });
                }

                count = mRealmObjectList.size();

                notifyDataSetChanged();
            }
        };
        return filter;
    }

    public boolean showMore() {
        if (count == mRealmObjectList.size()) {
            return true;
        } else {
            count = Math.min(count + stepNumber, mRealmObjectList.size()); //don't go past the end
            notifyDataSetChanged(); //the count size has changed, so notify the super of the change
            return endReached();
        }
    }

    /**
     * @return true if then entire data set is being displayed, false otherwise
     */
    public boolean endReached() {
        return count == mRealmObjectList.size();
    }

    /**
     * Sets the ListView back to its initial count number
     */
    public void reset() {
        count = startCount;
        notifyDataSetChanged();
    }

    public interface Predicate<T> {
        public boolean filter(T t);
    }

    public interface MultiSelectListener {
        void onMultiSelectStateChanged(boolean enabled);
    }

    static class ViewHolder {
        private ImageView imgvAvatar;
        private TextView tvName;
        private TextView tvPhone;
        private TextView tvType;
        private CheckBox checkBox;
    }
}
