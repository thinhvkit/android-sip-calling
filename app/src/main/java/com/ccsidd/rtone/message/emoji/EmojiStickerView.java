package com.ccsidd.rtone.message.emoji;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ccsidd.rtone.R;
import com.ccsidd.rtone.message.emoji.emoji.Cars;
import com.ccsidd.rtone.message.emoji.emoji.Electronics;
import com.ccsidd.rtone.message.emoji.emoji.Emoji;
import com.ccsidd.rtone.message.emoji.emoji.Food;
import com.ccsidd.rtone.message.emoji.emoji.Nature;
import com.ccsidd.rtone.message.emoji.emoji.People;
import com.ccsidd.rtone.message.emoji.emoji.Sport;
import com.ccsidd.rtone.message.emoji.emoji.Sticker;
import com.ccsidd.rtone.message.emoji.emoji.Sticker2;
import com.ccsidd.rtone.message.emoji.emoji.Symbols;
import com.ccsidd.rtone.message.emoji.listeners.OnEmojiBackspaceClickListener;
import com.ccsidd.rtone.message.emoji.listeners.OnEmojiClickedListener;
import com.ccsidd.rtone.message.emoji.listeners.OnEmojiLongClickedListener;
import com.ccsidd.rtone.message.emoji.listeners.RepeatListener;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressLint("ViewConstructor")
final class EmojiStickerView extends LinearLayout implements ViewPager.OnPageChangeListener {
    private static final int BACKGROUND_COLOR = 0xffeceff1;

//    private static final int RECENT_INDEX = 0;
    private static final int STICKER_INDEX = 0;
//    private static final int STICKER_2_INDEX = 1;

    private static final long INITIAL_INTERVAL = TimeUnit.SECONDS.toMillis(1) / 2;
    private static final int NORMAL_INTERVAL = 50;

    @ColorInt private final int themeAccentColor;
    @Nullable OnEmojiBackspaceClickListener onEmojiBackspaceClickListener;

    private int emojiTabLastSelectedIndex = -1;

    private final ImageView[] emojiTabs;

//    private RecentEmojiGridView recentGridView;

    EmojiStickerView(final Context context, final OnEmojiClickedListener onEmojiClickedListener, final OnEmojiLongClickedListener onEmojiLongClickedListener, @NonNull final RecentEmoji recentEmoji) {
        super(context);

        View.inflate(context, R.layout.emoji_sticker_view, this);

        setOrientation(VERTICAL);
        setBackgroundColor(BACKGROUND_COLOR);

        final ViewPager emojisPager = (ViewPager) findViewById(R.id.emojis_pager);
        emojisPager.addOnPageChangeListener(this);

        final List<? extends View> views = getViews(context, onEmojiClickedListener, onEmojiLongClickedListener, recentEmoji);
        final EmojiPagerAdapter emojisAdapter = new EmojiPagerAdapter(views);
        emojisPager.setAdapter(emojisAdapter);

        emojiTabs = new ImageView[STICKER_INDEX + 1];
//        emojiTabs = new ImageView[STICKER_2_INDEX + 1];
//        emojiTabs[RECENT_INDEX] = (ImageView) findViewById(R.id.emojis_tab_0_recent);
        emojiTabs[STICKER_INDEX] = (ImageView) findViewById(R.id.emojis_tab_1_sticker);
//        emojiTabs[STICKER_2_INDEX] = (ImageView) findViewById(R.id.emojis_tab_2_sticker);

        handleOnClicks(emojisPager);

        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        themeAccentColor = value.data;

//        final int startIndex = recentGridView.numberOfRecentEmojis() > 0 ? RECENT_INDEX : STICKER_INDEX;
        final int startIndex = STICKER_INDEX;
        emojisPager.setCurrentItem(startIndex);
        onPageSelected(startIndex);
    }

