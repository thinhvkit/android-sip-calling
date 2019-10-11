package com.ccsidd.rtone.message.messagelist;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.activities.MessageActivity;
import com.ccsidd.rtone.chips.ChipsUtil;
import com.ccsidd.rtone.message.dialog.QKDialog;
import com.ccsidd.rtone.message.emoji.EmojiHandler;
import com.ccsidd.rtone.message.view.CollapsableLinearLayout;
import com.ccsidd.rtone.objects.Contact;
import com.ccsidd.rtone.objects.Message;
import com.ccsidd.rtone.message.base.RecyclerArrayAdapter;
import com.ccsidd.rtone.message.ThemeManager;
import com.ccsidd.rtone.message.common.emoji.EmojiRegistry;
import com.ccsidd.rtone.message.view.AvatarView;
import com.ccsidd.rtone.utilities.Utility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.OrderedRealmCollection;


public class MessageListAdapter extends RecyclerArrayAdapter<Message, MessageListViewHolder, MessageItem> {
    private final String TAG = "MessageListAdapter";

    public static final int INCOMING_ITEM = 0;
    public static final int OUTGOING_ITEM = 1;

    private ArrayList<Long> mSelectedConversations = new ArrayList<>();

    private static final Pattern urlPattern = Pattern.compile(
            "\\b(https?:\\/\\/\\S+(?:png|jpe?g|gif)\\S*)\\b",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private MessageItemCache mMessageItemCache;
//    private MessageColumns.ColumnsMap mColumnsMap;

    private final Resources mRes;
    //private final SharedPreferences mPrefs;

    // Configuration options.
    private long mThreadId = -1;
    private long mRowId = -1;
    private Pattern mSearchHighlighter = null;
    private boolean mIsGroupConversation = false;
    private Handler mMessageListItemHandler = null; // TODO this isn't quite the same as the others
    private String mSelection = null;

    public MessageListAdapter(MessageActivity context, OrderedRealmCollection<Message> messages) {
        super(context, messages, true );
        mRes = mContext.getResources();
        //mPrefs = mContext.getPrefs();
        mMessageItemCache = new MessageItemCache(mContext, mSearchHighlighter, MessageColumns.CACHE_SIZE);
    }

    public MessageItem getItem(int position) {
        String type = "sms";
        long msgId = position;

        return mMessageItemCache.get(type, msgId, getData().get(position));
    }

    /*public Cursor getCursorForItem(MessageItem item) {
        if (CursorUtils.isValid(mCursor) && mCursor.moveToFirst()) {
            do {
                long id = mCursor.getLong(mColumnsMap.mColumnMsgId);
                String type = mCursor.getString(mColumnsMap.mColumnMsgType);

                if (id == item.mMsgId && type != null && type.equals(item.mType)) {
                    return mCursor;
                }
            } while (mCursor.moveToNext());
        }
        return null;
    }*/

//    public MessageColumns.ColumnsMap getColumnsMap() {
//        return mColumnsMap;
//    }

//    public void setIsGroupConversation(boolean b) {
//        mIsGroupConversation = b;
//    }

    @Override
    public MessageListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        int resource;
        boolean sent;

        if (viewType == INCOMING_ITEM) {
            resource = R.layout.list_item_message_in;
            sent = false;
        } else {
            resource = R.layout.list_item_message_out;
            sent = true;
        }

        View view = inflater.inflate(resource, parent, false);
        return setupViewHolder(view, sent);
    }

