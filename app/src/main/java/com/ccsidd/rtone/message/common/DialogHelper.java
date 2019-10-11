package com.ccsidd.rtone.message.common;

import android.content.Context;
import android.util.Log;
import android.view.View;


import com.ccsidd.rtone.R;
import com.ccsidd.rtone.activities.MessageActivity;
import com.ccsidd.rtone.objects.Conversation;
import com.ccsidd.rtone.objects.Message;
import com.ccsidd.rtone.message.dialog.QKDialog;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.realm.Realm;

public class DialogHelper {
    private static final String TAG = "DialogHelper";

    public static void showDeleteConversationDialog(Context context, String threadId) {
        Set<String> threadIds = new HashSet<>();
        threadIds.add(threadId);
        showDeleteConversationsDialog(context, threadIds);
    }

    public static void showDeleteConversationsDialog(final Context context, final Set<String> threadIds) {

        final Set<String> threads = new HashSet<>(threadIds); // Make a copy so the list isn't reset when multi-select is disabled
        new QKDialog()
                .setContext(context)
                .setTitle(R.string.delete_conversation)
                .setMessage(context.getString(R.string.delete_confirmation, threads.size()))
                .setPositiveButton(R.string.yes, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Realm realm = Realm.getDefaultInstance();
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                for (String phoneNum : threads) {
                                    realm.where(Conversation.class).equalTo("phoneNumber", phoneNum).findAll().deleteAllFromRealm();
                                    realm.where(Message.class).equalTo("phoneNumber", phoneNum).findAll().deleteAllFromRealm();
                                }
                            }
                        });

                        if (context instanceof MessageActivity) {
                            ((MessageActivity) context).onBackPressed();
                        }
                        Log.d(TAG, "Deleting threads: " + Arrays.toString(threads.toArray()));

                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();

    }
}
