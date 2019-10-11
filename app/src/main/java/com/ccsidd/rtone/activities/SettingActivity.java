package com.ccsidd.rtone.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.dialogs.AlertDialog;
import com.ccsidd.rtone.dialogs.SingleChoiceDialog;
import com.ccsidd.rtone.listeners.AlertDialogListener;
import com.ccsidd.rtone.listeners.SingleChoiceDialogListener;
import com.ccsidd.rtone.message.view.QKTextView;
import com.ccsidd.rtone.utilities.GlobalVars;
import com.ccsidd.rtone.utilities.ThemeUtils;
import com.ccsidd.rtone.utilities.Utility;
import com.ccsidd.rtone.view.AnimatedExpandableListView;
import com.innovattic.font.TypefaceManager;

import java.util.ArrayList;
import java.util.List;

public class SettingActivity extends AppCompatActivity {

    private AnimatedExpandableListView listView;
    private ExpandableAdapter adapter;
    private AudioManager am;
    private MediaPlayer ringtonePlayer;
    private Toolbar mToolbar;
    private QKTextView mTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String themes = Utility.getPref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, GlobalVars.PREFERENCES_DATA_THEMES);
        if (themes.isEmpty())
            themes = "0";
        ThemeUtils.setsTheme(Integer.parseInt(themes));
        ThemeUtils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);

        TypefaceManager.initialize(getApplicationContext(), R.xml.fonts);
        // AFTER SETTING THEME
        setContentView(R.layout.activity_setting);