    private void handleOnClicks(final ViewPager emojisPager) {
        for (int i = 0; i < emojiTabs.length; i++) {
            emojiTabs[i].setOnClickListener(new EmojiTabsClickListener(emojisPager, i));
        }
    }

    public void setOnEmojiBackspaceClickListener(@Nullable final OnEmojiBackspaceClickListener onEmojiBackspaceClickListener) {
        this.onEmojiBackspaceClickListener = onEmojiBackspaceClickListener;
    }

    @NonNull
    private List<? extends View> getViews(final Context context, @Nullable final OnEmojiClickedListener onEmojiClickedListener, @Nullable final OnEmojiLongClickedListener onEmojiLongClickedListener, @NonNull final RecentEmoji recentEmoji) {
//        recentGridView = new RecentEmojiGridView(context).init(onEmojiClickedListener, recentEmoji);
        final EmojiStickerGridView stickerGridView = createGridViewSticker(context, Sticker.DATA, onEmojiClickedListener, onEmojiLongClickedListener);
//        final EmojiStickerGridView sticker2GridView = createGridViewSticker(context, Sticker2.DATA, onEmojiClickedListener, onEmojiLongClickedListener);
        return Arrays.asList(stickerGridView);
    }

    private EmojiStickerGridView createGridViewSticker(@NonNull final Context context, final Emoji[] emojis, @Nullable final OnEmojiClickedListener onEmojiClickedListener, @Nullable final OnEmojiLongClickedListener onEmojiLongClickedListener) {
        final EmojiStickerGridView emojiGridView = new EmojiStickerGridView(context);
        final EmojiStickerArrayAdapter emojiArrayAdapter = new EmojiStickerArrayAdapter(getContext(), emojis);
        emojiArrayAdapter.setOnEmojiClickedListener(onEmojiClickedListener);
        emojiArrayAdapter.setOnEmojiLongClickedListener(onEmojiLongClickedListener);
        emojiGridView.setAdapter(emojiArrayAdapter);
        return emojiGridView;
    }

    @Override
    public void onPageSelected(final int i) {
        if (emojiTabLastSelectedIndex != i) {
            /*if (i == RECENT_INDEX) {
                recentGridView.invalidateEmojis();
            }*/

            switch (i) {
                case STICKER_INDEX:
                    if (emojiTabLastSelectedIndex >= 0 && emojiTabLastSelectedIndex < emojiTabs.length) {
                        emojiTabs[emojiTabLastSelectedIndex].setSelected(false);
                        emojiTabs[emojiTabLastSelectedIndex].clearColorFilter();
                    }

                    emojiTabs[i].setSelected(true);
//                    emojiTabs[i].setColorFilter(themeAccentColor, PorterDuff.Mode.SRC_IN);

                    emojiTabLastSelectedIndex = i;
                    break;
                /*case STICKER_2_INDEX:
                    if (emojiTabLastSelectedIndex >= 0 && emojiTabLastSelectedIndex < emojiTabs.length) {
                        emojiTabs[emojiTabLastSelectedIndex].setSelected(false);
                        emojiTabs[emojiTabLastSelectedIndex].clearColorFilter();
                    }

                    emojiTabs[i].setSelected(true);
//                    emojiTabs[i].setColorFilter(themeAccentColor, PorterDuff.Mode.SRC_IN);

                    emojiTabLastSelectedIndex = i;
                    break;*/

                default:
                    break;
            }
        }
    }

    @Override
    public void onPageScrolled(final int i, final float v, final int i2) {
        // Don't care
    }

    @Override
    public void onPageScrollStateChanged(final int i) {
        // Don't care
    }

    static class EmojiTabsClickListener implements OnClickListener {
        private final ViewPager emojisPager;
        private final int position;

        EmojiTabsClickListener(final ViewPager emojisPager, final int position) {
            this.emojisPager = emojisPager;
            this.position = position;
        }

        @Override
        public void onClick(final View v) {
            emojisPager.setCurrentItem(position);
        }
    }
}
