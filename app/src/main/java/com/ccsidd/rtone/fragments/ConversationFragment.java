package com.ccsidd.rtone.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.activities.MainActivity;
import com.ccsidd.rtone.activities.MessageActivity;
import com.ccsidd.rtone.message.ThemeManager;
import com.ccsidd.rtone.message.base.RecyclerArrayAdapter;
import com.ccsidd.rtone.message.common.DialogHelper;
import com.ccsidd.rtone.message.compose.ComposeActivity;
import com.ccsidd.rtone.message.conversationlist.ConversationItem;
import com.ccsidd.rtone.message.conversationlist.ConversationListAdapter;
import com.ccsidd.rtone.objects.Contact;
import com.ccsidd.rtone.objects.Conversation;
import com.ccsidd.rtone.observers.ObservableRecyclerView;
import com.ccsidd.rtone.observers.ObservableScrollViewCallbacks;
import com.ccsidd.rtone.observers.ScrollUtils;
import com.ccsidd.rtone.utilities.GlobalVars;
import com.ccsidd.rtone.utilities.Utility;
import com.innovattic.font.TypefaceManager;
import com.melnykov.fab.FloatingActionButton;

import java.util.HashSet;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import me.leolin.shortcutbadger.ShortcutBadger;


public class ConversationFragment extends Fragment implements RecyclerArrayAdapter.ItemClickListener<ConversationItem>, RecyclerArrayAdapter.MultiSelectListener {

    public static final String ARG_INITIAL_POSITION = "ARG_INITIAL_POSITION";
    private Realm realm;
    private ConversationListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private ObservableRecyclerView mRecyclerView;
    private FloatingActionButton mFab;
    private RealmChangeListener listener;
    private RealmResults<Contact> realmResults;

    private Context mContext;

    public ConversationFragment() {
        // Required empty public constructor
    }

