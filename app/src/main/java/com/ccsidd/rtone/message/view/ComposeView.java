package com.ccsidd.rtone.message.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.listeners.RecipientProvider;
import com.ccsidd.rtone.message.ThemeManager;
import com.ccsidd.rtone.message.emoji.EmojiEditText;
import com.ccsidd.rtone.message.emoji.EmojiPopup;
import com.ccsidd.rtone.message.emoji.EmojiStickerPopup;
import com.ccsidd.rtone.message.emoji.emoji.Emoji;
import com.ccsidd.rtone.message.emoji.listeners.OnEmojiBackspaceClickListener;
import com.ccsidd.rtone.message.emoji.listeners.OnEmojiClickedListener;
import com.ccsidd.rtone.message.emoji.listeners.OnEmojiPopupDismissListener;
import com.ccsidd.rtone.message.emoji.listeners.OnEmojiPopupShownListener;
import com.ccsidd.rtone.message.emoji.listeners.OnSoftKeyboardCloseListener;
import com.ccsidd.rtone.message.emoji.listeners.OnSoftKeyboardOpenListener;
import com.ccsidd.rtone.message.interfaces.ActivityLauncher;
import com.ccsidd.rtone.objects.Conversation;
import com.ccsidd.rtone.objects.Message;
import com.ccsidd.rtone.services.SipServiceCommand;
import com.ccsidd.rtone.utilities.GlobalVars;
import com.ccsidd.rtone.utilities.Utility;
import com.ccsidd.rtone.view.contacteditText.Recipient;
import com.github.lzyzsd.circleprogress.DonutProgress;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;

public class ComposeView extends LinearLayout implements View.OnClickListener {
    public final static String TAG = "ComposeView";
//    private static final int REQUEST_CODE_IMAGE = 0x00F1;
//    private static final int REQUEST_CODE_CAMERA = 0x00F2;
//    private static final int MIN_KEYBOARD_HEIGHT = 100;
//    private final String KEY_DELAYED_INFO_DIALOG_SHOWN = "delayed_info_dialog_shown";
    private final int ANIMATION_DURATION = 300;

    //    private Conversation mConversation;
//    private ConversationLegacy mConversationLegacy;
    private Context mContext;
    //    private SharedPreferences mPrefs;
    private Resources mRes;
    private ActivityLauncher mActivityLauncher;
    private OnSendListener mOnSendListener;
    private RecipientProvider mRecipientProvider;
    // Analytics
    // This string is sent along to events that happen in ComposeView, so that we know where they're
    // happening (i.e. QKReply, QKCompose, etc)
    private String mLabel;
    // Views
    private EmojiEditText mReplyText;
    private FrameLayout mButtonCompose;
    private FrameLayout mButtonEmoji;

//    private CollapsableLinearLayout mlayout_reply_emoji_compose;
//    private DonutProgress mProgress;
    private ImageView mButtonBackground;
    //    private ImageView mEmojiButtonBackground;
    private ImageView mComposeIcon;
    //    private FrameLayout mAttachmentLayout;
//    private AttachmentImageView mAttachment;
//    private ImageButton mCancel;
    private ImageView mEmojiIcon;
//    private ImageButton mEmojisSticker;
    //    private ImageButton mAttach;
//    private ImageButton mCamera;
//    private ImageButton mDelay;
    private View mAttachmentPanel;
    private QKTextView mLetterCount;
    // State
    private boolean mDelayedMessagingEnabled;
    private boolean mSendingCancelled;
    private boolean mIsSendingBlocked;
    private String mSendingBlockedMessage;
    private String mCurrentPhotoPath;
//    private ValueAnimator mProgressAnimator;
    private int mDelayDuration = 3000;
    private SendButtonState mButtonState = SendButtonState.ATTACH;
    private String mRecipient;
    private View rootView;
    private EmojiStickerPopup emojiStickerPopup;
//    private EmojiPopup emojiPopup;

    public ComposeView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ComposeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mContext = context;
//        mPrefs = mContext.getPrefs();
        mRes = mContext.getResources();

        rootView = this;

        mDelayedMessagingEnabled = false;
        try {
            mDelayDuration = 3;
            if (mDelayDuration < 1) {
                mDelayDuration = 1;
            } else if (mDelayDuration > 30) {
                mDelayDuration = 30;
            }
            mDelayDuration *= 1000;
        } catch (Exception e) {
            mDelayDuration = 3000;
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                true);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        // Get references to the views
        mReplyText = (EmojiEditText) findViewById(R.id.compose_reply_text);
        mButtonCompose = (FrameLayout) findViewById(R.id.compose_button);
        mButtonEmoji = (FrameLayout) findViewById(R.id.emoji_button);
//        mProgress = (DonutProgress) findViewById(R.id.progress);
        mButtonBackground = (ImageView) findViewById(R.id.compose_button_background);
//        mEmojiButtonBackground = (ImageView) findViewById(R.id.emoji_button_background);
        mComposeIcon = (ImageView) findViewById(R.id.compose_icon);
        mEmojiIcon = (ImageView) findViewById(R.id.emoji_icon);
//        mEmojisSticker = (ImageButton) findViewById(R.id.emojis_sticker);
        mAttachmentPanel = findViewById(R.id.attachment_panel);
//        mAttach = (ImageButton) findViewById(R.id.attach);
//        mCamera = (ImageButton) findViewById(R.id.camera);
//        mDelay = (ImageButton) findViewById(R.id.delay);
        mLetterCount = (QKTextView) findViewById(R.id.compose_letter_count);
//        mAttachmentLayout = (FrameLayout) findViewById(R.id.attachment);
//        mAttachment = (AttachmentImageView) findViewById(R.id.compose_attachment);
//        mCancel = (ImageButton) findViewById(R.id.cancel);

