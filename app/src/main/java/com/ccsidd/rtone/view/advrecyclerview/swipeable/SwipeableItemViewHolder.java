/*
 *    Copyright (C) 2015 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.ccsidd.rtone.view.advrecyclerview.swipeable;

import android.support.annotation.Nullable;
import android.view.View;

import com.ccsidd.rtone.view.advrecyclerview.swipeable.annotation.SwipeableItemAfterReactions;

/**
 * Interface which provides required information for swiping item.
 * <p>
 * Implement this interface on your sub-class of the {@link android.support.v7.widget.RecyclerView.ViewHolder}.
 */
public interface SwipeableItemViewHolder {
    /**
     * Sets the state flags value for swiping item
     *
     * @param flags Bitwise OR of these flags;
     *              - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.SwipeableItemConstants#STATE_FLAG_SWIPING}
     *              - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.SwipeableItemConstants#STATE_FLAG_IS_ACTIVE}
     *              - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.SwipeableItemConstants#STATE_FLAG_IS_UPDATED}
     */
    void setSwipeStateFlags(int flags);

    /**
     * Gets the state flags value for swiping item
     *
     * @return Bitwise OR of these flags;
     * - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.SwipeableItemConstants#STATE_FLAG_SWIPING}
     * - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.SwipeableItemConstants#STATE_FLAG_IS_ACTIVE}
     * - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.SwipeableItemConstants#STATE_FLAG_IS_UPDATED}
     */
    int getSwipeStateFlags();

    /**
     * Sets the result code of swiping item.
     *
     * @param result Result code. One of these values;
     *               - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#RESULT_NONE}
     *               - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#RESULT_SWIPED_LEFT}
     *               - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#RESULT_SWIPED_UP}
     *               - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#RESULT_SWIPED_RIGHT}
     *               - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#RESULT_SWIPED_DOWN}
     *               - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#RESULT_CANCELED}
     */
    void setSwipeResult(int result);

    /**
     * Gets the result code of swiping item.
     *
     * @return Result code. One of these values;
     * - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#RESULT_NONE}
     * - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#RESULT_SWIPED_LEFT}
     * - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#RESULT_SWIPED_UP}
     * - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#RESULT_SWIPED_RIGHT}
     * - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#RESULT_SWIPED_DOWN}
     * - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#RESULT_CANCELED}
     */
    int getSwipeResult();

    /**
     * Sets the reaction type of after swiping item.
     *
     * @param reaction After-reaction type. One of these values;
     *                 - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#AFTER_SWIPE_REACTION_DEFAULT}
     *                 - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#AFTER_SWIPE_REACTION_MOVE_TO_ORIGIN}
     *                 - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#AFTER_SWIPE_REACTION_REMOVE_ITEM}
     *                 - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION}
     *                 - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#AFTER_SWIPE_REACTION_DO_NOTHING}
     */
    void setAfterSwipeReaction(@SwipeableItemAfterReactions int reaction);

    /**
     * Gets the reaction type of after swiping item.
     *
     * @return After-reaction type. One of these values;
     * - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#AFTER_SWIPE_REACTION_DEFAULT}
     * - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#AFTER_SWIPE_REACTION_MOVE_TO_ORIGIN}
     * - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#AFTER_SWIPE_REACTION_REMOVE_ITEM}
     * - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION}
     * - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#AFTER_SWIPE_REACTION_DO_NOTHING}
     */
    @SwipeableItemAfterReactions
    int getAfterSwipeReaction();

    /**
     * Sets proportional swipe amount mode enabled.
     *
     * @param enabled True if swipe amount is specified in proportional value, otherwise amount is specified in pixels.
     */
    void setProportionalSwipeAmountModeEnabled(boolean enabled);

    /**
     * Gets whether the proportional swipe amount mode enabled.
     *
     * @return True if swipe amount is specified in proportional value, otherwise false.
     */
    boolean isProportionalSwipeAmountModeEnabled();

    /**
     * Sets the amount of horizontal swipe.
     *
     * @param amount Item swipe amount. Generally the range is [-1.0 .. 1.0] if the proportional amount mode is enabled, otherwise distance in pixels.
     *               In additionally, these special values can also be accepted;
     *               - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#OUTSIDE_OF_THE_WINDOW_LEFT}
     *               - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#OUTSIDE_OF_THE_WINDOW_RIGHT}
     */
    void setSwipeItemHorizontalSlideAmount(float amount);

