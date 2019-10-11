package com.ccsidd.rtone.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.message.ThemeManager;
import com.ccsidd.rtone.message.view.QKTextView;
import com.ccsidd.rtone.objects.Contact;
import com.ccsidd.rtone.objects.ContactList;
import com.ccsidd.rtone.objects.Phone;
import com.ccsidd.rtone.services.Logger;
import com.ccsidd.rtone.utilities.GlobalVars;
import com.ccsidd.rtone.utilities.ThemeUtils;
import com.ccsidd.rtone.utilities.Utility;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmList;

public class ContactActivity extends RuntimePermissionsActivity implements View.OnClickListener {

    public static final int REQUEST_CODE_GALLERY = 0x1;
    public static final int REQUEST_CODE_TAKE_PICTURE = 0x2;
    public static final int REQUEST_CODE_CROP_IMAGE = 0x3;
    final int REQUEST_CODE_ASK_CONTACT_PERMISSIONS = 125;
    final int REQUEST_CODE_ASK_CAMERA_PERMISSIONS = 126;
    String TAG = ContactActivity.class.getName();
    private LinearLayout layoutContact;
    private ImageView imgContactPhoto;
    private EditText edtName;
    private Button btnActionAdd;
    private Button btnSaveRtone;
    private Button btnSaveBoth;
    private String imagePath = null;
    private Uri uri;
    private ExifInterface exif;
    private Bitmap rotateBitmap;
    private Bitmap bitmap;
    private int contactID;
    private ContactList contactList;
    private Uri mCropImageUri;
    private Realm realm;
    private boolean hasContactPermission = false;

    private Toolbar mToolbar;
    private QKTextView mTitle;
    private ImageView mOverflowButton;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        colorMenuIcons(menu, ThemeManager.getTextOnColorPrimary());
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String themes = Utility.getPref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_THEMES);
        if (themes.isEmpty())
            themes = "0";
        ThemeUtils.setsTheme(Integer.parseInt(themes));
        ThemeUtils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_add);


        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar == null) {
            throw new RuntimeException("Toolbar not found in BaseActivity layout.");
        } else {
            mTitle = (QKTextView) mToolbar.findViewById(R.id.toolbar_title);
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        layoutContact = (LinearLayout) findViewById(R.id.layout_contact_detail);
        imgContactPhoto = (ImageView) findViewById(R.id.imgContactPhoto);
        imgContactPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //selectImage();
                uploadPhotoFromCamera();
            }
        });
        edtName = (EditText) findViewById(R.id.edt_ContactName);
        btnActionAdd = (Button) findViewById(R.id.btn_action_add);
        btnSaveRtone = (Button) findViewById(R.id.btnSaveRtone);
        btnSaveRtone.setOnClickListener(this);

        btnSaveBoth = (Button) findViewById(R.id.btnSaveBoth);
        btnSaveBoth.setOnClickListener(this);