    public static ConversationFragment newInstance() {
        ConversationFragment fragment = new ConversationFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_INITIAL_POSITION, 1);
        fragment.setArguments(args);
        return fragment;
    }

    public static boolean contains(RealmList<Contact> contacts, int id) {
        for (Contact c : contacts) {
            if (c != null && c.getId() == id) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
//        Utility.configureRealm(getActivity(), Utility.getPref(getActivity(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_ACCOUNT_DEFAULT));
//        realm.beginTransaction();
//        for(int i = 0; i < 1000; i++){
//            Conversation conversation = new Conversation();
//            conversation.setPhoneNumber("phone" + i);
//            conversation.setLastMessage("mess" + i);
//            conversation.setType(0);
//            conversation.setUnRead(true);
//            realm.copyToRealmOrUpdate(conversation);
//        }
//        realm.commitTransaction();

    }

    @Override
    public void onStart() {
        super.onStart();
        realm = Realm.getDefaultInstance();
        realmResults = realm.where(Contact.class).findAllAsync();
        listener = new RealmChangeListener<RealmResults<Conversation>>() {
            @Override
            public void onChange(RealmResults<Conversation> s) {
                mAdapter.notifyDataSetChanged();
            }
        };
        realmResults.addChangeListener(listener);
    }

    @Override
    public void onStop() {
        super.onStop();
        realmResults.removeChangeListener(listener);
        realm.close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_conversations, container, false);
        mContext = view.getContext();
        Activity parentActivity = getActivity();

        realm = Realm.getDefaultInstance();
        mAdapter = new ConversationListAdapter(mContext, realm.where(Conversation.class).findAllSortedAsync("time", Sort.DESCENDING));
        mAdapter.setItemClickListener(this);
        mAdapter.setMultiSelectListener(this);
        mLayoutManager = new LinearLayoutManager(mContext);

        mRecyclerView = (ObservableRecyclerView) view.findViewById(R.id.conversations_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mFab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        //mFab.setColorNormal(ThemeManager.getColor());
        //mFab.setColorPressed(ColorUtils.lighten(ThemeManager.getColor()));
        mFab.attachToRecyclerView(mRecyclerView);
        mFab.setColorFilter(ThemeManager.getSentBubbleColor());


        if (parentActivity instanceof ObservableScrollViewCallbacks) {
            // Scroll to the specified position after layout
            Bundle args = getArguments();
            if (args != null && args.containsKey(ARG_INITIAL_POSITION)) {
                final int initialPosition = args.getInt(ARG_INITIAL_POSITION, 0);
                ScrollUtils.addOnGlobalLayoutListener(mRecyclerView, new Runnable() {
                    @Override
                    public void run() {
                        // scrollTo() doesn't work, should use setSelection()
                        mRecyclerView.scrollVerticallyToPosition(initialPosition);
                    }
                });
            }

            // TouchInterceptionViewGroup should be a parent view other than ViewPager.
            // This is a workaround for the issue #117:
            // https://github.com/ksoichiro/Android-ObservableScrollView/issues/117
            mRecyclerView.setTouchInterceptionViewGroup((ViewGroup) parentActivity.findViewById(R.id.root));

            mRecyclerView.setScrollViewCallbacks((ObservableScrollViewCallbacks) parentActivity);
        }

        return view;
    }

    public void onFabClick(View v) {
        if (mAdapter.isInMultiSelectMode()) {
            mAdapter.disableMultiSelectMode(true);
        } else {
            mContext.startActivity(new Intent(getActivity(), ComposeActivity.class));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        if (mAdapter.isInMultiSelectMode()) {
            inflater.inflate(R.menu.conversations_selection, menu);
            getActivity().setTitle(getString(R.string.title_conversations_selected, mAdapter.getSelectedItems().size()));

//            menu.findItem(R.id.menu_block).setVisible(false);
//            menu.findItem(R.id.menu_mark_read).setIcon(true ? R.drawable.ic_mark_read : R.drawable.ic_mark_unread);
//            menu.findItem(R.id.menu_mark_read).setTitle(true ? R.string.menu_mark_read : R.string.menu_mark_unread);
//            menu.findItem(R.id.menu_block).setTitle(true ? R.string.menu_unblock_conversations : R.string.menu_block_conversations);
            for (int i = 0; i < menu.size(); i++) {
                MenuItem menuItem = menu.getItem(i);
                Drawable newIcon = menuItem.getIcon();
                if (newIcon != null) {
                    int[] attrs = new int[]{R.attr.numberPadStyle};
                    TypedArray ta = mContext.obtainStyledAttributes(attrs);
                    newIcon.setColorFilter(ta.getColor(0, 0xFF1565C0), PorterDuff.Mode.SRC_ATOP);
                    menuItem.setIcon(newIcon);
                    ta.recycle();

                }
            }
        }
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                Set<String> phoneNum = new HashSet<>();
                for (ConversationItem conversationItem : mAdapter.getSelectedItems().values()) {
                    phoneNum.add(conversationItem.getRecipients().get(0));
                }
                DialogHelper.showDeleteConversationsDialog(mContext, phoneNum);
                mAdapter.disableMultiSelectMode(true);
                return true;
            case R.id.select_all:
                item.setTitle(mAdapter.getSelectedItems().size() == mAdapter.getItemCount() ? "deselect all" : "select all");
                mAdapter.toggleSelectionAll();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(ConversationItem conversation, View view) {
        if (mAdapter.isInMultiSelectMode()) {
            mAdapter.toggleSelection(conversation.getThreadId(), conversation);
        } else {

            getActivity().startActivity(new Intent(getActivity(), MessageActivity.class)
                    .putExtra("phoneNumber", conversation.getRecipients().get(0)));
        }
    }

    @Override
    public void onItemLongClick(ConversationItem conversation, View view) {
        mAdapter.toggleSelection(conversation.getThreadId(), conversation);
    }

    @Override
    public void onMultiSelectStateChanged(boolean enabled) {
        getActivity().invalidateOptionsMenu();
        if (!enabled)
            getActivity().setTitle("");
        mFab.setImageResource(enabled ? R.drawable.ic_cancel : R.drawable.ic_add);
    }

    @Override
    public void onItemAdded(long id) {
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onItemRemoved(long id) {
        getActivity().invalidateOptionsMenu();
    }
}
