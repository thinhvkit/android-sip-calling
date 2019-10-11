package com.ccsidd.rtone.fragments;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ButtonBarLayout;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.adapters.MultiChoiceDialogAdapter;
import com.ccsidd.rtone.objects.Contact;
import com.ccsidd.rtone.objects.ContactBlockList;
import com.ccsidd.rtone.objects.Phone;
import com.ccsidd.rtone.searchview.SearchViewLayout;
import com.ccsidd.rtone.services.Logger;
import com.ccsidd.rtone.utilities.Utility;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import co.moonmonkeylabs.realmsearchview.RealmSearchAdapter;
import co.moonmonkeylabs.realmsearchview.RealmSearchView;
import co.moonmonkeylabs.realmsearchview.RealmSearchViewHolder;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by thinhvo on 11/17/16.
 */

public class BlockContactFragment extends Fragment implements MultiChoiceDialogAdapter.MultiSelectListener {

    public static final String TAG = "BlockContactFragment";
    private RealmSearchView realmSearchView;
    private ContactRecyclerViewAdapter adapter;
    private MultiChoiceDialogAdapter<ContactBlockList> multiContact;
    private Realm realm;
    private MaterialSearchView searchView;
    SearchViewLayout searchViewLayout;

    private Context mContext;

    public BlockContactFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_search, container, false);

        mContext = view.getContext();

//        realmSearchView = (RealmSearchView) view.findViewById(R.id.search_view);
//
        realm = Realm.getDefaultInstance();
//        adapter = new ContactRecyclerViewAdapter(getActivity(), realm, "displayName");
//        realmSearchView.setAdapter(adapter);

        RealmResults<Contact> contactList = realm.where(Contact.class).findAllSorted("displayName", Sort.ASCENDING);
        final ArrayList<ContactBlockList> listContact = new ArrayList<>();
        for (Contact contact : contactList) {
            if (contact.getPhoneNumbers() != null)
                for (Phone phone : contact.getPhoneNumbers()) {
                    if (realm.where(ContactBlockList.class)
                            .contains("phoneNumbers", phone.getNumber())
                            .findAll().size() > 0)
                        continue;
                    ContactBlockList con = new ContactBlockList();
                    con.setDisplayName(contact.getDisplayName());
                    con.setImageUri(contact.getImageUri());
                    con.setPhoneNumbers(phone.getNumber());
                    listContact.add(con);
                }
        }

        final AlertDialog builder = new AlertDialog.Builder(getActivity(), R.style.MyDialogTheme).create();
        multiContact = new MultiChoiceDialogAdapter<>(getActivity(), R.layout.fragment_contact_search_row, listContact);
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View viewSub = layoutInflater.inflate(R.layout.fragment_contact_search, null);
        builder.setView(viewSub);

