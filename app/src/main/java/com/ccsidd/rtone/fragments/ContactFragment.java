package com.ccsidd.rtone.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.activities.BlockListActivity;
import com.ccsidd.rtone.activities.ContactActivity;
import com.ccsidd.rtone.activities.MainActivity;
import com.ccsidd.rtone.activities.MessageActivity;
import com.ccsidd.rtone.adapters.ContactAdapter;
import com.ccsidd.rtone.dialogs.FunctionDialog;
import com.ccsidd.rtone.listeners.ContactAdapterListener;
import com.ccsidd.rtone.listeners.DialogListener;
import com.ccsidd.rtone.listeners.PhoneListener;
import com.ccsidd.rtone.objects.Contact;
import com.ccsidd.rtone.objects.ContactBlockList;
import com.ccsidd.rtone.objects.Phone;
import com.ccsidd.rtone.observers.ObservableListView;
import com.ccsidd.rtone.observers.ObservableScrollViewCallbacks;
import com.ccsidd.rtone.observers.ScrollUtils;
import com.ccsidd.rtone.searchview.SearchViewLayout;
import com.ccsidd.rtone.utilities.GlobalVars;
import com.ccsidd.rtone.utilities.Utility;
import com.innovattic.font.FontTextView;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class ContactFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, DialogListener, PhoneListener, ContactAdapterListener {

    public static final String ARG_INITIAL_POSITION = "ARG_INITIAL_POSITION";
    public ObservableListView listContact;
    public boolean flag_delete;
    private FontTextView totalcontact;
    private ContactAdapter<Contact> adapter;
    private Realm realm;
    private FunctionDialog functionDialog;
    private String fragmentName = "ContactFragment";
    private Activity parentActivity;
    private boolean hasCallback;
    private boolean hasPermission = false;
    Runnable showMore = new Runnable() {
        public void run() {
            adapter.showMore(); //show more views and find out if
            hasCallback = false;
        }
    };
    private Handler mHandler;

    public ContactFragment() {
        // Required empty public constructor
    }

    public static ContactFragment newInstance() {
        ContactFragment fragment = new ContactFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_INITIAL_POSITION, 1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mHandler = new Handler();
    }

    @Override
    public void onStart() {
        super.onStart();
        realm = Realm.getDefaultInstance();
    }

    @Override
    public void onStop() {
        super.onStop();
        realm.close();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        parentActivity = getActivity();
        final View view;
        if(parentActivity instanceof MainActivity)
            view = inflater.inflate(R.layout.fragment_contact, container, false);
        else
            view = inflater.inflate(R.layout.fragment_contact_block, container, false);
        flag_delete = false;
        final Context context = view.getContext();
        //Utility.configureRealm(getActivity(), Utility.getPref(getActivity(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_ACCOUNT_DEFAULT));

        listContact = (ObservableListView) view.findViewById(R.id.list_fragment);

        if (parentActivity instanceof ObservableScrollViewCallbacks) {
            Bundle args = getArguments();
            if (args != null && args.containsKey(ARG_INITIAL_POSITION)) {
                final int initialPosition = args.getInt(ARG_INITIAL_POSITION, 0);
                ScrollUtils.addOnGlobalLayoutListener(listContact, new Runnable() {
                    @Override
                    public void run() {
                        listContact.setSelection(initialPosition);
                    }
                });
            }

            listContact.setTouchInterceptionViewGroup((ViewGroup) parentActivity.findViewById(R.id.root));
            listContact.setScrollViewCallbacks((ObservableScrollViewCallbacks) parentActivity);
        }

        totalcontact = (FontTextView) view.findViewById(R.id.txtTotal_Contact);

        realm = Realm.getDefaultInstance();
        if (parentActivity instanceof MainActivity) {
            adapter = new ContactAdapter(context, fragmentName, realm.where(Contact.class).findAllSorted("displayName", Sort.ASCENDING), this);
            MaterialSearchView searchView = (MaterialSearchView) parentActivity.findViewById(R.id.search_view);
            searchView.setVoiceSearch(true);
            searchView.setHintTextColor(R.color.hintColorPhoneNumber);
            searchView.setCursorDrawable(R.drawable.cursor_drawable);
            searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String query) {
                    adapter.getFilter().filter(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    adapter.getFilter().filter(newText);
                    return false;
                }
            });
        }else {
            adapter = new ContactAdapter(context, fragmentName + "-Block", realm.where(Contact.class).findAllSorted("displayName", Sort.ASCENDING), this);

            SearchViewLayout searchViewLayout = (SearchViewLayout) parentActivity.findViewById(R.id.search_view_container);
            searchViewLayout.setSearchBoxListener(new SearchViewLayout.SearchBoxListener() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.getFilter().filter(s.toString());

                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        totalcontact.setText("CONTACTS : " + adapter.getCount());

        listContact.setAdapter(adapter);
        listContact.setOnItemClickListener(this);
        listContact.setOnScrollListener(new AbsListView.OnScrollListener() {
//            private int mLastFirstVisibleItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount && !adapter.endReached() && !hasCallback) { //check if we've reached the bottom
                    mHandler.postDelayed(showMore, 200);
                    hasCallback = true;
                }

//                if (mLastFirstVisibleItem < firstVisibleItem) {
//                    hideFab();
//                }
//                if (mLastFirstVisibleItem > firstVisibleItem) {
//                    showFab();
//                }
//                mLastFirstVisibleItem = firstVisibleItem;
            }
        });

        //mFabIsShown = true;

        return view;
    }

    /*public void showFab() {
        if (!mFabIsShown) {
            ViewPropertyAnimator.animate(fab).cancel();
            ViewPropertyAnimator.animate(fab).scaleX(1).scaleY(1).setDuration(200).start();
            mFabIsShown = true;
        }
    }

    public void hideFab() {
        if (mFabIsShown) {
            ViewPropertyAnimator.animate(fab).cancel();
            ViewPropertyAnimator.animate(fab).scaleX(0).scaleY(0).setDuration(200).start();
            mFabIsShown = false;
        }
    }*/

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        if (position == adapter.getLastVisiblePosition()) {
            return;
        }
        Contact contact = (Contact) adapter.getItemAtPosition(position);
        if (contact == null)
            return;
        phonesDialog(contact);
    }

    public void phonesDialog(Contact contact){
        if (contact == null)
            return;
        ArrayList<String> functions = new ArrayList<>();
        for (Phone phoneNumber : contact.getPhoneNumbers()) {
            if (parentActivity instanceof BlockListActivity) {
                if (realm.where(ContactBlockList.class)
                        .contains("phoneNumbers", phoneNumber.getNumber())
                        .findAll().size() == 0)
                    functions.add(phoneNumber.getNumber());
            }else {
                functions.add(phoneNumber.getNumber());
            }
        }

        functionDialog = new FunctionDialog(getActivity(), contact, functions, this);
        if (parentActivity instanceof MainActivity) {
            functionDialog.setPhoneDialog(true);
            functionDialog.setDialogAdapterListenerr(this);
        }
        functionDialog.show();
    }

    @Override
    public void doAction(final String function, Object data) {
        if (data instanceof Contact) {
            final Contact contact = (Contact) data;
            final android.support.v7.app.AlertDialog builder = new android.support.v7.app.AlertDialog.Builder(getActivity(), R.style.MyDialogTheme).create();
            switch (function) {
                case GlobalVars.DIALOG_FUNCTION_EDIT_CONTACT:
                    Intent iContact = new Intent(getActivity(), ContactActivity.class);
                    iContact.putExtra("id", contact.getId());
                    getActivity().startActivity(iContact);
                    functionDialog.dismiss();
                    break;
                case GlobalVars.DIALOG_FUNCTION_BLOCK_CONTACT:
                    blockContact(contact);
                    functionDialog.dismiss();
                    break;
                case GlobalVars.DIALOG_FUNCTION_UNBLOCK_CONTACT:
                    unBlockContact(contact);
                    functionDialog.dismiss();
                    break;
                case GlobalVars.DIALOG_FUNCTION_DELETE_CONTACT:
                    builder.setButton(Dialog.BUTTON_NEUTRAL, "RTone", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            deleteContact(contact, 1);

                            builder.dismiss();
                        }
                    });
                    builder.setButton(Dialog.BUTTON_POSITIVE, "Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            builder.dismiss();
                        }
                    });

                    builder.setButton(Dialog.BUTTON_NEGATIVE, "Rtone and Phone book", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteContact(contact, 0);

                            builder.dismiss();
                        }
                    });