        mButtonCompose.setOnClickListener(this);
        mButtonEmoji.setOnClickListener(this);
//        mEmojisSticker.setOnClickListener(this);

//        mlayout_reply_emoji_compose = (CollapsableLinearLayout) findViewById(R.id.layout_reply_emoji_compose);
//
//        mAttachmentPanel.getLayoutParams().height = height;

//        mAttach.setOnClickListener(this);
//        mCamera.setOnClickListener(this);
//        mCancel.setOnClickListener(this);
//        mDelay.setOnClickListener(this);

        mButtonBackground.setColorFilter(ThemeManager.getSentBubbleColor(), PorterDuff.Mode.SRC_ATOP);
        mButtonBackground.setAlpha(0.1f);

//        mEmojiButtonBackground.setColorFilter(ThemeManager.getSentBubbleColor(), PorterDuff.Mode.SRC_ATOP);
//        mEmojiButtonBackground.setAlpha(1f);

        mEmojiIcon.setColorFilter(ThemeManager.getSentBubbleColor(), PorterDuff.Mode.SRC_ATOP);
        mComposeIcon.setColorFilter(ThemeManager.getTextOnColorPrimary(), PorterDuff.Mode.SRC_ATOP);

//        mAttachmentPanel.setBackgroundColor(ThemeManager.getColor());

        /*LiveViewManager.registerView(QKPreference.THEME, this, key -> {
            mButtonBackground.setColorFilter(ThemeManager.getColor(), PorterDuff.Mode.SRC_ATOP);
            mComposeIcon.setColorFilter(ThemeManager.getTextOnColorPrimary(), PorterDuff.Mode.SRC_ATOP);
            mAttachmentPanel.setBackgroundColor(ThemeManager.getColor());
            mAttach.setColorFilter(ThemeManager.getTextOnColorPrimary(), PorterDuff.Mode.SRC_ATOP);
            mCamera.setColorFilter(ThemeManager.getTextOnColorPrimary(), PorterDuff.Mode.SRC_ATOP);
            updateDelayButton();
            mProgress.setUnfinishedStrokeColor(ThemeManager.getTextOnColorSecondary());
            mProgress.setFinishedStrokeColor(ThemeManager.getTextOnColorPrimary());
            if (ThemeManager.getSentBubbleRes() != 0) mReplyText.setBackgroundResource(ThemeManager.getSentBubbleRes());
        });

        LiveViewManager.registerView(QKPreference.BACKGROUND, this, key -> {
            mReplyText.getBackground().setColorFilter(ThemeManager.getNeutralBubbleColor(), PorterDuff.Mode.SRC_ATOP);
            getBackground().setColorFilter(ThemeManager.getBackgroundColor(), PorterDuff.Mode.SRC_ATOP);
        });*/

        // There is an option for using the return button instead of the emoticon button in the
        // keyboard; set that up here.
        switch (/*Integer.parseInt(mPrefs.getString("SettingsFragment.ENTER_BUTTON", "0"))*/ 2) {
            case 0: // emoji
                break;
            case 1: // new line
                mReplyText.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES |
                        InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE);
                mReplyText.setSingleLine(false);
                break;
            case 2: // send
                mReplyText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                mReplyText.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                mReplyText.setSingleLine(false);
                mReplyText.setOnKeyListener(new OnKeyListener() { //Workaround because ACTION_SEND does not support multiline mode
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (keyCode == 66) {
                            sendSms();
                            return true;
                        }
                        return false;
                    }
                });
                break;
        }

        mReplyText.setTextChangedListener(new QKEditText.TextChangedListener() {
            @Override
            public void onTextChanged(CharSequence s) {
                int length = s.length();

                updateButtonState(length);

                // If the reply is within 10 characters of the SMS limit (160), it will start counting down
                // If the reply exceeds the SMS limit, it will count down until an extra message will have to be sent, and shows how many messages will currently be sent
                /*if (length < 150) {
                    mLetterCount.setText("");
                } else if (150 <= length && length <= 160) {
                    mLetterCount.setText("" + (160 - length));
                } else if (160 < length) {
                    mLetterCount.setText((160 - length % 160) + "/" + (length / 160 + 1));
                }*/
            }
        });
        /*mReplyText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (v == mReplyText) {
                    if (hasFocus) {
                        dismisSticker();
                        updateButtonState();
                    }
                }
            }
        });*/

        mReplyText.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
