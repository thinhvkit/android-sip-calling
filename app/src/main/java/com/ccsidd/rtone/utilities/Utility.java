package com.ccsidd.rtone.utilities;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.ccsidd.rtone.Migration;
import com.ccsidd.rtone.R;
import com.ccsidd.rtone.activities.CallActivity;
import com.ccsidd.rtone.message.view.AvatarView;
import com.ccsidd.rtone.objects.Contact;
import com.ccsidd.rtone.objects.ContactBlockList;
import com.ccsidd.rtone.objects.TelephonyInfo;
import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class Utility {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static ArrayList<ToneGenerator> tones = new ArrayList<>();
    private static Migration migration = new Migration();
    private static Point size;

    public static void callNumber(final Context context, final String phoneNumber) {
        if (phoneNumber.length() <= 0)
            return;

        String number = convertDial(context, phoneNumber);

        String realNumber = formatNumberphone(number);

        Intent callIntent = new Intent(context, CallActivity.class);
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        callIntent.putExtra("kind", "out");
        callIntent.putExtra("real", realNumber);
        context.startActivity(callIntent);
    }

    public static String formatNumberphone(String phoneNumber) {
        if (phoneNumber != null && phoneNumber.length() > 8 && phoneNumber.charAt(0) != '+')
            return "+" + phoneNumber.replace("-", "").replace(" ", "").replace("(", "").replace(")", "");
        else
            return phoneNumber.replace("-", "").replace(" ", "").replace("(", "").replace(")", "");
    }

    public static Contact getContactDisplayNameByNumber(final String number) {
        Realm realm = Realm.getDefaultInstance();
        Contact contact = new Contact();
        if (number.length() < 8) {
            contact.setDisplayName(number);
            contact.setImageUri("");
            return contact;
        } else {
            Contact ct = realm.where(Contact.class).endsWith("phoneNumbers.number", number.substring(number.length() - 8)).findFirst();
            if (ct == null) {
                contact.setDisplayName(number);
                contact.setImageUri("");

            } else {
                contact.setId(ct.getId());
                contact.setDisplayName(ct.getDisplayName());
                contact.setPhoneNumbers(ct.getPhoneNumbers());
                contact.setImageUri(ct.getImageUri());
            }

            realm.close();
            return contact;

        }
    }

    public static String convertDial(Context context, String phoneNumber) {
//        boolean blVQstate = Boolean.valueOf(getPref(context, GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_VQ_MODE));
        String number = phoneNumber;
//        if (!blVQstate) {
//            if (!number.startsWith("+65"))
//                number = number.replaceFirst("^\\+", GlobalVars.DIAL_PREMIUM_MODE_PLUS);
//            number = number.replaceFirst("^00\\d", GlobalVars.DIAL_PREMIUM_MODE_00x);
//            number = number.replaceFirst("^011", GlobalVars.DIAL_PREMIUM_MODE_011);
//            number = number.replaceFirst("^01\\d", GlobalVars.DIAL_PREMIUM_MODE_01x);
//            number = number.replaceFirst("^020", GlobalVars.DIAL_PREMIUM_MODE_020);
//            number = number.replaceFirst("^02\\d", GlobalVars.DIAL_PREMIUM_MODE_02x);
//            number = number.replaceFirst("^030", GlobalVars.DIAL_PREMIUM_MODE_030);
//            if (number.startsWith("15050"))
//                number = number.replaceFirst("15050", GlobalVars.DIAL_PREMIUM_MODE_15xx);
//            number = number.replaceFirst("^15\\d{2}", GlobalVars.DIAL_PREMIUM_MODE_15xx);
//        } else {
//            if (!number.startsWith("+65"))
//                number = number.replaceFirst("^\\+", GlobalVars.DIAL_VQ_MODE_PLUS);
//            number = number.replaceFirst("^00\\d", GlobalVars.DIAL_VQ_MODE_00x);
//            number = number.replaceFirst("^011", GlobalVars.DIAL_VQ_MODE_011);
//            number = number.replaceFirst("^01\\d", GlobalVars.DIAL_VQ_MODE_01x);
//            number = number.replaceFirst("^020", GlobalVars.DIAL_VQ_MODE_020);
//            number = number.replaceFirst("^02\\d", GlobalVars.DIAL_VQ_MODE_02x);
//            number = number.replaceFirst("^030", GlobalVars.DIAL_VQ_MODE_030);
//            if (!number.startsWith("15050"))
//                number = number.replaceFirst("^15\\d{2}", GlobalVars.DIAL_VQ_MODE_15xx);
//        }

        //for test
        if (number.startsWith("86000") || number.startsWith("68000") || number.startsWith("85000"))
            return number;
        //
        if (number.startsWith("3164")) {
            number = number.replaceFirst("^3164", "+653164");
        } else if (number.startsWith("653164")) {
            number = number.replaceFirst("^653164", "+653164");
        } else if (number.startsWith("011")) {
            number = number.replaceFirst("011", "+62");
        } else if (number.startsWith("020")) {
            number = number.replaceFirst("020", "+60");
        } else if (number.startsWith("030")) {
            number = number.replaceFirst("030", "+60");
        } else if (number.startsWith("6") || number.startsWith("8") || number.startsWith("9")) {
            number = "+65" + number;
        } else if (number.length() > 2 && number.substring(0, 3).matches("^00\\d")) {
            number = number.replaceFirst("^00\\d", "+");
        } else if (number.length() > 2 && number.substring(0, 3).matches("^01\\d")) {
            number = number.replaceFirst("^01\\d", "+");
        } else if (number.length() > 3 && number.substring(0, 4).matches("^15\\d\\d")) {
            number = number.replaceFirst("^15\\d\\d", "+");
        } else if (number.startsWith("021")) {
            number = number.replaceFirst("021", "+");
        }

        return number;
    }

    private static void isDualSimOrNot(Context context) {
        TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(context);

        String imeiSIM1 = telephonyInfo.getImeiSIM1();
        String imeiSIM2 = telephonyInfo.getImeiSIM2();

        boolean isSIM1Ready = telephonyInfo.isSIM1Ready();
        boolean isSIM2Ready = telephonyInfo.isSIM2Ready();

        boolean isDualSIM = telephonyInfo.isDualSIM();
        Log.e("Dual = ", " IME1 : " + imeiSIM1 + "\n" +
                " IME2 : " + imeiSIM2 + "\n" +
                " IS DUAL SIM : " + isDualSIM + "\n" +
                " IS SIM1 READY : " + isSIM1Ready + "\t" + "\n" +
                " IS SIM2 READY : " + isSIM2Ready + "\n" +
                " Network Operation SIM1: " + telephonyInfo.getNetworkOperatorSIM1() + "\n" +
                " Network Operation SIM2: " + telephonyInfo.getNetworkOperatorSIM2() + "\n");
    }

    public static void printTelephonyManagerMethodNamesForThisDevice(Context context) {

        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        Class<?> telephonyClass;
        try {
            telephonyClass = Class.forName(telephony.getClass().getName());
            Method[] methods = telephonyClass.getMethods();
            for (int idx = 0; idx < methods.length; idx++) {

                Log.e("info", methods[idx] + " declared by " + methods[idx].getDeclaringClass());
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String getNumberLabel(int numberType, String label) {
        String result = "";
        switch (numberType) {
            case ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM:
                if (label != null)
                    result = label;
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                result = "Home";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                result = "Mobile";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                result = "Work";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK:
                result = "Fax Work";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME:
                result = "Fax Home";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_PAGER:
                result = "Pager";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
                result = "Other";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_CALLBACK:
                result = "Callback";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_CAR:
                result = "Car";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN:
                result = "Company Main";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_ISDN:
                result = "ISDN";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:
                result = "Main";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX:
                result = "Other Fax";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_RADIO:
                result = "Radio";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_TELEX:
                result = "Telex";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_TTY_TDD:
                result = "TTY TDD";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:
                result = "Work Mobile";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER:
                result = "Work Pager";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_ASSISTANT:
                result = "Assistant";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_MMS:
                result = "MMS";
                break;
        }
        return result;
    }

    public static void removePref(Context context, String name, String key) {
        SharedPreferences pre = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.remove(key);
        editor.commit();
    }

    public static void savePref(Context context, String name, String key, String data) {
        SharedPreferences pre = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.putString(key, data);
        editor.commit();
    }

    public static String getPref(Context context, String name, String key) {
        SharedPreferences pre = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        String result = pre.getString(key, "");
        return result;
    }

    public static void runOnUiThread(Runnable runnable) {
        final Handler UIHandler = new Handler(Looper.getMainLooper());
        UIHandler.post(runnable);
    }

    public static void runOnMainThread(Context context, Runnable runnable) {
        Handler mainHandler = new Handler(context.getMainLooper());
        mainHandler.post(runnable);
    }

    public static Gson createGson() {
        Gson gson = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .create();
        return gson;
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /*public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;
    public static int TYPE_NOT_CONNECTED = 0;

    public static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI && activeNetwork.isAvailable())
                return TYPE_WIFI;

            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE && activeNetwork.isAvailable())
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    public static String getConnectivityStatusString(Context context) {
        int conn = getConnectivityStatus(context);
        String status = null;
        if (conn == TYPE_WIFI) {
            status = "Wifi enabled";
        } else if (conn == TYPE_MOBILE) {
            status = "Mobile data enabled";
        } else if (conn == TYPE_NOT_CONNECTED) {
            status = "Not connected to Internet";
        }
        return status;
    }

    public static boolean checkNetwork(Context context)
    {
        ConnectivityManager manager = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();

        if(null != activeNetwork){
            boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
            if(isWiFi)
                return true;
            else {
                String data = getPref(context, GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_APPLICATION_VOIP_OVER_3G);
                if (data.isEmpty())
                    data = "true";
                boolean isVoIP = Boolean.parseBoolean(data);
                boolean is3G = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;

                if (isVoIP && is3G)
                    return true;
                else
                    return false;
            }
        }
        else
            return false;
    }*/

    public static boolean checkNetwork(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnectedOrConnecting() && networkInfo.isAvailable());
    }

    private static List<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getListFiles(file));
            }
        }
        return inFiles;
    }

    public static void setupTones() {
        if (tones.size() == 0) {
            tones.add(new ToneGenerator(AudioManager.STREAM_DTMF, ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE));
            tones.add(new ToneGenerator(AudioManager.STREAM_VOICE_CALL, ToneGenerator.MAX_VOLUME));
        }
    }

    public static ToneGenerator getTone(String tone) {
        if (tones == null || tones.size() == 0)
            setupTones();
        ToneGenerator result = null;
        switch (tone.toLowerCase()) {
            case "dtmf":
                result = tones.get(0);
                break;
            case "ringback":
                result = tones.get(1);
                break;
        }
        return result;
    }

    public static void configureRealm(Context context, String fileName) {
        if (fileName == null || fileName.length() == 0) {
            fileName = "default";
        }
        String path = context.getFilesDir() + String.format("/%s", fileName);
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name(String.format("/%s/%s.realm", fileName, fileName))
                .schemaVersion(5)
                .migration(migration)
//                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);

    }

    public static void removeTrash(Context context, String name) {
        File[] files = new File(context.getFilesDir().getAbsolutePath()).listFiles();
        for (File file : files) {
            if (file.isDirectory() && !file.getName().matches(String.format("%s$", name)))
//                Log.e("rtone-utility", String.valueOf(new File(context.getFilesDir().getAbsolutePath() + "/" + name).delete()));
            {
                Realm.init(context);
                RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
//                        .schemaVersion(0)
                        .name(String.format("/%s/%s.realm", file.getName(), file.getName()))
//                .migration(realmMigration)
                        .build();
                if (!Realm.getDefaultInstance().isClosed())
                    Realm.getDefaultInstance().close();
                Realm.deleteRealm(realmConfiguration);
                Log.e("com.ccsidd.rtone", "deleteRealmFile" + realmConfiguration.toString());
            }
        }

//        for (File file : files) {
//            if (file.isDirectory())
//                Log.e("rtone-utility", file.getName());
//        }
    }

    public static boolean blockNumber(String number) {
        //Contact is blocked
        Realm realm = Realm.getDefaultInstance();
        RealmResults<ContactBlockList> results = realm.where(ContactBlockList.class)
                .contains("phoneNumbers", Utility.formatNumberphone(number)).findAll();
        if (results.size() > 0) {
            realm.close();
            return true;
        }
        realm.close();
        return false;
    }

    public static void saveFile(Context context, Bitmap b, String picName) {
        FileOutputStream fos;
        try {
            if (b == null)
                return;
            fos = context.openFileOutput(picName, Context.MODE_PRIVATE);
            b.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(context.getPackageName(), "file not found");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(context.getPackageName(), "io exception");
            e.printStackTrace();
        }

    }

    public static Bitmap loadBitmapFromPath(Context context, String picName) {
        Bitmap b = null;
        FileInputStream fis;
        try {
            if (fileExist(context, picName)) {
                fis = context.openFileInput(picName);
                b = BitmapFactory.decodeStream(fis);
                fis.close();
            } else
                return null;

        } catch (FileNotFoundException e) {
            Log.d(context.getPackageName(), "file not found");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(context.getPackageName(), "io exception");
            e.printStackTrace();
        }
        return b;
    }

    public static void deleteFileBitmap(Context context, String filePath) {
        String finalPath = context.getFilesDir().toString() + File.separator + filePath;
        File file = new File(finalPath);
        if (file.exists())
            file.delete();
    }

    public static Boolean fileExist(Context context, String filePath) {
        if (filePath == null)
            return false;
        String finalPath = context.getFilesDir().toString() + File.separator + filePath;
        File file = new File(finalPath);
        return file.exists();
    }

   /* public static boolean checkPlayServices(Activity activity) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(activity.getPackageName(), "This device is not supported.");
                activity.finish();
            }
            return false;
        }
        return true;
    }*/

    public static String convertToStringRepresentation(final long value) {
        long K = 1024;
        long M = K * K;
        long G = M * K;
        long T = G * K;
        final long[] dividers = new long[]{T, G, M, K, 1};
        final String[] units = new String[]{"TB", "GB", "MB", "KB", "B"};
        if (value == 0)
            return 0 + "B";
        if (value < 1)
            throw new IllegalArgumentException("Invalid file size: " + value);
        String result = null;
        for (int i = 0; i < dividers.length; i++) {
            final long divider = dividers[i];
            if (value >= divider) {
                result = format(value, divider, units[i]);
                break;
            }
        }
        return result;
    }

    private static String format(final long value, final long divider, final String unit) {
        final double result =
                divider > 1 ? (double) value / (double) divider : (double) value;
        return new DecimalFormat("#,##0.#").format(result) + unit;
    }

    public static Bitmap getBitmapFromContact(Context context, long id) {
        Bitmap bitmap = null;

        if(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            return bitmap;
        }
        try {
            Uri contactUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(id));
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), contactUri, true);
            if (input == null) {
                return null;
            }
            BufferedInputStream buf = new BufferedInputStream(input);
            bitmap = BitmapFactory.decodeStream(buf);
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static void loadAvatar(final Context context, final ImageView ivAvatar, Contact contact) {
        if (contact.getImageUri() == null) {
            ivAvatar.setImageResource(R.drawable.unknown_contact);
        } else if (contact.getImageUri().length() > 0) {
            Bitmap bitmap = Utility.loadBitmapFromPath(context, contact.getImageUri());
            if (bitmap != null) {
                ivAvatar.setImageBitmap(bitmap);
            } else
                ivAvatar.setImageResource(R.drawable.unknown_contact);
        }else {
            new AsyncTask<Integer, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Integer... integers) {
                    return Utility.getBitmapFromContact(context, integers[0]);
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);
                    if (bitmap != null) {
                        ivAvatar.setImageBitmap(bitmap);
                    } else
                        ivAvatar.setImageResource(R.drawable.unknown_contact);
                }
            }.execute(contact.getId());
        }
    }

    public static void loadAvatar(final Context context, final AvatarView ivAvatar, Contact contact) {
        if (contact.getImageUri() == null) {
            ivAvatar.setImageResource(R.drawable.unknown_contact);
        } else if (contact.getImageUri().length() > 0) {
            Bitmap bitmap = Utility.loadBitmapFromPath(context, contact.getImageUri());
            if (bitmap != null) {
                ivAvatar.setImageDrawable(new BitmapDrawable(context.getResources(), bitmap));
            }else {
                ivAvatar.setImageDrawable(null);
            }
        }else {
            new AsyncTask<Integer, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Integer... integers) {
                    return Utility.getBitmapFromContact(context, integers[0]);
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);
                    if (bitmap != null) {
                        ivAvatar.setImageDrawable(new BitmapDrawable(context.getResources(), bitmap));
                    }else {
                        ivAvatar.setImageDrawable(null);
                    }
                }
            }.execute(contact.getId());
        }
    }

    public static int getScreenWidthInPx(Context context) {
        if(size == null) {
            calculateScreenSize(context);
        }

        switch(getCurrentOrientation(context)) {
            case 1:
            default:
                return size.x;
            case 2:
                return size.y;
        }
    }

    public static int getScreenHeightInPx(Context context) {
        if(size == null) {
            calculateScreenSize(context);
        }

        switch(getCurrentOrientation(context)) {
            case 1:
            default:
                return size.y;
            case 2:
                return size.x;
        }
    }

    private static void calculateScreenSize(Context context) {
        Display var1 = ((WindowManager)context.getSystemService(context.WINDOW_SERVICE)).getDefaultDisplay();
        size = new Point();
        switch(getCurrentOrientation(context)) {
            case 1:
            default:
                size.x = var1.getWidth();
                size.y = var1.getHeight();
                break;
            case 2:
                size.y = var1.getWidth();
                size.x = var1.getHeight();
        }
    }

    public static int getCurrentOrientation(Context context) {
        WindowManager var1 = (WindowManager)context.getApplicationContext().getSystemService(context.WINDOW_SERVICE);
        int var2 = var1.getDefaultDisplay().getRotation();
        return var2 != 3 && var2 != 1?1:2;
    }

    public static int dp(int val, Context context) {
        return (int)(context.getResources().getDisplayMetrics().density * (float)val);
    }

    public static int getViewInset(View view) {
        if(view != null && Build.VERSION.SDK_INT >= 21) {
            try {
                Field var1 = View.class.getDeclaredField("mAttachInfo");
                var1.setAccessible(true);
                Object var2 = var1.get(view);
                if(var2 != null) {
                    Field var3 = var2.getClass().getDeclaredField("mStableInsets");
                    var3.setAccessible(true);
                    Rect var4 = (Rect)var3.get(var2);
                    return var4.bottom;
                }
            } catch (Exception var5) {
                Log.e("Error", "Failed to get view inset", var5);
            }

            return 0;
        } else {
            return 0;
        }
    }
}