    private MessageListViewHolder setupViewHolder(View view, boolean sent) {
        MessageListViewHolder holder = new MessageListViewHolder(mContext, view);

        if (sent) {
            // set up colors
            holder.mBodyTextView.setOnColorBackground(ThemeManager.getSentBubbleColor() != ThemeManager.getNeutralBubbleColor());
            holder.mDateView.setOnColorBackground(false);
            holder.mDeliveredIndicator.setColorFilter(ThemeManager.getTextOnBackgroundSecondary(), PorterDuff.Mode.SRC_ATOP);
            holder.mLockedIndicator.setColorFilter(ThemeManager.getTextOnBackgroundSecondary(), PorterDuff.Mode.SRC_ATOP);

            // set up avatar
            holder.mAvatarView.setImageDrawable(null);
            //holder.mAvatarView.setContactName(AvatarView.ME);
            ((RelativeLayout.LayoutParams) holder.mMessageBlock.getLayoutParams()).setMargins(0, 0, 0, 0);
            holder.mAvatarView.setVisibility(View.GONE);

        } else {
            // set up colors
            holder.mBodyTextView.setOnColorBackground(ThemeManager.getReceivedBubbleColor() != ThemeManager.getNeutralBubbleColor());
            holder.mDateView.setOnColorBackground(false);
            holder.mDeliveredIndicator.setColorFilter(ThemeManager.getTextOnBackgroundSecondary(), PorterDuff.Mode.SRC_ATOP);
            holder.mLockedIndicator.setColorFilter(ThemeManager.getTextOnBackgroundSecondary(), PorterDuff.Mode.SRC_ATOP);
//            holder.mMmsView.getForeground().setColorFilter(ThemeManager.getBackgroundColor(), PorterDuff.Mode.SRC_ATOP);
        }

//        holder.mRoot.setBackgroundDrawable(ThemeManager.getRippleBackground());

       /* LiveViewManager.registerView(QKPreference.BACKGROUND, this, key -> {
            holder.mRoot.setBackgroundDrawable(ThemeManager.getRippleBackground());
            holder.mSlideShowButton.setBackgroundDrawable(ThemeManager.getRippleBackground());
            holder.mMmsView.getForeground().setColorFilter(ThemeManager.getBackgroundColor(), PorterDuff.Mode.SRC_ATOP);
        });*/
        return holder;
    }

    @Override
    public void onBindViewHolder(MessageListViewHolder holder, int position) {
        MessageItem messageItem = getItem(position);

        holder.mData = messageItem;
        holder.mContext = mContext;
        holder.mClickListener = mItemClickListener;
        holder.mRoot.setOnClickListener(holder);
        holder.mRoot.setOnLongClickListener(holder);
        //holder.mPresenter = null;

        // Here we're avoiding reseting the avatar to the empty avatar when we're rebinding
        // to the same item. This happens when there's a DB change which causes the message item
        // cache in the MessageListAdapter to get cleared. When an mms MessageItem is newly
        // created, it has no info in it except the message id. The info is eventually loaded
        // and bindCommonMessage is called again (see onPduLoaded below). When we haven't loaded
        // the pdu, we don't want to call updateContactView because it
        // will set the avatar to the generic avatar then when this method is called again
        // from onPduLoaded, it will reset to the real avatar. This test is to avoid that flash.
        boolean pduLoaded = messageItem.isSms();

        bindTimestamp(holder, messageItem);
        bindGrouping(holder, messageItem, position);

        if (pduLoaded) {
            bindAvatar(holder, messageItem);
        }
        bindBody(holder, messageItem);
        bindMmsView(holder, messageItem);
        bindIndicators(holder, messageItem);
//        bindVcard(holder, messageItem);

//        if (messageItem.mMessageType == PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND) {
//            bindNotifInd(holder, messageItem);
//        } else {
//            if (holder.mDownloadButton != null) {
//                holder.mDownloadButton.setVisibility(View.GONE);
//                holder.mDownloadingLabel.setVisibility(View.GONE);
//            }
//        }
    }