//                dismisEmoji();
                dismisSticker();
                return false;
            }
        });

        mReplyText.setMaxLines(3);

//        mProgressAnimator = new ValueAnimator();
//        mProgressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
//        mProgressAnimator.setDuration(mDelayDuration);
//        mProgressAnimator.setIntValues(0, 360);
//        mProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                mProgress.setProgress((int) animation.getAnimatedValue());
//            }
//        });
//        mProgressAnimator.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                super.onAnimationEnd(animation);
//                mProgress.setVisibility(INVISIBLE);
//                mProgress.setProgress(0);
//
////                if (!mSendingCancelled) {
////                    sendSms();
//                // In case they only enabled it for a particular message, let's set it back to the pref value
//                //mDelayedMessagingEnabled = mPrefs.getBoolean("SettingsFragment.DELAYED", false);
////                    updateDelayButton();
////                } else {
////                    mSendingCancelled = false;
//                updateButtonState();
////                }
//            }
//        });

//        setUpEmojiPopup();
        setUpEmojiStickerPopup();
    }

    /**
     * Sets the ActivityLauncher. This can be an Activity, a Fragment, or in general something that
     * implements startActivityForResult(Intent, int), and onActivityResult(int, int, Intent); this
     * instance must be able to launch and get results for activties.
     * <p/>
     * Additionally, in the onActivityResult(int, int, Intent) method, the ActivityLauncher instance
     * should pass along that result value to this ComposeFragment, using its own onActivityResult
     * method.
     *
     * @param launcher
     */
    public void setActivityLauncher(ActivityLauncher launcher) {
        mActivityLauncher = launcher;
    }

    /**
     * Sets a listener to be pinged when an SMS message is sent.
     *
     * @param l
     */
    public void setOnSendListener(OnSendListener l) {
        mOnSendListener = l;
    }

    /**
     * Sets a RecipientProvider. The RecipientProvider provides one method, getRecipientAddresses,
     * which returns a String[] of recipient addresses. This method will be called when we're trying
     * to send an SMS/MMS message, and onOpenConversation has NOT been called with a non-null
     * Conversation object, i.e. we cannot use the Conversation object to get recipient addresses.
     *
     * @param p
     */
    public void setRecipientProvider(RecipientProvider p) {
        mRecipientProvider = p;
    }

    /**
     * Handles activity results that were started by this View. Returns true if the result was
     * handled by this view, false otherwise.
     * <p>
     * //     * @param requestCode
     * //     * @param resultCode
     * //     * @param data
     */

    public void setRootView(View rootView) {
        this.rootView = rootView;
    }

    /*public boolean onActivityResult(int requestCode, int resultCode, final Intent data) {
        boolean result = false;

        if (requestCode == REQUEST_CODE_IMAGE && resultCode == Activity.RESULT_OK) {
            result = true;

            Toast.makeText(mContext, R.string.compose_loading_attachment, Toast.LENGTH_LONG).show();
            new ImageLoaderTask(mContext, data.getData()).execute();
        } else if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK) {
            result = true;
            Toast.makeText(mContext, R.string.compose_loading_attachment, Toast.LENGTH_LONG).show();
            new ImageLoaderFromCameraTask().execute((Void[]) null);
        }

        return result;
    }*/
    private void updateButtonState() {
        updateButtonState(mReplyText == null ? 0 : mReplyText.getText().length());
    }

    /**
     * Sets the button image based on the length of the reply text, and whether or not the drawable
     * is set.
     */
    private void updateButtonState(int length) {
        SendButtonState buttonState;

        if (mAttachmentPanel.getVisibility() == View.VISIBLE) {
            buttonState = SendButtonState.CLOSE;
        } else if (length > 0 /*|| mAttachment.hasAttachment()*/) {
            buttonState = SendButtonState.SEND;
            mButtonBackground.setAlpha(1f);
        } else {
            buttonState = SendButtonState.ATTACH;
            mButtonBackground.setAlpha(0.05f);
        }

        updateButtonState(buttonState);
    }

    private void updateButtonState(SendButtonState buttonState) {
        if (mButtonState != buttonState) {

            // Check if we need to switch animations
            AnimationDrawable animation = null;
            if (buttonState == SendButtonState.SEND) {
                animation = (AnimationDrawable) ContextCompat.getDrawable(mContext, R.drawable.plus_to_arrow);
            } else if (mButtonState == SendButtonState.SEND) {
                animation = (AnimationDrawable) ContextCompat.getDrawable(mContext, R.drawable.arrow_to_plus);
            }
            if (animation != null) {
                mComposeIcon.setImageDrawable(animation);
                animation.start();
            }

            // Handle any necessary rotation
            float rotation = mComposeIcon.getRotation();
            float target = buttonState == SendButtonState.ATTACH || buttonState == SendButtonState.SEND ? 0 : 45;
            ObjectAnimator.ofFloat(mComposeIcon, "rotation", rotation, target)
                    .setDuration(ANIMATION_DURATION)
                    .start();

            mButtonState = buttonState;
        }
    }

    /**
     * Sets the text of the Reply edit text.
     *
     * @param text
     */
    public void setText(String text) {
        mReplyText.setText(text);
        mReplyText.setSelection(mReplyText.getText().length());
    }

    public void setSendingUnblocked() {
        mSendingBlockedMessage = null;
        mIsSendingBlocked = false;
    }

    public void setSendingBlocked(String message) {
        mSendingBlockedMessage = message;
        mIsSendingBlocked = true;
    }

    /**
     * Requests focus to the Reply edit text.
     */
    public void requestReplyTextFocus() {
        mReplyText.requestFocus();
    }

    public void sendSms() {
        String body = mReplyText.getText().toString();

//        final Drawable attachment;
//        if (mAttachment.hasAttachment()) {
//            attachment = mAttachment.getDrawable();
//        } else {
//            attachment = null;
//        }

        Recipient[] recipients = null;
        if (mRecipientProvider != null) {
            recipients = mRecipientProvider.getRecipientAddresses();
        }
//
//        // If we have some recipients, send the message!
        if (recipients != null && recipients.length > 0) {
            clearAttachment();

            mReplyText.setText("");
            if (!body.equals("")) {
                Calendar cal = Calendar.getInstance();
                for (Recipient recipient : recipients) {
                    final Message message = new Message();
                    message.setBody(body);
                    message.setType(1);
                    message.setPhoneNumber(recipient.getNumber());
                    message.setTime(cal.getTimeInMillis());
                    message.setUnRead(false);

                    final Conversation conversation = new Conversation();
                    conversation.setLastMessage(body);
                    conversation.setType(1);
                    conversation.setPhoneNumber(recipient.getNumber());
                    conversation.setTime(cal.getTimeInMillis());
                    conversation.setUnRead(false);

                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.copyToRealm(message);
                        }
                    });
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.copyToRealmOrUpdate(conversation);
                        }
                    });
                    realm.close();

                    String defaultAccount = Utility.getPref(mContext, GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_ACCOUNT_DEFAULT);
                    SipServiceCommand.addBuddy(mContext, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls",
                            "sip:" + message.getPhoneNumber() + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls");
                    SipServiceCommand.sendMessage(mContext, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls",
                            "sip:" + message.getPhoneNumber() + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls",
                            message.getBody());
                    // Notify the listener about the new text message
                    if (mOnSendListener != null) {
                        mOnSendListener.onSend(message);
                    }
                }
            }


