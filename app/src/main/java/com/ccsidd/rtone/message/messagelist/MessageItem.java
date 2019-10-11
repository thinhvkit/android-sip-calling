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

package com.ccsidd.rtone.message.messagelist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Telephony.Sms;

import com.ccsidd.rtone.objects.Message;
import com.ccsidd.rtone.message.common.formatter.FormatterFactory;
import com.ccsidd.rtone.message.common.utils.DateFormatter;

import java.util.regex.Pattern;

/**
 * Mostly immutable model for an SMS/MMS message.
 * <p>
 * <p>The only mutable field is the cached formatted message member,
 * the formatting of which is done outside this model in MessageListItem.
 */
public class MessageItem {
    private static String TAG = "MessageItem";

    public enum DeliveryStatus {NONE, INFO, FAILED, PENDING, RECEIVED}

    final Context mContext;
    public final String mType;
    public final long mMsgId;
    public int mBoxId;

    public DeliveryStatus mDeliveryStatus;
    public boolean mLocked;            // locked to prevent auto-deletion

    public long mDate;
    public String mTimestamp;
    public String mAddress;
    public String mContact;
    public String mBody; // Body of SMS
    public Pattern mHighlight; // portion of message to highlight (from search)
    public int mErrorCode;

    // The only non-immutable field.  Not synchronized, as access will
    // only be from the main GUI thread.  Worst case if accessed from
    // another thread is it'll return null and be set again from that
    // thread.
    public CharSequence mCachedFormattedMessage;

    // The last message is cached above in mCachedFormattedMessage. In the latest design, we
    // show "Sending..." in place of the timestamp when a message is being sent. mLastSendingState
    // is used to keep track of the last sending state so that if the current sending state is
    // different, we can clear the message cache so it will get rebuilt and recached.
    public boolean mLastSendingState;

    @SuppressLint("NewApi")
    public MessageItem(Context context, long msgId, String type, final Message messages
            , Pattern highlight, boolean canBlock) {
        mContext = context;
        mMsgId = msgId;
        mHighlight = highlight;
        mType = type;
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        if ("sms".equals(type)) {

            long status = Sms.STATUS_NONE;
            if (status == Sms.STATUS_NONE) {
                // No delivery report requested
                mDeliveryStatus = DeliveryStatus.NONE;
            } else if (status >= Sms.STATUS_FAILED) {
                // Failure
                mDeliveryStatus = DeliveryStatus.FAILED;
            } else if (status >= Sms.STATUS_PENDING) {
                // Pending
                mDeliveryStatus = DeliveryStatus.PENDING;
            } else {
                // Success
                mDeliveryStatus = DeliveryStatus.RECEIVED;
            }

            // Set contact and message body
            mBoxId = messages.getType();
            mAddress = messages.getPhoneNumber();

            mBody = messages.getBody();
            mBody = FormatterFactory.format(mBody);

            // Unless the message is currently in the progress of being sent, it gets a time stamp.
//            if (!isOutgoingMessage()) {
                // Set "received" or "sent" time stamp
//                boolean sent = prefs.getBoolean(QKPreference.SENT_TIMESTAMPS.getKey(), false) && !isMe();
                mDate = messages.getTime();
                mTimestamp = DateFormatter.getMessageTimestamp(context, mDate);
//            }

            mLocked = false;
            mErrorCode = 0;
        }
    }


    public boolean isSms() {
        return mType.equals("sms");
    }


    public boolean isMe() {
        // Logic matches MessageListAdapter.getItemViewType which is used to decide which
        // type of MessageListItem to create: a left or right justified item depending on whether
        // the message is incoming or outgoing.
        boolean isIncomingSms = isSms() && mBoxId == 0;
        return !(isIncomingSms);
    }

    public boolean isOutgoingMessage() {
        boolean isOutgoingSms = isSms() && mBoxId == 1;
        return isOutgoingSms;
    }

    public boolean isSending() {
        return !isFailedMessage() && isOutgoingMessage();
    }

    public boolean isFailedMessage() {
        boolean isFailedSms = isSms() && mBoxId == 2;
        return isFailedSms;
    }

    // Note: This is the only mutable field in this class.  Think of
    // mCachedFormattedMessage as a C++ 'mutable' field on a const
    // object, with this being a lazy accessor whose logic to set it
    // is outside the class for model/view separation reasons.  In any
    // case, please keep this class conceptually immutable.
    public void setCachedFormattedMessage(CharSequence formattedMessage) {
        mCachedFormattedMessage = formattedMessage;
    }

    public CharSequence getCachedFormattedMessage() {
        boolean isSending = isSending();
        if (isSending != mLastSendingState) {
            mLastSendingState = isSending;
            mCachedFormattedMessage = null;         // clear cache so we'll rebuild the message
            // to show "Sending..." or the sent date.
        }
        return mCachedFormattedMessage;
    }

    public long getMessageId() {
        return mMsgId;
    }

    public int getBoxId() {
        return mBoxId;
    }

    @Override
    public String toString() {
        return "type: " + mType +
                " address: " + mAddress +
                " contact: " + mContact +
                " delivery status: " + mDeliveryStatus;
    }
}
