package com.ccsidd.rtone.message.dialog.conversationdetails;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.chips.ChipsUtil;
import com.ccsidd.rtone.objects.Contact;
import com.ccsidd.rtone.message.view.AvatarView;
import com.ccsidd.rtone.message.view.QKTextView;
import com.ccsidd.rtone.utilities.Utility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ConversationDetailsContactListAdapter extends ArrayAdapter {

    private ArrayList<String> mContacts;
    Context mContext;

    public ConversationDetailsContactListAdapter(Context context, ArrayList<String> contacts) {
        super(context, R.layout.list_item_recipient);
        mContacts = contacts;
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.list_item_recipient, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Contact contact = Utility.getContactDisplayNameByNumber(mContacts.get(position));

        holder.name.setText(contact.getDisplayName());
        holder.address.setText(mContacts.get(position));
        //holder.avatar.setContactName(contact.getDisplayName());
        //holder.avatar.assignContactFromPhone(mContacts.get(position), true);
        if (contact.getImageUri() != null && contact.getImageUri().length() > 0) {
            Bitmap bitmap = Utility.loadBitmapFromPath(mContext, contact.getImageUri());
            if (bitmap == null) {
                Uri uri = Uri.parse(contact.getImageUri());
                if (uri != null)
                    try {
                        InputStream is = mContext.getContentResolver().openInputStream(
                                uri);
                        if (is != null) {
                            byte[] buffer = new byte[1024 * 60];
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            try {
                                int size;
                                while ((size = is.read(buffer)) != -1) {
                                    baos.write(buffer, 0, size);
                                }
                            } finally {
                                is.close();
                            }
                            byte[] photoBytes = baos.toByteArray();
                            if (photoBytes != null && photoBytes.length > 0) {
                                Bitmap photo = ChipsUtil.getClip(BitmapFactory.decodeByteArray(photoBytes, 0,
                                        photoBytes.length));

                                holder.avatar.setImageDrawable(new BitmapDrawable(mContext.getResources(), photo));
                            }
                        }
                    } catch (IOException ex) {
                        // ignore
                    }
            } else
                holder.avatar.setImageDrawable(new BitmapDrawable(mContext.getResources(), bitmap));
        } else {
            //holder.avatar.assignContactFromPhone(mContacts.get(position), false);
            holder.avatar.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.unknown_contact));
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return mContacts.size();
    }

    static class ViewHolder {
        AvatarView avatar;
        QKTextView name;
        QKTextView address;

        public ViewHolder(View view) {
            avatar = (AvatarView) view.findViewById(R.id.avatar);
            name = (QKTextView) view.findViewById(R.id.name);
            address = (QKTextView) view.findViewById(R.id.address);
        }
    }
}
