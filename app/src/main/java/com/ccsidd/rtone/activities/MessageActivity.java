package com.ccsidd.rtone.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.ccsidd.rtone.Foreground;
import com.ccsidd.rtone.R;
import com.ccsidd.rtone.listeners.AlertDialogListener;
import com.ccsidd.rtone.listeners.RecipientProvider;
import com.ccsidd.rtone.message.ThemeManager;
import com.ccsidd.rtone.message.base.RecyclerArrayAdapter;
import com.ccsidd.rtone.message.common.DialogHelper;
import com.ccsidd.rtone.message.conversationlist.ConversationItem;
import com.ccsidd.rtone.message.dialog.QKDialog;
import com.ccsidd.rtone.message.dialog.conversationdetails.ConversationDetailsDialog;
import com.ccsidd.rtone.message.messagelist.MessageItem;
import com.ccsidd.rtone.message.messagelist.MessageListAdapter;
import com.ccsidd.rtone.message.view.CollapsableLinearLayout;
import com.ccsidd.rtone.message.view.ComposeView;
import com.ccsidd.rtone.message.view.MessageListRecyclerView;
import com.ccsidd.rtone.message.view.QKTextView;
import com.ccsidd.rtone.message.view.SmoothLinearLayoutManager;
import com.ccsidd.rtone.objects.Contact;
import com.ccsidd.rtone.objects.Conversation;
import com.ccsidd.rtone.objects.Message;
import com.ccsidd.rtone.services.Logger;
import com.ccsidd.rtone.utilities.GlobalVars;
import com.ccsidd.rtone.utilities.ThemeUtils;
import com.ccsidd.rtone.utilities.Utility;
import com.ccsidd.rtone.view.contacteditText.Recipient;
import com.innovattic.font.TypefaceManager;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class MessageActivity extends AppCompatActivity implements Foreground.Listener, ComposeView.OnSendListener, RecyclerArrayAdapter.MultiSelectListener,
        RecyclerArrayAdapter.ItemClickListener<MessageItem>, RecipientProvider {

    private static final int MENU_VIEW_MESSAGE_DETAILS = 17;
    private static final int MENU_DELETE_MESSAGE = 18;
    private static final int MENU_FORWARD_MESSAGE = 21;
    private static final int MENU_COPY_MESSAGE_TEXT = 24;
    private static final int MENU_ADD_ADDRESS_TO_CONTACTS = 27;
    private static final int MENU_LOCK_MESSAGE = 28;
    private static final int MENU_UNLOCK_MESSAGE = 29;
    private Realm realm;
    private String defaultAccount = "";
    public String phoneNumber = "";
    private String TAG = MessageActivity.class.getName();
    private MessageListRecyclerView mRecyclerView;
    private MessageListAdapter mAdapter;
    private String themes;
    private ComposeView mComposeView;
    private Toolbar mToolbar;
    private ImageView mOverflowButton;
    private QKTextView mTitle;
    private SmoothLinearLayoutManager mLayoutManager;
    private ConversationDetailsDialog mConversationDetailsDialog;
    private RecyclerView.AdapterDataObserver adapterDataObserver;
    private Foreground.Binding listenerBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        themes = Utility.getPref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_THEMES);
        if (themes.isEmpty())
            themes = "0";
        ThemeUtils.setsTheme(Integer.parseInt(themes));
        ThemeUtils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);

        TypefaceManager.initialize(getApplicationContext(), R.xml.fonts);
        // AFTER SETTING THEME
        setContentView(R.layout.activity_message);

        if(listenerBinding != null){
            listenerBinding.unbind();
        }
        listenerBinding = Foreground.get(getApplication()).addListener(this);

        phoneNumber = getIntent().getStringExtra("phoneNumber");

        ViewGroup rootView = (ViewGroup) findViewById(R.id.root);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Contact contact = Utility.getContactDisplayNameByNumber(Utility.formatNumberphone(phoneNumber));
        mTitle = (QKTextView) mToolbar.findViewById(R.id.toolbar_title);
        mTitle.setText(contact.getDisplayName());

        defaultAccount = Utility.getPref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_ACCOUNT_DEFAULT);
        Utility.configureRealm(this, defaultAccount);

        mRecyclerView = (MessageListRecyclerView) findViewById(R.id.conversation);

        if(realm != null){
            realm.close();
        }
        realm = Realm.getDefaultInstance();
        mAdapter = new MessageListAdapter(this, realm.where(Message.class).equalTo("phoneNumber", phoneNumber).findAllSortedAsync("time", Sort.ASCENDING));
        mAdapter.setItemClickListener(this);
        mAdapter.setMultiSelectListener(this);

        adapterDataObserver = new RecyclerView.AdapterDataObserver() {
            private long mLastMessageId = -1;
            @Override
            public void onChanged() {
                Logger.info(TAG, "Apdater onChange");

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmResults<Conversation> realmConvResults = realm.where(Conversation.class).equalTo("phoneNumber", phoneNumber).findAll();
                        if (realmConvResults.size() > 0 && realmConvResults.first().isUnRead())
                            realmConvResults.first().setUnRead(false);
                        RealmResults<Message> realmMessResults = realm.where(Message.class).equalTo("phoneNumber", phoneNumber).findAll();
                        for (Message message : realmMessResults) {
                            if (message.isUnRead()) {
                                message.setUnRead(false);
                            }
                        }
                    }
                });

                LinearLayoutManager manager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                int position;

                position = mAdapter.getItemCount() - 1;

                if (mAdapter.getCount() > 0) {
//                    MessageItem lastMessage = mAdapter.getItem(mAdapter.getCount() - 1);
//                    if (mLastMessageId >= 0 && mLastMessageId != lastMessage.getMessageId()) {
//                        //
//                    }
//                    mLastMessageId = lastMessage.getMessageId();

                    // Scroll to bottom only if a new message was inserted in this conversation
                    int lastFirstVisiblePosition = ((LinearLayoutManager)mRecyclerView.getLayoutManager()).findLastVisibleItemPosition();
                    if (position == -1 || lastFirstVisiblePosition == -1)
                        return;
                    if (position - lastFirstVisiblePosition < 10) {
                        manager.smoothScrollToPosition(mRecyclerView, null, position);
                    }else{
                        String content = mAdapter.getItem(position).mBody;
                        content = content.startsWith("[[") && content.endsWith("]]") ? "Have a sticker" : content;
                        Toast.makeText(MessageActivity.this, content, Toast.LENGTH_SHORT).show();
                    }

                }
            }
        };
