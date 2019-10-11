package com.ccsidd.rtone.message.base;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.ccsidd.rtone.objects.Message;

import java.util.ArrayList;
import java.util.HashMap;

import io.realm.OrderedRealmCollection;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmResults;


public abstract class RecyclerArrayAdapter<T extends RealmModel, VH extends RecyclerView.ViewHolder, DataType>
        extends RecyclerView.Adapter<VH> {

    public interface ItemClickListener<DataType> {
        void onItemClick(DataType object, View view);

        void onItemLongClick(DataType object, View view);
    }

    public interface MultiSelectListener {
        void onMultiSelectStateChanged(boolean enabled);

        void onItemAdded(long id);

        void onItemRemoved(long id);
    }

    @NonNull
    protected final Context mContext;
    //protected ArrayList<Message> mMessages;
    protected final LayoutInflater inflater;

    private final boolean hasAutoUpdates;
    private final RealmChangeListener listener;
    @Nullable
    private OrderedRealmCollection<T> adapterData;

    public RecyclerArrayAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<T> data, boolean autoUpdate) {
        //noinspection ConstantConditions
        if (context == null) {
            throw new IllegalArgumentException("Context can not be null");
        }

        this.mContext = context;
        this.adapterData = data;
        //this.mMessages = new ArrayList<>();
        this.inflater = LayoutInflater.from(context);
        this.hasAutoUpdates = autoUpdate;

        // Right now don't use generics, since we need maintain two different
        // types of listeners until RealmList is properly supported.
        // See https://github.com/realm/realm-java/issues/989
        this.listener = hasAutoUpdates ? new RealmChangeListener() {
            @Override
            public void onChange(Object results) {
                notifyDataSetChanged();
            }
        } : null;
    }

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if (hasAutoUpdates && isDataValid()) {
            //noinspection ConstantConditions
            addListener(adapterData);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(final RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (hasAutoUpdates && isDataValid()) {
            //noinspection ConstantConditions
            removeListener(adapterData);
        }
    }

    /**
     * Returns the current ID for an item. Note that item IDs are not stable so you cannot rely on the item ID being the
     * same after notifyDataSetChanged() or {@link #updateData(OrderedRealmCollection)} has been called.
     *
     * @param index position of item in the adapter.
     * @return current item ID.
     */
    @Override
    public long getItemId(final int index) {
        return index;
    }

    @Override
    public int getItemCount() {
        //noinspection ConstantConditions
        return isDataValid() ? adapterData.size() : 0;
    }

//    /**
//     * Returns the item associated with the specified position.
//     * Can return {@code null} if provided Realm instance by {@link OrderedRealmCollection} is closed.
//     *
//     * @param index index of the item.
//     * @return the item at the specified position, {@code null} if adapter data is not valid.
//     */
//    @SuppressWarnings("WeakerAccess")
//    @Nullable
//    public T getItem(int index) {
//        //noinspection ConstantConditions
//        return isDataValid() ? adapterData.get(index) : null;
//    }

    /**
     * Returns data associated with this adapter.
     *
     * @return adapter data.
     */
    @Nullable
    public OrderedRealmCollection<T> getData() {
        return adapterData;
    }

    /**
     * Updates the data associated to the Adapter. Useful when the query has been changed.
     * If the query does not change you might consider using the automaticUpdate feature.
     *
     * @param data the new {@link OrderedRealmCollection} to display.
     */
    @SuppressWarnings("WeakerAccess")
    public void updateData(@Nullable OrderedRealmCollection<T> data) {
        if (hasAutoUpdates) {
            if (adapterData != null) {
                removeListener(adapterData);
            }
            if (data != null) {
                addListener(data);
            }
        }

        this.adapterData = data;
        notifyDataSetChanged();
    }

    private void addListener(@NonNull OrderedRealmCollection<T> data) {
        if (data instanceof RealmResults) {
            RealmResults realmResults = (RealmResults) data;
            //noinspection unchecked
            realmResults.addChangeListener(listener);
        } else if (data instanceof RealmList) {
            RealmList realmList = (RealmList) data;
            //noinspection unchecked
            //realmList.realm.handlerController.addChangeListenerAsWeakReference(listener);
        } else {
            throw new IllegalArgumentException("RealmCollection not supported: " + data.getClass());
        }
    }

    private void removeListener(@NonNull OrderedRealmCollection<T> data) {
        if (data instanceof RealmResults) {
            RealmResults realmResults = (RealmResults) data;
            realmResults.removeChangeListener(listener);
        } else if (data instanceof RealmList) {
            RealmList realmList = (RealmList) data;
            //noinspection unchecked
            //realmList.realm.handlerController.removeWeakChangeListener(listener);
        } else {
            throw new IllegalArgumentException("RealmCollection not supported: " + data.getClass());
        }
    }

    private boolean isDataValid() {
        return adapterData != null && adapterData.isValid();
    }

    protected HashMap<Long, DataType> mSelectedItems = new HashMap<>();

    protected ItemClickListener<DataType> mItemClickListener;
    protected RecyclerArrayAdapter.MultiSelectListener mMultiSelectListener;

    public void setItemClickListener(ItemClickListener<DataType> conversationClickListener) {
        mItemClickListener = conversationClickListener;
    }

    public void setMultiSelectListener(RecyclerArrayAdapter.MultiSelectListener multiSelectListener) {
        mMultiSelectListener = multiSelectListener;
    }

    public void changeArrayList(ArrayList<Message> messages) {
        if (messages != null) {
            //mMessages.clear();
            //mMessages.addAll(messages);
            notifyDataSetChanged();
        } else
            notifyDataSetChanged();
    }

    public OrderedRealmCollection<T> getAdapterData() {
        return adapterData;
    }

    public int getCount() {
        return isDataValid() ? adapterData.size() : 0;
    }

    protected abstract DataType getItem(int position);

    public boolean isInMultiSelectMode() {
        return mSelectedItems.size() > 0;
    }

    public HashMap<Long, DataType> getSelectedItems() {
        return mSelectedItems;
    }

    public void disableMultiSelectMode(boolean requestCallback) {
        if (isInMultiSelectMode()) {
            mSelectedItems.clear();
            notifyDataSetChanged();

            if (requestCallback && mMultiSelectListener != null) {
                mMultiSelectListener.onMultiSelectStateChanged(false);
            }
        }
    }

    public boolean isSelected(long threadId) {
        return mSelectedItems.containsKey(threadId);
    }

    public void setSelected(long threadId, DataType object) {

        if (!mSelectedItems.containsKey(threadId)) {
            mSelectedItems.put(threadId, object);
            notifyDataSetChanged();

            if (mMultiSelectListener != null) {
                mMultiSelectListener.onItemAdded(threadId);

                if (mSelectedItems.size() == 1) {
                    mMultiSelectListener.onMultiSelectStateChanged(true);
                }
            }
        }
    }

    public void setUnselected(long threadId) {
        if (mSelectedItems.containsKey(threadId)) {
            mSelectedItems.remove(threadId);
            notifyDataSetChanged();

            if (mMultiSelectListener != null) {
                mMultiSelectListener.onItemRemoved(threadId);

                if (mSelectedItems.size() == 0) {
                    mMultiSelectListener.onMultiSelectStateChanged(false);
                }
            }
        }
    }

    public void toggleSelection(long threadId, DataType object) {
        if (isSelected(threadId)) {
            setUnselected(threadId);
        } else {
            setSelected(threadId, object);
        }
    }

    /**
     * Select all the view in the adapter
     */
    public void selectAll() {
        for (int i = 0; i < getAdapterData().size(); i++) {
            if (!mSelectedItems.containsKey((long) i)) {
                mSelectedItems.put((long) i, getItem(i));

            }
        }
        notifyDataSetChanged();

        if (mMultiSelectListener != null) {
            mMultiSelectListener.onMultiSelectStateChanged(true);
        }
    }

    public void deselectAll() {
        for (int i = 0; i < getAdapterData().size(); i++) {
            if (mSelectedItems.containsKey((long) i)) {
                mSelectedItems.remove((long) i);

            }
        }
        notifyDataSetChanged();

        if (mMultiSelectListener != null) {
            mMultiSelectListener.onMultiSelectStateChanged(false);
        }
    }

    public void toggleSelectionAll() {
        if (mSelectedItems.size() == getAdapterData().size()) {
            deselectAll();
        } else {
            selectAll();
        }
    }

}
