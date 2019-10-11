package com.ccsidd.rtone.message.compose;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.listeners.RecipientProvider;
import com.ccsidd.rtone.message.interfaces.ActivityLauncher;
import com.ccsidd.rtone.message.view.AutoCompleteContactView;
import com.ccsidd.rtone.message.view.ComposeView;
import com.ccsidd.rtone.message.view.StarredContactsView;
import com.ccsidd.rtone.objects.Message;
import com.ccsidd.rtone.view.contacteditText.ContactEditText;
import com.ccsidd.rtone.view.contacteditText.Recipient;

public class ComposeFragment extends Fragment implements ActivityLauncher, RecipientProvider,
        ComposeView.OnSendListener, AdapterView.OnItemClickListener {

    public static final String TAG = "ComposeFragment";

    private AutoCompleteContactView mRecipients;
    private ComposeView mComposeView;
    private StarredContactsView mStarredContactsView;
    private ContactEditText contactEditText;
    private Context mContext;

    public ComposeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_compose, container, false);
        mContext = view.getContext();

//        mRecipients = (AutoCompleteContactView) view.findViewById(R.id.compose_recipients);
//        mRecipients.setOnItemClickListener(this);

        ViewGroup rootView = (ViewGroup) view.findViewById(R.id.root);
        contactEditText = (ContactEditText) view.findViewById(R.id.textfield_tv);

        mComposeView = (ComposeView) view.findViewById(R.id.compose_view);
        //mComposeView.onOpenConversation(null, null);
        mComposeView.setActivityLauncher(this);
        mComposeView.setRecipientProvider(this);
        mComposeView.setOnSendListener(this);
        mComposeView.setLabel("Compose");
        mComposeView.setRootView(rootView);

        //mStarredContactsView = (StarredContactsView) view.findViewById(R.id.starred_contacts);
        //mStarredContactsView.setComposeScreenViews(mRecipients, mComposeView);

        //new Handler().postDelayed(() -> KeyboardUtils.showAndFocus(mContext, mRecipients), 100);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                KeyboardUtils.showAndFocus(mContext, mRecipients);
//            }
//        }, 100);
//
        return view;
    }

    @Override
    public void onSend(final Message message) {
        /*long threadId = Utils.getOrCreateThreadId(mContext, recipients[0]);
        if (threadId != 0) {
            mContext.finish();
            MessageListActivity.launch(mContext, threadId, -1, null, true);
        } else {
            mContext.onBackPressed();
        }*/

        getActivity().finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //if (mComposeView != null) {
        //mComposeView.saveDraft();
        //}
        mComposeView.removeOnGlobalLayout();
    }

    /**
     * @return the addresses of all the contacts in the AutoCompleteContactsView.
     */
    @Override
    /*public String[] getRecipientAddresses() {
        DrawableRecipientChip[] chips = mRecipients.getRecipients();

        String[] addresses = new String[chips.length];

        for (int i = 0; i < chips.length; i++) {
            addresses[i] = PhoneNumberUtils.stripSeparators(chips[i].getEntry().getDestination());
        }

        return addresses;
    }*/
    public Recipient[] getRecipientAddresses() {
        return contactEditText.getRecipients();
    }

    /**
     * Photo Selection result
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
//        if (!mComposeView.onActivityResult(requestCode, resultCode, data)) {
        // Wasn't handled by ComposeView
//        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mRecipients.onItemClick(parent, view, position, id);
//        mStarredContactsView.collapse();
        mComposeView.requestReplyTextFocus();
    }

    public boolean isReplyTextEmpty() {
        if (mComposeView != null) {
            return mComposeView.isReplyTextEmpty();
        }
        return true;
    }

}
