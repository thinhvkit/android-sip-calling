/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ccsidd.rtone.message.conversationlist;

import android.content.Context;
import android.text.TextUtils;

import com.ccsidd.rtone.objects.Conversation;
import com.ccsidd.rtone.services.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/**
 * Mostly immutable model for an SMS/MMS message.
 * <p>
 * <p>The only mutable field is the cached formatted message member,
 * the formatting of which is done outside this model in MessageListItem.
 */
public class ConversationItem {
    private static final String TAG = "ConversationItem";
    private final Context mContext;

    // The thread ID of this conversation.  Can be zero in the case of a
    // new conversation where the recipient set is changing as the user
    // types and we have not hit the database yet to create a thread.
    private long mThreadId;

    private ArrayList<String> mRecipients;    // The current set of recipients.
    private long mDate;                 // The last update time.
    private int mMessageCount;          // Number of messages.
    private String mSnippet;            // Text of the most recent message.
    private boolean mHasUnreadMessages; // True if there are unread messages.
    private boolean mHasAttachment;     // True if any message has an attachment.
    private boolean mHasError;          // True if any message is in an error state.
    private boolean mIsChecked;         // True if user has selected the conversation for a

    private ConversationItem(Context context) {
        mContext = context;
        mRecipients = new ArrayList<>();
        mThreadId = 0;
    }

    private ConversationItem(Context context, long threadId) {

        mContext = context;
        if (!loadFromThreadId(threadId)) {
            mRecipients = new ArrayList<>();
            mThreadId = 0;
        }
    }

    private ConversationItem(Context context, Conversation conversation, long threadId) {

        mContext = context;
        mRecipients = new ArrayList<>();
        fillFromArray(context, this, conversation, threadId);
    }

    public static ConversationItem createNew(Context context) {
        return new ConversationItem(context);
    }

    /**
     * Find the conversation matching the provided thread ID.
     */
    public static ConversationItem get(Context context, long threadId) {
        ConversationItem conv = Cache.get(threadId);
        if (conv != null)
            return conv;

        conv = new ConversationItem(context, threadId);
        try {
            Cache.put(conv);
        } catch (IllegalStateException e) {
            Logger.error(TAG,"Tried to add duplicate Conversation to Cache (from threadId): " + conv);
            if (!Cache.replace(conv)) {
                Logger.error(TAG,"get by threadId cache.replace failed on " + conv);
            }
        }
        return conv;
    }

    /**
     * Find the conversation matching the provided recipient set.
     * When called with an empty recipient list, equivalent to {@link #createNew}.
     */
    public static ConversationItem get(Context context, ArrayList<String> recipients) {
        // If there are no recipients in the list, make a new conversation.
        if (recipients.size() < 1) {
            return createNew(context);
        }

        ConversationItem conv = Cache.get(recipients);
        if (conv != null)
            return conv;

        long threadId = getOrCreateThreadId(context, recipients);
        conv = new ConversationItem(context, threadId);
        Logger.debug(TAG, "Conversation.get: created new conversation " + /*conv.toString()*/ "xxxxxxx");

        if (!conv.getRecipients().containsAll(recipients)) {
            Logger.error(TAG, "Conversation.get: new conv's recipients don't match input recpients "
                    + /*recipients*/ "xxxxxxx");
        }

        try {
            Cache.put(conv);
        } catch (IllegalStateException e) {
            Logger.error(TAG, "Tried to add duplicate Conversation to Cache (from recipients): " + conv);
            if (!Cache.replace(conv)) {
                Logger.error(TAG, "get by recipients cache.replace failed on " + conv);
            }
        }

        return conv;
    }

    public static ConversationItem from(Context context, long convId, Conversation conversation) {
        // First look in the cache for the Conversation and return that one. That way, all the
        // people that are looking at the cached copy will get updated when fillFromArray() is
        // called with this cursor.
        long threadId = convId;
        if (threadId > 0) {
            ConversationItem conv = Cache.get(threadId);
            if (conv != null) {
                fillFromArray(context, conv, conversation, threadId);   // update the existing conv in-place
                return conv;
            }
        }
        ConversationItem conv = new ConversationItem(context, conversation, threadId);
        try {
            Cache.put(conv);
        } catch (IllegalStateException e) {
            Logger.error(TAG, "Tried to add duplicate Conversation to Cache (from cursor): " +
                    conv);
            if (!Cache.replace(conv)) {
                Logger.error(TAG, "Converations.from cache.replace failed on " + conv);
            }
        }
        return conv;
    }

    public synchronized long getThreadId() {
        return mThreadId;
    }

    /**
     * Guarantees that the conversation has been created in the database.
     * This will make a blocking database call if it hasn't.
     *
     * @return The thread ID of this conversation in the database
     */
    public synchronized long ensureThreadId() {

        if (mThreadId <= 0) {
            mThreadId = getOrCreateThreadId(mContext, mRecipients);
        }

        return mThreadId;
    }

    public synchronized void clearThreadId() {
        // remove ourself from the cache
        Cache.remove(mThreadId);

        mThreadId = 0;
    }

    public synchronized void setRecipients(ArrayList<String> list) {

        mRecipients = list;

        // Invalidate thread ID because the recipient set has changed.
        mThreadId = 0;
    }

    /**
     * Returns the recipient set of this conversation.
     */
    public synchronized ArrayList<String> getRecipients() {
        return mRecipients;
    }

    /**
     * Returns the time of the last update to this conversation in milliseconds,
     * on the {@link System#currentTimeMillis} timebase.
     */
    public synchronized long getDate() {
        return mDate;
    }

    /**
     * Returns the number of messages in this conversation, excluding the draft
     * (if it exists).
     */
    public synchronized int getMessageCount() {
        return mMessageCount;
    }