    /**
     * Gets the amount of horizontal swipe.
     *
     * @return Item swipe amount. Generally the range is [-1.0 .. 1.0] if the proportional amount mode is enabled, otherwise distance in pixels. Additionally these special values may also be returned;
     * - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#OUTSIDE_OF_THE_WINDOW_LEFT}
     * - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#OUTSIDE_OF_THE_WINDOW_RIGHT}
     */
    float getSwipeItemHorizontalSlideAmount();

    /**
     * Sets the amount of vertical swipe.
     *
     * @param amount Item swipe amount. Generally the range is [-1.0 .. 1.0] if the proportional amount mode is enabled, otherwise distance in pixels.
     *               In additionally, these special values can also be accepted;
     *               - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#OUTSIDE_OF_THE_WINDOW_TOP}
     *               - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#OUTSIDE_OF_THE_WINDOW_BOTTOM}
     */
    void setSwipeItemVerticalSlideAmount(float amount);

    /**
     * Gets the amount of vertical swipe.
     *
     * @return Item swipe amount. Generally the range is [-1.0 .. 1.0] if the proportional amount mode is enabled, otherwise distance in pixels. Additionally these special values may also be returned;
     * - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#OUTSIDE_OF_THE_WINDOW_TOP}
     * - {@link com.ccsidd.rtone.view.advrecyclerview.swipeable.RecyclerViewSwipeManager#OUTSIDE_OF_THE_WINDOW_BOTTOM}
     */
    float getSwipeItemVerticalSlideAmount();

    /**
     * Sets the maximum item left swipe amount.
     *
     * @param amount Item swipe amount.
     *               If the item is configured as proportional amount mode, specify limit amount in range of [-1.0 .. 1.0].
     *               Otherwise, specify limit distance in pixels.
     */
    void setMaxLeftSwipeAmount(float amount);

    /**
     * Gets the maximum item left swipe amount.
     *
     * @return Item swipe amount. If the item is configured as proportional amount mode, generally the range is [-1.0 .. 1.0], otherwise the value is specified in pixels.
     */
    float getMaxLeftSwipeAmount();

    /**
     * Sets the maximum item up swipe amount.
     *
     * @param amount Item swipe amount.
     *               If the item is configured as proportional amount mode, specify limit amount in range of [-1.0 .. 1.0].
     *               Otherwise, specify limit distance in pixels.
     */
    void setMaxUpSwipeAmount(float amount);

    /**
     * Gets the maximum item up swipe amount.
     *
     * @return Item swipe amount. If the item is configured as proportional amount mode, generally the range is [-1.0 .. 1.0], otherwise the value is specified in pixels.
     */
    float getMaxUpSwipeAmount();

    /**
     * Sets the maximum item right swipe amount.
     *
     * @param amount Item swipe amount.
     *               If the item is configured as proportional amount mode, specify limit amount in range of [-1.0 .. 1.0].
     *               Otherwise, specify limit distance in pixels.
     */
    void setMaxRightSwipeAmount(float amount);

    /**
     * Gets the maximum item right swipe amount.
     *
     * @return Item swipe amount. If the item is configured as proportional amount mode, generally the range is [-1.0 .. 1.0], otherwise the value is specified in pixels.
     */
    float getMaxRightSwipeAmount();

    /**
     * Sets the maximum item down swipe amount.
     *
     * @param amount Item swipe amount.
     *               If the item is configured as proportional amount mode, specify limit amount in range of [-1.0 .. 1.0].
     *               Otherwise, specify limit distance in pixels.
     */
    void setMaxDownSwipeAmount(float amount);

    /**
     * Gets the maximum item down swipe amount.
     *
     * @return Item swipe amount. If the item is configured as proportional amount mode, generally the range is [-1.0 .. 1.0], otherwise the value is specified in pixels.
     */
    float getMaxDownSwipeAmount();

    /**
     * Gets the container view for the swipeable area.
     * <p>NOTE: Please DO NOT return <code>itemView</code> for this method.
     * An IllegalArgumentException with massage "Tmp detached view should be removed from RecyclerView before it can be recycled"
     * will be raised by RecyclerView.Recycler.</p>
     *
     * @return The container view instance.
     */
    @Nullable
    View getSwipeableContainerView();

    /**
     * Called when sets background of the swiping item.
     *
     * @param horizontalAmount Horizontal slide amount of the item view. (slide left: &lt; 0, slide right: 0 &gt;)
     * @param verticalAmount   Vertical slide amount of the item view. (slide up: &lt; 0, slide down: 0 &gt;)
     * @param isSwiping        Indicates whether the user is swiping the item or not
     */
    void onSlideAmountUpdated(float horizontalAmount, float verticalAmount, boolean isSwiping);
}
