package com.ccsidd.rtone.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.adapters.ContactAdapter;
import com.ccsidd.rtone.dialogs.AlertDialog;
import com.ccsidd.rtone.dialogs.FunctionDialog;
import com.ccsidd.rtone.listeners.AlertDialogListener;
import com.ccsidd.rtone.listeners.ContactAdapterListener;
import com.ccsidd.rtone.listeners.DialogListener;
import com.ccsidd.rtone.objects.CallLog;
import com.ccsidd.rtone.objects.ContactBlockList;
import com.ccsidd.rtone.observers.ObservableListView;
import com.ccsidd.rtone.observers.ObservableScrollViewCallbacks;
import com.ccsidd.rtone.observers.ScrollUtils;
import com.ccsidd.rtone.utilities.GlobalVars;
import com.ccsidd.rtone.utilities.Utility;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class RecentFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, ContactAdapterListener, DialogListener {

    public static final String ARG_INITIAL_POSITION = "ARG_INITIAL_POSITION";
    private ObservableListView lsvRecent;
    private ContactAdapter<CallLog> adapterRecent;
    private Realm realm;
    BroadcastReceiver mReceiver;
    private FunctionDialog functionDialog;
    private boolean hasCallback;
    Runnable showMore = new Runnable() {
        public void run() {
            adapterRecent.showMore(); //show more views and find out if
            hasCallback = false;
        }
    };
    private Handler mHandler;

    public RecentFragment() {
        // Required empty public constructor
    }

    public static RecentFragment newInstance() {
        RecentFragment fragment = new RecentFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

//        Utility.configureRealm(getActivity(), Utility.getPref(getActivity(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_ACCOUNT_DEFAULT));

        mHandler = new Handler();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            getActivity().unregisterReceiver(mReceiver);
        } catch (Exception ex) {
        }

        realm.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent, container, false);
        final Context context = view.getContext();
        lsvRecent = (ObservableListView) view.findViewById(R.id.list_fragment);
        Activity parentActivity = getActivity();
        realm = Realm.getDefaultInstance();
        adapterRecent = new ContactAdapter(context, "RecentFragment", realm.where(CallLog.class).findAllSorted("date", Sort.DESCENDING), this);
        lsvRecent.setAdapter(adapterRecent);
        lsvRecent.setOnItemClickListener(this);

        if (parentActivity instanceof ObservableScrollViewCallbacks) {
            // Scroll to the specified position after layout
            Bundle args = getArguments();
            if (args != null && args.containsKey(ARG_INITIAL_POSITION)) {
                final int initialPosition = args.getInt(ARG_INITIAL_POSITION, 0);
                ScrollUtils.addOnGlobalLayoutListener(lsvRecent, new Runnable() {
                    @Override
                    public void run() {
                        // scrollTo() doesn't work, should use setSelection()
                        lsvRecent.setSelection(initialPosition);
                    }
                });
            }

            // TouchInterceptionViewGroup should be a parent view other than ViewPager.
            // This is a workaround for the issue #117:
            // https://github.com/ksoichiro/Android-ObservableScrollView/issues/117
            lsvRecent.setTouchInterceptionViewGroup((ViewGroup) parentActivity.findViewById(R.id.root));

            lsvRecent.setScrollViewCallbacks((ObservableScrollViewCallbacks) parentActivity);
        }

        lsvRecent.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount && !adapterRecent.endReached() && !hasCallback) { //check if we've reached the bottom
                    mHandler.postDelayed(showMore, 300);
                    hasCallback = true;
                }
            }
        });

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        final CallLog callLog = (CallLog) adapterView.getItemAtPosition(position);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(callLog);
            }
        });
        Utility.callNumber(getActivity(), callLog.getNumber());
        lsvRecent.setEnabled(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                lsvRecent.setEnabled(true);
            }
        }, 500);
    }

    @Override
    public void onClickSubMenu(Object data) {
        CallLog callLog = (CallLog) data;
        ArrayList<String> functions = new ArrayList<>();
        if (!callLog.isHasContact())
            functions.add(GlobalVars.DIALOG_FUNCTION_ADD_CONTACT);
        functions.add(GlobalVars.DIALOG_FUNCTION_CLEAR_RECENT);
        functions.add(GlobalVars.DIALOG_FUNCTION_CLEAR_ALL_RECENT);
        if (Utility.blockNumber(callLog.getNumber())) {
            functions.add(GlobalVars.DIALOG_FUNCTION_UNBLOCK_CONTACT);
        } else
            functions.add(GlobalVars.DIALOG_FUNCTION_BLOCK_CONTACT);
        functionDialog = new FunctionDialog(getActivity(), callLog, functions, this);
        functionDialog.setTitle(callLog.getName());
        functionDialog.show();
    }

    @Override
    public void doAction(String function, Object data) {
        if (data instanceof CallLog) {
            final CallLog callLog = (CallLog) data;
            String name = callLog.getName();
            if (name.length() == 0)
                name = callLog.getNumber();

            switch (function) {
                case GlobalVars.DIALOG_FUNCTION_ADD_CONTACT:
                    Intent updateIntent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                    updateIntent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
                    updateIntent.putExtra("finishActivityOnSaveCompleted", true);
                    updateIntent.putExtra(ContactsContract.Intents.Insert.PHONE, callLog.getNumber());
                    startActivity(updateIntent);
                    break;
                case GlobalVars.DIALOG_FUNCTION_CLEAR_RECENT:

                    AlertDialog alertDialogClear = new AlertDialog(getActivity(), "Alert", "Do you want to clear " + "<strong>" + name + "</strong>" + "?");
                    alertDialogClear.setAlertDialogListener(new AlertDialogListener() {
                        @Override
                        public void btnYesPressed(AlertDialog alertDialog) {
                            alertDialog.dismiss();
                            Utility.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    deleteLog(callLog.getId());
                                    adapterRecent.reset();
                                }
                            });
                            functionDialog.dismiss();
                        }

                        @Override
                        public void btnNoPressed(AlertDialog alertDialog) {
                            alertDialog.dismiss();
                        }
                    });
                    alertDialogClear.show();
                    break;
                case GlobalVars.DIALOG_FUNCTION_CLEAR_ALL_RECENT:

                    AlertDialog alertDialogClearAll = new AlertDialog(getActivity(), "Alert", "Do you want to clear all calls from " + "<strong>" + name + "</strong>" + "?");
                    alertDialogClearAll.setAlertDialogListener(new AlertDialogListener() {
                        @Override
                        public void btnYesPressed(AlertDialog alertDialog) {
                            alertDialog.dismiss();
                            if (callLog.getNumber().length() == 0) {
                                functionDialog.dismiss();
                                return;
                            }
                            deleteLogsFromNumber(callLog.getNumber());

                            Utility.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapterRecent.reset();
                                }
                            });
                            functionDialog.dismiss();
                        }

                        @Override
                        public void btnNoPressed(AlertDialog alertDialog) {
                            alertDialog.dismiss();
                        }
                    });
                    alertDialogClearAll.show();
                    break;
                case GlobalVars.DIALOG_FUNCTION_BLOCK_CONTACT:
                    try {
                        final ProgressDialog progress = new ProgressDialog(getActivity(), R.style.StyledDialog);
                        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progress.setCancelable(false);
                        progress.show();

                        realm = Realm.getDefaultInstance();
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                ContactBlockList contactBlockList = new ContactBlockList();
                                contactBlockList.setDisplayName(callLog.getName());
                                contactBlockList.setPhoneNumbers(callLog.getNumber());
                                contactBlockList.setImageUri(callLog.getImageUri());
                                realm.copyToRealm(contactBlockList);
                                progress.dismiss();
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    functionDialog.dismiss();
                    break;
                case GlobalVars.DIALOG_FUNCTION_UNBLOCK_CONTACT:
                    try {
                        final ProgressDialog progress = new ProgressDialog(getActivity(), R.style.StyledDialog);
                        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progress.setCancelable(false);
                        progress.show();

                        realm = Realm.getDefaultInstance();
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                RealmResults<ContactBlockList> result = realm.where(ContactBlockList.class).equalTo("phoneNumbers", callLog.getNumber()).findAll();
                                result.deleteAllFromRealm();
                                progress.dismiss();
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    functionDialog.dismiss();
                    break;
            }
        }
    }

    private boolean deleteLogsFromNumber(String number) {
        final String logNumber = number;
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<CallLog> callLogResult = realm.where(CallLog.class).equalTo("number", logNumber).findAll();
                    callLogResult.deleteAllFromRealm();
                }
            });
        } catch (Exception exception) {
            return false;
        }
        return true;
    }

    private boolean deleteLog(int id) {
        final int logId = id;
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    final CallLog callLogResult = realm.where(CallLog.class).equalTo("id", logId).findFirst();
                    callLogResult.deleteFromRealm();
                }
            });
        } catch (Exception exception) {
            return false;
        }
        return true;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        onListItemSelect(position);
        return false;
    }

    private void onListItemSelect(int position) {
        adapterRecent.toggleSelection(position);
        //boolean hasCheckedItems = adapterRecent.getSelectedCount() > 0;

    }
}