    /**
     * Set the number of messages in this conversation, excluding the draft
     * (if it exists).
     */
    public synchronized void setMessageCount(int cnt) {
        mMessageCount = cnt;
    }

    /**
     * Returns a snippet of text from the most recent message in the conversation.
     */
    public synchronized String getSnippet() {
        return mSnippet;
    }

    /**
     * Returns true if there are any unread messages in the conversation.
     */
    public boolean hasUnreadMessages() {
        synchronized (this) {
            return mHasUnreadMessages;
        }
    }

    private void setHasUnreadMessages(boolean flag) {
        synchronized (this) {
            mHasUnreadMessages = flag;
        }
    }

    /**
     * Returns true if any messages in the conversation have attachments.
     */
    public synchronized boolean hasAttachment() {
        return mHasAttachment;
    }

    /**
     * Returns true if any messages in the conversation are in an error state.
     */
    public synchronized boolean hasError() {
        return mHasError;
    }

    /**
     * Returns true if this conversation is selected for a multi-operation.
     */
    public synchronized boolean isChecked() {
        return mIsChecked;
    }

    public synchronized void setIsChecked(boolean isChecked) {
        mIsChecked = isChecked;
    }

    private static long getOrCreateThreadId(Context context, ArrayList<String> list) {
        HashSet<String> recipients = new HashSet<>();
        String cacheContact = null;
        for (String c : list) {
            cacheContact = c;
            if (cacheContact != null) {
                recipients.add(cacheContact);
            } else {
                recipients.add(c);
            }
        }
        Random random = new Random();
        long retVal = random.nextLong();
        return retVal;
    }

    private boolean loadFromThreadId(long threadId) {
        Conversation c = new Conversation();
        fillFromArray(mContext, this, c, threadId);
        return true;
    }

    private static void fillFromArray(Context context, ConversationItem conv,
                                      Conversation conversation, long threadId) {
        synchronized (conv) {
            conv.mThreadId = threadId;
            conv.mDate = conversation.getTime();
            conv.mMessageCount = 0;

            // Replace the snippet with a default value if it's empty.
            String snippet = conversation.getLastMessage();
            if (TextUtils.isEmpty(snippet)) {
                snippet = "NO Subject";
            }
            conv.mSnippet = snippet;

            conv.setHasUnreadMessages(conversation.isUnRead());
            conv.mHasError = (false);
            conv.mHasAttachment = (false);

            ArrayList<String> mRecipients = new ArrayList<>();
            mRecipients.add(conversation.getPhoneNumber());
            conv.mRecipients = mRecipients;
        }
    }

    /**
     * Private cache for the use of the various forms of Conversation.get.
     */
    private static class Cache {
        private static Cache sInstance = new Cache();

        static Cache getInstance() {
            return sInstance;
        }

        private final HashSet<ConversationItem> mCache;

        private Cache() {
            mCache = new HashSet<>(10);
        }

        /**
         * Return the conversation with the specified thread ID, or
         * null if it's not in cache.
         */
        static ConversationItem get(long threadId) {
            synchronized (sInstance) {
                for (ConversationItem c : sInstance.mCache) {
                    if (c.getThreadId() == threadId) {
                        return c;
                    }
                }
            }
            return null;
        }

        /**
         * Return the conversation with the specified recipient
         * list, or null if it's not in cache.
         */
        static ConversationItem get(ArrayList<String> list) {
            synchronized (sInstance) {
                for (ConversationItem c : sInstance.mCache) {
                    if (c.getRecipients().equals(list)) {
                        return c;
                    }
                }
            }
            return null;
        }

        /**
         * Put the specified conversation in the cache.  The caller
         * should not place an already-existing conversation in the
         * cache, but rather update it in place.
         */
        static void put(ConversationItem c) {
            synchronized (sInstance) {

                if (sInstance.mCache.contains(c)) {
                    throw new IllegalStateException("cache already contains " + c +
                            " threadId: " + c.mThreadId);
                }
                sInstance.mCache.add(c);
            }
        }

        /**
         * Replace the specified conversation in the cache. This is used in cases where we
         * lookup a conversation in the cache by threadId, but don't find it. The caller
         * then builds a new conversation (from the cursor) and tries to add it, but gets
         * an exception that the conversation is already in the cache, because the hash
         * is based on the recipients and it's there under a stale threadId. In this function
         * we remove the stale entry and add the new one. Returns true if the operation is
         * successful
         */
        static boolean replace(ConversationItem c) {
            synchronized (sInstance) {
                if (!sInstance.mCache.contains(c)) {
                    return false;
                }
                // Here it looks like we're simply removing and then re-adding the same object
                // to the hashset. Because the hashkey is the conversation's recipients, and not
                // the thread id, we'll actually remove the object with the stale threadId and
                // then add the the conversation with updated threadId, both having the same
                // recipients.
                sInstance.mCache.remove(c);
                sInstance.mCache.add(c);
                return true;
            }
        }

        static void remove(long threadId) {
            synchronized (sInstance) {
                for (ConversationItem c : sInstance.mCache) {
                    if (c.getThreadId() == threadId) {
                        sInstance.mCache.remove(c);
                        return;
                    }
                }
            }
        }

        /**
         * Remove all conversations from the cache that are not in
         * the provided set of thread IDs.
         */
        static void keepOnly(Set<Long> threads) {
            synchronized (sInstance) {
                Iterator<ConversationItem> iter = sInstance.mCache.iterator();
                while (iter.hasNext()) {
                    ConversationItem c = iter.next();
                    if (!threads.contains(c.getThreadId())) {
                        iter.remove();
                    }
                }
            }
        }
    }
}
