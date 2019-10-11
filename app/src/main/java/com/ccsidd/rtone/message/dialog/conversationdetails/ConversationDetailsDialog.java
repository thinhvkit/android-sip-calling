package com.ccsidd.rtone.message.dialog.conversationdetails;

import android.app.FragmentManager;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.objects.Message;
import com.ccsidd.rtone.message.common.utils.DateFormatter;
import com.ccsidd.rtone.message.conversationlist.ConversationItem;
import com.ccsidd.rtone.message.dialog.QKDialog;
import com.ccsidd.rtone.message.interfaces.ConversationDetails;
import com.ccsidd.rtone.message.view.QKTextView;

import io.realm.Realm;


public class ConversationDetailsDialog implements ConversationDetails {

    private Context mContext;
    private FragmentManager mFragmentManager;

    public ConversationDetailsDialog(Context context, FragmentManager fragmentManager) {
        mContext = context;
        mFragmentManager = fragmentManager;
    }

    @Override
    public void showDetails(ConversationItem conversation) {
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        AbsListView.LayoutParams listParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        View view = View.inflate(mContext, R.layout.dialog_conversation_details, null);
        view.setLayoutParams(listParams);
        ((QKTextView) view.findViewById(R.id.date)).setText(DateFormatter.getDate(mContext, conversation.getDate()));
        Realm realm = Realm.getDefaultInstance();
        int messageCount = realm.where(Message.class).equalTo("phoneNumber", conversation.getRecipients().get(0)).findAll().size();
        ((QKTextView) view.findViewById(R.id.message_count)).setText(Integer.toString(messageCount));
        ((QKTextView) view.findViewById(R.id.recipients)).setText(mContext.getString(
                R.string.dialog_conversation_details_recipients, Integer.toString(conversation.getRecipients().size())));

        ListView listView = new ListView(mContext);
        listView.setLayoutParams(params);
        listView.addHeaderView(view);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setAdapter(new ConversationDetailsContactListAdapter(mContext, conversation.getRecipients()));

        new QKDialog()
                .setContext(mContext)
                .setTitle(R.string.dialog_conversation_details_title)
                .setCustomView(listView)
                .setPositiveButton(R.string.okay, null)
                .show();
    }

}
