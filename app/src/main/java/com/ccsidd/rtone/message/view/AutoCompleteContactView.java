package com.ccsidd.rtone.message.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.MultiAutoCompleteTextView;

import com.ccsidd.rtone.chips.BaseRecipientAdapter;
import com.ccsidd.rtone.chips.RecipientEditTextView;
import com.ccsidd.rtone.objects.Contact;
import com.ccsidd.rtone.objects.Phone;
import com.ccsidd.rtone.message.common.FontManager;
import com.ccsidd.rtone.message.ThemeManager;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class AutoCompleteContactView extends RecipientEditTextView {
    public static final String TAG = "AutoCompleteContactView";

    private Context mContext;
    private BaseRecipientAdapter mAdapter;

    public AutoCompleteContactView(Context context) {
        this(context, null);
        if (!isInEditMode()) {
            init(context);
        }
    }

    public AutoCompleteContactView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init(context);
        }
    }

    private void init(Context context) {
        mContext = (Context) context;

        //mAdapter = new BaseRecipientAdapter(BaseRecipientAdapter.QUERY_TYPE_PHONE, getContext());
        final ArrayList<Contact> result = new ArrayList<>();
//        String defaultAccount = Utility.getPref(mContext, GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_ACCOUNT_DEFAULT);
//        Utility.configureRealm(mContext, defaultAccount);
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Contact> contactListFromDB = realm.where(Contact.class).findAllSorted("displayName", Sort.ASCENDING);
        for (Contact contact : contactListFromDB) {
            Contact con = new Contact();
            con.setId(contact.getId());
            con.setDisplayName(contact.getDisplayName());
            con.setImageUri(contact.getImageUri());
            for (Phone phone : contact.getPhoneNumbers()) {
                Phone pho = new Phone();
                pho.setLabel(phone.getLabel());
                pho.setNumber(phone.getNumber());
                pho.setPrimary(phone.isPrimary());
                pho.setType(phone.getType());
                con.getPhoneNumbers().add(pho);
            }
            result.add(con);
        }
        realm.close();

        mAdapter = new BaseRecipientAdapter(result, getContext(), 10);

        setThreshold(1);
        setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        setAdapter(mAdapter);
        setOnItemClickListener(this);

        setTypeface(FontManager.getFont(mContext));
//        LiveViewManager.registerView(QKPreference.FONT_FAMILY, this, key -> {
//            setTypeface(FontManager.getFont(mContext));
//        });

        setTypeface(FontManager.getFont(mContext));
//        LiveViewManager.registerView(QKPreference.FONT_WEIGHT, this, key -> {
//            setTypeface(FontManager.getFont(mContext));
//        });

        setTextSize(TypedValue.COMPLEX_UNIT_SP, FontManager.getTextSize(mContext, FontManager.TEXT_TYPE_PRIMARY));
        /*LiveViewManager.registerView(QKPreference.FONT_SIZE, this, key -> {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, FontManager.getTextSize(mContext, FontManager.TEXT_TYPE_PRIMARY));
        });*/

        setTextColor(ThemeManager.getTextOnBackgroundPrimary());
        setHintTextColor(ThemeManager.getTextOnBackgroundSecondary());
//        LiveViewManager.registerView(QKPreference.BACKGROUND, this, key -> {
//            setTextColor(ThemeManager.getTextOnBackgroundPrimary());
//            setHintTextColor(ThemeManager.getTextOnBackgroundSecondary());
//        });

        mAdapter.setShowMobileOnly(true);
        /*LiveViewManager.registerView(QKPreference.MOBILE_ONLY, this, key -> {
            if (mAdapter != null) {
                SharedPreferences prefs1 = mContext.getPrefs();
                mAdapter.setShowMobileOnly(prefs1.getBoolean(SettingsFragment.MOBILE_ONLY, false));
            }
        });*/
    }
}
