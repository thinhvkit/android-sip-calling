package com.ccsidd.rtone.message.messagelist;

import android.content.Context;
import android.util.Log;
import android.util.LruCache;

import com.ccsidd.rtone.objects.Message;

import java.util.regex.Pattern;

public class MessageItemCache extends LruCache<Long, MessageItem> {
    private final String TAG = "MessageItemCache";

    private Context mContext;
    private Pattern mSearchHighlighter;

    public MessageItemCache(Context context, Pattern searchHighlighter, int maxSize) {
        super(maxSize);

        mContext = context;
        mSearchHighlighter = searchHighlighter;
    }

    @Override
    protected void entryRemoved(boolean evicted, Long key, MessageItem oldValue,
                                MessageItem newValue) {
    }

    /**
     * Generates a unique key for this message item given its type and message ID.
     *
     * @param type
     * @param msgId
     */
    public long getKey(String type, long msgId) {
        if (type.equals("mms")) {
            return -msgId;
        } else {
            return msgId;
        }
    }


    public MessageItem get(String type, long msgId, Message message) {
        //long key = getKey(type, msgId);
        long key = msgId;
        MessageItem item = get(key);

        if (item == null) {
            try {
                item = new MessageItem(mContext, msgId, type, message, mSearchHighlighter, false);
                //key = getKey(item.mType, item.mMsgId);
                put(key, item);
            } catch (Exception e) {
                Log.e(TAG, "getCachedMessageItem: ", e);
            }
        }
        return item;
    }
}