    /**
     * Binds a MessageItem that hasn't been downloaded yet
     */
    /*private void bindNotifInd(final MessageListViewHolder holder, final MessageItem messageItem) {
        holder.showMmsView(false);

        switch (messageItem.getMmsDownloadStatus()) {
            case DownloadManager.STATE_PRE_DOWNLOADING:
            case DownloadManager.STATE_DOWNLOADING:
                showDownloadingAttachment(holder);
                break;
            case DownloadManager.STATE_UNKNOWN:
            case DownloadManager.STATE_UNSTARTED:
                DownloadManager downloadManager = DownloadManager.getInstance();
                boolean autoDownload = downloadManager.isAuto();
                boolean dataSuspended = (QKSMSApp.getApplication().getTelephonyManager()
                        .getDataState() == TelephonyManager.DATA_SUSPENDED);

                // If we're going to automatically start downloading the mms attachment, then
                // don't bother showing the download button for an instant before the actual
                // download begins. Instead, show downloading as taking place.
                if (autoDownload && !dataSuspended) {
                    showDownloadingAttachment(holder);
                    break;
                }
            case DownloadManager.STATE_TRANSIENT_FAILURE:
            case DownloadManager.STATE_PERMANENT_FAILURE:
            case DownloadManager.STATE_SKIP_RETRYING:
            default:
                holder.inflateDownloadControls();
                holder.mDownloadingLabel.setVisibility(View.GONE);
                holder.mDownloadButton.setVisibility(View.VISIBLE);
                holder.mDownloadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.mDownloadingLabel.setVisibility(View.VISIBLE);
                        holder.mDownloadButton.setVisibility(View.GONE);
                        Intent intent = new Intent(mContext, TransactionService.class);
                        intent.putExtra(TransactionBundle.URI, messageItem.mMessageUri.toString());
                        intent.putExtra(TransactionBundle.TRANSACTION_TYPE, Transaction.RETRIEVE_TRANSACTION);
                        mContext.startService(intent);

                        DownloadManager.getInstance().markState(messageItem.mMessageUri, DownloadManager.STATE_PRE_DOWNLOADING);
                    }
                });
                break;
        }

        // Hide the indicators.
        holder.mLockedIndicator.setVisibility(View.GONE);
        holder.mDeliveredIndicator.setVisibility(View.GONE);
        holder.mDetailsIndicator.setVisibility(View.GONE);
    }*/

    /*private void showDownloadingAttachment(MessageListViewHolder holder) {
        holder.inflateDownloadControls();
        holder.mDownloadingLabel.setVisibility(View.VISIBLE);
        holder.mDownloadButton.setVisibility(View.GONE);
    }*/
    private boolean shouldShowTimestamp(MessageItem messageItem, int position) {
        if (position == 0) {
            return true;
        }
        if (position == getData().size() - 1) {
            return true;
        }
        MessageItem messageItem2 = getItem(position - 1);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(messageItem.mDate);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(messageItem2.mDate);

        if (cal.get(Calendar.DAY_OF_MONTH) != cal2.get(Calendar.DAY_OF_MONTH)) {
            return true;
        }
        int MAX_DURATION = 5 * 60 * 1000;
        return (messageItem2.mDate - messageItem.mDate >= MAX_DURATION);
    }


    private boolean shouldShowAvatar(MessageItem messageItem, int position) {
        if (position == 0) {
            return true;
        }

        MessageItem messageItem2 = getItem(position - 1);

        if (messagesFromDifferentPeople(messageItem, messageItem2)) {
            // If the messages are from different people, then we don't care about any of the other checks,
            // we need to show the avatar/timestamp. This is used for group chats, which is why we want
            // both to be incoming messages
            return true;
        } else {
            int MAX_DURATION = 60 * 60 * 1000;
            return (messageItem.getBoxId() != messageItem2.getBoxId() || messageItem.mDate - messageItem2.mDate >= MAX_DURATION);
        }
    }

    private boolean messagesFromDifferentPeople(MessageItem a, MessageItem b) {
        return (a.mAddress != null && b.mAddress != null &&
                !a.mAddress.equals(b.mAddress) &&
                !a.isOutgoingMessage() && !b.isOutgoingMessage());
    }

    private int getBubbleBackgroundResource(boolean showAvatar, boolean isMine) {
        if (showAvatar && isMine) return ThemeManager.getSentBubbleRes();
        else if (showAvatar && !isMine) return ThemeManager.getReceivedBubbleRes();
        else if (!showAvatar && isMine) return ThemeManager.getSentBubbleAltRes();
        else if (!showAvatar && !isMine) return ThemeManager.getReceivedBubbleAltRes();
        else return -1;
    }

