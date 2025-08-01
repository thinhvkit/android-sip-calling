package com.ccsidd.rtone.message.common.emoji;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.ImageView;

import com.ccsidd.rtone.R;
import com.vdurmont.emoji.EmojiParser;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class EmojiRegistry {
    public static Map<String, String> EMOJIS_MAP = new HashMap<>();

    static {
        EMOJIS_MAP.put(":)", Emojis.SMILE);
        EMOJIS_MAP.put(":-)", Emojis.SMILE);
        EMOJIS_MAP.put("=)", Emojis.SMILEY);
        EMOJIS_MAP.put(";)", Emojis.WINK);
        EMOJIS_MAP.put(";-)", Emojis.WINK);
        EMOJIS_MAP.put(":D", Emojis.GRINNING);
        EMOJIS_MAP.put("=D", Emojis.GRIN);
        EMOJIS_MAP.put("({})", Emojis.HUGGING);
//        EMOJIS_MAP.put("=))", Emojis.ROLLING_ON_THE_FLOOR_LAUGHING); //Unicode 9.0
        EMOJIS_MAP.put(":\")", Emojis.BLUSH);
        EMOJIS_MAP.put(":(", Emojis.FROWNING);
        EMOJIS_MAP.put(":-(", Emojis.FROWNING);
        EMOJIS_MAP.put(":')", Emojis.JOY);
        EMOJIS_MAP.put(":'-)", Emojis.JOY);
        EMOJIS_MAP.put(":'(", Emojis.CRY);
        EMOJIS_MAP.put(":'-(", Emojis.CRY);
        EMOJIS_MAP.put(":P", Emojis.STUCK_OUT_TONGUE);
        EMOJIS_MAP.put(":-P", Emojis.STUCK_OUT_TONGUE);
        EMOJIS_MAP.put(":p", Emojis.STUCK_OUT_TONGUE);
        EMOJIS_MAP.put(":-p", Emojis.STUCK_OUT_TONGUE);
        EMOJIS_MAP.put(";P", Emojis.STUCK_OUT_TONGUE_WINKING_EYE);
        EMOJIS_MAP.put(";-P", Emojis.STUCK_OUT_TONGUE_WINKING_EYE);
        EMOJIS_MAP.put(";p", Emojis.STUCK_OUT_TONGUE_WINKING_EYE);
        EMOJIS_MAP.put(";-p", Emojis.STUCK_OUT_TONGUE_WINKING_EYE);
//        EMOJIS_MAP.put("=P~", Emojis.DROOLING);
        EMOJIS_MAP.put("<3", Emojis.HEART);
        EMOJIS_MAP.put("<3<3", Emojis.HEART_EYES);
        EMOJIS_MAP.put(":*", Emojis.KISSING);
        EMOJIS_MAP.put(":-*", Emojis.KISSING);
        EMOJIS_MAP.put(":|", Emojis.NEUTRAL_FACE);
        EMOJIS_MAP.put(":-|", Emojis.NEUTRAL_FACE);
//        EMOJIS_MAP.put("/:)", Emojis.RAISED_EYEBROWS);
//        EMOJIS_MAP.put(":>", Emojis.SMUG);
        EMOJIS_MAP.put("=\\", Emojis.DISAPPOINTED);
        EMOJIS_MAP.put("=-\\", Emojis.DISAPPOINTED);
        EMOJIS_MAP.put(":-\\", Emojis.DISAPPOINTED);
        EMOJIS_MAP.put("3-|", Emojis.DISAPPOINTED_RELIEVED);
        EMOJIS_MAP.put(":]x", Emojis.UNAMUSED);
        EMOJIS_MAP.put(":-||", Emojis.RAGE);
        EMOJIS_MAP.put(":$", Emojis.FLUSHED);
        EMOJIS_MAP.put(":o", Emojis.HUSHED);
        EMOJIS_MAP.put(":-o", Emojis.HUSHED);
        EMOJIS_MAP.put(":0", Emojis.HUSHED);
        EMOJIS_MAP.put(":-0", Emojis.HUSHED);
        EMOJIS_MAP.put(":O", Emojis.ASTONISHED);
        EMOJIS_MAP.put(":-O", Emojis.ASTONISHED);
        EMOJIS_MAP.put(";)", Emojis.WINK);
        EMOJIS_MAP.put(";-)", Emojis.WINK);
        EMOJIS_MAP.put(":/", Emojis.CONFUSED);
        EMOJIS_MAP.put(">:(", Emojis.ANGRY);
        EMOJIS_MAP.put(">:-(", Emojis.ANGRY);
        EMOJIS_MAP.put("%-}", Emojis.DIZZY_FACE);
        EMOJIS_MAP.put("O:)", Emojis.INNOCENT);
        EMOJIS_MAP.put("O:-)", Emojis.INNOCENT);
        EMOJIS_MAP.put("O-)", Emojis.INNOCENT);
        EMOJIS_MAP.put("0=)", Emojis.INNOCENT);
        EMOJIS_MAP.put("0:-)", Emojis.INNOCENT);
        EMOJIS_MAP.put(">=)", Emojis.SMILING_IMP);
        EMOJIS_MAP.put(">:)", Emojis.SMILING_IMP);
        EMOJIS_MAP.put(">:-)", Emojis.SMILING_IMP);
        EMOJIS_MAP.put(":x", Emojis.ZIPPER_MOUTH);
        EMOJIS_MAP.put(":-x", Emojis.ZIPPER_MOUTH);
        EMOJIS_MAP.put("*nerd*", Emojis.NERD);
        EMOJIS_MAP.put("8-|", Emojis.NERD);
        EMOJIS_MAP.put(":-B", Emojis.NERD);
        EMOJIS_MAP.put("B-)", Emojis.SUNGLASSES);
        EMOJIS_MAP.put("8-)", Emojis.SUNGLASSES);
        EMOJIS_MAP.put("~o)", Emojis.COFFEE);
        EMOJIS_MAP.put("C(_)", Emojis.COFFEE);
        EMOJIS_MAP.put("|_P", Emojis.COFFEE);
        EMOJIS_MAP.put("*beer*", Emojis.BEER);
        EMOJIS_MAP.put("*wine*", Emojis.WINE_GLASS);
        EMOJIS_MAP.put("())=(", Emojis.WINE_GLASS);
        EMOJIS_MAP.put("*dine*", Emojis.FORK_KNIFE_PLATE);
        EMOJIS_MAP.put("@>--", Emojis.ROSE);
        EMOJIS_MAP.put("@>-;--", Emojis.ROSE);
        EMOJIS_MAP.put("*gift*", Emojis.GIFT);
        EMOJIS_MAP.put("*bday*", Emojis.BIRTHDAY);
        EMOJIS_MAP.put("(*)", Emojis.STAR);
        EMOJIS_MAP.put(":poop:", Emojis.HANKEY);
        EMOJIS_MAP.put("*poop*", Emojis.HANKEY);
//        EMOJIS_MAP.put(":&", Emojis.NAUSEATED);

        EMOJIS_MAP.put("*a1*", Emojis.CRY);
        EMOJIS_MAP.put("*a2*", Emojis.SMILE);
        EMOJIS_MAP.put("*a3*", Emojis.ANGRY);
        EMOJIS_MAP.put("*a4*", Emojis.HAIRCUT);
        EMOJIS_MAP.put("*a5*", Emojis.HAND);
        EMOJIS_MAP.put("*a6*", Emojis.HANDBAG);
        EMOJIS_MAP.put("*a7*", Emojis.ACCEPT);
        EMOJIS_MAP.put("*a8*", Emojis.SAKE);
        EMOJIS_MAP.put("*a9*", Emojis.ALARM_CLOCK);
        EMOJIS_MAP.put("*a10*", Emojis.LABEL);
        EMOJIS_MAP.put("*a11*", Emojis.AMPHORA);
        EMOJIS_MAP.put("*a12*", Emojis.GREEN_APPLE);

    }

    /**
     * Replaces emoji codes within the text with unicode emojis.
     * <p>
     * Note that an emoji code must be surrounded on both sides by one of:
     * <p>
     * - whitespace (i.e. ' ' or '\t')
     * - punctuation (one of [.,;:"'?!])
     * - the beginning or end of the string
     */
    public static String parseEmojis(String body) {
        if (TextUtils.isEmpty(body)) {
            return body;
        }

        // whitespace and punctuation characters
        String ACCEPTED_CHARS = "[\\s.,?:;'\"!]";

        // explanation: we want to match:
        // 1) either an accepted char or the beginning of the text (i.e. ^)
        // 2) the actual emoji code (to be inserted in the loop)
        // 3) either an accepted char or the end of the text (i.e. $)
        String REGEX_TEMPLATE = String.format("(^|%s)%s(%s|$)", ACCEPTED_CHARS, "%s", ACCEPTED_CHARS);

        // iterate over all the entries
        for (Map.Entry<String, String> entry : EmojiRegistry.EMOJIS_MAP.entrySet()) {
            // quote the emoji code because some characters like ) are protected in regex land
            String quoted = Pattern.quote(entry.getKey());
            String regex = String.format(REGEX_TEMPLATE, quoted);

            // the $1 and $2 represent the character* that came before the emoji and the
            // character* that came after the emoji
            // * - or beginning / end of text
            body = body.replaceAll(regex, "$1:" + entry.getValue() + ":$2");
        }
        return EmojiParser.parseToUnicode(body);
    }

    /*public static Drawable parseImage(Context context, String body) {
        Drawable drawable = null;
        if (TextUtils.isEmpty(body)) {
            return null;
        }

        // whitespace and punctuation characters
        String ACCEPTED_CHARS = "[\\s.,?:;'\"!]";

        // explanation: we want to match:
        // 1) either an accepted char or the beginning of the text (i.e. ^)
        // 2) the actual emoji code (to be inserted in the loop)
        // 3) either an accepted char or the end of the text (i.e. $)
        String REGEX_TEMPLATE = String.format("(^|%s)%s(%s|$)", ACCEPTED_CHARS, "%s", ACCEPTED_CHARS);

        // iterate over all the entries
        for (Map.Entry<String, String> entry : EmojiRegistry.EMOJIS_MAP.entrySet()) {
            // quote the emoji code because some characters like ) are protected in regex land
            //String quoted = Pattern.quote(entry.getKey());

            if (entry.getKey().equalsIgnoreCase(body)) {
                switch (entry.getValue()) {
                    case Emojis.SMILE:
                        drawable = ContextCompat.getDrawable(context, R.drawable.a1);
                        break;
                    case Emojis.CRY:
                        drawable = ContextCompat.getDrawable(context, R.drawable.a2);
                        break;
                    case Emojis.ANGRY:
                        drawable = ContextCompat.getDrawable(context, R.drawable.a3);
                        break;
                    case Emojis.HAIRCUT:
                        drawable = ContextCompat.getDrawable(context, R.drawable.a4);
                        break;
                    case Emojis.HAND:
                        drawable = ContextCompat.getDrawable(context, R.drawable.a5);
                        break;
                    case Emojis.HANDBAG:
                        drawable = ContextCompat.getDrawable(context, R.drawable.a6);
                        break;
                    case Emojis.ACCEPT:
                        drawable = ContextCompat.getDrawable(context, R.drawable.a7);
                        break;
                    case Emojis.SAKE:
                        drawable = ContextCompat.getDrawable(context, R.drawable.a8);
                        break;
                    case Emojis.ALARM_CLOCK:
                        drawable = ContextCompat.getDrawable(context, R.drawable.a9);
                        break;
                    case Emojis.LABEL:
                        drawable = ContextCompat.getDrawable(context, R.drawable.a10);
                        break;
                    case Emojis.AMPHORA:
                        drawable = ContextCompat.getDrawable(context, R.drawable.a11);
                        break;
                    case Emojis.GREEN_APPLE:
                        drawable = ContextCompat.getDrawable(context, R.drawable.a12);
                        break;
                }
            }

//            String regex = String.format(REGEX_TEMPLATE, quoted);

            // the $1 and $2 represent the character* that came before the emoji and the
            // character* that came after the emoji
            // * - or beginning / end of text
            //body = body.replaceAll(regex, "$1:" + entry.getValue() + ":$2");
        }
        return drawable;
    }*/
}