//                    builder.setMessage("Do you want DELETE " + contact.getDisplayName() + "?");
                    builder.setTitle("DELETE Contact " + contact.getDisplayName());
                    builder.show();
                    functionDialog.dismiss();
                    break;
                default:
                    if (parentActivity instanceof BlockListActivity) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                ContactBlockList contactBlockList = new ContactBlockList();
                                contactBlockList.setDisplayName(contact.getDisplayName());
                                contactBlockList.setPhoneNumbers(function);
                                contactBlockList.setImageUri(contact.getImageUri());
                                realm.copyToRealm(contactBlockList);
                            }
                        });
                        adapter.notifyDataSetChanged();
                    }
                    functionDialog.dismiss();
                    break;
            }
        }
    }

    private void deleteContact(final Contact contact, int i) {
        try {
            if (i == 0) {
                ContentResolver contactHelper = getActivity().getContentResolver();
                ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
                String[] args = new String[]{contact.getId() + ""};
                ops.add(ContentProviderOperation.newDelete(RawContacts.CONTENT_URI).withSelection(RawContacts.CONTACT_ID + "=?", args).build());
                try {
                    contactHelper.applyBatch(ContactsContract.AUTHORITY, ops);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (OperationApplicationException e) {
                    e.printStackTrace();
                }
            }

            // All changes to data must happen in a transaction
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    // remove a single object
                    realm.where(Contact.class).equalTo("id", contact.getId()).findAll().deleteAllFromRealm();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void blockContact(final Contact contact) {
        try {
//            final ProgressDialog progress = new ProgressDialog(getActivity(), R.style.StyledDialog);
//            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            progress.setCancelable(false);
//            progress.show();

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Set<Phone> Phones = new HashSet<>();
                    Phones.addAll(contact.getPhoneNumbers());
                    for (final Phone phone : Phones) {
                        ContactBlockList contactBlockList = new ContactBlockList();
                        contactBlockList.setDisplayName(contact.getDisplayName());
                        contactBlockList.setPhoneNumbers(phone.getNumber() == null ? "" : phone.getNumber());
                        contactBlockList.setImageUri(contact.getImageUri());
                        realm.copyToRealm(contactBlockList);
//                        progress.dismisSticker();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unBlockContact(final Contact contact) {
        try {
            try {

//                final ProgressDialog progress = new ProgressDialog(getActivity(), R.style.StyledDialog);
//                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//                progress.setCancelable(false);
//                progress.show();

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        Set<Phone> Phones = new HashSet<>();
                        Phones.addAll(contact.getPhoneNumbers());
                        for (final Phone phone : Phones) {
                            RealmResults<ContactBlockList> result = realm.where(ContactBlockList.class)
                                    .equalTo("phoneNumbers", phone.getNumber() == null ? "" : phone.getNumber()).findAll();
                            result.deleteAllFromRealm();
//                            progress.dismisSticker();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClickSubMenu(Object data) {
        if (data == null)
            return;
        Contact contact = (Contact) data;
        ArrayList<String> functions = new ArrayList<>();

        if (parentActivity instanceof MainActivity) {
            functions.add(GlobalVars.DIALOG_FUNCTION_EDIT_CONTACT);
            functions.add(GlobalVars.DIALOG_FUNCTION_DELETE_CONTACT);
        }

        boolean isBlock = false;
        for (Phone phone : contact.getPhoneNumbers()) {
            isBlock = realm.where(ContactBlockList.class)
                    .contains("phoneNumbers", phone.getNumber() == null ? "" : phone.getNumber())
                    .findAll().size() > 0;
            if (isBlock)
                break;
        }
        if (isBlock)
            functions.add(GlobalVars.DIALOG_FUNCTION_UNBLOCK_CONTACT);
        else
            functions.add(GlobalVars.DIALOG_FUNCTION_BLOCK_CONTACT);

        functionDialog = new FunctionDialog(getActivity(), contact, functions, this);
        functionDialog.setTitle(contact.getDisplayName());
        functionDialog.setPhoneDialog(false);
        functionDialog.show();
    }

    @Override
    public void onClick(View view) {
        //
    }

    @Override
    public void onItemClickListener(int position, Object data) {
        //
    }

    @Override
    public void onCall(String phoneNumber) {
        Utility.callNumber(getActivity(), phoneNumber);
        functionDialog.dismiss();
    }

    @Override
    public void onMessage(String phoneNumber) {
        Intent iMess = new Intent(getActivity(), MessageActivity.class);
        iMess.putExtra("phoneNumber", phoneNumber);
        getActivity().startActivity(iMess);
        functionDialog.dismiss();
    }
}
