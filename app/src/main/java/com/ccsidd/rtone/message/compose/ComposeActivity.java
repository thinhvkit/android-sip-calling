package com.ccsidd.rtone.message.compose;

import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.message.base.QKActivity;
import com.ccsidd.rtone.message.dialog.QKDialog;


public class ComposeActivity extends QKActivity {

    private ComposeFragment mComposeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_compose);
        showBackButton(true);

        FragmentManager fm = getFragmentManager();
        mComposeFragment = (ComposeFragment) fm.findFragmentByTag(ComposeFragment.TAG);
        if (mComposeFragment == null) {
            mComposeFragment = new ComposeFragment();
        }

        fm.beginTransaction()
                .replace(R.id.content_frame, mComposeFragment, ComposeFragment.TAG)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.compose, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (mComposeFragment != null && !mComposeFragment.isReplyTextEmpty()
                && mComposeFragment.getRecipientAddresses().length == 0) {
            // If there is Draft message and no recipients are set
            new QKDialog()
                    .setContext(this)
                    .setMessage(R.string.discard_message_reason)
                    .setPositiveButton(R.string.yes, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ComposeActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}