    private void bindGrouping(MessageListViewHolder holder, MessageItem messageItem, int position) {

        boolean showAvatar = shouldShowAvatar(messageItem, position);
        boolean showTimestamp = shouldShowTimestamp(messageItem, position);

        holder.mLayoutDateView.setVisibility(showTimestamp ? View.VISIBLE : View.GONE);
        holder.mSpace.setVisibility(showAvatar ? View.VISIBLE : View.GONE);

        holder.mBodyTextView.setBackgroundResource(getBubbleBackgroundResource(showAvatar, messageItem.isMe()));

        if (messageItem.isMe()) {
            holder.mBodyTextView.getBackground().setColorFilter(ThemeManager.getSentBubbleColor(), PorterDuff.Mode.SRC_ATOP);
        } else {
            holder.mBodyTextView.getBackground().setColorFilter(ThemeManager.getReceivedBubbleColor(), PorterDuff.Mode.SRC_ATOP);
        }

        if (messageItem.isMe())
            holder.mAvatarView.setVisibility(View.GONE);
        else holder.mAvatarView.setVisibility(showAvatar ? View.VISIBLE : View.GONE);
        /*holder.setLiveViewCallback(key -> {
            if (messageItem.isMe()) {
                holder.mBodyTextView.getBackground().setColorFilter(ThemeManager.getSentBubbleColor(), PorterDuff.Mode.SRC_ATOP);
            } else {
                holder.mBodyTextView.getBackground().setColorFilter(ThemeManager.getReceivedBubbleColor(), PorterDuff.Mode.SRC_ATOP);
            }
        });

        if (messageItem.isMe() && !mPrefs.getBoolean(SettingsFragment.HIDE_AVATAR_SENT, true)) {
            holder.mAvatarView.setVisibility(showAvatar ? View.VISIBLE : View.GONE);
        } else if (!messageItem.isMe() && !mPrefs.getBoolean(SettingsFragment.HIDE_AVATAR_RECEIVED, false)) {
            holder.mAvatarView.setVisibility(showAvatar ? View.VISIBLE : View.GONE);
        }*/
    }