//        FontEditText search = (FontEditText) view.findViewById(R.id.select_contact_searchText);
//        if (listContact.size() < 10) {
//            search.setVisibility(View.GONE);
//        }
//        search.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                multiContact.getFilter().filter(s.toString());
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });

        CheckBox chkAll = (CheckBox) view.findViewById(R.id.chkAllContact);
        chkAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                multiContact.checkAll(isChecked);
            }
        });
        Button btnSave = (Button) view.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ArrayList<ContactBlockList> users = multiContact.getSelection();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        for (final ContactBlockList contact : users) {
                            ContactBlockList contactBlockList = new ContactBlockList();
                            contactBlockList.setDisplayName(contact.getDisplayName());
                            contactBlockList.setPhoneNumbers(contact.getPhoneNumbers());
                            contactBlockList.setImageUri(contact.getImageUri());
                            realm.copyToRealm(contactBlockList);
                        }
                        searchViewLayout.collapse();
                    }
                });
            }
        });
        ListView lvContact = (ListView) view.findViewById(R.id.select_contact_list);
        lvContact.setAdapter(multiContact);
        lvContact.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        multiContact.setOriginalSource(listContact);
        multiContact.setMultiSelectListener(this);

        searchViewLayout = (SearchViewLayout) getActivity().findViewById(R.id.search_view_container);
        searchViewLayout.setSearchBoxListener(new SearchViewLayout.SearchBoxListener() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                multiContact.getFilter().filter(s.toString());

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
            realm = null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.block_contact_menu, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        //searchView.setMenuItem(searchMenuItem);

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
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_block:
                final ArrayList<ContactBlockList> users = multiContact.getSelection();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        for (final ContactBlockList contact : users) {
                            ContactBlockList contactBlockList = new ContactBlockList();
                            contactBlockList.setDisplayName(contact.getDisplayName());
                            contactBlockList.setPhoneNumbers(contact.getPhoneNumbers());
                            contactBlockList.setImageUri(contact.getImageUri());
                            realm.copyToRealm(contactBlockList);
                        }
                    }
                });

                return true;
            /*case R.id.select_all:
                if (multiContact.getSelection().size() == multiContact.getCount())
                    multiContact.checkAll(false);
                else
                    multiContact.checkAll(true);
                return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMultiSelectStateChanged(boolean enabled) {
        if (enabled)
            getActivity().invalidateOptionsMenu();
    }

    public class ContactRecyclerViewAdapter
            extends RealmSearchAdapter<Contact, ContactRecyclerViewAdapter.ViewHolder> {
        Context context;
        Map<String, Boolean> checkBoxState;
        ArrayList<String> selection;

        public ContactRecyclerViewAdapter(
                Context context,
                Realm realm,
                String filterColumnName) {
            super(context, realm, filterColumnName);
            this.context = context;
            checkBoxState = new HashMap<>();
            selection = new ArrayList<>();
        }

        @Override
        public ViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int viewType) {
            ViewHolder vh = new ViewHolder(new ContactItemView(viewGroup.getContext()));
            return vh;
        }

        @Override
        public void onBindRealmViewHolder(ViewHolder viewHolder, int position) {
            final Contact contact = realmResults.get(position);
            viewHolder.contactItemView.tvName.setText(contact.getDisplayName());

            for (final Phone phone : contact.getPhoneNumbers()) {

                LinearLayout linearLayout = new LinearLayout(context);
                TextView textView = new TextView(context);
                Switch aSwitch = new Switch(context);
                linearLayout.addView(textView);
                linearLayout.addView(aSwitch);
                linearLayout.setPadding(0, 10, 0, 10);
                viewHolder.contactItemView.layoutPhone.addView(linearLayout);
                textView.setText(phone.getNumber());

                final boolean check = checkBoxState.get(phone.getNumber()) != null
                        ? checkBoxState.get(phone.getNumber())
                        : false;
                aSwitch.setChecked(check);
                aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (b) {
                            checkBoxState.put(phone.getNumber(), true);
                            selection.add(phone.getNumber());
                        } else {
                            checkBoxState.put(phone.getNumber(), false);
                            selection.remove(phone.getNumber());
                        }
                    }
                });
            }

            if (contact.getImageUri() != null && contact.getImageUri().length() > 0) {
                Bitmap bitmap = Utility.loadBitmapFromPath(context, contact.getImageUri());
                if (bitmap == null)
                    viewHolder.contactItemView.imgAvatar.setImageURI(Uri.parse(contact.getImageUri()));
                else
                    viewHolder.contactItemView.imgAvatar.setImageBitmap(bitmap);

            } else
                viewHolder.contactItemView.imgAvatar.setImageResource(R.drawable.unknown_contact);
        }

        public void checkAll(boolean check) {
            selection.clear();
            for (Contact key : realmResults) {
                for (Phone phone : key.getPhoneNumbers()) {
                    if (check)
                        selection.add(phone.getNumber());
                    checkBoxState.put(phone.getNumber(), check);
                }
            }

            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateFooterViewHolder(ViewGroup viewGroup) {
            View v = inflater.inflate(R.layout.footer_view, viewGroup, false);
            return new ViewHolder(
                    (FrameLayout) v,
                    (TextView) v.findViewById(R.id.txtTotal_Contact));
        }

        @Override
        public void onBindFooterViewHolder(ViewHolder holder, int position) {
            super.onBindFooterViewHolder(holder, position);
            holder.itemView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    }
            );
        }

        public class ViewHolder extends RealmSearchViewHolder {

            private ContactItemView contactItemView;

            public ViewHolder(FrameLayout container, TextView footerTextView) {
                super(container, footerTextView);
            }

            public ViewHolder(ContactItemView contactItemView) {
                super(contactItemView);
                this.contactItemView = contactItemView;
            }
        }
    }

    class ContactItemView extends RelativeLayout {

        TextView tvName;
        LinearLayout layoutPhone;
        ImageView imgAvatar;

        public ContactItemView(Context context) {
            super(context);
            init(context);
        }

        private void init(Context context) {
            View view = inflate(context, R.layout.fragment_contact_search_row, this);
            tvName = (TextView) view.findViewById(R.id.dialog_contact_row_name);
            //layoutPhone = (LinearLayout) view.findViewById(R.id.layout_phone_check);
            imgAvatar = (ImageView) view.findViewById(R.id.dialog_contact_row_imgv);
        }
    }
}