//        final RealmList<Phone> phones = new RealmList<>();
        contactList = new ContactList(this);

        contactID = getIntent().getIntExtra("id", 0);

        realm = Realm.getDefaultInstance();
        Contact contact = realm.where(Contact.class).equalTo("id", contactID).findFirst();

        if (mTitle != null) {
            mTitle.setText(R.string.action_add_contact);
        }

        final ArrayAdapter<CharSequence> phonetype_adapter =
                ArrayAdapter.createFromResource(this, R.array.phone_types, R.layout.spinner_text);

        if (contact != null) {
            mTitle.setText(R.string.action_edit_contact);
            Bitmap bitmap;
            if (contact.getImageUri() == null) {
                bitmap = null;
            } else if (contact.getImageUri().length() > 0) {
                bitmap = Utility.loadBitmapFromPath(this, contact.getImageUri());
            } else {

                bitmap = Utility.getBitmapFromContact(this, contact.getId());
            }

            if (bitmap != null) {
                rotateBitmap = bitmap;
                imgContactPhoto.setImageBitmap(bitmap);
            } else
                imgContactPhoto.setImageResource(R.drawable.unknown_contact);

            //Sub phoneNumber view
            RealmList<Phone> phone = contact.getPhoneNumbers();
            for (Phone p : phone) {
                final View viewPhone = getLayoutInflater().inflate(R.layout.contact_add_phone, null);
                EditText edtPhone = (EditText) viewPhone.findViewById(R.id.edt_ContactPhone);
                Spinner spPhoneType = (Spinner) viewPhone.findViewById(R.id.spin_PhoneType);
                edtName.setText(contact.getDisplayName());
                edtPhone.setText(p.getNumber());
                Button btnActionSub = (Button) viewPhone.findViewById(R.id.btn_action_sub);
                btnActionSub.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 2 view not phoneNumber view
                        if (layoutContact.getChildCount() > 2) {
                            layoutContact.removeView(viewPhone);
                        }
                    }
                });
                phonetype_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spPhoneType.setAdapter(phonetype_adapter);
                spPhoneType.setSelection(p.getType() - 1);
                layoutContact.addView(viewPhone);
            }
        } else {
            //Sub phoneNumber view
            final View viewPhone = getLayoutInflater().inflate(R.layout.contact_add_phone, null);
            EditText edtPhone = (EditText) viewPhone.findViewById(R.id.edt_ContactPhone);
            Spinner spPhoneType = (Spinner) viewPhone.findViewById(R.id.spin_PhoneType);
            if (contact != null) {
                edtName.setText(contact.getDisplayName());
                edtPhone.setText(contact.getPhoneNumbers().first().getNumber());
                spPhoneType.setSelection(contact.getPhoneNumbers().first().getType() - 1);
            }

            Button btnActionSub = (Button) viewPhone.findViewById(R.id.btn_action_sub);
            btnActionSub.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 2 view not phoneNumber view
                    if (layoutContact.getChildCount() > 2) {
                        layoutContact.removeView(viewPhone);
                    }
                }
            });
            phonetype_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spPhoneType.setAdapter(phonetype_adapter);
            layoutContact.addView(viewPhone);
        }

        btnActionAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutContact.getChildCount() - 2 < phonetype_adapter.getCount()) {
                    final View viewPhone = getLayoutInflater().inflate(R.layout.contact_add_phone, null);
                    EditText edtPhone = (EditText) viewPhone.findViewById(R.id.edt_ContactPhone);
                    Spinner spPhoneType = (Spinner) viewPhone.findViewById(R.id.spin_PhoneType);
                    Button btnActionSub = (Button) viewPhone.findViewById(R.id.btn_action_sub);
                    phonetype_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spPhoneType.setAdapter(phonetype_adapter);
                    btnActionSub.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 2 view not phoneNumber view
                            if (layoutContact.getChildCount() > 2) {
                                layoutContact.removeView(viewPhone);
                            }
                        }
                    });
                    layoutContact.addView(viewPhone);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    private void clickPhotoFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), imageFileName);
        uri = Uri.fromFile(imageStorageDir);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);

    }

    private void uploadPhotoFromCamera() {
        CropImage.startPickImageActivity(this);
    }

    public void displayImageBitmap(String image_path) {
        try {
            File mediaFile = new File(image_path);
            Bitmap myBitmap = BitmapFactory.decodeFile(mediaFile.getAbsolutePath());
            int height = (myBitmap.getHeight() * 512 / myBitmap.getWidth());
            Bitmap scale = Bitmap.createScaledBitmap(myBitmap, 512, height, true);
            int rotate = 0;
            exif = new ExifInterface(mediaFile.getAbsolutePath());

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    rotate = 0;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }

            Matrix matrix = new Matrix();
            matrix.postRotate(rotate);
            rotateBitmap = Bitmap.createBitmap(scale, 0, 0, scale.getWidth(), scale.getHeight(), matrix, true);
            imgContactPhoto.setImageBitmap(rotateBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                mCropImageUri = imageUri;
//                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                requestAppPermissions(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE},
                        R.string.runtime_permissions_txt,
                        REQUEST_CODE_ASK_CAMERA_PERMISSIONS);
            } else {
                // no permissions required or already grunted, can start crop image activity
                startCropImageActivity(imageUri);
            }
        }

        // handle result of CropImageActivity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                displayImageBitmap(result.getUri().getPath());
                Toast.makeText(this, "Cropping successful, Sample: " + result.getSampleSize(), Toast.LENGTH_LONG).show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // required permissions granted, start crop image activity
            startCropImageActivity(mCropImageUri);
        } else {
            Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
        }
    }*/

    @Override
    public void onPermissionsGranted(int requestCode) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_CAMERA_PERMISSIONS:
                if (mCropImageUri != null)
                    startCropImageActivity(mCropImageUri);
                break;
            case REQUEST_CODE_ASK_CONTACT_PERMISSIONS:
                hasContactPermission = true;
                break;

        }
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .setScaleType(CropImageView.ScaleType.CENTER)
                .setMultiTouchEnabled(true)
                .start(this);
    }

    @Override
    public void onClick(View v) {
        final Contact contact = new Contact();
        final RealmList<Phone> phones = new RealmList<>();
        contact.setDisplayName(edtName.getText().toString());

        for (int i = 0; i < layoutContact.getChildCount(); i++) {
            View view = layoutContact.getChildAt(i);
            if (view.getId() == R.id.layout_phone_detail) {
                Phone phone = new Phone();
                for (int j = 0; j < ((LinearLayout) view).getChildCount(); j++) {
                    View view2 = ((LinearLayout) view).getChildAt(j);
                    if (view2.getId() == R.id.edt_ContactPhone)
                        phone.setNumber(((EditText) view2).getText().toString());
                    else if (view2.getId() == R.id.spin_PhoneType)
                        phone.setType(((Spinner) view2).getSelectedItemPosition() + 1);
                }
                phones.add(phone);
            }
        }
        if (phones.size() == 0)
            return;
        contact.setPhoneNumbers(phones);
        if (contactID != 0) {
            contact.setId(contactID);
        } else {
            contact.setId((int) new Date().getTime());
        }

        //String defaultAccount = Utility.getPref(this, GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_ACCOUNT_DEFAULT);
        //Utility.configureRealm(this, defaultAccount);

        switch (v.getId()) {
            case R.id.btnSaveRtone:
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        if (rotateBitmap != null) {
                            contact.setImageUri(contact.getId() + contact.getDisplayName() + ".jpg");
                            Utility.saveFile(ContactActivity.this, rotateBitmap, contact.getImageUri());
                        }

                        Realm realm = Realm.getDefaultInstance();
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.copyToRealmOrUpdate(contact);
                            }
                        });
                        realm.close();

                        return null;
                    }
                }.execute();

                finish();
                break;
            case R.id.btnSaveBoth:

                requestAppPermissions(new String[]{
                                Manifest.permission.WRITE_CONTACTS},
                        R.string.runtime_permissions_txt,
                        REQUEST_CODE_ASK_CONTACT_PERMISSIONS);

                if(hasContactPermission) {
                    new AsyncTask<Void, Void, Void>() {

                        @Override
                        protected Void doInBackground(Void... voids) {
                            if (contactID != 0) {
                                if (contactList.getAllNumber(contactID).size() > 0) {
                                    updateContact(contact, rotateBitmap);
                                } else {
                                    addContact(contact);
                                    Realm realm = Realm.getDefaultInstance();
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            realm.where(Contact.class).equalTo("id", contactID).findAll().deleteAllFromRealm();
                                        }
                                    });
                                    realm.close();
                                }

                            } else {
                                addContact(contact);
                            }
                            contactList.fetchAll();

                            return null;
                        }
                    }.execute();
                    finish();
                }
                break;
        }
    }

    public void addContact(Contact contact) {
        ArrayList<ContentProviderOperation> insertOperation = new ArrayList<>();
        int rawContactID = insertOperation.size();

        // Adding insert operation to operations list
        // For insert a new raw contact in the ContactsContract.RawContacts
        insertOperation.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if (rotateBitmap != null) {    // If an image is selected successfully
            rotateBitmap.compress(Bitmap.CompressFormat.PNG, 75, stream);

            // For insert Photo in the ContactsContract.Data
            insertOperation.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                    .withValue(ContactsContract.Data.IS_SUPER_PRIMARY, 1)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, stream.toByteArray())
                    .build());

            try {
                stream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // For insert display phoneNumber in the ContactsContract.Data
        insertOperation.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.getDisplayName())
                .build());
        // For insert Mobile Number in the ContactsContract.Data

        for (Phone phone : contact.getPhoneNumbers()) {
            insertOperation.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone.getNumber())
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, phone.getType())
                    .build());
        }

        // For insert Work Email in the ContactsContract.Data
        /*insertOperation.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                .withValue(ContactsContract.Data.MIMETYPE, Email.CONTENT_ITEM_TYPE)
                .withValue(Email.ADDRESS, "")
                .withValue(Email.TYPE, Email.TYPE_WORK)
                .build());*/
        try {
            // Executing all the insert operations as a single database transaction
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, insertOperation);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }


    public Boolean hasPhoneType(RealmList<Phone> phones, Phone phoneNd) {
        for (Phone phone : phones) {
            if (phone.getType() == phoneNd.getType())
                return true;

        }
        return false;
    }

    private void updateContact(final Contact contact, Bitmap bitmap) {
        try {
            String id = contact.getId() + "";
            String name = contact.getDisplayName();
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();

            ops.add(ContentProviderOperation
                    .newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE
                            + "=?", new String[]{id, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE})
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                    .build());

            deletePhone(contact);
            for (Phone phone : contact.getPhoneNumbers()) {

//                if (hasPhoneType(phones, phoneNumber)) {
//                    ops.add(ContentProviderOperation
//                            .newUpdate(ContactsContract.Data.CONTENT_URI)
//                            .withSelection(ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE
//                                    + "=? AND " + ContactsContract.CommonDataKinds.Organization.TYPE + "=?"
//                                    , new String[]{id, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
//                                    , String.valueOf(phoneNumber.getType())})
//                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber.getNumber())
//                            .build());
//                } else {
                long rawContactId = -1;
                Cursor c = getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,
                        new String[]{ContactsContract.RawContacts._ID},
                        ContactsContract.RawContacts.CONTACT_ID + "=?",
                        new String[]{String.valueOf(contact.getId())}, null);
                try {
                    if (c.moveToFirst()) {
                        rawContactId = c.getLong(0);
                    }
                } finally {
                    c.close();
                }

                ContentValues values = new ContentValues();
                values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
                values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone.getNumber());
                values.put(ContactsContract.CommonDataKinds.Phone.TYPE, phone.getType());
                values.put(ContactsContract.CommonDataKinds.Phone.LABEL, "free directory assistance");
                Uri dataUri = getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

//                }
            }
            if (bitmap != null) {
                ByteArrayOutputStream image = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, image);

                ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(ContactsContract.Data.CONTACT_ID + "=? AND " +
                                ContactsContract.Data.MIMETYPE + "=?", new String[]{id, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE})
                        .withValue(ContactsContract.Data.IS_SUPER_PRIMARY, 1)
                        .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, image.toByteArray())
                        .build());
            }
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deletePhone(final Contact contact) {
        Cursor cur = getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts._ID},
                ContactsContract.RawContacts.CONTACT_ID + "=?",
                new String[]{String.valueOf(contact.getId())}, null);
        int rowId = -1;
        if (cur.moveToFirst()) {
            rowId = cur.getInt(cur.getColumnIndex(ContactsContract.RawContacts._ID));
        }

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        String selectPhone = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " +
                ContactsContract.Data.MIMETYPE + " = ?";
        String[] phoneArgs = new String[]{Integer.toString(rowId),
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE};

        ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                .withSelection(selectPhone, phoneArgs).build());
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }
}
