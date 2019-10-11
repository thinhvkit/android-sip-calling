package com.ccsidd.rtone.message.conversationlist;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.chips.ChipsUtil;
import com.ccsidd.rtone.message.emoji.EmojiHandler;
import com.ccsidd.rtone.message.view.AvatarView;
import com.ccsidd.rtone.objects.Contact;
import com.ccsidd.rtone.objects.Conversation;
import com.ccsidd.rtone.message.base.RecyclerArrayAdapter;
import com.ccsidd.rtone.message.ThemeManager;
import com.ccsidd.rtone.message.common.FontManager;
import com.ccsidd.rtone.message.common.emoji.EmojiRegistry;
import com.ccsidd.rtone.message.common.utils.DateFormatter;
import com.ccsidd.rtone.utilities.Utility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.realm.OrderedRealmCollection;


public class ConversationListAdapter extends RecyclerArrayAdapter<Conversation, ConversationListViewHolder, ConversationItem> {


    //    private final SharedPreferences mPrefs;
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    public ConversationListAdapter(Context context, @Nullable OrderedRealmCollection<Conversation> conversations) {
        super(context,conversations, true);
//        mPrefs = mContext.getPxrefs();
    }

    protected ConversationItem getItem(int position) {
        long convId = position;
        return ConversationItem.from(mContext, convId, getAdapterData().get(position));
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

    @Override
    public ConversationListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.list_item_conversation, null);
        ConversationListViewHolder holder = new ConversationListViewHolder(mContext, view);
        if (viewType == VIEW_TYPE_HEADER) {

            view.findViewById(R.id.header).setVisibility(View.VISIBLE);
            view.findViewById(R.id.conversation_list_row).setVisibility(View.GONE);
            view.findViewById(R.id.divider).setVisibility(View.GONE);

        } else {
            view.findViewById(R.id.header).setVisibility(View.GONE);
            holder.mutedView.setImageResource(R.drawable.ic_notifications_muted);
            holder.unreadView.setImageResource(R.drawable.ic_unread_indicator);
            holder.errorIndicator.setImageResource(R.drawable.ic_error);

            holder.mutedView.setColorFilter(ThemeManager.getColor());
            holder.unreadView.setColorFilter(ThemeManager.getColor());
            holder.errorIndicator.setColorFilter(ThemeManager.getColor());
//        LiveViewManager.registerView(QKPreference.THEME, this, key -> {
//            holder.mutedView.setColorFilter(ThemeManager.getColor());
//            holder.unreadView.setColorFilter(ThemeManager.getColor());
//            holder.errorIndicator.setColorFilter(ThemeManager.getColor());
//        });

            holder.root.setBackgroundDrawable(ThemeManager.getRippleBackground());
//        LiveViewManager.registerView(QKPreference.BACKGROUND, this, key -> {
//            holder.root.setBackgroundDrawable(ThemeManager.getRippleBackground());
//        });
        }
        return holder;

    }

    @Override
    public void onBindViewHolder(final ConversationListViewHolder holder, int position) {
        if (position == 0)
            return;
        final ConversationItem conversation = getItem(position - 1);

        holder.mData = conversation;
        holder.mContext = mContext;
        holder.mClickListener = mItemClickListener;
        holder.root.setOnClickListener(holder);
        holder.root.setOnLongClickListener(holder);

        holder.mutedView.setVisibility(View.GONE);

        holder.errorIndicator.setVisibility(conversation.hasError() ? View.VISIBLE : View.GONE);

        final boolean hasUnreadMessages = conversation.hasUnreadMessages();
        int[] attrs = new int[]{R.attr.numberPadStyle};
        TypedArray ta = mContext.obtainStyledAttributes(attrs);
        if (hasUnreadMessages) {
            holder.unreadView.setVisibility(View.VISIBLE);
            holder.snippetView.setTextColor(ta.getColor(0, 0xFF1565C0));
            holder.dateView.setTextColor(ta.getColor(0, 0xFF1565C0));
            holder.fromView.setType(FontManager.TEXT_TYPE_PRIMARY_BOLD);
            holder.fromView.setTextColor(ta.getColor(0, 0xFF1565C0));
            holder.snippetView.setMaxLines(5);
        } else {
            holder.unreadView.setVisibility(View.GONE);
            holder.snippetView.setTextColor(ta.getColor(0, 0xFF1565C0));
            holder.dateView.setTextColor(ta.getColor(0, 0xFF1565C0));
            holder.fromView.setType(FontManager.TEXT_TYPE_PRIMARY);
            holder.fromView.setTextColor(ta.getColor(0, 0xFF1565C0));
            holder.snippetView.setMaxLines(1);
        }

        //holder.dateView.setTextColor(hasUnreadMessages ? ThemeManager.getColor() : ThemeManager.getTextOnBackgroundSecondary());
//        LiveViewManager.registerView(QKPreference.THEME, this, key -> {
//            holder.dateView.setTextColor(hasUnreadMessages ? ThemeManager.getColor() : ThemeManager.getTextOnBackgroundSecondary());
//        });

        if (isInMultiSelectMode()) {
            holder.mSelected.setVisibility(View.VISIBLE);
            if (isSelected(conversation.getThreadId())) {
                holder.mSelected.setImageResource(R.drawable.ic_selected);
                holder.mSelected.setColorFilter(ThemeManager.getColor());
                holder.mSelected.setAlpha(1f);
            } else {
                holder.mSelected.setImageResource(R.drawable.ic_unselected);
                holder.mSelected.setColorFilter(ThemeManager.getColor());
                holder.mSelected.setAlpha(0.5f);
            }
        } else {
            holder.mSelected.setVisibility(View.GONE);
        }

        holder.mAvatarView.setVisibility(View.VISIBLE);
        Contact contact = Utility.getContactDisplayNameByNumber(conversation.getRecipients().get(0));
        Utility.loadAvatar(mContext, holder.mAvatarView, contact);
//        LiveViewManager.registerView(QKPreference.HIDE_AVATAR_CONVERSATIONS, this, key -> {
//            holder.mAvatarView.setVisibility(QKPreferences.getBoolean(QKPreference.HIDE_AVATAR_CONVERSATIONS) ? View.GONE : View.VISIBLE);
//        });

        // Date
        holder.dateView.setText(DateFormatter.getConversationTimestamp(mContext, conversation.getDate()));

        // Subject
        String emojiSnippet = conversation.getSnippet();
//        if (mPrefs.getBoolean(SettingsFragment.AUTO_EMOJI, false)) {
//        emojiSnippet = EmojiRegistry.parseEmojis(emojiSnippet);
//        }

        SpannableStringBuilder buf = new SpannableStringBuilder();
        buf.append(emojiSnippet);
        EmojiHandler.addEmojis(mContext, buf, 80);

        holder.snippetView.setText(buf);
        holder.fromView.setText(contact.getDisplayName());

//        Contact.addListener(holder);

        // Update the avatar and name
//        holder.onUpdate(conversation.getRecipients().size() == 1 ? conversation.getRecipients().get(0) : null);
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }

}
