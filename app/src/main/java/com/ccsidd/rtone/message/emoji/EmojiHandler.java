package com.ccsidd.rtone.message.emoji;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;

import com.ccsidd.rtone.R;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"checkstyle:magicnumber", "PMD.GodClass"})
public final class EmojiHandler {
    private static final SparseIntArray EMOJIS_MAP = new SparseIntArray(949);
    private static final SparseIntArray SOFTBANKS_MAP = new SparseIntArray(27);

    private static final Map<String, Integer> STICKERS_MAP = new HashMap<>();

    static {
        //@formatter:off
        // People
        EMOJIS_MAP.put(0x1f600, R.drawable.emoji_1f600);
        EMOJIS_MAP.put(0x1f62c, R.drawable.emoji_1f62c);
        EMOJIS_MAP.put(0x1f601, R.drawable.emoji_1f601);
        EMOJIS_MAP.put(0x1f602, R.drawable.emoji_1f602);
        EMOJIS_MAP.put(0x1f603, R.drawable.emoji_1f603);
        EMOJIS_MAP.put(0x1f604, R.drawable.emoji_1f604);
        EMOJIS_MAP.put(0x1f605, R.drawable.emoji_1f605);
        EMOJIS_MAP.put(0x1f606, R.drawable.emoji_1f606);
        EMOJIS_MAP.put(0x1f607, R.drawable.emoji_1f607);
        EMOJIS_MAP.put(0x1f609, R.drawable.emoji_1f609);
        EMOJIS_MAP.put(0x1f60a, R.drawable.emoji_1f60a);
        EMOJIS_MAP.put(0x1f642, R.drawable.emoji_1f642);
        EMOJIS_MAP.put(0x1f643, R.drawable.emoji_1f643);
        EMOJIS_MAP.put(0x263a, R.drawable.emoji_263a);
        EMOJIS_MAP.put(0x1f60b, R.drawable.emoji_1f60b);
        EMOJIS_MAP.put(0x1f60c, R.drawable.emoji_1f60c);
        EMOJIS_MAP.put(0x1f60d, R.drawable.emoji_1f60d);
        EMOJIS_MAP.put(0x1f618, R.drawable.emoji_1f618);
        EMOJIS_MAP.put(0x1f617, R.drawable.emoji_1f617);
        EMOJIS_MAP.put(0x1f619, R.drawable.emoji_1f619);
        EMOJIS_MAP.put(0x1f61a, R.drawable.emoji_1f61a);
        EMOJIS_MAP.put(0x1f61c, R.drawable.emoji_1f61c);
        EMOJIS_MAP.put(0x1f61d, R.drawable.emoji_1f61d);
        EMOJIS_MAP.put(0x1f61b, R.drawable.emoji_1f61b);
        EMOJIS_MAP.put(0x1f911, R.drawable.emoji_1f911);
        EMOJIS_MAP.put(0x1f913, R.drawable.emoji_1f913);
        EMOJIS_MAP.put(0x1f60e, R.drawable.emoji_1f60e);
        EMOJIS_MAP.put(0x1f917, R.drawable.emoji_1f917);
        EMOJIS_MAP.put(0x1f60f, R.drawable.emoji_1f60f);
        EMOJIS_MAP.put(0x1f636, R.drawable.emoji_1f636);
        EMOJIS_MAP.put(0x1f610, R.drawable.emoji_1f610);
        EMOJIS_MAP.put(0x1f611, R.drawable.emoji_1f611);
        EMOJIS_MAP.put(0x1f612, R.drawable.emoji_1f612);
        EMOJIS_MAP.put(0x1f644, R.drawable.emoji_1f644);
        EMOJIS_MAP.put(0x1f914, R.drawable.emoji_1f914);
        EMOJIS_MAP.put(0x1f633, R.drawable.emoji_1f633);
        EMOJIS_MAP.put(0x1f61e, R.drawable.emoji_1f61e);
        EMOJIS_MAP.put(0x1f61f, R.drawable.emoji_1f61f);
        EMOJIS_MAP.put(0x1f620, R.drawable.emoji_1f620);
        EMOJIS_MAP.put(0x1f621, R.drawable.emoji_1f621);
        EMOJIS_MAP.put(0x1f614, R.drawable.emoji_1f614);
        EMOJIS_MAP.put(0x1f615, R.drawable.emoji_1f615);
        EMOJIS_MAP.put(0x1f641, R.drawable.emoji_1f641);
        EMOJIS_MAP.put(0x2639, R.drawable.emoji_2639);
        EMOJIS_MAP.put(0x1f623, R.drawable.emoji_1f623);
        EMOJIS_MAP.put(0x1f616, R.drawable.emoji_1f616);
        EMOJIS_MAP.put(0x1f62b, R.drawable.emoji_1f62b);
        EMOJIS_MAP.put(0x1f629, R.drawable.emoji_1f629);
        EMOJIS_MAP.put(0x1f624, R.drawable.emoji_1f624);
        EMOJIS_MAP.put(0x1f62e, R.drawable.emoji_1f62e);
        EMOJIS_MAP.put(0x1f631, R.drawable.emoji_1f631);
        EMOJIS_MAP.put(0x1f628, R.drawable.emoji_1f628);
        EMOJIS_MAP.put(0x1f630, R.drawable.emoji_1f630);
        EMOJIS_MAP.put(0x1f62f, R.drawable.emoji_1f62f);
        EMOJIS_MAP.put(0x1f626, R.drawable.emoji_1f626);
        EMOJIS_MAP.put(0x1f627, R.drawable.emoji_1f627);
        EMOJIS_MAP.put(0x1f622, R.drawable.emoji_1f622);
        EMOJIS_MAP.put(0x1f625, R.drawable.emoji_1f625);
        EMOJIS_MAP.put(0x1f62a, R.drawable.emoji_1f62a);
        EMOJIS_MAP.put(0x1f613, R.drawable.emoji_1f613);
        EMOJIS_MAP.put(0x1f62d, R.drawable.emoji_1f62d);
        EMOJIS_MAP.put(0x1f635, R.drawable.emoji_1f635);
        EMOJIS_MAP.put(0x1f632, R.drawable.emoji_1f632);
        EMOJIS_MAP.put(0x1f910, R.drawable.emoji_1f910);
        EMOJIS_MAP.put(0x1f637, R.drawable.emoji_1f637);
        EMOJIS_MAP.put(0x1f912, R.drawable.emoji_1f912);
        EMOJIS_MAP.put(0x1f915, R.drawable.emoji_1f915);
        EMOJIS_MAP.put(0x1f634, R.drawable.emoji_1f634);
        EMOJIS_MAP.put(0x1f4a4, R.drawable.emoji_1f4a4);
        EMOJIS_MAP.put(0x1f4a9, R.drawable.emoji_1f4a9);
        EMOJIS_MAP.put(0x1f608, R.drawable.emoji_1f608);
        EMOJIS_MAP.put(0x1f47f, R.drawable.emoji_1f47f);
        EMOJIS_MAP.put(0x1f479, R.drawable.emoji_1f479);
        EMOJIS_MAP.put(0x1f47a, R.drawable.emoji_1f47a);
        EMOJIS_MAP.put(0x1f480, R.drawable.emoji_1f480);
        EMOJIS_MAP.put(0x1f47b, R.drawable.emoji_1f47b);
        EMOJIS_MAP.put(0x1f47d, R.drawable.emoji_1f47d);
        EMOJIS_MAP.put(0x1f916, R.drawable.emoji_1f916);
        EMOJIS_MAP.put(0x1f63a, R.drawable.emoji_1f63a);
        EMOJIS_MAP.put(0x1f638, R.drawable.emoji_1f638);
        EMOJIS_MAP.put(0x1f639, R.drawable.emoji_1f639);
        EMOJIS_MAP.put(0x1f63b, R.drawable.emoji_1f63b);
        EMOJIS_MAP.put(0x1f63c, R.drawable.emoji_1f63c);
        EMOJIS_MAP.put(0x1f63d, R.drawable.emoji_1f63d);
        EMOJIS_MAP.put(0x1f640, R.drawable.emoji_1f640);
        EMOJIS_MAP.put(0x1f63f, R.drawable.emoji_1f63f);
        EMOJIS_MAP.put(0x1f63e, R.drawable.emoji_1f63e);
        EMOJIS_MAP.put(0x1f64c, R.drawable.emoji_1f64c_1f3fb);
        EMOJIS_MAP.put(0x1f44f, R.drawable.emoji_1f44f_1f3fb);
        EMOJIS_MAP.put(0x1f44b, R.drawable.emoji_1f44b_1f3fb);
        EMOJIS_MAP.put(0x1f44d, R.drawable.emoji_1f44d_1f3fb);
        EMOJIS_MAP.put(0x1f44e, R.drawable.emoji_1f44e_1f3fb);
        EMOJIS_MAP.put(0x1f44a, R.drawable.emoji_1f44a_1f3fb);
        EMOJIS_MAP.put(0x270a, R.drawable.emoji_270a_1f3fb);
        EMOJIS_MAP.put(0x270c, R.drawable.emoji_270c_1f3fb);
        EMOJIS_MAP.put(0x1f44c, R.drawable.emoji_1f44c_1f3fb);
        EMOJIS_MAP.put(0x270b, R.drawable.emoji_270b_1f3fb);
        EMOJIS_MAP.put(0x1f450, R.drawable.emoji_1f450_1f3fb);
        EMOJIS_MAP.put(0x1f4aa, R.drawable.emoji_1f4aa_1f3fb);
        EMOJIS_MAP.put(0x1f64f, R.drawable.emoji_1f64f_1f3fb);
        EMOJIS_MAP.put(0x261d, R.drawable.emoji_261d_1f3fb);
        EMOJIS_MAP.put(0x1f446, R.drawable.emoji_1f446_1f3fb);
        EMOJIS_MAP.put(0x1f447, R.drawable.emoji_1f447_1f3fb);
        EMOJIS_MAP.put(0x1f448, R.drawable.emoji_1f448_1f3fb);
        EMOJIS_MAP.put(0x1f449, R.drawable.emoji_1f449_1f3fb);
        EMOJIS_MAP.put(0x1f595, R.drawable.emoji_1f595_1f3fb);
        EMOJIS_MAP.put(0x1f590, R.drawable.emoji_1f590_1f3fb);
        EMOJIS_MAP.put(0x1f918, R.drawable.emoji_1f918_1f3fb);
        EMOJIS_MAP.put(0x1f596, R.drawable.emoji_1f596_1f3fb);
        EMOJIS_MAP.put(0x270d, R.drawable.emoji_270d_1f3fb);
        EMOJIS_MAP.put(0x1f485, R.drawable.emoji_1f485_1f3fb);
        EMOJIS_MAP.put(0x1f444, R.drawable.emoji_1f444);
        EMOJIS_MAP.put(0x1f445, R.drawable.emoji_1f445);
        EMOJIS_MAP.put(0x1f442, R.drawable.emoji_1f442_1f3fb);
        EMOJIS_MAP.put(0x1f443, R.drawable.emoji_1f443_1f3fb);
        EMOJIS_MAP.put(0x1f441, R.drawable.emoji_1f441);
        EMOJIS_MAP.put(0x1f440, R.drawable.emoji_1f440);
        EMOJIS_MAP.put(0x1f464, R.drawable.emoji_1f464);
        EMOJIS_MAP.put(0x1f465, R.drawable.emoji_1f465);
        EMOJIS_MAP.put(0x1f5e3, R.drawable.emoji_1f5e3);
        EMOJIS_MAP.put(0x1f476, R.drawable.emoji_1f476_1f3fb);
        EMOJIS_MAP.put(0x1f466, R.drawable.emoji_1f466_1f3fb);
        EMOJIS_MAP.put(0x1f467, R.drawable.emoji_1f467_1f3fb);
        EMOJIS_MAP.put(0x1f468, R.drawable.emoji_1f468_1f3fb);
        EMOJIS_MAP.put(0x1f469, R.drawable.emoji_1f469_1f3fb);
        EMOJIS_MAP.put(0x1f471, R.drawable.emoji_1f471_1f3fb);
        EMOJIS_MAP.put(0x1f474, R.drawable.emoji_1f474_1f3fb);
        EMOJIS_MAP.put(0x1f475, R.drawable.emoji_1f475_1f3fb);
        EMOJIS_MAP.put(0x1f472, R.drawable.emoji_1f472_1f3fb);
        EMOJIS_MAP.put(0x1f473, R.drawable.emoji_1f473_1f3fb);
        EMOJIS_MAP.put(0x1f46e, R.drawable.emoji_1f46e_1f3fb);
        EMOJIS_MAP.put(0x1f477, R.drawable.emoji_1f477_1f3fb);
        EMOJIS_MAP.put(0x1f482, R.drawable.emoji_1f482_1f3fb);
        EMOJIS_MAP.put(0x1f575, R.drawable.emoji_1f575);
        EMOJIS_MAP.put(0x1f385, R.drawable.emoji_1f385_1f3fb);
        EMOJIS_MAP.put(0x1f47c, R.drawable.emoji_1f47c_1f3fb);
        EMOJIS_MAP.put(0x1f478, R.drawable.emoji_1f478_1f3fb);
        EMOJIS_MAP.put(0x1f470, R.drawable.emoji_1f470_1f3fb);
        EMOJIS_MAP.put(0x1f6b6, R.drawable.emoji_1f6b6_1f3fb);
        EMOJIS_MAP.put(0x1f3c3, R.drawable.emoji_1f3c3_1f3fb);
        EMOJIS_MAP.put(0x1f483, R.drawable.emoji_1f483_1f3fb);
        EMOJIS_MAP.put(0x1f46f, R.drawable.emoji_1f46f);
        EMOJIS_MAP.put(0x1f46b, R.drawable.emoji_1f46b);
        EMOJIS_MAP.put(0x1f46c, R.drawable.emoji_1f46c);
        EMOJIS_MAP.put(0x1f46d, R.drawable.emoji_1f46d);
        EMOJIS_MAP.put(0x1f647, R.drawable.emoji_1f647_1f3fb);
        EMOJIS_MAP.put(0x1f481, R.drawable.emoji_1f481_1f3fb);
        EMOJIS_MAP.put(0x1f645, R.drawable.emoji_1f645_1f3fb);
        EMOJIS_MAP.put(0x1f646, R.drawable.emoji_1f646_1f3fb);
        EMOJIS_MAP.put(0x1f64b, R.drawable.emoji_1f64b_1f3fb);
        EMOJIS_MAP.put(0x1f64e, R.drawable.emoji_1f64e_1f3fb);
        EMOJIS_MAP.put(0x1f64d, R.drawable.emoji_1f64d_1f3fb);
        EMOJIS_MAP.put(0x1f487, R.drawable.emoji_1f487_1f3fb);
        EMOJIS_MAP.put(0x1f486, R.drawable.emoji_1f486_1f3fb);
        EMOJIS_MAP.put(0x1f491, R.drawable.emoji_1f491);
        EMOJIS_MAP.put(0x1f48f, R.drawable.emoji_1f48f);
        EMOJIS_MAP.put(0x1f46a, R.drawable.emoji_1f46a);
        EMOJIS_MAP.put(0x1f45a, R.drawable.emoji_1f45a);
        EMOJIS_MAP.put(0x1f455, R.drawable.emoji_1f455);
        EMOJIS_MAP.put(0x1f456, R.drawable.emoji_1f456);
        EMOJIS_MAP.put(0x1f454, R.drawable.emoji_1f454);
        EMOJIS_MAP.put(0x1f457, R.drawable.emoji_1f457);
        EMOJIS_MAP.put(0x1f459, R.drawable.emoji_1f459);
        EMOJIS_MAP.put(0x1f458, R.drawable.emoji_1f458);
        EMOJIS_MAP.put(0x1f484, R.drawable.emoji_1f484);
        EMOJIS_MAP.put(0x1f48b, R.drawable.emoji_1f48b);
        EMOJIS_MAP.put(0x1f463, R.drawable.emoji_1f463);
        EMOJIS_MAP.put(0x1f460, R.drawable.emoji_1f460);
        EMOJIS_MAP.put(0x1f461, R.drawable.emoji_1f461);
        EMOJIS_MAP.put(0x1f462, R.drawable.emoji_1f462);
        EMOJIS_MAP.put(0x1f45e, R.drawable.emoji_1f45e);
        EMOJIS_MAP.put(0x1f45f, R.drawable.emoji_1f45f);
        EMOJIS_MAP.put(0x1f452, R.drawable.emoji_1f452);
        EMOJIS_MAP.put(0x1f3a9, R.drawable.emoji_1f3a9);
        EMOJIS_MAP.put(0x1f393, R.drawable.emoji_1f393);
        EMOJIS_MAP.put(0x1f451, R.drawable.emoji_1f451);
        EMOJIS_MAP.put(0x26d1, R.drawable.emoji_26d1);
        SOFTBANKS_MAP.put(0xe43a, R.drawable.emoji_1f392);
        EMOJIS_MAP.put(0x1f45d, R.drawable.emoji_1f45d);
        EMOJIS_MAP.put(0x1f45b, R.drawable.emoji_1f45b);
        EMOJIS_MAP.put(0x1f45c, R.drawable.emoji_1f45c);
        EMOJIS_MAP.put(0x1f4bc, R.drawable.emoji_1f4bc);
        EMOJIS_MAP.put(0x1f453, R.drawable.emoji_1f453);
        EMOJIS_MAP.put(0x1f576, R.drawable.emoji_1f576);
        EMOJIS_MAP.put(0x1f48d, R.drawable.emoji_1f48d);
        EMOJIS_MAP.put(0x1f302, R.drawable.emoji_1f302);

        // Nature
        EMOJIS_MAP.put(0x1f436, R.drawable.emoji_1f436);
        EMOJIS_MAP.put(0x1f431, R.drawable.emoji_1f431);
        EMOJIS_MAP.put(0x1f42d, R.drawable.emoji_1f42d);
        EMOJIS_MAP.put(0x1f439, R.drawable.emoji_1f439);
        EMOJIS_MAP.put(0x1f430, R.drawable.emoji_1f430);
        EMOJIS_MAP.put(0x1f43b, R.drawable.emoji_1f43b);
        EMOJIS_MAP.put(0x1f43c, R.drawable.emoji_1f43c);
        EMOJIS_MAP.put(0x1f428, R.drawable.emoji_1f428);
        EMOJIS_MAP.put(0x1f42f, R.drawable.emoji_1f42f);
        EMOJIS_MAP.put(0x1f981, R.drawable.emoji_1f981);
        EMOJIS_MAP.put(0x1f42e, R.drawable.emoji_1f42e);
        EMOJIS_MAP.put(0x1f437, R.drawable.emoji_1f437);
        EMOJIS_MAP.put(0x1f43d, R.drawable.emoji_1f43d);
        EMOJIS_MAP.put(0x1f438, R.drawable.emoji_1f438);
        EMOJIS_MAP.put(0x1f419, R.drawable.emoji_1f419);
        EMOJIS_MAP.put(0x1f435, R.drawable.emoji_1f435);
        EMOJIS_MAP.put(0x1f648, R.drawable.emoji_1f648);
        EMOJIS_MAP.put(0x1f649, R.drawable.emoji_1f649);
        EMOJIS_MAP.put(0x1f64a, R.drawable.emoji_1f64a);
        EMOJIS_MAP.put(0x1f412, R.drawable.emoji_1f412);
        EMOJIS_MAP.put(0x1f414, R.drawable.emoji_1f414);
        EMOJIS_MAP.put(0x1f427, R.drawable.emoji_1f427);
        EMOJIS_MAP.put(0x1f426, R.drawable.emoji_1f426);
        EMOJIS_MAP.put(0x1f424, R.drawable.emoji_1f424);
        EMOJIS_MAP.put(0x1f423, R.drawable.emoji_1f423);
        EMOJIS_MAP.put(0x1f425, R.drawable.emoji_1f425);
        EMOJIS_MAP.put(0x1f43a, R.drawable.emoji_1f43a);
        EMOJIS_MAP.put(0x1f417, R.drawable.emoji_1f417);
        EMOJIS_MAP.put(0x1f434, R.drawable.emoji_1f434);
        EMOJIS_MAP.put(0x1f984, R.drawable.emoji_1f984);
        EMOJIS_MAP.put(0x1f41d, R.drawable.emoji_1f41d);
        EMOJIS_MAP.put(0x1f41b, R.drawable.emoji_1f41b);
        EMOJIS_MAP.put(0x1f40c, R.drawable.emoji_1f40c);
        EMOJIS_MAP.put(0x1f41e, R.drawable.emoji_1f41e);
        EMOJIS_MAP.put(0x1f41c, R.drawable.emoji_1f41c);
        EMOJIS_MAP.put(0x1f577, R.drawable.emoji_1f577);
        EMOJIS_MAP.put(0x1f982, R.drawable.emoji_1f982);
        EMOJIS_MAP.put(0x1f980, R.drawable.emoji_1f980);
        EMOJIS_MAP.put(0x1f40d, R.drawable.emoji_1f40d);
        EMOJIS_MAP.put(0x1f422, R.drawable.emoji_1f422);
        EMOJIS_MAP.put(0x1f420, R.drawable.emoji_1f420);
        EMOJIS_MAP.put(0x1f41f, R.drawable.emoji_1f41f);
        EMOJIS_MAP.put(0x1f421, R.drawable.emoji_1f421);
        EMOJIS_MAP.put(0x1f42c, R.drawable.emoji_1f42c);
        EMOJIS_MAP.put(0x1f433, R.drawable.emoji_1f433);
        EMOJIS_MAP.put(0x1f40b, R.drawable.emoji_1f40b);
        EMOJIS_MAP.put(0x1f40a, R.drawable.emoji_1f40a);
        EMOJIS_MAP.put(0x1f406, R.drawable.emoji_1f406);
        EMOJIS_MAP.put(0x1f405, R.drawable.emoji_1f405);
        EMOJIS_MAP.put(0x1f403, R.drawable.emoji_1f403);
        EMOJIS_MAP.put(0x1f402, R.drawable.emoji_1f402);
        EMOJIS_MAP.put(0x1f404, R.drawable.emoji_1f404);
        EMOJIS_MAP.put(0x1f42a, R.drawable.emoji_1f42a);
        EMOJIS_MAP.put(0x1f42b, R.drawable.emoji_1f42b);
        EMOJIS_MAP.put(0x1f418, R.drawable.emoji_1f418);
        EMOJIS_MAP.put(0x1f410, R.drawable.emoji_1f410);
        EMOJIS_MAP.put(0x1f40f, R.drawable.emoji_1f40f);
        EMOJIS_MAP.put(0x1f411, R.drawable.emoji_1f411);
        EMOJIS_MAP.put(0x1f40e, R.drawable.emoji_1f40e);
        EMOJIS_MAP.put(0x1f416, R.drawable.emoji_1f416);
        EMOJIS_MAP.put(0x1f400, R.drawable.emoji_1f400);
        EMOJIS_MAP.put(0x1f401, R.drawable.emoji_1f401);
        EMOJIS_MAP.put(0x1f413, R.drawable.emoji_1f413);
        EMOJIS_MAP.put(0x1f983, R.drawable.emoji_1f983);
        EMOJIS_MAP.put(0x1f54a, R.drawable.emoji_1f54a);
        EMOJIS_MAP.put(0x1f415, R.drawable.emoji_1f415);
        EMOJIS_MAP.put(0x1f429, R.drawable.emoji_1f429);
        EMOJIS_MAP.put(0x1f408, R.drawable.emoji_1f408);
        EMOJIS_MAP.put(0x1f407, R.drawable.emoji_1f407);
        EMOJIS_MAP.put(0x1f43f, R.drawable.emoji_1f43f);
        EMOJIS_MAP.put(0x1f43e, R.drawable.emoji_1f43e);
        EMOJIS_MAP.put(0x1f409, R.drawable.emoji_1f409);
        EMOJIS_MAP.put(0x1f432, R.drawable.emoji_1f432);
        EMOJIS_MAP.put(0x1f335, R.drawable.emoji_1f335);
        SOFTBANKS_MAP.put(0xe033, R.drawable.emoji_1f384);
        EMOJIS_MAP.put(0x1f332, R.drawable.emoji_1f332);
        EMOJIS_MAP.put(0x1f333, R.drawable.emoji_1f333);
        EMOJIS_MAP.put(0x1f334, R.drawable.emoji_1f334);
        SOFTBANKS_MAP.put(0xe110, R.drawable.emoji_1f331);
        EMOJIS_MAP.put(0x1f33f, R.drawable.emoji_1f33f);
        EMOJIS_MAP.put(0x1f340, R.drawable.emoji_1f340);
        EMOJIS_MAP.put(0x2618, R.drawable.emoji_2618);
        EMOJIS_MAP.put(0x1f38d, R.drawable.emoji_1f38d);
        EMOJIS_MAP.put(0x1f38b, R.drawable.emoji_1f38b);
        EMOJIS_MAP.put(0x1f343, R.drawable.emoji_1f343);
        EMOJIS_MAP.put(0x1f342, R.drawable.emoji_1f342);
        EMOJIS_MAP.put(0x1f341, R.drawable.emoji_1f341);
        EMOJIS_MAP.put(0x1f33e, R.drawable.emoji_1f33e);
        EMOJIS_MAP.put(0x1f33a, R.drawable.emoji_1f33a);
        EMOJIS_MAP.put(0x1f33b, R.drawable.emoji_1f33b);
        EMOJIS_MAP.put(0x1f339, R.drawable.emoji_1f339);
        EMOJIS_MAP.put(0x1f337, R.drawable.emoji_1f337);
        EMOJIS_MAP.put(0x1f33c, R.drawable.emoji_1f33c);
        EMOJIS_MAP.put(0x1f338, R.drawable.emoji_1f338);
        EMOJIS_MAP.put(0x1f490, R.drawable.emoji_1f490);
        EMOJIS_MAP.put(0x1f344, R.drawable.emoji_1f344);
        EMOJIS_MAP.put(0x1f330, R.drawable.emoji_1f330);
        EMOJIS_MAP.put(0x1f383, R.drawable.emoji_1f383);
        EMOJIS_MAP.put(0x1f41a, R.drawable.emoji_1f41a);
        EMOJIS_MAP.put(0x1f578, R.drawable.emoji_1f578);
        EMOJIS_MAP.put(0x1f30d, R.drawable.emoji_1f30d);
        EMOJIS_MAP.put(0x1f30e, R.drawable.emoji_1f30e);
        EMOJIS_MAP.put(0x1f30f, R.drawable.emoji_1f30f);
        EMOJIS_MAP.put(0x1f315, R.drawable.emoji_1f315);
        EMOJIS_MAP.put(0x1f316, R.drawable.emoji_1f316);
        EMOJIS_MAP.put(0x1f317, R.drawable.emoji_1f317);
        EMOJIS_MAP.put(0x1f318, R.drawable.emoji_1f318);
        EMOJIS_MAP.put(0x1f311, R.drawable.emoji_1f311);
        EMOJIS_MAP.put(0x1f312, R.drawable.emoji_1f312);
        EMOJIS_MAP.put(0x1f313, R.drawable.emoji_1f313);
        EMOJIS_MAP.put(0x1f314, R.drawable.emoji_1f314);
        EMOJIS_MAP.put(0x1f31a, R.drawable.emoji_1f31a);
        EMOJIS_MAP.put(0x1f31d, R.drawable.emoji_1f31d);
        EMOJIS_MAP.put(0x1f31b, R.drawable.emoji_1f31b);
        EMOJIS_MAP.put(0x1f31c, R.drawable.emoji_1f31c);
        EMOJIS_MAP.put(0x1f31e, R.drawable.emoji_1f31e);
        EMOJIS_MAP.put(0x1f319, R.drawable.emoji_1f319);
        EMOJIS_MAP.put(0x2b50, R.drawable.emoji_2b50);
        EMOJIS_MAP.put(0x1f31f, R.drawable.emoji_1f31f);
        SOFTBANKS_MAP.put(0xe335, R.drawable.emoji_1f31f);
        EMOJIS_MAP.put(0x1f4ab, R.drawable.emoji_1f4ab);
        EMOJIS_MAP.put(0x2728, R.drawable.emoji_2728);
        SOFTBANKS_MAP.put(0xe32e, R.drawable.emoji_2728);
        EMOJIS_MAP.put(0x2604, R.drawable.emoji_2604);
        EMOJIS_MAP.put(0x2600, R.drawable.emoji_2600);
        EMOJIS_MAP.put(0x1f324, R.drawable.emoji_1f324);
        EMOJIS_MAP.put(0x26c5, R.drawable.emoji_26c5);
        EMOJIS_MAP.put(0x1f325, R.drawable.emoji_1f325);
        EMOJIS_MAP.put(0x1f326, R.drawable.emoji_1f326);
        EMOJIS_MAP.put(0x2601, R.drawable.emoji_2601);
        EMOJIS_MAP.put(0x1f327, R.drawable.emoji_1f327);
        EMOJIS_MAP.put(0x26c8, R.drawable.emoji_26c8);
        EMOJIS_MAP.put(0x1f329, R.drawable.emoji_1f329);
        EMOJIS_MAP.put(0x26a1, R.drawable.emoji_26a1);
        SOFTBANKS_MAP.put(0xe11d, R.drawable.emoji_1f525);
        EMOJIS_MAP.put(0x1f4a5, R.drawable.emoji_1f4a5);
        EMOJIS_MAP.put(0x2744, R.drawable.emoji_2744);
        EMOJIS_MAP.put(0x1f328, R.drawable.emoji_1f328);
        EMOJIS_MAP.put(0x2603, R.drawable.emoji_2603);
        EMOJIS_MAP.put(0x26c4, R.drawable.emoji_26c4);
        EMOJIS_MAP.put(0x1f32c, R.drawable.emoji_1f32c);
        EMOJIS_MAP.put(0x1f4a8, R.drawable.emoji_1f4a8);
        EMOJIS_MAP.put(0x1f32a, R.drawable.emoji_1f32a);
        EMOJIS_MAP.put(0x1f32b, R.drawable.emoji_1f32b);
        EMOJIS_MAP.put(0x2602, R.drawable.emoji_2602);
        EMOJIS_MAP.put(0x2614, R.drawable.emoji_2614);
        EMOJIS_MAP.put(0x1f4a6, R.drawable.emoji_1f4a6);
        EMOJIS_MAP.put(0x1f4a7, R.drawable.emoji_1f4a7);
        EMOJIS_MAP.put(0x1f30a, R.drawable.emoji_1f30a);

        // Food
        EMOJIS_MAP.put(0x1f34f, R.drawable.emoji_1f34f);
        EMOJIS_MAP.put(0x1f34e, R.drawable.emoji_1f34e);
        EMOJIS_MAP.put(0x1f350, R.drawable.emoji_1f350);
        EMOJIS_MAP.put(0x1f34a, R.drawable.emoji_1f34a);
        EMOJIS_MAP.put(0x1f34b, R.drawable.emoji_1f34b);
        EMOJIS_MAP.put(0x1f34c, R.drawable.emoji_1f34c);
        EMOJIS_MAP.put(0x1f349, R.drawable.emoji_1f349);
        EMOJIS_MAP.put(0x1f347, R.drawable.emoji_1f347);
        EMOJIS_MAP.put(0x1f353, R.drawable.emoji_1f353);
        EMOJIS_MAP.put(0x1f348, R.drawable.emoji_1f348);
        EMOJIS_MAP.put(0x1f352, R.drawable.emoji_1f352);
        EMOJIS_MAP.put(0x1f351, R.drawable.emoji_1f351);
        EMOJIS_MAP.put(0x1f34d, R.drawable.emoji_1f34d);
        EMOJIS_MAP.put(0x1f345, R.drawable.emoji_1f345);
        EMOJIS_MAP.put(0x1f346, R.drawable.emoji_1f346);
        EMOJIS_MAP.put(0x1f336, R.drawable.emoji_1f336);
        EMOJIS_MAP.put(0x1f33d, R.drawable.emoji_1f33d);
        EMOJIS_MAP.put(0x1f360, R.drawable.emoji_1f360);
        EMOJIS_MAP.put(0x1f36f, R.drawable.emoji_1f36f);
        EMOJIS_MAP.put(0x1f35e, R.drawable.emoji_1f35e);
        EMOJIS_MAP.put(0x1f9c0, R.drawable.emoji_1f9c0);
        EMOJIS_MAP.put(0x1f357, R.drawable.emoji_1f357);
        EMOJIS_MAP.put(0x1f356, R.drawable.emoji_1f356);
        EMOJIS_MAP.put(0x1f364, R.drawable.emoji_1f364);
        EMOJIS_MAP.put(0x1f373, R.drawable.emoji_1f373);
        EMOJIS_MAP.put(0x1f354, R.drawable.emoji_1f354);
        EMOJIS_MAP.put(0x1f35f, R.drawable.emoji_1f35f);
        EMOJIS_MAP.put(0x1f32d, R.drawable.emoji_1f32d);
        EMOJIS_MAP.put(0x1f355, R.drawable.emoji_1f355);
        EMOJIS_MAP.put(0x1f35d, R.drawable.emoji_1f35d);
        EMOJIS_MAP.put(0x1f32e, R.drawable.emoji_1f32e);
        EMOJIS_MAP.put(0x1f32f, R.drawable.emoji_1f32f);
        EMOJIS_MAP.put(0x1f35c, R.drawable.emoji_1f35c);
        EMOJIS_MAP.put(0x1f372, R.drawable.emoji_1f372);
        EMOJIS_MAP.put(0x1f365, R.drawable.emoji_1f365);
        EMOJIS_MAP.put(0x1f363, R.drawable.emoji_1f363);
        EMOJIS_MAP.put(0x1f371, R.drawable.emoji_1f371);
        EMOJIS_MAP.put(0x1f35b, R.drawable.emoji_1f35b);
        EMOJIS_MAP.put(0x1f359, R.drawable.emoji_1f359);
        EMOJIS_MAP.put(0x1f35a, R.drawable.emoji_1f35a);
        EMOJIS_MAP.put(0x1f358, R.drawable.emoji_1f358);
        EMOJIS_MAP.put(0x1f362, R.drawable.emoji_1f362);
        EMOJIS_MAP.put(0x1f361, R.drawable.emoji_1f361);
        EMOJIS_MAP.put(0x1f367, R.drawable.emoji_1f367);
        EMOJIS_MAP.put(0x1f368, R.drawable.emoji_1f368);
        EMOJIS_MAP.put(0x1f366, R.drawable.emoji_1f366);
        EMOJIS_MAP.put(0x1f370, R.drawable.emoji_1f370);
        EMOJIS_MAP.put(0x1f382, R.drawable.emoji_1f382);
        EMOJIS_MAP.put(0x1f36e, R.drawable.emoji_1f36e);
        EMOJIS_MAP.put(0x1f36c, R.drawable.emoji_1f36c);
        EMOJIS_MAP.put(0x1f36d, R.drawable.emoji_1f36d);
        EMOJIS_MAP.put(0x1f36b, R.drawable.emoji_1f36b);
        EMOJIS_MAP.put(0x1f37f, R.drawable.emoji_1f37f);
        EMOJIS_MAP.put(0x1f369, R.drawable.emoji_1f369);
        EMOJIS_MAP.put(0x1f36a, R.drawable.emoji_1f36a);
        EMOJIS_MAP.put(0x1f37a, R.drawable.emoji_1f37a);
        EMOJIS_MAP.put(0x1f37b, R.drawable.emoji_1f37b);
        EMOJIS_MAP.put(0x1f377, R.drawable.emoji_1f377);
        EMOJIS_MAP.put(0x1f378, R.drawable.emoji_1f378);
        EMOJIS_MAP.put(0x1f379, R.drawable.emoji_1f379);
        EMOJIS_MAP.put(0x1f37e, R.drawable.emoji_1f37e);
        EMOJIS_MAP.put(0x1f376, R.drawable.emoji_1f376);
        EMOJIS_MAP.put(0x1f375, R.drawable.emoji_1f375);
        EMOJIS_MAP.put(0x2615, R.drawable.emoji_2615);
        EMOJIS_MAP.put(0x1f37c, R.drawable.emoji_1f37c);
        EMOJIS_MAP.put(0x1f374, R.drawable.emoji_1f374);
        EMOJIS_MAP.put(0x1f37d, R.drawable.emoji_1f37d);

        // Sport
        EMOJIS_MAP.put(0x26bd, R.drawable.emoji_26bd);
        EMOJIS_MAP.put(0x1f3c0, R.drawable.emoji_1f3c0);
        EMOJIS_MAP.put(0x1f3c8, R.drawable.emoji_1f3c8);
        EMOJIS_MAP.put(0x26be, R.drawable.emoji_26be);
        EMOJIS_MAP.put(0x1f3be, R.drawable.emoji_1f3be);
        EMOJIS_MAP.put(0x1f3d0, R.drawable.emoji_1f3d0);
        EMOJIS_MAP.put(0x1f3c9, R.drawable.emoji_1f3c9);
        EMOJIS_MAP.put(0x1f3b1, R.drawable.emoji_1f3b1);
        EMOJIS_MAP.put(0x26f3, R.drawable.emoji_26f3);
        EMOJIS_MAP.put(0x1f3cc, R.drawable.emoji_1f3cc);
        EMOJIS_MAP.put(0x1f3d3, R.drawable.emoji_1f3d3);
        EMOJIS_MAP.put(0x1f3f8, R.drawable.emoji_1f3f8);
        EMOJIS_MAP.put(0x1f3d2, R.drawable.emoji_1f3d2);
        EMOJIS_MAP.put(0x1f3d1, R.drawable.emoji_1f3d1);
        EMOJIS_MAP.put(0x1f3cf, R.drawable.emoji_1f3cf);
        EMOJIS_MAP.put(0x1f3bf, R.drawable.emoji_1f3bf);
        EMOJIS_MAP.put(0x26f7, R.drawable.emoji_26f7);
        EMOJIS_MAP.put(0x1f3c2, R.drawable.emoji_1f3c2);
        EMOJIS_MAP.put(0x26f8, R.drawable.emoji_26f8);
        EMOJIS_MAP.put(0x1f3f9, R.drawable.emoji_1f3f9);
        EMOJIS_MAP.put(0x1f3a3, R.drawable.emoji_1f3a3);
        EMOJIS_MAP.put(0x1f6a3, R.drawable.emoji_1f6a3_1f3fb);
        EMOJIS_MAP.put(0x1f3ca, R.drawable.emoji_1f3ca_1f3fb);
        EMOJIS_MAP.put(0x1f3c4, R.drawable.emoji_1f3c4_1f3fb);
        EMOJIS_MAP.put(0x1f6c0, R.drawable.emoji_1f6c0_1f3fb);
        EMOJIS_MAP.put(0x26f9, R.drawable.emoji_26f9_1f3fb);
        EMOJIS_MAP.put(0x1f3cb, R.drawable.emoji_1f3cb_1f3fb);
        EMOJIS_MAP.put(0x1f6b4, R.drawable.emoji_1f6b4_1f3fb);
        EMOJIS_MAP.put(0x1f6b5, R.drawable.emoji_1f6b5_1f3fb);
        EMOJIS_MAP.put(0x1f3c7, R.drawable.emoji_1f3c7_1f3fb);
        EMOJIS_MAP.put(0x1f574, R.drawable.emoji_1f574);
        EMOJIS_MAP.put(0x1f3c6, R.drawable.emoji_1f3c6);
        EMOJIS_MAP.put(0x1f3bd, R.drawable.emoji_1f3bd);
        EMOJIS_MAP.put(0x1f3c5, R.drawable.emoji_1f3c5);
        EMOJIS_MAP.put(0x1f396, R.drawable.emoji_1f396);
        EMOJIS_MAP.put(0x1f397, R.drawable.emoji_1f397);
        EMOJIS_MAP.put(0x1f3f5, R.drawable.emoji_1f3f5);
        SOFTBANKS_MAP.put(0xe125, R.drawable.emoji_1f3ab);
        EMOJIS_MAP.put(0x1f39f, R.drawable.emoji_1f39f);
        EMOJIS_MAP.put(0x1f3ad, R.drawable.emoji_1f3ad);
        EMOJIS_MAP.put(0x1f3a8, R.drawable.emoji_1f3a8);
        EMOJIS_MAP.put(0x1f3aa, R.drawable.emoji_1f3aa);
        EMOJIS_MAP.put(0x1f3a4, R.drawable.emoji_1f3a4);
        EMOJIS_MAP.put(0x1f3a7, R.drawable.emoji_1f3a7);
        EMOJIS_MAP.put(0x1f3bc, R.drawable.emoji_1f3bc);
        EMOJIS_MAP.put(0x1f3b9, R.drawable.emoji_1f3b9);
        EMOJIS_MAP.put(0x1f3b7, R.drawable.emoji_1f3b7);
        EMOJIS_MAP.put(0x1f3ba, R.drawable.emoji_1f3ba);
        EMOJIS_MAP.put(0x1f3bb, R.drawable.emoji_1f3bb);
        EMOJIS_MAP.put(0x1f3b8, R.drawable.emoji_1f3b8);
        EMOJIS_MAP.put(0x1f3ac, R.drawable.emoji_1f3ac);
        EMOJIS_MAP.put(0x1f3ae, R.drawable.emoji_1f3ae);
        EMOJIS_MAP.put(0x1f47e, R.drawable.emoji_1f47e);
        EMOJIS_MAP.put(0x1f3af, R.drawable.emoji_1f3af);
        EMOJIS_MAP.put(0x1f3b2, R.drawable.emoji_1f3b2);
        EMOJIS_MAP.put(0x1f3b0, R.drawable.emoji_1f3b0);
        EMOJIS_MAP.put(0x1f3b3, R.drawable.emoji_1f3b3);

        // Cars
        EMOJIS_MAP.put(0x1f697, R.drawable.emoji_1f697);
        EMOJIS_MAP.put(0x1f695, R.drawable.emoji_1f695);
        EMOJIS_MAP.put(0x1f699, R.drawable.emoji_1f699);
        EMOJIS_MAP.put(0x1f68c, R.drawable.emoji_1f68c);
        EMOJIS_MAP.put(0x1f68e, R.drawable.emoji_1f68e);
        EMOJIS_MAP.put(0x1f3ce, R.drawable.emoji_1f3ce);
        EMOJIS_MAP.put(0x1f693, R.drawable.emoji_1f693);
        EMOJIS_MAP.put(0x1f691, R.drawable.emoji_1f691);
        EMOJIS_MAP.put(0x1f692, R.drawable.emoji_1f692);
        EMOJIS_MAP.put(0x1f690, R.drawable.emoji_1f690);
        EMOJIS_MAP.put(0x1f69a, R.drawable.emoji_1f69a);
        EMOJIS_MAP.put(0x1f69b, R.drawable.emoji_1f69b);
        EMOJIS_MAP.put(0x1f69c, R.drawable.emoji_1f69c);
        EMOJIS_MAP.put(0x1f3cd, R.drawable.emoji_1f3cd);
        EMOJIS_MAP.put(0x1f6b2, R.drawable.emoji_1f6b2);
        EMOJIS_MAP.put(0x1f6a8, R.drawable.emoji_1f6a8);
        EMOJIS_MAP.put(0x1f694, R.drawable.emoji_1f694);
        EMOJIS_MAP.put(0x1f68d, R.drawable.emoji_1f68d);
        EMOJIS_MAP.put(0x1f698, R.drawable.emoji_1f698);
        EMOJIS_MAP.put(0x1f696, R.drawable.emoji_1f696);
        EMOJIS_MAP.put(0x1f6a1, R.drawable.emoji_1f6a1);
        EMOJIS_MAP.put(0x1f6a0, R.drawable.emoji_1f6a0);
        EMOJIS_MAP.put(0x1f69f, R.drawable.emoji_1f69f);
        EMOJIS_MAP.put(0x1f68b, R.drawable.emoji_1f68b);
        EMOJIS_MAP.put(0x1f683, R.drawable.emoji_1f683);
        EMOJIS_MAP.put(0x1f69d, R.drawable.emoji_1f69d);
        EMOJIS_MAP.put(0x1f684, R.drawable.emoji_1f684);
        EMOJIS_MAP.put(0x1f685, R.drawable.emoji_1f685);
        EMOJIS_MAP.put(0x1f688, R.drawable.emoji_1f688);
        EMOJIS_MAP.put(0x1f69e, R.drawable.emoji_1f69e);
        EMOJIS_MAP.put(0x1f682, R.drawable.emoji_1f682);
        EMOJIS_MAP.put(0x1f686, R.drawable.emoji_1f686);
        EMOJIS_MAP.put(0x1f687, R.drawable.emoji_1f687);
        EMOJIS_MAP.put(0x1f68a, R.drawable.emoji_1f68a);
        EMOJIS_MAP.put(0x1f689, R.drawable.emoji_1f689);
        EMOJIS_MAP.put(0x1f681, R.drawable.emoji_1f681);
        EMOJIS_MAP.put(0x1f6e9, R.drawable.emoji_1f6e9);
        EMOJIS_MAP.put(0x2708, R.drawable.emoji_2708);
        EMOJIS_MAP.put(0x1f6eb, R.drawable.emoji_1f6eb);
        EMOJIS_MAP.put(0x1f6ec, R.drawable.emoji_1f6ec);
        EMOJIS_MAP.put(0x26f5, R.drawable.emoji_26f5);
        EMOJIS_MAP.put(0x1f6e5, R.drawable.emoji_1f6e5);
        EMOJIS_MAP.put(0x1f6a4, R.drawable.emoji_1f6a4);
        EMOJIS_MAP.put(0x26f4, R.drawable.emoji_26f4);
        EMOJIS_MAP.put(0x1f6f3, R.drawable.emoji_1f6f3);
        EMOJIS_MAP.put(0x1f680, R.drawable.emoji_1f680);
        EMOJIS_MAP.put(0x1f6f0, R.drawable.emoji_1f6f0);
        EMOJIS_MAP.put(0x1f4ba, R.drawable.emoji_1f4ba);
        EMOJIS_MAP.put(0x2693, R.drawable.emoji_2693);
        EMOJIS_MAP.put(0x1f6a7, R.drawable.emoji_1f6a7);
        EMOJIS_MAP.put(0x26fd, R.drawable.emoji_26fd);
        EMOJIS_MAP.put(0x1f68f, R.drawable.emoji_1f68f);
        EMOJIS_MAP.put(0x1f6a6, R.drawable.emoji_1f6a6);
        EMOJIS_MAP.put(0x1f6a5, R.drawable.emoji_1f6a5);
        EMOJIS_MAP.put(0x1f3c1, R.drawable.emoji_1f3c1);
        EMOJIS_MAP.put(0x1f6a2, R.drawable.emoji_1f6a2);
        EMOJIS_MAP.put(0x1f3a1, R.drawable.emoji_1f3a1);
        EMOJIS_MAP.put(0x1f3a2, R.drawable.emoji_1f3a2);
        EMOJIS_MAP.put(0x1f3a0, R.drawable.emoji_1f3a0);
        EMOJIS_MAP.put(0x1f3d7, R.drawable.emoji_1f3d7);
        EMOJIS_MAP.put(0x1f301, R.drawable.emoji_1f301);
        EMOJIS_MAP.put(0x1f5fc, R.drawable.emoji_1f5fc);
        EMOJIS_MAP.put(0x1f3ed, R.drawable.emoji_1f3ed);
        EMOJIS_MAP.put(0x26f2, R.drawable.emoji_26f2);
        SOFTBANKS_MAP.put(0xe446, R.drawable.emoji_1f391);
        EMOJIS_MAP.put(0x26f0, R.drawable.emoji_26f0);
        EMOJIS_MAP.put(0x1f3d4, R.drawable.emoji_1f3d4);
        EMOJIS_MAP.put(0x1f5fb, R.drawable.emoji_1f5fb);
        EMOJIS_MAP.put(0x1f30b, R.drawable.emoji_1f30b);
        EMOJIS_MAP.put(0x1f5fe, R.drawable.emoji_1f5fe);
        EMOJIS_MAP.put(0x1f3d5, R.drawable.emoji_1f3d5);
        EMOJIS_MAP.put(0x26fa, R.drawable.emoji_26fa);
        EMOJIS_MAP.put(0x1f3de, R.drawable.emoji_1f3de);
        EMOJIS_MAP.put(0x1f6e3, R.drawable.emoji_1f6e3);
        EMOJIS_MAP.put(0x1f6e4, R.drawable.emoji_1f6e4);
        EMOJIS_MAP.put(0x1f305, R.drawable.emoji_1f305);
        EMOJIS_MAP.put(0x1f304, R.drawable.emoji_1f304);
        EMOJIS_MAP.put(0x1f3dc, R.drawable.emoji_1f3dc);
        EMOJIS_MAP.put(0x1f3d6, R.drawable.emoji_1f3d6);
        EMOJIS_MAP.put(0x1f3dd, R.drawable.emoji_1f3dd);
        EMOJIS_MAP.put(0x1f307, R.drawable.emoji_1f307);
        EMOJIS_MAP.put(0x1f306, R.drawable.emoji_1f306);
        EMOJIS_MAP.put(0x1f3d9, R.drawable.emoji_1f3d9);
        EMOJIS_MAP.put(0x1f303, R.drawable.emoji_1f303);
        EMOJIS_MAP.put(0x1f320, R.drawable.emoji_1f320);
        EMOJIS_MAP.put(0x1f309, R.drawable.emoji_1f309);
        EMOJIS_MAP.put(0x1f30c, R.drawable.emoji_1f30c);
        EMOJIS_MAP.put(0x1f386, R.drawable.emoji_1f386);
        EMOJIS_MAP.put(0x1f387, R.drawable.emoji_1f387);
        SOFTBANKS_MAP.put(0xe44c, R.drawable.emoji_1f308);
        EMOJIS_MAP.put(0x1f3d8, R.drawable.emoji_1f3d8);
        EMOJIS_MAP.put(0x1f3f0, R.drawable.emoji_1f3f0);
        EMOJIS_MAP.put(0x1f3ef, R.drawable.emoji_1f3ef);
        EMOJIS_MAP.put(0x1f3df, R.drawable.emoji_1f3df);
        EMOJIS_MAP.put(0x1f5fd, R.drawable.emoji_1f5fd);
        EMOJIS_MAP.put(0x1f3e0, R.drawable.emoji_1f3e0);
        EMOJIS_MAP.put(0x1f3e1, R.drawable.emoji_1f3e1);
        EMOJIS_MAP.put(0x1f3da, R.drawable.emoji_1f3da);
        EMOJIS_MAP.put(0x1f3e2, R.drawable.emoji_1f3e2);
        EMOJIS_MAP.put(0x1f3ec, R.drawable.emoji_1f3ec);
        EMOJIS_MAP.put(0x1f3e3, R.drawable.emoji_1f3e3);
        EMOJIS_MAP.put(0x1f3e4, R.drawable.emoji_1f3e4);
        EMOJIS_MAP.put(0x1f3e5, R.drawable.emoji_1f3e5);
        EMOJIS_MAP.put(0x1f3e6, R.drawable.emoji_1f3e6);
        EMOJIS_MAP.put(0x1f3e8, R.drawable.emoji_1f3e8);
        EMOJIS_MAP.put(0x1f3ea, R.drawable.emoji_1f3ea);
        EMOJIS_MAP.put(0x1f3eb, R.drawable.emoji_1f3eb);
        EMOJIS_MAP.put(0x1f3e9, R.drawable.emoji_1f3e9);
        EMOJIS_MAP.put(0x1f492, R.drawable.emoji_1f492);
        EMOJIS_MAP.put(0x1f3db, R.drawable.emoji_1f3db);
        EMOJIS_MAP.put(0x26ea, R.drawable.emoji_26ea);
        EMOJIS_MAP.put(0x1f54c, R.drawable.emoji_1f54c);
        EMOJIS_MAP.put(0x1f54d, R.drawable.emoji_1f54d);
        EMOJIS_MAP.put(0x1f54b, R.drawable.emoji_1f54b);
        EMOJIS_MAP.put(0x26e9, R.drawable.emoji_26e9);

        // Electronics
        EMOJIS_MAP.put(0x231a, R.drawable.emoji_231a);
        EMOJIS_MAP.put(0x1f4f1, R.drawable.emoji_1f4f1);
        EMOJIS_MAP.put(0x1f4f2, R.drawable.emoji_1f4f2);
        EMOJIS_MAP.put(0x1f4bb, R.drawable.emoji_1f4bb);
        EMOJIS_MAP.put(0x2328, R.drawable.emoji_2328);
        EMOJIS_MAP.put(0x1f5a5, R.drawable.emoji_1f5a5);
        EMOJIS_MAP.put(0x1f5a8, R.drawable.emoji_1f5a8);
        EMOJIS_MAP.put(0x1f5b1, R.drawable.emoji_1f5b1);
        EMOJIS_MAP.put(0x1f5b2, R.drawable.emoji_1f5b2);
        EMOJIS_MAP.put(0x1f579, R.drawable.emoji_1f579);
        EMOJIS_MAP.put(0x1f5dc, R.drawable.emoji_1f5dc);
        EMOJIS_MAP.put(0x1f4bd, R.drawable.emoji_1f4bd);
        EMOJIS_MAP.put(0x1f4be, R.drawable.emoji_1f4be);
        EMOJIS_MAP.put(0x1f4bf, R.drawable.emoji_1f4bf);
        EMOJIS_MAP.put(0x1f4c0, R.drawable.emoji_1f4c0);
        EMOJIS_MAP.put(0x1f4fc, R.drawable.emoji_1f4fc);
        EMOJIS_MAP.put(0x1f4f7, R.drawable.emoji_1f4f7);
        EMOJIS_MAP.put(0x1f4f8, R.drawable.emoji_1f4f8);
        EMOJIS_MAP.put(0x1f4f9, R.drawable.emoji_1f4f9);
        EMOJIS_MAP.put(0x1f3a5, R.drawable.emoji_1f3a5);
        EMOJIS_MAP.put(0x1f4fd, R.drawable.emoji_1f4fd);
        EMOJIS_MAP.put(0x1f39e, R.drawable.emoji_1f39e);
        EMOJIS_MAP.put(0x1f4de, R.drawable.emoji_1f4de);
        EMOJIS_MAP.put(0x260e, R.drawable.emoji_260e);
        EMOJIS_MAP.put(0x1f4df, R.drawable.emoji_1f4df);
        EMOJIS_MAP.put(0x1f4e0, R.drawable.emoji_1f4e0);
        EMOJIS_MAP.put(0x1f4fa, R.drawable.emoji_1f4fa);
        EMOJIS_MAP.put(0x1f4fb, R.drawable.emoji_1f4fb);
        EMOJIS_MAP.put(0x1f399, R.drawable.emoji_1f399);
        EMOJIS_MAP.put(0x1f39a, R.drawable.emoji_1f39a);
        EMOJIS_MAP.put(0x1f39b, R.drawable.emoji_1f39b);
        EMOJIS_MAP.put(0x23f1, R.drawable.emoji_23f1);
        EMOJIS_MAP.put(0x23f2, R.drawable.emoji_23f2);
        EMOJIS_MAP.put(0x23f0, R.drawable.emoji_23f0);
        EMOJIS_MAP.put(0x1f570, R.drawable.emoji_1f570);
        EMOJIS_MAP.put(0x23f3, R.drawable.emoji_23f3);
        EMOJIS_MAP.put(0x231b, R.drawable.emoji_231b);
        EMOJIS_MAP.put(0x1f4e1, R.drawable.emoji_1f4e1);
        EMOJIS_MAP.put(0x1f50b, R.drawable.emoji_1f50b);
        EMOJIS_MAP.put(0x1f50c, R.drawable.emoji_1f50c);
        EMOJIS_MAP.put(0x1f4a1, R.drawable.emoji_1f4a1);
        EMOJIS_MAP.put(0x1f526, R.drawable.emoji_1f526);
        EMOJIS_MAP.put(0x1f56f, R.drawable.emoji_1f56f);
        EMOJIS_MAP.put(0x1f5d1, R.drawable.emoji_1f5d1);
        EMOJIS_MAP.put(0x1f6e2, R.drawable.emoji_1f6e2);
        EMOJIS_MAP.put(0x1f4b8, R.drawable.emoji_1f4b8);
        EMOJIS_MAP.put(0x1f4b5, R.drawable.emoji_1f4b5);
        EMOJIS_MAP.put(0x1f4b4, R.drawable.emoji_1f4b4);
        EMOJIS_MAP.put(0x1f4b7, R.drawable.emoji_1f4b7);
        EMOJIS_MAP.put(0x1f4b6, R.drawable.emoji_1f4b6);
        EMOJIS_MAP.put(0x1f4b0, R.drawable.emoji_1f4b0);
        EMOJIS_MAP.put(0x1f4b3, R.drawable.emoji_1f4b3);
        EMOJIS_MAP.put(0x1f48e, R.drawable.emoji_1f48e);
        EMOJIS_MAP.put(0x2696, R.drawable.emoji_2696);
        EMOJIS_MAP.put(0x1f527, R.drawable.emoji_1f527);
        EMOJIS_MAP.put(0x1f528, R.drawable.emoji_1f528);
        EMOJIS_MAP.put(0x2692, R.drawable.emoji_2692);
        EMOJIS_MAP.put(0x1f6e0, R.drawable.emoji_1f6e0);
        EMOJIS_MAP.put(0x26cf, R.drawable.emoji_26cf);
        EMOJIS_MAP.put(0x1f529, R.drawable.emoji_1f529);
        EMOJIS_MAP.put(0x2699, R.drawable.emoji_2699);
        EMOJIS_MAP.put(0x26d3, R.drawable.emoji_26d3);
        EMOJIS_MAP.put(0x1f52b, R.drawable.emoji_1f52b);
        EMOJIS_MAP.put(0x1f4a3, R.drawable.emoji_1f4a3);
        EMOJIS_MAP.put(0x1f52a, R.drawable.emoji_1f52a);
        EMOJIS_MAP.put(0x1f5e1, R.drawable.emoji_1f5e1);
        EMOJIS_MAP.put(0x2694, R.drawable.emoji_2694);
        EMOJIS_MAP.put(0x1f6e1, R.drawable.emoji_1f6e1);
        EMOJIS_MAP.put(0x1f6ac, R.drawable.emoji_1f6ac);
        EMOJIS_MAP.put(0x2620, R.drawable.emoji_2620);
        EMOJIS_MAP.put(0x26b0, R.drawable.emoji_26b0);
        EMOJIS_MAP.put(0x26b1, R.drawable.emoji_26b1);
        EMOJIS_MAP.put(0x1f3fa, R.drawable.emoji_1f3fa);
        EMOJIS_MAP.put(0x1f52e, R.drawable.emoji_1f52e);
        EMOJIS_MAP.put(0x1f4ff, R.drawable.emoji_1f4ff);
        EMOJIS_MAP.put(0x1f488, R.drawable.emoji_1f488);
        EMOJIS_MAP.put(0x2697, R.drawable.emoji_2697);
        EMOJIS_MAP.put(0x1f52c, R.drawable.emoji_1f52c);
        EMOJIS_MAP.put(0x1f52d, R.drawable.emoji_1f52d);
        EMOJIS_MAP.put(0x1f573, R.drawable.emoji_1f573);
        EMOJIS_MAP.put(0x1f48a, R.drawable.emoji_1f48a);
        EMOJIS_MAP.put(0x1f489, R.drawable.emoji_1f489);
        EMOJIS_MAP.put(0x1f321, R.drawable.emoji_1f321);
        EMOJIS_MAP.put(0x1f3f7, R.drawable.emoji_1f3f7);
        EMOJIS_MAP.put(0x1f516, R.drawable.emoji_1f516);
        EMOJIS_MAP.put(0x1f6bd, R.drawable.emoji_1f6bd);
        EMOJIS_MAP.put(0x1f6bf, R.drawable.emoji_1f6bf);
        EMOJIS_MAP.put(0x1f6c1, R.drawable.emoji_1f6c1);
        EMOJIS_MAP.put(0x1f511, R.drawable.emoji_1f511);
        EMOJIS_MAP.put(0x1f5dd, R.drawable.emoji_1f5dd);
        EMOJIS_MAP.put(0x1f6cb, R.drawable.emoji_1f6cb);
        EMOJIS_MAP.put(0x1f6cc, R.drawable.emoji_1f6cc);
        EMOJIS_MAP.put(0x1f6cf, R.drawable.emoji_1f6cf);
        EMOJIS_MAP.put(0x1f6aa, R.drawable.emoji_1f6aa);
        EMOJIS_MAP.put(0x1f6ce, R.drawable.emoji_1f6ce);
        EMOJIS_MAP.put(0x1f5bc, R.drawable.emoji_1f5bc);
        EMOJIS_MAP.put(0x1f5fa, R.drawable.emoji_1f5fa);
        EMOJIS_MAP.put(0x26f1, R.drawable.emoji_26f1);
        EMOJIS_MAP.put(0x1f5ff, R.drawable.emoji_1f5ff);
        EMOJIS_MAP.put(0x1f6cd, R.drawable.emoji_1f6cd);
        EMOJIS_MAP.put(0x1f388, R.drawable.emoji_1f388);
        EMOJIS_MAP.put(0x1f38f, R.drawable.emoji_1f38f);
        EMOJIS_MAP.put(0x1f380, R.drawable.emoji_1f380);
        EMOJIS_MAP.put(0x1f381, R.drawable.emoji_1f381);
        EMOJIS_MAP.put(0x1f38a, R.drawable.emoji_1f38a);
        EMOJIS_MAP.put(0x1f389, R.drawable.emoji_1f389);
        SOFTBANKS_MAP.put(0xe438, R.drawable.emoji_1f38e);
        EMOJIS_MAP.put(0x1f390, R.drawable.emoji_1f390);
        EMOJIS_MAP.put(0x1f38c, R.drawable.emoji_1f38c);
        EMOJIS_MAP.put(0x1f3ee, R.drawable.emoji_1f3ee);
        EMOJIS_MAP.put(0x2709, R.drawable.emoji_2709);
        EMOJIS_MAP.put(0x1f4e9, R.drawable.emoji_1f4e9);
        EMOJIS_MAP.put(0x1f4e8, R.drawable.emoji_1f4e8);
        EMOJIS_MAP.put(0x1f4e7, R.drawable.emoji_1f4e7);
        EMOJIS_MAP.put(0x1f48c, R.drawable.emoji_1f48c);
        EMOJIS_MAP.put(0x1f4ee, R.drawable.emoji_1f4ee);
        EMOJIS_MAP.put(0x1f4ea, R.drawable.emoji_1f4ea);
        EMOJIS_MAP.put(0x1f4eb, R.drawable.emoji_1f4eb);
        EMOJIS_MAP.put(0x1f4ec, R.drawable.emoji_1f4ec);
        EMOJIS_MAP.put(0x1f4ed, R.drawable.emoji_1f4ed);
        EMOJIS_MAP.put(0x1f4e6, R.drawable.emoji_1f4e6);
        EMOJIS_MAP.put(0x1f4ef, R.drawable.emoji_1f4ef);
        EMOJIS_MAP.put(0x1f4e5, R.drawable.emoji_1f4e5);
        EMOJIS_MAP.put(0x1f4e4, R.drawable.emoji_1f4e4);
        EMOJIS_MAP.put(0x1f4dc, R.drawable.emoji_1f4dc);
        EMOJIS_MAP.put(0x1f4c3, R.drawable.emoji_1f4c3);
        EMOJIS_MAP.put(0x1f4d1, R.drawable.emoji_1f4d1);
        EMOJIS_MAP.put(0x1f4ca, R.drawable.emoji_1f4ca);
        EMOJIS_MAP.put(0x1f4c8, R.drawable.emoji_1f4c8);
        EMOJIS_MAP.put(0x1f4c9, R.drawable.emoji_1f4c9);
        EMOJIS_MAP.put(0x1f4c4, R.drawable.emoji_1f4c4);
        EMOJIS_MAP.put(0x1f4c5, R.drawable.emoji_1f4c5);
        EMOJIS_MAP.put(0x1f4c6, R.drawable.emoji_1f4c6);
        EMOJIS_MAP.put(0x1f5d3, R.drawable.emoji_1f5d3);
        EMOJIS_MAP.put(0x1f4c7, R.drawable.emoji_1f4c7);
        EMOJIS_MAP.put(0x1f5c3, R.drawable.emoji_1f5c3);
        EMOJIS_MAP.put(0x1f5f3, R.drawable.emoji_1f5f3);
        EMOJIS_MAP.put(0x1f5c4, R.drawable.emoji_1f5c4);
        EMOJIS_MAP.put(0x1f4cb, R.drawable.emoji_1f4cb);
        EMOJIS_MAP.put(0x1f5d2, R.drawable.emoji_1f5d2);
        EMOJIS_MAP.put(0x1f4c1, R.drawable.emoji_1f4c1);
        EMOJIS_MAP.put(0x1f4c2, R.drawable.emoji_1f4c2);
        EMOJIS_MAP.put(0x1f5c2, R.drawable.emoji_1f5c2);
        EMOJIS_MAP.put(0x1f5de, R.drawable.emoji_1f5de);
        EMOJIS_MAP.put(0x1f4f0, R.drawable.emoji_1f4f0);
        EMOJIS_MAP.put(0x1f4d3, R.drawable.emoji_1f4d3);
        EMOJIS_MAP.put(0x1f4d5, R.drawable.emoji_1f4d5);
        EMOJIS_MAP.put(0x1f4d7, R.drawable.emoji_1f4d7);
        EMOJIS_MAP.put(0x1f4d8, R.drawable.emoji_1f4d8);
        EMOJIS_MAP.put(0x1f4d9, R.drawable.emoji_1f4d9);
        EMOJIS_MAP.put(0x1f4d4, R.drawable.emoji_1f4d4);
        EMOJIS_MAP.put(0x1f4d2, R.drawable.emoji_1f4d2);
        EMOJIS_MAP.put(0x1f4da, R.drawable.emoji_1f4da);
        EMOJIS_MAP.put(0x1f4d6, R.drawable.emoji_1f4d6);
        EMOJIS_MAP.put(0x1f517, R.drawable.emoji_1f517);
        EMOJIS_MAP.put(0x1f4ce, R.drawable.emoji_1f4ce);
        EMOJIS_MAP.put(0x1f587, R.drawable.emoji_1f587);
        EMOJIS_MAP.put(0x2702, R.drawable.emoji_2702);
        EMOJIS_MAP.put(0x1f4d0, R.drawable.emoji_1f4d0);
        EMOJIS_MAP.put(0x1f4cf, R.drawable.emoji_1f4cf);
        EMOJIS_MAP.put(0x1f4cc, R.drawable.emoji_1f4cc);
        EMOJIS_MAP.put(0x1f4cd, R.drawable.emoji_1f4cd);
        EMOJIS_MAP.put(0x1f6a9, R.drawable.emoji_1f6a9);
        EMOJIS_MAP.put(0x1f3f3, R.drawable.emoji_1f3f3);
        EMOJIS_MAP.put(0x1f3f4, R.drawable.emoji_1f3f4);
        EMOJIS_MAP.put(0x1f510, R.drawable.emoji_1f510);
        EMOJIS_MAP.put(0x1f512, R.drawable.emoji_1f512);
        EMOJIS_MAP.put(0x1f513, R.drawable.emoji_1f513);
        EMOJIS_MAP.put(0x1f50f, R.drawable.emoji_1f50f);
        EMOJIS_MAP.put(0x1f58a, R.drawable.emoji_1f58a);
        EMOJIS_MAP.put(0x1f58b, R.drawable.emoji_1f58b);
        EMOJIS_MAP.put(0x2712, R.drawable.emoji_2712);
        EMOJIS_MAP.put(0x1f4dd, R.drawable.emoji_1f4dd);
        EMOJIS_MAP.put(0x270f, R.drawable.emoji_270f);
        EMOJIS_MAP.put(0x1f58d, R.drawable.emoji_1f58d);
        EMOJIS_MAP.put(0x1f58c, R.drawable.emoji_1f58c);
        EMOJIS_MAP.put(0x1f50d, R.drawable.emoji_1f50d);
        EMOJIS_MAP.put(0x1f50e, R.drawable.emoji_1f50e);

        /// Symbols
        EMOJIS_MAP.put(0x2764, R.drawable.emoji_2764);
        EMOJIS_MAP.put(0x1f49b, R.drawable.emoji_1f49b);
        EMOJIS_MAP.put(0x1f49a, R.drawable.emoji_1f49a);
        EMOJIS_MAP.put(0x1f499, R.drawable.emoji_1f499);
        EMOJIS_MAP.put(0x1f49c, R.drawable.emoji_1f49c);
        EMOJIS_MAP.put(0x1f494, R.drawable.emoji_1f494);
        EMOJIS_MAP.put(0x2763, R.drawable.emoji_2763);
        EMOJIS_MAP.put(0x1f495, R.drawable.emoji_1f495);
        EMOJIS_MAP.put(0x1f49e, R.drawable.emoji_1f49e);
        EMOJIS_MAP.put(0x1f493, R.drawable.emoji_1f493);
        EMOJIS_MAP.put(0x1f497, R.drawable.emoji_1f497);
        EMOJIS_MAP.put(0x1f496, R.drawable.emoji_1f496);
        EMOJIS_MAP.put(0x1f498, R.drawable.emoji_1f498);
        EMOJIS_MAP.put(0x1f49d, R.drawable.emoji_1f49d);
        EMOJIS_MAP.put(0x1f49f, R.drawable.emoji_1f49f);
        EMOJIS_MAP.put(0x262e, R.drawable.emoji_262e);
        EMOJIS_MAP.put(0x271d, R.drawable.emoji_271d);
        EMOJIS_MAP.put(0x262a, R.drawable.emoji_262a);
        EMOJIS_MAP.put(0x1f549, R.drawable.emoji_1f549);
        EMOJIS_MAP.put(0x2638, R.drawable.emoji_2638);
        EMOJIS_MAP.put(0x1f54e, R.drawable.emoji_1f54e);
        EMOJIS_MAP.put(0x262f, R.drawable.emoji_262f);
        EMOJIS_MAP.put(0x1f233, R.drawable.emoji_1f233);
        EMOJIS_MAP.put(0x1f239, R.drawable.emoji_1f239);
        SOFTBANKS_MAP.put(0xe532, R.drawable.emoji_1f170);
        SOFTBANKS_MAP.put(0xe533, R.drawable.emoji_1f171);
        SOFTBANKS_MAP.put(0xe534, R.drawable.emoji_1f18e);
        SOFTBANKS_MAP.put(0xe535, R.drawable.emoji_1f17e);
        EMOJIS_MAP.put(0x1f250, R.drawable.emoji_1f250);
        EMOJIS_MAP.put(0x3299, R.drawable.emoji_3299);
        EMOJIS_MAP.put(0x3297, R.drawable.emoji_3297);
        EMOJIS_MAP.put(0x1f234, R.drawable.emoji_1f234);
        EMOJIS_MAP.put(0x1f232, R.drawable.emoji_1f232);
        EMOJIS_MAP.put(0x1f191, R.drawable.emoji_1f191);
        EMOJIS_MAP.put(0x1f198, R.drawable.emoji_1f198);
        EMOJIS_MAP.put(0x26d4, R.drawable.emoji_26d4);
        EMOJIS_MAP.put(0x1f4db, R.drawable.emoji_1f4db);
        EMOJIS_MAP.put(0x1f6ab, R.drawable.emoji_1f6ab);
        EMOJIS_MAP.put(0x274c, R.drawable.emoji_274c);
        EMOJIS_MAP.put(0x2b55, R.drawable.emoji_2b55);
        SOFTBANKS_MAP.put(0xe334, R.drawable.emoji_1f4a2);
        EMOJIS_MAP.put(0x1f51e, R.drawable.emoji_1f51e);
        EMOJIS_MAP.put(0x1f4f5, R.drawable.emoji_1f4f5);
        EMOJIS_MAP.put(0x1f6af, R.drawable.emoji_1f6af);
        EMOJIS_MAP.put(0x1f6b1, R.drawable.emoji_1f6b1);
        EMOJIS_MAP.put(0x1f6b3, R.drawable.emoji_1f6b3);
        EMOJIS_MAP.put(0x1f6b7, R.drawable.emoji_1f6b7);
        EMOJIS_MAP.put(0x203c, R.drawable.emoji_203c);
        EMOJIS_MAP.put(0x2049, R.drawable.emoji_2049);
        EMOJIS_MAP.put(0x2757, R.drawable.emoji_2757);
        EMOJIS_MAP.put(0x2753, R.drawable.emoji_2753);
        EMOJIS_MAP.put(0x2755, R.drawable.emoji_2755);
        EMOJIS_MAP.put(0x2754, R.drawable.emoji_2754);
        EMOJIS_MAP.put(0x1f4af, R.drawable.emoji_1f4af);
        SOFTBANKS_MAP.put(0xe252, R.drawable.emoji_26a0);
        EMOJIS_MAP.put(0x1f6b8, R.drawable.emoji_1f6b8);
        EMOJIS_MAP.put(0x1f506, R.drawable.emoji_1f506);
        EMOJIS_MAP.put(0x1f505, R.drawable.emoji_1f505);
        EMOJIS_MAP.put(0x1f531, R.drawable.emoji_1f531);
        EMOJIS_MAP.put(0x1f530, R.drawable.emoji_1f530);
        EMOJIS_MAP.put(0x267b, R.drawable.emoji_267b);
        EMOJIS_MAP.put(0x2733, R.drawable.emoji_2733);
        EMOJIS_MAP.put(0x2747, R.drawable.emoji_2747);
        EMOJIS_MAP.put(0x274e, R.drawable.emoji_274e);
        EMOJIS_MAP.put(0x2705, R.drawable.emoji_2705);
        EMOJIS_MAP.put(0x1f4b9, R.drawable.emoji_1f4b9);
        EMOJIS_MAP.put(0x1f300, R.drawable.emoji_1f300);
        EMOJIS_MAP.put(0x1f6be, R.drawable.emoji_1f6be);
        EMOJIS_MAP.put(0x1f6b0, R.drawable.emoji_1f6b0);
        EMOJIS_MAP.put(0x1f17f, R.drawable.emoji_1f17f);
        EMOJIS_MAP.put(0x267f, R.drawable.emoji_267f);
        EMOJIS_MAP.put(0x1f6ad, R.drawable.emoji_1f6ad);
        EMOJIS_MAP.put(0x1f202, R.drawable.emoji_1f202);
        EMOJIS_MAP.put(0x24c2, R.drawable.emoji_24c2);
        EMOJIS_MAP.put(0x1f6c2, R.drawable.emoji_1f6c2);
        EMOJIS_MAP.put(0x1f6c4, R.drawable.emoji_1f6c4);
        EMOJIS_MAP.put(0x1f6c5, R.drawable.emoji_1f6c5);
        EMOJIS_MAP.put(0x1f6c3, R.drawable.emoji_1f6c3);
        EMOJIS_MAP.put(0x1f6b9, R.drawable.emoji_1f6b9);
        EMOJIS_MAP.put(0x1f6ba, R.drawable.emoji_1f6ba);
        EMOJIS_MAP.put(0x1f6bc, R.drawable.emoji_1f6bc);
        EMOJIS_MAP.put(0x1f6bb, R.drawable.emoji_1f6bb);
        EMOJIS_MAP.put(0x1f6ae, R.drawable.emoji_1f6ae);
        SOFTBANKS_MAP.put(0xe225, R.drawable.emoji_0030);
        SOFTBANKS_MAP.put(0xe21c, R.drawable.emoji_0031);
        SOFTBANKS_MAP.put(0xe21d, R.drawable.emoji_0032);
        SOFTBANKS_MAP.put(0xe21e, R.drawable.emoji_0033);
        SOFTBANKS_MAP.put(0xe21f, R.drawable.emoji_0034);
        SOFTBANKS_MAP.put(0xe220, R.drawable.emoji_0035);
        SOFTBANKS_MAP.put(0xe221, R.drawable.emoji_0036);
        SOFTBANKS_MAP.put(0xe222, R.drawable.emoji_0037);
        SOFTBANKS_MAP.put(0xe223, R.drawable.emoji_0038);
        SOFTBANKS_MAP.put(0xe224, R.drawable.emoji_0039);
        EMOJIS_MAP.put(0x1f51f, R.drawable.emoji_1f51f);
        EMOJIS_MAP.put(0x1f522, R.drawable.emoji_1f522);
        EMOJIS_MAP.put(0x1f523, R.drawable.emoji_1f523);
        EMOJIS_MAP.put(0x2b06, R.drawable.emoji_2b06);
        EMOJIS_MAP.put(0x2b07, R.drawable.emoji_2b07);
        EMOJIS_MAP.put(0x2b05, R.drawable.emoji_2b05);
        EMOJIS_MAP.put(0x27a1, R.drawable.emoji_27a1);
        EMOJIS_MAP.put(0x1f520, R.drawable.emoji_1f520);
        EMOJIS_MAP.put(0x1f521, R.drawable.emoji_1f521);
        EMOJIS_MAP.put(0x1f524, R.drawable.emoji_1f524);
        EMOJIS_MAP.put(0x2197, R.drawable.emoji_2197);
        EMOJIS_MAP.put(0x2196, R.drawable.emoji_2196);
        EMOJIS_MAP.put(0x2198, R.drawable.emoji_2198);
        EMOJIS_MAP.put(0x2199, R.drawable.emoji_2199);
        EMOJIS_MAP.put(0x2194, R.drawable.emoji_2194);
        EMOJIS_MAP.put(0x2195, R.drawable.emoji_2195);
        EMOJIS_MAP.put(0x1f504, R.drawable.emoji_1f504);
        EMOJIS_MAP.put(0x25c0, R.drawable.emoji_25c0);
        EMOJIS_MAP.put(0x25b6, R.drawable.emoji_25b6);
        EMOJIS_MAP.put(0x1f53c, R.drawable.emoji_1f53c);
        EMOJIS_MAP.put(0x1f53d, R.drawable.emoji_1f53d);
        EMOJIS_MAP.put(0x21a9, R.drawable.emoji_21a9);
        EMOJIS_MAP.put(0x21aa, R.drawable.emoji_21aa);
        EMOJIS_MAP.put(0x2139, R.drawable.emoji_2139);
        EMOJIS_MAP.put(0x23ea, R.drawable.emoji_23ea);
        EMOJIS_MAP.put(0x23e9, R.drawable.emoji_23e9);
        EMOJIS_MAP.put(0x23ed, R.drawable.emoji_23ed);
        EMOJIS_MAP.put(0x23ef, R.drawable.emoji_23ef);
        EMOJIS_MAP.put(0x23ee, R.drawable.emoji_23ee);
        EMOJIS_MAP.put(0x23f8, R.drawable.emoji_23f8);
        EMOJIS_MAP.put(0x23f9, R.drawable.emoji_23f9);
        EMOJIS_MAP.put(0x23fa, R.drawable.emoji_23fa);
        EMOJIS_MAP.put(0x23eb, R.drawable.emoji_23eb);
        EMOJIS_MAP.put(0x23ec, R.drawable.emoji_23ec);
        EMOJIS_MAP.put(0x2935, R.drawable.emoji_2935);
        EMOJIS_MAP.put(0x2934, R.drawable.emoji_2934);
        EMOJIS_MAP.put(0x1f197, R.drawable.emoji_1f197);
        EMOJIS_MAP.put(0x1f500, R.drawable.emoji_1f500);
        EMOJIS_MAP.put(0x1f501, R.drawable.emoji_1f501);
        EMOJIS_MAP.put(0x1f502, R.drawable.emoji_1f502);
        EMOJIS_MAP.put(0x1f195, R.drawable.emoji_1f195);
        EMOJIS_MAP.put(0x1f199, R.drawable.emoji_1f199);
        EMOJIS_MAP.put(0x1f192, R.drawable.emoji_1f192);
        EMOJIS_MAP.put(0x1f193, R.drawable.emoji_1f193);
        EMOJIS_MAP.put(0x1f196, R.drawable.emoji_1f196);
        EMOJIS_MAP.put(0x1f4f6, R.drawable.emoji_1f4f6);
        EMOJIS_MAP.put(0x1f3a6, R.drawable.emoji_1f3a6);
        EMOJIS_MAP.put(0x1f201, R.drawable.emoji_1f201);
        EMOJIS_MAP.put(0x1f4b2, R.drawable.emoji_1f4b2);
        EMOJIS_MAP.put(0x1f4b1, R.drawable.emoji_1f4b1);
        EMOJIS_MAP.put(0x00a9, R.drawable.emoji_00a9);
        EMOJIS_MAP.put(0x00ae, R.drawable.emoji_00ae);
        EMOJIS_MAP.put(0x2122, R.drawable.emoji_2122);
        EMOJIS_MAP.put(0x1f51d, R.drawable.emoji_1f51d);
        EMOJIS_MAP.put(0x1f51a, R.drawable.emoji_1f51a);
        EMOJIS_MAP.put(0x1f519, R.drawable.emoji_1f519);
        EMOJIS_MAP.put(0x1f51b, R.drawable.emoji_1f51b);
        EMOJIS_MAP.put(0x1f51c, R.drawable.emoji_1f51c);
        EMOJIS_MAP.put(0x1f503, R.drawable.emoji_1f503);
        EMOJIS_MAP.put(0x2716, R.drawable.emoji_2716);
        EMOJIS_MAP.put(0x2795, R.drawable.emoji_2795);
        EMOJIS_MAP.put(0x2796, R.drawable.emoji_2796);
        EMOJIS_MAP.put(0x2797, R.drawable.emoji_2797);
        EMOJIS_MAP.put(0x2714, R.drawable.emoji_2714);
        EMOJIS_MAP.put(0x2611, R.drawable.emoji_2611);
        EMOJIS_MAP.put(0x1f518, R.drawable.emoji_1f518);
        EMOJIS_MAP.put(0x27b0, R.drawable.emoji_27b0);
        EMOJIS_MAP.put(0x3030, R.drawable.emoji_3030);
        EMOJIS_MAP.put(0x1f4ae, R.drawable.emoji_1f4ae);
        EMOJIS_MAP.put(0x25fb, R.drawable.emoji_25fb);
        EMOJIS_MAP.put(0x25fe, R.drawable.emoji_25fe);
        EMOJIS_MAP.put(0x25fd, R.drawable.emoji_25fd);
        EMOJIS_MAP.put(0x25aa, R.drawable.emoji_25aa);
        EMOJIS_MAP.put(0x25ab, R.drawable.emoji_25ab);
        EMOJIS_MAP.put(0x1f53a, R.drawable.emoji_1f53a);
        EMOJIS_MAP.put(0x1f532, R.drawable.emoji_1f532);
        EMOJIS_MAP.put(0x1f533, R.drawable.emoji_1f533);
        EMOJIS_MAP.put(0x26ab, R.drawable.emoji_26ab);
        EMOJIS_MAP.put(0x26aa, R.drawable.emoji_26aa);
        EMOJIS_MAP.put(0x1f534, R.drawable.emoji_1f534);
        EMOJIS_MAP.put(0x1f535, R.drawable.emoji_1f535);
        EMOJIS_MAP.put(0x1f53b, R.drawable.emoji_1f53b);
        EMOJIS_MAP.put(0x2b1c, R.drawable.emoji_2b1c);
        EMOJIS_MAP.put(0x2b1b, R.drawable.emoji_2b1b);
        EMOJIS_MAP.put(0x1f536, R.drawable.emoji_1f536);
        EMOJIS_MAP.put(0x1f537, R.drawable.emoji_1f537);
        EMOJIS_MAP.put(0x1f538, R.drawable.emoji_1f538);
        EMOJIS_MAP.put(0x1f539, R.drawable.emoji_1f539);
        EMOJIS_MAP.put(0x1f50a, R.drawable.emoji_1f50a);
        EMOJIS_MAP.put(0x1f509, R.drawable.emoji_1f509);
        EMOJIS_MAP.put(0x1f508, R.drawable.emoji_1f508);
        EMOJIS_MAP.put(0x1f507, R.drawable.emoji_1f507);
        EMOJIS_MAP.put(0x1f514, R.drawable.emoji_1f514);
        EMOJIS_MAP.put(0x1f515, R.drawable.emoji_1f515);
        EMOJIS_MAP.put(0x1f4e2, R.drawable.emoji_1f4e2);
        EMOJIS_MAP.put(0x1f4e3, R.drawable.emoji_1f4e3);
        EMOJIS_MAP.put(0x1f0cf, R.drawable.emoji_1f0cf);
        EMOJIS_MAP.put(0x1f004, R.drawable.emoji_1f004);
        EMOJIS_MAP.put(0x2660, R.drawable.emoji_2660);
        EMOJIS_MAP.put(0x2665, R.drawable.emoji_2665);
        EMOJIS_MAP.put(0x2663, R.drawable.emoji_2663);
        EMOJIS_MAP.put(0x2666, R.drawable.emoji_2666);
        EMOJIS_MAP.put(0x1f3b4, R.drawable.emoji_1f3b4);
        EMOJIS_MAP.put(0x1f4ac, R.drawable.emoji_1f4ac);
        EMOJIS_MAP.put(0x1f5ef, R.drawable.emoji_1f5ef);
        EMOJIS_MAP.put(0x1f4ad, R.drawable.emoji_1f4ad);
        EMOJIS_MAP.put(0x1f55b, R.drawable.emoji_1f55b);
        EMOJIS_MAP.put(0x1f567, R.drawable.emoji_1f567);
        EMOJIS_MAP.put(0x1f550, R.drawable.emoji_1f550);
        EMOJIS_MAP.put(0x1f55c, R.drawable.emoji_1f55c);
        EMOJIS_MAP.put(0x1f551, R.drawable.emoji_1f551);
        EMOJIS_MAP.put(0x1f55d, R.drawable.emoji_1f55d);
        EMOJIS_MAP.put(0x1f552, R.drawable.emoji_1f552);
        EMOJIS_MAP.put(0x1f55e, R.drawable.emoji_1f55e);
        EMOJIS_MAP.put(0x1f553, R.drawable.emoji_1f553);
        EMOJIS_MAP.put(0x1f55f, R.drawable.emoji_1f55f);
        EMOJIS_MAP.put(0x1f554, R.drawable.emoji_1f554);
        EMOJIS_MAP.put(0x1f560, R.drawable.emoji_1f560);
        EMOJIS_MAP.put(0x1f555, R.drawable.emoji_1f555);
        EMOJIS_MAP.put(0x1f556, R.drawable.emoji_1f556);
        EMOJIS_MAP.put(0x1f557, R.drawable.emoji_1f557);
        EMOJIS_MAP.put(0x1f558, R.drawable.emoji_1f558);
        EMOJIS_MAP.put(0x1f559, R.drawable.emoji_1f559);
        EMOJIS_MAP.put(0x1f55a, R.drawable.emoji_1f55a);
        EMOJIS_MAP.put(0x1f561, R.drawable.emoji_1f561);
        EMOJIS_MAP.put(0x1f562, R.drawable.emoji_1f562);
        EMOJIS_MAP.put(0x1f563, R.drawable.emoji_1f563);
        EMOJIS_MAP.put(0x1f564, R.drawable.emoji_1f564);
        EMOJIS_MAP.put(0x1f565, R.drawable.emoji_1f565);
        EMOJIS_MAP.put(0x1f566, R.drawable.emoji_1f566);

        /// Sticker
        STICKERS_MAP.put("[[admire]]", R.drawable.admire);
        STICKERS_MAP.put("[[angry]]", R.drawable.angry);
        STICKERS_MAP.put("[[bang]]", R.drawable.bang);
        STICKERS_MAP.put("[[beer]]", R.drawable.beer);
        STICKERS_MAP.put("[[cheer]]", R.drawable.cheer);
        STICKERS_MAP.put("[[congrats]]", R.drawable.congrats);
        STICKERS_MAP.put("[[goodjob]]", R.drawable.goodjob);
        STICKERS_MAP.put("[[hi]]", R.drawable.hi);
        STICKERS_MAP.put("[[hmm]]", R.drawable.hmm);
        STICKERS_MAP.put("[[ididit]]", R.drawable.ididit);
        STICKERS_MAP.put("[[late]]", R.drawable.late);
        STICKERS_MAP.put("[[loveproof]]", R.drawable.loveproof);
        STICKERS_MAP.put("[[loveyou]]", R.drawable.loveyou);
        STICKERS_MAP.put("[[meh]]", R.drawable.meh);
        STICKERS_MAP.put("[[no]]", R.drawable.no);
        STICKERS_MAP.put("[[sad]]", R.drawable.sad);
        STICKERS_MAP.put("[[seeyou]]", R.drawable.seeyou);
        STICKERS_MAP.put("[[shiok]]", R.drawable.shiok);
        STICKERS_MAP.put("[[sleeping]]", R.drawable.sleeping);
        STICKERS_MAP.put("[[sorry]]", R.drawable.sorry);
        STICKERS_MAP.put("[[speechless]]", R.drawable.speechless);
        STICKERS_MAP.put("[[tellme]]", R.drawable.tellme);
        STICKERS_MAP.put("[[tired]]", R.drawable.tired);
        STICKERS_MAP.put("[[what]]", R.drawable.what);
        STICKERS_MAP.put("[[whatever]]", R.drawable.whatever);

        /// Sticker
        EMOJIS_MAP.put(0x4001, R.drawable.num77);
        EMOJIS_MAP.put(0x4002, R.drawable.num78);
        EMOJIS_MAP.put(0x4003, R.drawable.num79);
        EMOJIS_MAP.put(0x4004, R.drawable.num80);
        EMOJIS_MAP.put(0x4005, R.drawable.num81);
        EMOJIS_MAP.put(0x4006, R.drawable.num82);
        EMOJIS_MAP.put(0x4007, R.drawable.num83);
        EMOJIS_MAP.put(0x4008, R.drawable.num84);
        EMOJIS_MAP.put(0x4009, R.drawable.num85);

    }
    //@formatter:on