//            AnalyticsManager.getInstance().sendEvent(
//                    AnalyticsManager.CATEGORY_MESSAGES,
//                    AnalyticsManager.ACTION_SEND_MESSAGE,
//                    mLabel
//            );
//
//            Transaction sendTransaction = new Transaction(mContext, SmsHelper.getSendSettings(mContext));
//
//            com.moez.QKSMS.mmssms.Message message = new com.moez.QKSMS.mmssms.Message(body, recipients);
//            message.setType(com.moez.QKSMS.mmssms.Message.TYPE_SMSMMS);
//            if (attachment != null) {
//                message.setImage(ImageUtils.drawableToBitmap(attachment));
//            }
//

//
//            long threadId = mConversation != null ? mConversation.getThreadId() : 0;
//            if (!message.toString().equals("")) {
//                sendTransaction.sendNewMessage(message, threadId);
//            }
//            NotificationManager.update(mContext);
//
//            if (mConversationLegacy != null) {
//                mConversationLegacy.markRead();
//            }

            // Reset the image button state
            updateButtonState();

            // Otherwise, show a toast to the user to prompt them to add recipients.
        } else {
            Toast.makeText(
                    mContext,
                    mRes.getString(R.string.error_no_recipients),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    public void sendSticker(String sticker) {

        Recipient[] recipients = null;
        if (mRecipientProvider != null) {
            recipients = mRecipientProvider.getRecipientAddresses();
        }
        // If we have some recipients, send the message!
        if (recipients != null && recipients.length > 0) {
            clearAttachment();

            if (!sticker.equals("")) {
                Calendar cal = Calendar.getInstance();
                for (Recipient recipient : recipients) {
                    final Message message = new Message();
                    message.setBody(sticker);
                    message.setType(1);
                    message.setPhoneNumber(recipient.getNumber());
                    message.setTime(cal.getTimeInMillis());
                    message.setUnRead(false);

                    final Conversation conversation = new Conversation();
                    conversation.setLastMessage(sticker);
                    conversation.setType(1);
                    conversation.setPhoneNumber(recipient.getNumber());
                    conversation.setTime(cal.getTimeInMillis());
                    conversation.setUnRead(false);

                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.copyToRealm(message);
                        }
                    });
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.copyToRealmOrUpdate(conversation);
                        }
                    });
                    realm.close();

                    String defaultAccount = Utility.getPref(mContext, GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_ACCOUNT_DEFAULT);
                    SipServiceCommand.addBuddy(mContext, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls",
                            "sip:" + message.getPhoneNumber() + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls");
                    SipServiceCommand.sendMessage(mContext, "sip:" + defaultAccount + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls",
                            "sip:" + message.getPhoneNumber() + "@" + GlobalVars.SERVER_IP_ADDRESS_TLS + ";transport=tls",
                            message.getBody());
                    // Notify the listener about the new text message
                    if (mOnSendListener != null) {
                        mOnSendListener.onSend(message);
                    }
                }
            }

            // Otherwise, show a toast to the user to prompt them to add recipients.
        } else {
            Toast.makeText(
                    mContext,
                    mRes.getString(R.string.error_no_recipients),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    public void dismisSticker() {
        if (emojiStickerPopup != null && emojiStickerPopup.isShowing()) {
            emojiStickerPopup.dismiss();
        }
    }

    /*public void dismisEmoji() {
        if (emojiPopup != null && emojiPopup.isShowing()) {
            emojiPopup.dismiss();
        }
    }*/

    public void removeOnGlobalLayout() {
        if (emojiStickerPopup != null) {
            emojiStickerPopup.removeOnGlobalLayout();
        }
    }

    /*private void setUpEmojiPopup() {
        emojiPopup = EmojiPopup.Builder.fromRootView(rootView).setOnEmojiBackspaceClickListener(new OnEmojiBackspaceClickListener() {
            @Override
            public void onEmojiBackspaceClicked(final View v) {
                Log.d("MainActivity", "Clicked on Backspace");
            }
        }).setOnEmojiClickedListener(new OnEmojiClickedListener() {
            @Override
            public void onEmojiClicked(final Emoji emoji) {
                Log.d("MainActivity", "Clicked on emoji");
            }
        }).setOnEmojiPopupShownListener(new OnEmojiPopupShownListener() {
            @Override
            public void onEmojiPopupShown() {
                mEmojiIcon.setImageResource(R.drawable.ic_keyboard_grey_500_36dp);
            }
        }).setOnSoftKeyboardOpenListener(new OnSoftKeyboardOpenListener() {
            @Override
            public void onKeyboardOpen(final int keyBoardHeight) {
                Log.d("MainActivity", "Opened soft keyboard");
            }
        }).setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
            @Override
            public void onEmojiPopupDismiss() {
                mEmojiIcon.setImageResource(R.drawable.emoji_people);
            }
        }).setOnSoftKeyboardCloseListener(new OnSoftKeyboardCloseListener() {
            @Override
            public void onKeyboardClose() {
                emojiPopup.dismiss();
            }
        }).build(mReplyText);
    }*/

    private void setUpEmojiStickerPopup() {
        emojiStickerPopup = EmojiStickerPopup.Builder.fromRootView(rootView).setOnEmojiBackspaceClickListener(new OnEmojiBackspaceClickListener() {
            @Override
            public void onEmojiBackspaceClicked(final View v) {
                Log.d("MainActivity", "Clicked on Backspace");
            }
        }).setOnEmojiClickedListener(new OnEmojiClickedListener() {
            @Override
            public void onEmojiClicked(final Emoji emoji) {
                Log.d("MainActivity", "Clicked on emoji");
                sendSticker(emoji.getEmoji());

            }
        }).setOnEmojiPopupShownListener(new OnEmojiPopupShownListener() {
            @Override
            public void onEmojiPopupShown() {
                mEmojiIcon.setImageResource(R.drawable.ic_keyboard_grey_500_36dp);
//                mlayout_reply_emoji_compose.collapse();

            }
        }).setOnSoftKeyboardOpenListener(new OnSoftKeyboardOpenListener() {
            @Override
            public void onKeyboardOpen(final int keyBoardHeight) {
                Log.d("MainActivity", "Opened soft keyboard");
            }
        }).setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
            @Override
            public void onEmojiPopupDismiss() {
                mEmojiIcon.setImageResource(R.drawable.emoji_people);
//                mlayout_reply_emoji_compose.expand();
            }
        }).setOnSoftKeyboardCloseListener(new OnSoftKeyboardCloseListener() {
            @Override
            public void onKeyboardClose() {
                emojiStickerPopup.dismiss();
            }
        }).build(mReplyText);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.compose_button:
                sendSms();
                break;
            case R.id.emoji_button:
                emojiStickerPopup.toggle();
                updateButtonState();
                break;
            /*case R.id.emojis_sticker:
                emojiStickerPopup.toggle();
                updateButtonState();
                break;*/
            /*case R.id.cancel:
                clearAttachment();
                break;*/

            /*case R.id.attach:
                if (hasSetupMms()) {
                    mAttachmentPanel.setVisibility(GONE);
                    updateButtonState();
                    chooseAttachmentFromGallery();
                }
                break;

            case R.id.camera:
                if (hasSetupMms()) {
                    mAttachmentPanel.setVisibility(GONE);
                    updateButtonState();
                    attachFromCamera();
                }
                break;

            case R.id.delay:
                if (!mPrefs.getBoolean(KEY_DELAYED_INFO_DIALOG_SHOWN, false) && !mDelayedMessagingEnabled) {
                    //showDelayedMessagingInfo();
                } else {
                    toggleDelayedMessaging();
                }
                break;*/
        }
    }

    private void toggleDelayedMessaging() {
        mDelayedMessagingEnabled = !mDelayedMessagingEnabled;
        //updateDelayButton();
        mAttachmentPanel.setVisibility(GONE);
        updateButtonState();
    }

    /*public void sendDelayedSms() {
        mProgress.setVisibility(VISIBLE);
        updateButtonState(SendButtonState.CANCEL);
        mProgressAnimator.start();
    }*/

    private void handleComposeButtonClick() {
        switch (mButtonState) {
            case ATTACH:
                mAttachmentPanel.setVisibility(VISIBLE);
                updateButtonState();
                break;

            case SEND:
                // If the API version is less than KitKat, they can send an SMS; so do this.
                if (Build.VERSION.SDK_INT < 19) {
                    if (mDelayedMessagingEnabled) {
                        //sendDelayedSms();
                    } else {
                        sendSms();
                    }
                } else {
                    // Otherwise... check if we're not the default SMS app
                    //boolean isDefaultSmsApp = Utils.isDefaultSmsApp(mContext);

                    // Now make sure that a client hasn't blocked sending, i.e. in the welcome
                    // screen when we have a demo conversation.
                    if (mIsSendingBlocked) {
                        // Show the sending blocked message (if it exists)
                        Toast.makeText(
                                mContext,
                                mSendingBlockedMessage,
                                Toast.LENGTH_SHORT
                        ).show();

                        //} else if (!isDefaultSmsApp) {
                        // Ask to become the default SMS app
                        //new DefaultSmsHelper(mContext, R.string.not_default_send).showIfNotDefault(this);

                    } else if (!TextUtils.isEmpty(mReplyText.getText()) /*|| mAttachment.hasAttachment()*/) {
                        if (mDelayedMessagingEnabled) {
                            //sendDelayedSms();
                        } else {
                            sendSms();
                        }
                    }
                }
                break;

            case CLOSE:
                mAttachmentPanel.setVisibility(GONE);
                updateButtonState();
                break;

            case CANCEL:
                mSendingCancelled = true;
//                mProgressAnimator.end();
                //updateButtonState();
                break;
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * If there's a pending delayed message, end the progress animation and go ahead with sending the message
     */
//    private void sendPendingDelayedMessage() {
//        if (mButtonState == SendButtonState.CANCEL && mProgressAnimator != null) {
//            mProgressAnimator.end();
//        }
//    }

    /*private void showDelayedMessagingInfo() {
        new QKDialog()
                .setContext(mContext)
                .setTitle(R.string.pref_delayed)
                .setMessage(R.string.delayed_messaging_info)
                .setNegativeButton(R.string.just_once, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggleDelayedMessaging();
                    }
                })
                .setPositiveButton(R.string.enable, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPrefs.edit().putBoolean(SettingsFragment.DELAYED, true).apply();
                        toggleDelayedMessaging();
                    }
                })
                .show();
        mPrefs.edit().putBoolean(KEY_DELAYED_INFO_DIALOG_SHOWN, true).apply(); //This should be changed, the dialog should be shown each time when delayed messaging is disabled.
    }*/

    /**
     * Loads message data from an intent. Currently supports text/plain and image/* ACTION_SEND
     * intents.
     *
     * @param intent The intent with the data to load.
     */
    public void loadMessageFromIntent(final Intent intent) {
        String type = intent == null ? null : intent.getType();

        if (intent != null) {

            if (type != null) {

                if ("text/plain".equals(type)) {
                    mReplyText.setText(intent.getStringExtra(Intent.EXTRA_TEXT));

                } else if (type.startsWith("image/")) {

                    Uri uri = intent.getData();

                    // If the Uri is null, try looking elsewhere for it. [1] [2]
                    // [1]: http://stackoverflow.com/questions/10386885/intent-filter-intent-getdata-returns-null
                    // [2]: http://developer.android.com/reference/android/content/Intent.html#ACTION_SEND
                    if (uri == null) {
                        Bundle extras = intent.getExtras();
                        if (extras != null) {
                            uri = (Uri) extras.get(Intent.EXTRA_STREAM);
                        }
                    }

                    //new ImageLoaderTask(mContext, uri).execute();

                    // If the Uri is still null here, throw the exception.
                    if (uri == null) {
                        // TODO show the user some kind of feedback
                    }
                }
            } else {
                if (intent.getExtras() != null) {
                    String body = intent.getExtras().getString("sms_body");
                    if (body != null) {
                        mReplyText.setText(body);
                    }
                }
            }
        }
    }

    /*private boolean hasSetupMms() {
        if (TextUtils.isEmpty(mPrefs.getString(SettingsFragment.MMSC_URL, ""))
                && TextUtils.isEmpty(mPrefs.getString(SettingsFragment.MMS_PROXY, ""))
                && TextUtils.isEmpty(mPrefs.getString(SettingsFragment.MMS_PORT, ""))) {

            // Not so fast! You need to set up MMS first.
            MMSSetupFragment f = new MMSSetupFragment();
            Bundle args = new Bundle();
            args.putBoolean(MMSSetupFragment.ARG_ASK_FIRST, true);
            f.setArguments(args);

            ((Activity) mContext).getFragmentManager()
                    .beginTransaction()
                    .add(f, MMSSetupFragment.TAG)
                    .commit();

            return false;
        }

        return true;
    }*/

    /*private void attachFromCamera() {

        AnalyticsManager.getInstance().sendEvent(
                AnalyticsManager.CATEGORY_MESSAGES,
                AnalyticsManager.ACTION_ATTACH_FROM_CAMERA,
                mLabel
        );

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(mContext.getPackageManager()) != null) {

            File file = null;
            try {
                file = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (file != null) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                mActivityLauncher.startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA);
            }
        } else {
            // Send a toast saying there was a camera error
            if (mContext != null) {
                String message = mContext.getResources().getString(R.string.attachment_camera_error);
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    /*private void chooseAttachmentFromGallery() {

        AnalyticsManager.getInstance().sendEvent(
                AnalyticsManager.CATEGORY_MESSAGES,
                AnalyticsManager.ACTION_ATTACH_IMAGE,
                mLabel
        );

        try {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image*//*");
            mActivityLauncher.startActivityForResult(photoPickerIntent, REQUEST_CODE_IMAGE);
        } catch (ActivityNotFoundException e) {
            // Send a toast saying no picture apps
            if (mContext != null) {
                String message = mContext.getResources().getString(R.string.attachment_app_not_found);
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    /**
     * Clears the image from the attachment view.
     */
    public void clearAttachment() {
//        mAttachment.setImageBitmap(null);
//        mAttachmentLayout.setVisibility(View.GONE);
        updateButtonState();
    }

    /**
     * Sets the conversation for this compose view. This will setup the ComposeView with drafts.
     *
     * @param conversationLegacy
     */
    /*public void onOpenConversation(Conversation conversation, ConversationLegacy conversationLegacy) {
        long threadId = mConversation != null ? mConversation.getThreadId() : -1;
        if (threadId > 0) sendPendingDelayedMessage();
        long newThreadId = conversation != null ? conversation.getThreadId() : -1;
        if (mConversation != null && mConversationLegacy != null && threadId != newThreadId) {
            // Save the old draft first before updating the conversation objects.
            saveDraft();
        }

        mConversation = conversation;
        mConversationLegacy = conversationLegacy;

        // If the conversation was different, set up the draft here.
        if (threadId != newThreadId || newThreadId == -1) {
            setupDraft();
        }
    }*/

    /**
     * Sets the image of the attachment view.
     * <p>
     * //     * @param imageBitmap the bitmap
     */
    /*public void setAttachment(Bitmap imageBitmap) {
        if (imageBitmap == null) {
            clearAttachment();
        } else {
            AnalyticsManager.getInstance().sendEvent(
                    AnalyticsManager.CATEGORY_MESSAGES,
                    AnalyticsManager.ACTION_ATTACH_IMAGE,
                    mLabel
            );

            mAttachment.setImageBitmap(imageBitmap);
            mAttachmentLayout.setVisibility(View.VISIBLE);
            updateButtonState();
        }
    }*/
    public void setLabel(String label) {
        mLabel = label;
    }

    /**
     * Saves a draft to the conversation.
     */
    /*public void saveDraft() {
        // If the conversation_reply view is null, then we won't worry about saving drafts at all. We also don't save
        // drafts if a message is about to be sent (delayed)
        if (mReplyText != null && mButtonState != SendButtonState.CANCEL) {
            String draft = mReplyText.getText().toString();

            if (mConversation != null) {
                if (mConversationLegacy.hasDraft() && TextUtils.isEmpty(draft)) {
                    mConversationLegacy.clearDrafts();

                } else if (!TextUtils.isEmpty(draft) &&
                        (!mConversationLegacy.hasDraft() || !draft.equals(mConversationLegacy.getDraft()))) {
                    mConversationLegacy.saveDraft(draft);
                }
            } else {
                // Only show the draft if we saved text, not if we just cleared some
                if (!TextUtils.isEmpty(draft)) {
                    if (mRecipientProvider != null) {
                        String[] addresses = mRecipientProvider.getRecipientAddresses();

                        if (addresses != null && addresses.length > 0) {
                            // save the message for each of the addresses
                            for (int i = 0; i < addresses.length; i++) {
                                ContentValues values = new ContentValues();
                                values.put("address", addresses[i]);
                                values.put("date", System.currentTimeMillis());
                                values.put("read", 1);
                                values.put("type", 4);

                                // attempt to create correct thread id
                                long threadId = Utils.getOrCreateThreadId(mContext, addresses[i]);

                                Log.v(TAG, "saving message with thread id: " + threadId);

                                values.put("thread_id", threadId);
                                Uri messageUri = mContext.getContentResolver().insert(Uri.parse("content://sms/draft"), values);

                                Log.v(TAG, "inserted to uri: " + messageUri);

//                                ConversationLegacy mConversationLegacy = new ConversationLegacy(mContext, threadId);
//                                mConversationLegacy.saveDraft(draft);
                            }
                        }
                    }
                }
            }
        }

    }*/

    /**
     * Displays the draft message to the user.
     */
    /*private void setupDraft() {
        if (mConversationLegacy != null) {
            if (mConversationLegacy.hasDraft()) {
                String text = mConversationLegacy.getDraft();

                mReplyText.setText(text);
                mReplyText.setSelection(text != null ? text.length() : 0);
                clearAttachment();
            } else {
                // Since this view can be reused, it's important to set the text to empty when there
                // isn't a new draft. Or else the previous conversation's draft can be carried on to
                // this new conversation.
                mReplyText.setText("");
                clearAttachment();
            }
        }
    }*/
    public String getRecipients() {
        return this.mRecipient;
    }

    public void setRecipients(String recipient) {
        mRecipient = recipient;
    }

    public boolean isReplyTextEmpty() {
        return TextUtils.isEmpty(mReplyText.getText());
    }

    enum SendButtonState {
        SEND, // send a messaage
        ATTACH, // open the attachment panel
        CLOSE, // close the attachment panel
        CANCEL // cancel a message while it's sending
    }

    /*private class ImageLoaderFromCameraTask extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Void... params) {// Get the dimensions of the View
            int targetW = mAttachment.getMaxWidth();
            int targetH = mAttachment.getMaxHeight();

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

            long maxAttachmentSize =
                    SmsHelper.getSendSettings(mContext).getMaxAttachmentSize();
            bitmap = ImageUtils.shrink(bitmap, 90, maxAttachmentSize);

            // Now, rotation the bitmap according to the Exif data.
            ExifInterface ei = null;
            try {
                ei = new ExifInterface(mCurrentPhotoPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            switch(orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateImage(bitmap, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateImage(bitmap, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateImage(bitmap, 270);
                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    return rotateImage(bitmap, 0); // No rotation
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            setAttachment(bitmap);
            mCurrentPhotoPath = null;
        }
    }

    private class ImageLoaderTask extends AsyncTask<Uri, Void, Void> {
        final Context mContext;
        final Uri mUri;
        final Handler mHandler;

        public ImageLoaderTask(final Context context, final Uri uri) {
            mContext = context;
            mUri = uri;
            mHandler = new Handler();
        }

        public void execute() {
            execute(mUri);
        }

        @Override
        protected Void doInBackground(Uri... params) {

            if (params.length < 1) {
                Log.e(TAG, "ImageLoaderTask called with no Uri");
                return null;
            }

            try {
                Uri uri = params[0];

                // Decode the image from the Uri into a bitmap [1], and shrink it
                // according to the user's settings.
                // [1]: http://stackoverflow.com/questions/13930009/how-can-i-get-an-image-from-another-application
                InputStream inputStream = mContext.getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                long maxAttachmentSize =
                        SmsHelper.getSendSettings(mContext).getMaxAttachmentSize();
                bitmap = ImageUtils.shrink(bitmap, 90, maxAttachmentSize);

                // Now, rotation the bitmap according to the Exif data.
                final int rotation = ImageUtils.getOrientation(mContext, uri);
                Matrix matrix = new Matrix();
                matrix.postRotate(rotation);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                        bitmap.getHeight(), matrix, true);

                // Can't post UI updates on a background thread.
                final Bitmap imageBitmap = bitmap;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setAttachment(imageBitmap);
                    }
                });

            } catch (FileNotFoundException | NullPointerException e) {
                // Make a toast to the user that the file they've requested to view
                // isn't available.
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(
                                mContext,
                                mRes.getString(R.string.error_file_not_found),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
            }
            return null;
        }
    }

    private void updateDelayButton() {
        mDelay.setColorFilter(mDelayedMessagingEnabled ?
                        ThemeManager.getTextOnColorPrimary() : ThemeManager.getTextOnColorSecondary(),
                PorterDuff.Mode.SRC_ATOP);
    }*/

    public interface OnSendListener {
        void onSend(Message message);
    }
}
