package com.ccsidd.rtone.message.messagelist;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.message.base.ClickyViewHolder;
import com.ccsidd.rtone.message.view.AvatarView;
import com.ccsidd.rtone.message.view.CollapsableLinearLayout;
import com.ccsidd.rtone.message.view.QKTextView;

import pl.droidsonroids.gif.GifImageView;

public class MessageListViewHolder extends ClickyViewHolder<MessageItem> {
    private final String TAG = "MessageListViewHolder";

    // Views
    protected View mRoot;
    protected QKTextView mBodyTextView;
    protected CollapsableLinearLayout mLayoutDateView;
    protected QKTextView mDateView;
    protected ImageView mLockedIndicator;
    protected ImageView mDeliveredIndicator;
    protected ImageView mDetailsIndicator;
    protected AvatarView mAvatarView;
    protected LinearLayout mMessageBlock;
    protected View mSpace;
    protected FrameLayout mMmsView;
    protected GifImageView mImageView;
//    protected ImageButton mSlideShowButton;
//    protected Button mDownloadButton;
    /*protected QKTextView mDownloadingLabel;

    protected ImageLoadedCallback mImageLoadedCallback;
    protected Presenter mPresenter;*/

    public MessageListViewHolder(Context context, View view) {
        super(context, view);

        mRoot = view;
        mBodyTextView = (QKTextView) view.findViewById(R.id.text_view);
        mLayoutDateView = (CollapsableLinearLayout) view.findViewById(R.id.layout_date_view);
        mDateView = (QKTextView) view.findViewById(R.id.date_view);
        mLockedIndicator = (ImageView) view.findViewById(R.id.locked_indicator);
        mDeliveredIndicator = (ImageView) view.findViewById(R.id.delivered_indicator);
        mDetailsIndicator = (ImageView) view.findViewById(R.id.details_indicator);
        mAvatarView = (AvatarView) view.findViewById(R.id.avatar);
        mMessageBlock = (LinearLayout) view.findViewById(R.id.message_block);
        mSpace = view.findViewById(R.id.space);
        mMmsView = (FrameLayout) view.findViewById(R.id.mms_view);
        mImageView = (GifImageView) view.findViewById(R.id.image_view);
//        mSlideShowButton = (ImageButton) view.findViewById(R.id.play_slideshow_button);
    }

    protected void showMmsView(boolean visible) {
        mMmsView.setVisibility(visible ? View.VISIBLE : View.GONE);
        mImageView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /*protected void inflateDownloadControls() {
        if (mDownloadButton == null) {
            mRoot.findViewById(R.id.mms_downloading_view_stub).setVisibility(View.VISIBLE);
            mDownloadButton = (Button) mRoot.findViewById(R.id.btn_download_msg);
            mDownloadingLabel = (QKTextView) mRoot.findViewById(R.id.label_downloading);
        }
    }

    protected void setLiveViewCallback(LiveView liveViewCallback) {
        LiveViewManager.registerView(QKPreference.THEME, this, liveViewCallback);
    }*/

    //@Override
    public void setImage(Bitmap bitmap) {
        if (bitmap == null) {
            showMmsView(false);
        } else {
            showMmsView(true);

            try {
                mImageView.setImageBitmap(bitmap);
                mImageView.setVisibility(View.VISIBLE);
            } catch (java.lang.OutOfMemoryError e) {
                Log.e(TAG, "setImage: out of memory: ", e);
            }
        }
    }

    /*@Override
    public void setImageRegionFit(String fit) {

    }

    @Override
    public void setImageVisibility(boolean visible) {

    }

    @Override
    public void setVideo(String name, Uri video) {

    }

    @Override
    public void setVideoThumbnail(String name, Bitmap bitmap) {
        showMmsView(true);

        try {
            mImageView.setImageBitmap(bitmap);
            mImageView.setVisibility(View.VISIBLE);
        } catch (java.lang.OutOfMemoryError e) {
            Log.e(TAG, "setVideo: out of memory: ", e);
        }
    }

    @Override
    public void setVideoVisibility(boolean visible) {

    }

    @Override
    public void startVideo() {

    }

    @Override
    public void stopVideo() {

    }

    @Override
    public void pauseVideo() {

    }

    @Override
    public void seekVideo(int seekTo) {

    }

    @Override
    public void setAudio(Uri audio, String name, Map<String, ?> extras) {

    }

    @Override
    public void startAudio() {

    }

    @Override
    public void stopAudio() {

    }

    @Override
    public void pauseAudio() {

    }

    @Override
    public void seekAudio(int seekTo) {

    }

    @Override
    public void setText(String name, String text) {

    }

    @Override
    public void setTextVisibility(boolean visible) {

    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setVisibility(boolean visible) {

    }

    static protected class ImageLoadedCallback implements ItemLoadedCallback<ThumbnailManager.ImageLoaded> {
        private long mMessageId;
        private final MessageListViewHolder mListItem;

        public ImageLoadedCallback(MessageListViewHolder listItem) {
            mListItem = listItem;
            mListItem.setImage(null, null);
            mMessageId = listItem.mData.getMessageId();
        }

        public void reset(MessageListViewHolder listItem) {
            mMessageId = listItem.mData.getMessageId();
        }

        public void onItemLoaded(ThumbnailManager.ImageLoaded imageLoaded, Throwable exception) {
            // Make sure we're still pointing to the same message. The list item could have // been recycled.
            MessageItem msgItem = mListItem.mData;
            if (msgItem != null && msgItem.getMessageId() == mMessageId) {
                if (imageLoaded.mIsVideo) {
                    mListItem.setVideoThumbnail(null, imageLoaded.mBitmap);
                } else {
                    mListItem.setImage(null, imageLoaded.mBitmap);
                }
            }
        }
    }*/
}