//        mAdapter.registerAdapterDataObserver(adapterDataObserver);

        mRecyclerView.setAdapter(mAdapter);

        mLayoutManager = new SmoothLinearLayoutManager(this);
        mLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        if(mComposeView != null){
            mComposeView.removeOnGlobalLayout();
        }

        mComposeView = (ComposeView) findViewById(R.id.compose_view);
        mComposeView.setLabel("MessageList");
        mComposeView.setRecipientProvider(this);
        mComposeView.setOnSendListener(this);
        mComposeView.setRootView(rootView);

        mRecyclerView.setComposeView(mComposeView);
        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
//                mComposeView.dismisEmoji();
                mComposeView.dismisSticker();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                return false;
            }
        });

        mConversationDetailsDialog = new ConversationDetailsDialog(this, getFragmentManager());

        Utility.savePref(this, GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.KEY_PHONE_CHATTING, phoneNumber);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void onStart() {
        super.onStart();
        realm = Realm.getDefaultInstance();
        Logger.info(TAG, "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        realm.close();
        Logger.info(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        listenerBinding.unbind();
        mComposeView.removeOnGlobalLayout();
        realm.close();
//        mAdapter.unregisterAdapterDataObserver(adapterDataObserver);
        Logger.info(TAG, "onDestroy");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.registerAdapterDataObserver(adapterDataObserver);
        mAdapter.notifyDataSetChanged();
        Utility.savePref(this, GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.KEY_PHONE_CHATTING, phoneNumber);
        Logger.info(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdapter.unregisterAdapterDataObserver(adapterDataObserver);
        Utility.removePref(this, GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.KEY_PHONE_CHATTING);
        Logger.info(TAG, "onPause");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.message_list, menu);

        //colorMenuIcons(menu, ThemeManager.getTextOnColorPrimary());

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_call:
                com.ccsidd.rtone.dialogs.AlertDialog alert = new com.ccsidd.rtone.dialogs.AlertDialog(this, "", "");
                if (!Utility.checkNetwork(this)) {
                    alert.setTitle("Error");
                    alert.setMessage("Network is unreachable");
                    alert.setIsShowOK(true);
                    alert.setAlertDialogListener(new AlertDialogListener() {
                        @Override
                        public void btnYesPressed(com.ccsidd.rtone.dialogs.AlertDialog alertDialog) {
                            alertDialog.dismiss();
                        }

                        @Override
                        public void btnNoPressed(com.ccsidd.rtone.dialogs.AlertDialog alertDialog) {
                            alertDialog.dismiss();
                        }
                    });
                    alert.show();
                } else {
                    Utility.callNumber(this, phoneNumber);
                }
                return true;
            case R.id.menu_details:
                Conversation conversation = realm.where(Conversation.class).equalTo("phoneNumber", phoneNumber).findFirst();
                ConversationItem conversationItem = ConversationItem.from(this, 0, conversation);
                conversationItem.setMessageCount(realm.where(Message.class).equalTo("phoneNumber", phoneNumber).findAll().size());
                mConversationDetailsDialog.showDetails(conversationItem);
                return true;

            case R.id.menu_delete_conversation:
                DialogHelper.showDeleteConversationDialog(this, phoneNumber);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void colorMenuIcons(Menu menu, int color) {

        // Toolbar navigation icon
        Drawable navigationIcon = mToolbar.getNavigationIcon();
        if (navigationIcon != null) {
            navigationIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            mToolbar.setNavigationIcon(navigationIcon);
        }

        // Overflow icon
        colorOverflowButtonWhenReady(color);

        // Other icons
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            Drawable newIcon = menuItem.getIcon();
            if (newIcon != null) {
                newIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                menuItem.setIcon(newIcon);
            }
        }
    }

    private void colorOverflowButtonWhenReady(final int color) {
        if (mOverflowButton != null) {
            // We already have the overflow button, so just color it.
            Drawable icon = mOverflowButton.getDrawable();
            icon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            // Have to clear the image drawable first or else it won't take effect
            mOverflowButton.setImageDrawable(null);
            mOverflowButton.setImageDrawable(icon);

        } else {
            // Otherwise, find the overflow button by searching for the content description.
            final String overflowDesc = getString(R.string.abc_action_menu_overflow_description);
            final ViewGroup decor = (ViewGroup) getWindow().getDecorView();
            decor.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    decor.getViewTreeObserver().removeOnPreDrawListener(this);

                    final ArrayList<View> views = new ArrayList<>();
                    decor.findViewsWithText(views, overflowDesc,
                            View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);

                    if (views.isEmpty()) {
                        Logger.debug(TAG, "no views");
                    } else {
                        if (views.get(0) instanceof ImageView) {
                            mOverflowButton = (ImageView) views.get(0);
                            colorOverflowButtonWhenReady(color);
                        } else {
                            Log.w(TAG, "overflow button isn't an imageview");
                        }
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void onSend(final Message message) {
        //
    }

    @Override
    public Recipient[] getRecipientAddresses() {
        Recipient[] recipients = new Recipient[1];
        recipients[0] = new Recipient("", phoneNumber, "");
        return recipients;
    }

    @Override
    public void onItemClick(MessageItem messageItem, final View view) {

        CollapsableLinearLayout layoutDateView = (CollapsableLinearLayout) view.findViewById(R.id.layout_date_view);
        layoutDateView.setVisibility(View.VISIBLE);
        layoutDateView.toggle();

    }

    @Override
    public void onItemLongClick(MessageItem messageItem, View view) {

        QKDialog dialog = new QKDialog();
        dialog.setContext(this);
        dialog.setTitle(R.string.message_options);

        MsgListMenuClickListener l = new MsgListMenuClickListener(messageItem, view);

        if (messageItem.isSms()) {

            dialog.addMenuItem(R.string.copy_message_text, MENU_COPY_MESSAGE_TEXT);
        }
        /*if (messageItem.mLocked && mIsSmsEnabled) {
            dialog.addMenuItem(R.string.menu_unlock, MENU_UNLOCK_MESSAGE);
        } else if (mIsSmsEnabled) {
            dialog.addMenuItem(R.string.menu_lock, MENU_LOCK_MESSAGE);
        }*/

        dialog.addMenuItem(R.string.view_message_details, MENU_VIEW_MESSAGE_DETAILS);

        /*if (mIsSmsEnabled) {
            dialog.addMenuItem(R.string.delete_message, MENU_DELETE_MESSAGE);
        }*/

        dialog.buildMenu(l);
        dialog.show();
    }

    @Override
    public void onMultiSelectStateChanged(boolean enabled) {

    }

    @Override
    public void onItemAdded(long id) {

    }

    @Override
    public void onItemRemoved(long id) {

    }

    @Override
    public void onBecameForeground() {

    }

    @Override
    public void onBecameBackground() {

    }

    private final class MsgListMenuClickListener implements AdapterView.OnItemClickListener {
        private MessageItem mMsgItem;
        private View mView;

        public MsgListMenuClickListener(MessageItem msgItem, View view) {
            mMsgItem = msgItem;
            mView = view;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mMsgItem == null) {
                return;
            }

            switch ((int) id) {

                case MENU_COPY_MESSAGE_TEXT:
                    copyToClipboard(mMsgItem.mBody);
                    break;

                case MENU_FORWARD_MESSAGE:
                    //MessageUtils.forwardMessage(mContext, mMsgItem);
                    break;

                case MENU_VIEW_MESSAGE_DETAILS:
                    //showMessageDetails(mMsgItem);
                    CollapsableLinearLayout layoutDateView = (CollapsableLinearLayout) mView.findViewById(R.id.layout_date_view);
                    layoutDateView.setVisibility(View.VISIBLE);
                    layoutDateView.toggle();
                    break;

                case MENU_DELETE_MESSAGE:
                    //DeleteMessageListener l = new DeleteMessageListener(mMsgItem);
                    //confirmDeleteDialog(l, mMsgItem.mLocked);
                    break;

                case MENU_ADD_ADDRESS_TO_CONTACTS:
                    //MessageUtils.addToContacts(mContext, mMsgItem);
                    break;

                case MENU_LOCK_MESSAGE:
                    //MessageUtils.lockMessage(mContext, mMsgItem, true);
                    break;

                case MENU_UNLOCK_MESSAGE:
                    //MessageUtils.lockMessage(mContext, mMsgItem, false);
                    break;
            }
        }

        public void copyToClipboard(String str) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText(null, str));
        }
    }
}