    private EmojiHandler() {
        throw new AssertionError("No instances.");
    }

    private static boolean isSoftBankEmoji(final char c) {
        return (c >> 12) == 0xe;
    }

    private static boolean isSoftBankEmojiSticker(final char c) {
        return (c >> 12) == 0x3 || (c >> 12) == 0x4;
    }

    private static int getEmojiResource(final int codePoint) {
        return EMOJIS_MAP.get(codePoint);
    }
    private static int getStickerResource(final CharSequence charSequence) {
        return STICKERS_MAP.get(charSequence.toString());
    }
    private static int getSoftbankEmojiResource(final char c) {
        return SOFTBANKS_MAP.get(c);
    }

    @SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
    public static void addEmojis(final Context context, final Spannable text, final int emojiSize) {
        final int textLength = text.length();

        // remove spans throughout all text
        final EmojiSpan[] oldSpans = text.getSpans(0, textLength, EmojiSpan.class);
        // noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < oldSpans.length; i++) {
            text.removeSpan(oldSpans[i]);
        }

        int skip;
        int start = -1;
        int end = 0;

        for (int i = 0; i < textLength; i += skip) {
            skip = 0;
            int icon = 0;
            final char c = text.charAt(i);


            if(i > 0 && c == '[' && text.charAt(i - 1) == '['){
                start = i-1;
            }
            if(i > 2 && c == ']' && text.charAt(i - 1) == ']') {
                end = i;
                if(start != -1){
                    icon = getStickerResource(text.subSequence(start, end + 1));
                    text.setSpan(new EmojiSpan(context, icon, emojiSize), start, end + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    start = -1;
                    continue;
                }
            }

            if (isSoftBankEmoji(c)) {
                icon = getSoftbankEmojiResource(c);
                skip = icon == 0 ? 0 : 1;
            }

            if (icon == 0) {
                final int unicode = Character.codePointAt(text, i);
                skip = Character.charCount(unicode);

                if (unicode > 0xff) {
                    icon = getEmojiResource(unicode);
                }

                if (i + skip < textLength) {
                    final int followUnicode = Character.codePointAt(text, i + skip);

                    if (followUnicode >= 0x1f3fb && followUnicode <= 0x1f3ff) {
                        // Handle skin toned emojis by simply skipping them.
                        skip += Character.charCount(followUnicode);
                    } else if (followUnicode == 0xfe0f) {
                        int followSkip = Character.charCount(followUnicode);
                        if (i + skip + followSkip < textLength) {
                            final int nextFollowUnicode = Character.codePointAt(text, i + skip + followSkip);
                            if (nextFollowUnicode == 0x20e3) {
                                int nextFollowSkip = Character.charCount(nextFollowUnicode);
                                final int tempIcon = getKeyCapEmoji(unicode);

                                if (tempIcon == 0) {
                                    followSkip = 0;
                                    nextFollowSkip = 0;
                                } else {
                                    icon = tempIcon;
                                }
                                skip += followSkip + nextFollowSkip;
                            }
                        }
                    } else if (followUnicode == 0x20e3) {
                        int followSkip = Character.charCount(followUnicode);

                        final int tempIcon = getKeyCapEmoji(unicode);
                        if (tempIcon == 0) {
                            followSkip = 0;
                        } else {
                            icon = tempIcon;
                        }
                        skip += followSkip;
                    }
                }
            }

            if (icon > 0) {
                text.setSpan(new EmojiSpan(context, icon, emojiSize), i, i + skip, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    public static int addEmojis(final Context context, final String text) {

        final char c = text.charAt(0);
        if(isSoftBankEmojiSticker(c)){
            final int unicode = Character.codePointAt(text, 0);
            int icon = 0;
            if (unicode > 0xff) {
                icon = getEmojiResource(unicode);
            }

            return icon;
        }

        if(text.startsWith("[[") && text.endsWith("]]")){
            int icon = getStickerResource(text);
            return icon;
        }

        return 0;
    }

    private static int getKeyCapEmoji(final int unicode) {
        int icon = 0;
        switch (unicode) {
            case 0x0023:
                icon = R.drawable.emoji_0023;
                break;
            case 0x002a:
                icon = R.drawable.emoji_002a_20e3;
                break;
            case 0x0030:
                icon = R.drawable.emoji_0030;
                break;
            case 0x0031:
                icon = R.drawable.emoji_0031;
                break;
            case 0x0032:
                icon = R.drawable.emoji_0032;
                break;
            case 0x0033:
                icon = R.drawable.emoji_0033;
                break;
            case 0x0034:
                icon = R.drawable.emoji_0034;
                break;
            case 0x0035:
                icon = R.drawable.emoji_0035;
                break;
            case 0x0036:
                icon = R.drawable.emoji_0036;
                break;
            case 0x0037:
                icon = R.drawable.emoji_0037;
                break;
            case 0x0038:
                icon = R.drawable.emoji_0038;
                break;
            case 0x0039:
                icon = R.drawable.emoji_0039;
                break;
            default:
                break;
        }
        return icon;
    }
}