//        LinearLayout layout_back = (LinearLayout) findViewById(R.id.layout_back);
//        layout_back.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//            }
//        });
//        TextView tv = (TextView) findViewById(R.id.navigation_title);
//        tv.setText("Settings");
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar == null) {
            throw new RuntimeException("Toolbar not found in BaseActivity layout.");
        } else {
            mTitle = (QKTextView) mToolbar.findViewById(R.id.toolbar_title);
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (mTitle != null) {
            mTitle.setText("Settings");
        }


        ArrayList<GroupItem> items = getDataForView();

        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        adapter = new ExpandableAdapter(this);
        adapter.setData(items);

        listView = (AnimatedExpandableListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.expandGroup(0);

        listView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int previousGroup = -1;

            @Override
            public void onGroupExpand(int groupPosition) {
                if (groupPosition != previousGroup)
                    listView.collapseGroup(previousGroup);
                previousGroup = groupPosition;
            }
        });

        // In order to show animations, we need to use a custom click handler
        // for our ExpandableListView.
        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                // We call collapseGroupWithAnimation(int) and
                // expandGroupWithAnimation(int) to animate group
                // expansion/collapse.
                if (listView.isGroupExpanded(groupPosition)) {
                    listView.collapseGroupWithAnimation(groupPosition);
                } else {
                    listView.expandGroupWithAnimation(groupPosition);
                }

                GroupItem item = (GroupItem) parent.getAdapter().getItem(groupPosition);
                final GroupItem groupItem = item;
                if (item.type.equals("radio")) {
                    ArrayList<String> data = new ArrayList<>();
                    if (item.id.equals(GlobalVars.PREFERENCES_DATA_TRANSPORT))
                        data = getDataForTransport();
                    else if (item.id.equals(GlobalVars.PREFERENCES_DATA_CODEC))
                        data = getDataForCodec();

                    SingleChoiceDialog dialog = new SingleChoiceDialog(SettingActivity.this, data);
                    String value = Utility.getPref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, item.id);
                    if (value.isEmpty())
                        value = "0";
                    dialog.setCurrentSelected(Integer.parseInt(value));
                    dialog.setDialogListener(new SingleChoiceDialogListener() {
                        @Override
                        public void onClickItemListener(int position, Object data) {
                            Utility.savePref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, groupItem.id, String.valueOf(position));
                        }
                    });
                    dialog.setTitle(item.title);
                    dialog.show();
                }
                return true;
            }

        });

        adapter.setOnChildClick(new ChildListener() {
            @Override
            public void onChildClick(final ChildItem item, final ChildHolder holder) {
                final ChildItem childItem = item;
                final ChildHolder childHolder = holder;
                if (item.type.equals("check")) {
                    String value = Utility.getPref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, item.id);
                    if (value.isEmpty())
                        value = "false";
                    holder.checkBox.setChecked(!Boolean.parseBoolean(value));
                    Utility.savePref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, item.id, String.valueOf(holder.checkBox.isChecked()));
                } else if (item.type.equals("radio")) {
                    ArrayList<String> data = new ArrayList<>();
                    if (item.id.equals(GlobalVars.PREFERENCES_DATA_MEDIA_NOISE))
                        data = getDataForNoise();
                    else if (item.id.equals(GlobalVars.PREFERENCES_DATA_RINGTONES))
                        data = getDataForRingtones();
                    else if (item.id.equals(GlobalVars.PREFERENCES_DATA_THEMES))
                        data = getDataForThemes();
                    final SingleChoiceDialog dialog = new SingleChoiceDialog(SettingActivity.this, data);
                    String value = Utility.getPref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, item.id);
                    if (value.isEmpty())
                        value = "0";
                    dialog.setCurrentSelected(Integer.parseInt(value));
                    dialog.setDialogListener(new SingleChoiceDialogListener() {
                        @Override
                        public void onClickItemListener(int position, Object data) {
                            if (item.id.equals(GlobalVars.PREFERENCES_DATA_MEDIA_NOISE)) {

                            } else if (item.id.equals(GlobalVars.PREFERENCES_DATA_RINGTONES)) {
                                stopRingTone();
                                playRingTone(position);
                            } else if (item.id.equals(GlobalVars.PREFERENCES_DATA_THEMES)) {
                                dialog.dismiss();
                                ThemeUtils.changeToTheme(SettingActivity.this, position);
                                MainActivity.mainContext.recreate();
                            }
                            Utility.savePref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, childItem.id, String.valueOf(position));
                        }
                    });
                    dialog.setTitle(item.title);
                    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            stopRingTone();
                        }
                    });
                    dialog.show();
                } else if (item.type.equals("text")) {
                    AlertDialog alertDialog = new AlertDialog(SettingActivity.this, item.title, "");
                    alertDialog.setPromptDialog(true);
                    alertDialog.setPromptMessage(Utility.getPref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, item.id));
                    alertDialog.setAlertDialogListener(new AlertDialogListener() {
                        @Override
                        public void btnYesPressed(AlertDialog alertDialog) {
                            Utility.savePref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, childItem.id, alertDialog.getPromptMessage());
                            childHolder.detail.setText(alertDialog.getPromptMessage());
                            alertDialog.dismiss();
                        }

                        @Override
                        public void btnNoPressed(AlertDialog alertDialog) {
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.show();
                } else {
//                    Intent intent = new Intent(getApplicationContext(), RingtoneActivity.class);
//                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ringtonePlayer != null) {
            ringtonePlayer.reset();
            ringtonePlayer.release();
            ringtonePlayer = null;
        }
    }

    private ArrayList<String> getDataForTransport() {
        ArrayList<String> data = new ArrayList<>();
        data.add("UDP");
        data.add("TLS");
        return data;
    }

    private ArrayList<String> getDataForRingtones() {
        ArrayList<String> data = new ArrayList<>();
        data.add("Default");
        data.add("Ringtone 1");
        data.add("Ringtone 2");
        return data;
    }

    private ArrayList<String> getDataForThemes() {
        ArrayList<String> data = new ArrayList<>();
        data.add("Theme Light");
        data.add("Theme Dark");
        data.add("Theme Green");
        data.add("Theme Pink");
        data.add("Theme Dark City");
        data.add("Theme Red City");
        return data;
    }

    private ArrayList<String> getDataForCodec() {
        ArrayList<String> data = new ArrayList<>();
        data.add("G.711 A Law");
        data.add("G.711 U Law");
        data.add("iLBC");
        data.add("GSM FR");
        return data;
    }

    private ArrayList<String> getDataForNoise() {
        ArrayList<String> data = new ArrayList<>();
        data.add("Disable");
        data.add("Low (6 dB attenuation)");
        data.add("Medium (10 dB attenuation)");
        data.add("High (15 dB attenuation)");
        data.add("Very High (20 dB attenuation)");
        return data;
    }

    private ArrayList<GroupItem> getDataForView() {
        ArrayList<GroupItem> items = new ArrayList<>();
        // Application Settings
        GroupItem groupAppSetting = new GroupItem();
        groupAppSetting.id = "";
        groupAppSetting.title = "Application Settings";
        groupAppSetting.type = "list";
//        ChildItem childAppSettingVoIP = new ChildItem();
//        childAppSettingVoIP.id = GlobalVars.PREFERENCES_DATA_APPLICATION_VOIP_OVER_3G;
//        childAppSettingVoIP.title = "VoIP over 3G";
//        childAppSettingVoIP.type = "check";
        ChildItem childAppSettingDTMF = new ChildItem();
        childAppSettingDTMF.id = GlobalVars.PREFERENCES_DATA_APPLICATION_DTMF;
        childAppSettingDTMF.title = "DTMF Sounds";
        childAppSettingDTMF.type = "check";
        groupAppSetting.items.add(childAppSettingDTMF);

        //Sound
        ChildItem childAppSettingSound = new ChildItem();
        childAppSettingSound.id = GlobalVars.PREFERENCES_DATA_RINGTONES;
        childAppSettingSound.title = "Ringtones";
        childAppSettingSound.type = "radio";
        groupAppSetting.items.add(childAppSettingSound);

        //Themes
        ChildItem childAppSettingThemes = new ChildItem();
        childAppSettingThemes.id = GlobalVars.PREFERENCES_DATA_THEMES;
        childAppSettingThemes.title = "Themes";
        childAppSettingThemes.type = "radio";
        groupAppSetting.items.add(childAppSettingThemes);

        // Transport
//        GroupItem groupTransport = new GroupItem();
//        groupTransport.title = "Transport";
//        groupTransport.id = GlobalVars.PREFERENCES_DATA_TRANSPORT;
//        groupTransport.type = "radio";


        // Codec
//        GroupItem groupCodec = new GroupItem();
//        groupCodec.id = GlobalVars.PREFERENCES_DATA_CODEC;
//        groupCodec.title = "Codec";
//        groupCodec.type = "radio";
//
//        GroupItem groupMedia = new GroupItem();
//        groupMedia.title = "Media Configuration";
//        groupMedia.id = "";
//        groupMedia.type = "list";
//        ChildItem childMediaEcho = new ChildItem();
//        childMediaEcho.id = GlobalVars.PREFERENCES_DATA_MEDIA_ECHO;
//        childMediaEcho.title = "Echo Cancellation";
//        childMediaEcho.type = "check";
//        ChildItem childMediaNoise = new ChildItem();
//        childMediaNoise.id = GlobalVars.PREFERENCES_DATA_MEDIA_NOISE;
//        childMediaNoise.title = "Noise Suppression";
//        childMediaNoise.type = "radio";
//        groupMedia.items.add(childMediaEcho);
//        groupMedia.items.add(childMediaNoise);

//        GroupItem groupDial = new GroupItem();
//        groupDial.id = "";
//        groupDial.title = "Dial Rules";
//        groupDial.type = "list";
//
//        ChildItem childDialPlus = new ChildItem();
//        childDialPlus.id = GlobalVars.PREFERENCES_DATA_DIAL_PLUS;
//        childDialPlus.title = "Dial \"+\" as";
//        childDialPlus.type = "text";
//        ChildItem childDialRemove = new ChildItem();
//        childDialRemove.id = GlobalVars.PREFERENCES_DATA_DIAL_REMOVE;
//        childDialRemove.title = "Prefix to Remove";
//        childDialRemove.type = "text";
//        ChildItem childDialAdd = new ChildItem();
//        childDialAdd.id = GlobalVars.PREFERENCES_DATA_DIAL_ADD;
//        childDialAdd.title = "Prefix to Add";
//        childDialAdd.type = "text";
//        groupDial.items.add(childDialPlus);
//        groupDial.items.add(childDialRemove);
//        groupDial.items.add(childDialAdd);


//        GroupItem groupCall = new GroupItem();
//        groupCall.id = "";
//        groupCall.title = "Calls";
//        groupCall.type = "list";
//        ChildItem childCallWaiting = new ChildItem();
//        childCallWaiting.id = GlobalVars.PREFERENCES_DATA_CALL_WAITING;
//        childCallWaiting.title = "Call Waiting";
//        childCallWaiting.type = "check";
//        groupCall.items.add(childCallWaiting);


        items.add(groupAppSetting);
//        items.add(groupTransport);
//        items.add(groupCodec);
//        items.add(groupMedia);
//        items.add(groupDial);
//        items.add(groupCall);


        return items;
    }

    private void playRingTone(int ringtone) {
        if (ringtonePlayer == null) {
            setupRingTone(ringtone);
        }

        if (ringtonePlayer != null && !ringtonePlayer.isPlaying()) {
            //ringtonePlayer.setVolume(am.getStreamVolume(AudioManager.STREAM_MUSIC), am.getStreamVolume(AudioManager.STREAM_MUSIC));
            ringtonePlayer.setLooping(true);
            ringtonePlayer.start();
        }
    }

    private void stopRingTone() {
        if (ringtonePlayer != null && ringtonePlayer.isPlaying()) {
            ringtonePlayer.stop();
            ringtonePlayer.release();
            ringtonePlayer = null;
        }
    }

    private void setupRingTone(int ringtone) {
        switch (ringtone) {
            case 0:
                ringtonePlayer = MediaPlayer.create(this, R.raw.ringtone);
                break;
            case 1:
                ringtonePlayer = MediaPlayer.create(this, R.raw.elegant);
                break;
            case 2:
                ringtonePlayer = MediaPlayer.create(this, R.raw.oldschool);
                break;
            default:
                ringtonePlayer = MediaPlayer.create(this, R.raw.ringtone);

        }
        ringtonePlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public interface ChildListener {
        void onChildClick(ChildItem item, ChildHolder holder);
    }

    private static class GroupItem {
        String id;
        String title;
        String type;
        List<ChildItem> items = new ArrayList<>();
    }

    private static class ChildItem {
        String id;
        String title;
        String type;
    }

    private static class ChildHolder {
        TextView title;
        CheckBox checkBox;
        TextView detail;
    }

    private static class GroupHolder {
        TextView title;
        ImageView image;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * Adapter for our list of {@link GroupItem}s.
     */
    private class ExpandableAdapter extends AnimatedExpandableListView.AnimatedExpandableListAdapter {
        private LayoutInflater inflater;

        private ChildListener childListener;

        private List<GroupItem> items;

        public ExpandableAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        public void setData(List<GroupItem> items) {
            this.items = items;
        }

        @Override
        public ChildItem getChild(int groupPosition, int childPosition) {
            return items.get(groupPosition).items.get(childPosition);
        }

        public void setOnChildClick(ChildListener childListener) {
            this.childListener = childListener;
        }



        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            final ChildHolder holder;
            final ChildItem item = getChild(groupPosition, childPosition);
            if (convertView == null) {
                holder = new ChildHolder();
                convertView = inflater.inflate(R.layout.list_item, parent, false);
                holder.title = (TextView) convertView.findViewById(R.id.textTitle);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.textCB);
                holder.detail = (TextView) convertView.findViewById(R.id.textDetail);
                convertView.setTag(holder);
            } else {
                holder = (ChildHolder) convertView.getTag();
            }

            holder.title.setText(item.title);

            if (item.type.equals("check")) {
                holder.checkBox.setVisibility(View.VISIBLE);
                String value = Utility.getPref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, item.id);
                if (value.isEmpty())
                    value = "false";
                if (Boolean.parseBoolean(value))
                    holder.checkBox.setChecked(true);
                else
                    holder.checkBox.setChecked(false);

                if (childListener != null)
                    holder.checkBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            childListener.onChildClick(item, holder);
                        }
                    });
            } else
                holder.checkBox.setVisibility(View.GONE);

            if (item.type.equals("text")) {
                holder.detail.setVisibility(View.VISIBLE);
                String value = Utility.getPref(getApplicationContext(), GlobalVars.PREFERENCES_DATA_FILE_NAME, item.id);
                holder.detail.setText(value);
            } else
                holder.detail.setVisibility(View.GONE);

            if (childListener != null) {
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        childListener.onChildClick(item, holder);
                    }
                });
            }

            return convertView;
        }

        @Override
        public int getRealChildrenCount(int groupPosition) {
            return items.get(groupPosition).items.size();
        }

        @Override
        public GroupItem getGroup(int groupPosition) {
            return items.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return items.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupHolder holder;
            GroupItem item = getGroup(groupPosition);
            if (convertView == null) {
                holder = new GroupHolder();
                convertView = inflater.inflate(R.layout.group_item, parent, false);
                holder.title = (TextView) convertView.findViewById(R.id.textTitle);
                holder.image = (ImageView) convertView.findViewById(R.id.more);
                convertView.setTag(holder);
            } else {
                holder = (GroupHolder) convertView.getTag();
            }

            holder.title.setText(item.title);
            if (item.type.equals("list")) {
                holder.image.setVisibility(View.VISIBLE);
                int[] attrs;
                if (isExpanded) {
                    attrs = new int[]{R.attr.moreUp /* index 0 */};
                } else {
                    attrs = new int[]{R.attr.moreDown /* index 0 */};
                }
                TypedArray ta = obtainStyledAttributes(attrs);
                Drawable drawable = ta.getDrawable(0 /* index */);
                ta.recycle();

                holder.image.setImageDrawable(drawable);
            } else {
                holder.image.setVisibility(View.GONE);
            }

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isChildSelectable(int arg0, int arg1) {
            return true;
        }

    }

}