    private void bindBody(final MessageListViewHolder holder, MessageItem messageItem) {
        holder.mBodyTextView.setAutoLinkMask(0);
        SpannableStringBuilder buf = new SpannableStringBuilder();

        String body = messageItem.mBody;

        /*if (messageItem.mMessageType == PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND) {
            String msgSizeText = mContext.getString(R.string.message_size_label)
                    + String.valueOf((messageItem.mMessageSize + 1023) / 1024)
                    + mContext.getString(R.string.kilobyte);

            body = msgSizeText;
        }*/

        // Cleanse the subject
//        String subject = MessageUtils.cleanseMmsSubject(mContext, messageItem.mSubject, body);
//        boolean hasSubject = !TextUtils.isEmpty(subject);
//        if (hasSubject) {
//            buf.append(mContext.getResources().getString(R.string.inline_subject, subject));
//        }

        if (!TextUtils.isEmpty(body)) {
//            if (mPrefs.getBoolean(SettingsFragment.AUTO_EMOJI, false)) {
//            body = EmojiRegistry.parseEmojis(body);
//            }

            buf.append(body);
        }

        if (messageItem.mHighlight != null) {
            Matcher m = messageItem.mHighlight.matcher(buf.toString());
            while (m.find()) {
                buf.setSpan(new StyleSpan(Typeface.BOLD), m.start(), m.end(), 0);
            }
        }

        //EmojiHandler.addEmojis(mContext, buf, 80);

        if (!TextUtils.isEmpty(buf)) {
            holder.mBodyTextView.setText(buf);
//            Matcher matcher = urlPattern.matcher(holder.mBodyTextView.getText());
//            if (matcher.find()) { //only find the image to the first link
//                int matchStart = matcher.start(1);
//                int matchEnd = matcher.end();
//                final String imageUrl = buf.subSequence(matchStart, matchEnd).toString();
//                Ion.with(mContext).load(imageUrl).withBitmap().asBitmap().setCallback(new FutureCallback<Bitmap>() {
//                    @Override
//                    public void onCompleted(Exception e, Bitmap result) {
//                        try {
//                            try {
//                                holder.mImageView.setImageBitmap(result);
//                                holder.mImageView.setVisibility(View.VISIBLE);
//                            } catch (java.lang.OutOfMemoryError ex) {
//                                Logger.error(TAG, "setImage: out of memory: ", ex);
//                            }
//
//                            holder.mImageView.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl));
//                                    mContext.startActivity(i);
//                                }
//                            });
//
//                        } catch (NullPointerException imageException) {
//                            imageException.printStackTrace();
//                        }
//                    }
//                });
//            }
//            LinkifyUtils.addLinks(holder.mBodyTextView);
        }
        holder.mBodyTextView.setVisibility(TextUtils.isEmpty(buf) ? View.GONE : View.VISIBLE);
        holder.mBodyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.mRoot.callOnClick();
            }
        });
        holder.mBodyTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                holder.mRoot.performLongClick();
                return false;
            }
        });
    }

    private void bindTimestamp(MessageListViewHolder holder, MessageItem messageItem) {
        String timestamp;

//        if (messageItem.isSending()) {
//            timestamp = mContext.getString(R.string.status_sending);
//        } else
        if (messageItem.mTimestamp != null && !messageItem.mTimestamp.equals("")) {
            timestamp = messageItem.mTimestamp;
        } else if (messageItem.isOutgoingMessage() && messageItem.isFailedMessage()) {
            timestamp = mContext.getResources().getString(R.string.status_failed);
        } else {
            timestamp = "";
        }

        holder.mDateView.setText(timestamp);

        /*if (!mIsGroupConversation || messageItem.isMe() || TextUtils.isEmpty(messageItem.mContact)) {
            holder.mDateView.setText(timestamp);
        } else {
            holder.mDateView.setText(mContext.getString(R.string.message_timestamp_format, timestamp, messageItem.mContact));
        }*/

    }

    private void bindAvatar(final MessageListViewHolder holder, MessageItem messageItem) {
        if (!messageItem.isMe()) {
            Contact contact = Utility.getContactDisplayNameByNumber(messageItem.mAddress);
            Utility.loadAvatar(mContext, holder.mAvatarView, contact);
        }
    }

    private void bindMmsView(final MessageListViewHolder holder, MessageItem messageItem) {
        int resource = EmojiHandler.addEmojis(mContext, messageItem.mBody);

        if(resource > 0)
        {
            holder.showMmsView(true);
            holder.mBodyTextView.setVisibility(View.GONE);
            holder.mImageView.setImageResource(resource);
        }else{
            holder.showMmsView(false);
        }
        if (messageItem.isSms()) {
//            holder.showMmsView(false);
            //messageItem.setOnPduLoaded(null);
        } else {
            /*if (messageItem.mAttachmentType != SmsHelper.TEXT) {
                if (holder.mImageView == null) {
                    holder.setImage(null, null);
                }
                setImageViewOnClickListener(holder, messageItem);
                drawPlaybackButton(holder, messageItem);
            } else {
                holder.showMmsView(false);
            }*/

            /*if (messageItem.mSlideshow == null) {
                messageItem.setOnPduLoaded(messageItem1 -> {
                    if (mCursor == null) {
                        // The pdu has probably loaded after shutting down the fragment. Don't try to bind anything now
                        return;
                    }
                    if (messageItem1 != null && messageItem1.getMessageId() == messageItem1.getMessageId()) {
                        messageItem1.setCachedFormattedMessage(null);
                        bindGrouping(holder, messageItem);
                        bindBody(holder, messageItem);
                        bindTimestamp(holder, messageItem);
                        bindAvatar(holder, messageItem);
                        bindMmsView(holder, messageItem);
                        bindIndicators(holder, messageItem);
                        bindVcard(holder, messageItem);
                    }
                });
            } else {
                if (holder.mPresenter == null) {
                    holder.mPresenter = new MmsThumbnailPresenter(mContext, holder, messageItem.mSlideshow);
                } else {
                    holder.mPresenter.setModel(messageItem.mSlideshow);
                    holder.mPresenter.setView(holder);
                }
                if (holder.mImageLoadedCallback == null) {
                    holder.mImageLoadedCallback = new MessageListViewHolder.ImageLoadedCallback(holder);
                } else {
                    holder.mImageLoadedCallback.reset(holder);
                }
                holder.mPresenter.present(holder.mImageLoadedCallback);
            }*/
        }
    }

    private void bindIndicators(MessageListViewHolder holder, MessageItem messageItem) {
        // Locked icon
        if (messageItem.mLocked) {
            holder.mLockedIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.mLockedIndicator.setVisibility(View.GONE);
        }

        // Delivery icon - we can show a failed icon for both sms and mms, but for an actual
        // delivery, we only show the icon for sms. We don't have the information here in mms to
        // know whether the message has been delivered. For mms, msgItem.mDeliveryStatus set
        // to MessageItem.DeliveryStatus.RECEIVED simply means the setting requesting a
        // delivery report was turned on when the message was sent. Yes, it's confusing!
        if ((messageItem.isOutgoingMessage() && messageItem.isFailedMessage()) ||
                messageItem.mDeliveryStatus == MessageItem.DeliveryStatus.FAILED) {
            holder.mDeliveredIndicator.setVisibility(View.VISIBLE);
        } else if (messageItem.isSms() &&
                messageItem.mDeliveryStatus == MessageItem.DeliveryStatus.RECEIVED) {
            holder.mDeliveredIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.mDeliveredIndicator.setVisibility(View.GONE);
        }

        // Message details icon - this icon is shown both for sms and mms messages. For mms,
        // we show the icon if the read report or delivery report setting was set when the
        // message was sent. Showing the icon tells the user there's more information
        // by selecting the "View report" menu.
        if (messageItem.mDeliveryStatus == MessageItem.DeliveryStatus.INFO) {
            holder.mDetailsIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.mDetailsIndicator.setVisibility(View.GONE);
        }
    }

    /*private void bindVcard(MessageListViewHolder holder, MessageItem messageItem) {
        if (!ContentType.TEXT_VCARD.equals(messageItem.mTextContentType)) {
            return;
        }

        VCard vCard = Ezvcard.parse(messageItem.mBody).first();

        SpannableString name = new SpannableString(vCard.getFormattedName().getValue());
        name.setSpan(new UnderlineSpan(), 0, name.length(), 0);
        holder.mBodyTextView.setText(name);
    }*/

    /*private void setImageViewOnClickListener(MessageListViewHolder holder, final MessageItem msgItem) {
        if (holder.mImageView != null) {
            switch (msgItem.mAttachmentType) {
                case SmsHelper.IMAGE:
                case SmsHelper.VIDEO:
                    holder.mImageView.setOnClickListener(holder);
                    holder.mImageView.setOnLongClickListener(holder);
                    break;

                default:
                    holder.mImageView.setOnClickListener(null);
                    break;
            }
        }
    }*/

    /*private void drawPlaybackButton(MessageListViewHolder holder, MessageItem msgItem) {
        if (holder.mSlideShowButton != null) {
            switch (msgItem.mAttachmentType) {
                case SmsHelper.SLIDESHOW:
                case SmsHelper.AUDIO:
                case SmsHelper.VIDEO:
                    // Show the 'Play' button and bind message info on it.
                    holder.mSlideShowButton.setTag(msgItem);
                    // Set call-back for the 'Play' button.
                    holder.mSlideShowButton.setOnClickListener(holder);
                    holder.mSlideShowButton.setVisibility(View.VISIBLE);
                    break;
                default:
                    holder.mSlideShowButton.setVisibility(View.GONE);
                    break;
            }
        }
    }*/

    @Override
    public void changeArrayList(ArrayList<Message> messages) {
        if (messages != null)
            mMessageItemCache = new MessageItemCache(mContext, mSearchHighlighter, MessageColumns.CACHE_SIZE);

        super.changeArrayList(messages);
    }

    @Override
    public int getItemViewType(int position) {
        // This method shouldn't be called if our cursor is null, since the framework should know
        // that there aren't any items to look at in that case
        MessageItem item = getItem(position);
        int boxId = item.getBoxId();

        if (item.isSms()) {
            if (boxId == 0) {
                return INCOMING_ITEM;
            } else {
                return OUTGOING_ITEM;
            }
        } else {
            if (boxId == 0) {
                return INCOMING_ITEM;
            } else {
                return OUTGOING_ITEM;
            }
        }
    }

}
