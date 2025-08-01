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

import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

import com.ccsidd.rtone.view.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.ccsidd.rtone.view.advrecyclerview.swipeable.action.SwipeResultAction;
import com.ccsidd.rtone.view.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import com.ccsidd.rtone.view.advrecyclerview.utils.CustomRecyclerViewUtils;
import com.ccsidd.rtone.view.advrecyclerview.adapter.ItemIdComposer;
import com.ccsidd.rtone.view.advrecyclerview.utils.WrapperAdapterUtils;

/**
 * Provides item swipe operation for {@link android.support.v7.widget.RecyclerView}
 */
@SuppressWarnings("PointlessBitwiseExpression")
public class RecyclerViewSwipeManager implements SwipeableItemConstants {
    private static final String TAG = "ARVSwipeManager";

    /**
     * Used for listening item swipe events
     */
    public interface OnItemSwipeEventListener {
        /**
         * Callback method to be invoked when swiping is started.
         *
         * @param position The position of the item.
         */
        void onItemSwipeStarted(int position);

        /**
         * Callback method to be invoked when swiping is finished.
         *
         * @param position           The position of the item.
         * @param result             The result code of the swipe operation.
         * @param afterSwipeReaction The reaction type to the swipe operation.
         */
        void onItemSwipeFinished(int position, int result, int afterSwipeReaction);
    }

    // ---

    private static final int MIN_DISTANCE_TOUCH_SLOP_MUL = 5;
    private static final int SLIDE_ITEM_IMMEDIATELY_SET_TRANSLATION_THRESHOLD_DP = 8;

    private static final boolean LOCAL_LOGV = false;
    private static final boolean LOCAL_LOGD = false;

    private RecyclerView.OnItemTouchListener mInternalUseOnItemTouchListener;
    private RecyclerView mRecyclerView;

    private long mReturnToDefaultPositionAnimationDuration = 300;
    private long mMoveToSpecifiedPositionAnimationDuration = 200;
    private long mMoveToOutsideWindowAnimationDuration = 200;

    private int mTouchSlop;
    private int mMinFlingVelocity; // [pixels per second]
    private int mMaxFlingVelocity; // [pixels per second]
    private int mSwipeThresholdDistance; // [pixels]
    private int mInitialTouchX;
    private int mInitialTouchY;
    private long mCheckingTouchSlop = RecyclerView.NO_ID;
    private boolean mSwipeHorizontal;

    private ItemSlidingAnimator mItemSlideAnimator;
    private SwipeableItemWrapperAdapter<RecyclerView.ViewHolder> mWrapperAdapter;
    private RecyclerView.ViewHolder mSwipingItem;
    private int mSwipingItemPosition = RecyclerView.NO_POSITION;
    private long mSwipingItemId = RecyclerView.NO_ID;
    private final Rect mSwipingItemMargins = new Rect();
    private int mTouchedItemOffsetX;
    private int mTouchedItemOffsetY;
    private int mLastTouchX;
    private int mLastTouchY;
    private int mSwipingItemReactionType;
    private VelocityTracker mVelocityTracker;
    private SwipingItemOperator mSwipingItemOperator;
    private OnItemSwipeEventListener mItemSwipeEventListener;
    private InternalHandler mHandler;
    private int mLongPressTimeout;

    /**
     * Constructor.
     */
    public RecyclerViewSwipeManager() {
        mInternalUseOnItemTouchListener = new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                return RecyclerViewSwipeManager.this.onInterceptTouchEvent(rv, e);
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
                RecyclerViewSwipeManager.this.onTouchEvent(rv, e);
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
                RecyclerViewSwipeManager.this.onRequestDisallowInterceptTouchEvent(disallowIntercept);
            }
        };
        mVelocityTracker = VelocityTracker.obtain();
        mLongPressTimeout = ViewConfiguration.getLongPressTimeout();
    }

    /**
     * Create wrapped adapter.
     *
     * @param adapter The target adapter.
     * @return Wrapped adapter which is associated to this {@link RecyclerViewSwipeManager} instance.
     */
    @SuppressWarnings("unchecked")
    public RecyclerView.Adapter createWrappedAdapter(@NonNull RecyclerView.Adapter adapter) {
        if (!adapter.hasStableIds()) {
            throw new IllegalArgumentException("The passed adapter does not support stable IDs");
        }

        if (mWrapperAdapter != null) {
            throw new IllegalStateException("already have a wrapped adapter");
        }

        mWrapperAdapter = new SwipeableItemWrapperAdapter(this, adapter);

        return mWrapperAdapter;
    }

    /**
     * Indicates this manager instance has released or not.
     *
     * @return True if this manager instance has released
     */
    public boolean isReleased() {
        return (mInternalUseOnItemTouchListener == null);
    }

    /**
     * <p>Attaches {@link android.support.v7.widget.RecyclerView} instance.</p>
     * <p>Before calling this method, the target {@link android.support.v7.widget.RecyclerView} must set
     * the wrapped adapter instance which is returned by the
     * {@link #createWrappedAdapter(android.support.v7.widget.RecyclerView.Adapter)} method.</p>
     *
     * @param rv The {@link android.support.v7.widget.RecyclerView} instance
     */
    public void attachRecyclerView(@NonNull RecyclerView rv) {
        if (isReleased()) {
            throw new IllegalStateException("Accessing released object");
        }

        if (mRecyclerView != null) {
            throw new IllegalStateException("RecyclerView instance has already been set");
        }

        final int layoutOrientation = CustomRecyclerViewUtils.getOrientation(rv);
        if (layoutOrientation == CustomRecyclerViewUtils.ORIENTATION_UNKNOWN) {
            throw new IllegalStateException("failed to determine layout orientation");
        }

        mRecyclerView = rv;
        mRecyclerView.addOnItemTouchListener(mInternalUseOnItemTouchListener);

        final ViewConfiguration vc = ViewConfiguration.get(rv.getContext());

        mTouchSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mSwipeThresholdDistance = (int) (mTouchSlop * MIN_DISTANCE_TOUCH_SLOP_MUL);

        mItemSlideAnimator = new ItemSlidingAnimator(mWrapperAdapter);
        mItemSlideAnimator.setImmediatelySetTranslationThreshold(
                (int) (rv.getResources().getDisplayMetrics().density * SLIDE_ITEM_IMMEDIATELY_SET_TRANSLATION_THRESHOLD_DP + 0.5f));

        mSwipeHorizontal = (layoutOrientation == CustomRecyclerViewUtils.ORIENTATION_VERTICAL);
        mHandler = new InternalHandler(this);
    }

    /**
     * <p>Detach the {@link android.support.v7.widget.RecyclerView} instance and release internal field references.</p>
     * <p>This method should be called in order to avoid memory leaks.</p>
     */
    public void release() {
        cancelSwipe(true);

        if (mHandler != null) {
            mHandler.release();
            mHandler = null;
        }

        if (mRecyclerView != null && mInternalUseOnItemTouchListener != null) {
            mRecyclerView.removeOnItemTouchListener(mInternalUseOnItemTouchListener);
        }
        mInternalUseOnItemTouchListener = null;

        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }

        if (mItemSlideAnimator != null) {
            mItemSlideAnimator.endAnimations();
            mItemSlideAnimator = null;
        }

        mWrapperAdapter = null;
        mRecyclerView = null;
    }

    /**
     * Indicates whether currently performing item swiping.
     *
     * @return True if currently performing item swiping
     */
    public boolean isSwiping() {
        return (mSwipingItem != null) && (!mHandler.isCancelSwipeRequested());
    }

    /**
     * Sets the time required to consider press as long press. (default: 500ms)
     *
     * @param longPressTimeout Integer in milli seconds.
     */
    public void setLongPressTimeout(int longPressTimeout) {
        mLongPressTimeout = longPressTimeout;
    }

    /**
     * Sets swipe threshold distance.
     *
     * @param distanceInPixels Integer specifies threshold distance in pixels.
     */
    public void setSwipeThresholdDistance(int distanceInPixels) {
        mSwipeThresholdDistance = Math.max(distanceInPixels, mTouchSlop);
    }

    /**
     * Gets swipe threshold distance.
     *
     * @return Threshold distance in pixels.
     */
    public int getSwipeThresholdDistance() {
        return mSwipeThresholdDistance;
    }

    /*package*/ boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        final int action = MotionEventCompat.getActionMasked(e);

        if (LOCAL_LOGV) {
            Log.v(TAG, "onInterceptTouchEvent() action = " + action);
        }

        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (handleActionUpOrCancel(e, true)) {
                    return true;
                }
                break;

            case MotionEvent.ACTION_DOWN:
                if (!isSwiping()) {
                    handleActionDown(rv, e);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isSwiping()) {
                    // NOTE: The first ACTION_MOVE event will come here. (maybe a bug of RecyclerView?)
                    handleActionMoveWhileSwiping(e);
                    return true;
                } else {
                    if (handleActionMoveWhileNotSwiping(rv, e)) {
                        return true;
                    }
                }
                break;
        }

        return false;
    }

    /*package*/ void onTouchEvent(RecyclerView rv, MotionEvent e) {
        final int action = MotionEventCompat.getActionMasked(e);

        if (LOCAL_LOGV) {
            Log.v(TAG, "onTouchEvent() action = " + action);
        }

        if (!isSwiping()) {
            // Log.w(TAG, "onTouchEvent() - unexpected state");
            return;
        }

        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                handleActionUpOrCancel(e, true);
                break;

            case MotionEvent.ACTION_MOVE:
                handleActionMoveWhileSwiping(e);
                break;
        }
    }

    /*package*/ void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept) {
            cancelSwipe(true);
        }
    }

    private boolean handleActionDown(RecyclerView rv, MotionEvent e) {
        final RecyclerView.ViewHolder holder = CustomRecyclerViewUtils.findChildViewHolderUnderWithTranslation(rv, e.getX(), e.getY());

        if (!(holder instanceof SwipeableItemViewHolder)) {
            return false;
        }

        final int wrappedItemPosition = getWrappedItemPosition(holder);


        // verify the touched item is valid state
        if (!(wrappedItemPosition >= 0 && wrappedItemPosition < mWrapperAdapter.getItemCount())) {
            return false;
        }

        final long wrappedAdapterItemId = ItemIdComposer.extractWrappedIdPart(mWrapperAdapter.getItemId(wrappedItemPosition));
        final long wrappedItemId = ItemIdComposer.extractWrappedIdPart(holder.getItemId());

        if (wrappedItemId != wrappedAdapterItemId) {
            return false;
        }

        final int touchX = (int) (e.getX() + 0.5f);
        final int touchY = (int) (e.getY() + 0.5f);

        final View view = holder.itemView;
        final int translateX = (int) (ViewCompat.getTranslationX(view) + 0.5f);
        final int translateY = (int) (ViewCompat.getTranslationY(view) + 0.5f);
        final int viewX = touchX - (view.getLeft() + translateX);
        final int viewY = touchY - (view.getTop() + translateY);

        final int reactionType = mWrapperAdapter.getSwipeReactionType(holder, wrappedItemPosition, viewX, viewY);

        if (reactionType == 0) {
            return false;
        }

        mInitialTouchX = touchX;
        mInitialTouchY = touchY;
        mCheckingTouchSlop = holder.getItemId();
        mSwipingItemReactionType = reactionType;

        if ((reactionType & REACTION_START_SWIPE_ON_LONG_PRESS) != 0) {
            mHandler.startLongPressDetection(e, mLongPressTimeout);
        }

        return true;
    }

    private boolean handleActionUpOrCancel(MotionEvent e, boolean invokeFinish) {
        int action = MotionEvent.ACTION_CANCEL;

        if (e != null) {
            action = MotionEventCompat.getActionMasked(e);
            mLastTouchX = (int) (e.getX() + 0.5f);
            mLastTouchY = (int) (e.getY() + 0.5f);
        }

        if (isSwiping()) {
            if (invokeFinish) {
                handleActionUpOrCancelWhileSwiping(action);
            }
            return true;
        } else {
            handleActionUpOrCancelWhileNotSwiping();
            return false;
        }
    }

    private void handleActionUpOrCancelWhileNotSwiping() {
        if (mHandler != null) {
            mHandler.cancelLongPressDetection();
        }

        mCheckingTouchSlop = RecyclerView.NO_ID;
        mSwipingItemReactionType = 0;
    }

    private void handleActionUpOrCancelWhileSwiping(int action) {
        int result = RESULT_CANCELED;

        if (action == MotionEvent.ACTION_UP) {
            final float swipeThresholdDistanceCoeff = 0.8f;
            final float swipeThresholdVelocity = mMinFlingVelocity;

            final boolean horizontal = mSwipeHorizontal;
            final SwipeableItemViewHolder holder = (SwipeableItemViewHolder) mSwipingItem;
            final View containerView = SwipeableViewHolderUtils.getSwipeableContainerView(holder);
            final int containerSize = (horizontal) ? containerView.getWidth() : containerView.getHeight();
            final float distance = (horizontal) ? (mLastTouchX - mInitialTouchX) : (mLastTouchY - mInitialTouchY);
            final float absDistance = Math.abs(distance);
            final boolean canSwipeNegativeDir = (horizontal) ? SwipeReactionUtils.canSwipeLeft(mSwipingItemReactionType) : SwipeReactionUtils.canSwipeUp(mSwipingItemReactionType);
            final boolean canSwipePositiveDir = (horizontal) ? SwipeReactionUtils.canSwipeRight(mSwipingItemReactionType) : SwipeReactionUtils.canSwipeDown(mSwipingItemReactionType);
            final boolean proportional = holder.isProportionalSwipeAmountModeEnabled();
            float negativeDirLimit = (horizontal) ? holder.getMaxLeftSwipeAmount() : holder.getMaxUpSwipeAmount();
            float positiveDirLimit = (horizontal) ? holder.getMaxRightSwipeAmount() : holder.getMaxDownSwipeAmount();

            negativeDirLimit = adaptAmount(holder, horizontal, negativeDirLimit, proportional, false);
            positiveDirLimit = adaptAmount(holder, horizontal, positiveDirLimit, proportional, false);

            if (isSpecialSwipeAmountValue(negativeDirLimit)) {
                negativeDirLimit = -containerSize;
            }
            if (isSpecialSwipeAmountValue(positiveDirLimit)) {
                positiveDirLimit = containerSize;
            }

            mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity); // 1000: pixels per second


            final float velocity = (horizontal) ? mVelocityTracker.getXVelocity() : mVelocityTracker.getYVelocity();
            final float absVelocity = Math.abs(velocity);

            boolean swiped = false;
            boolean positiveDir = false;

            if (absDistance > mSwipeThresholdDistance) {
                if (absVelocity >= swipeThresholdVelocity) {
                    if ((velocity * distance) >= 0) {
                        swiped = true;
                        positiveDir = (velocity > 0);
                    }
                } else if ((distance < 0) && (distance <= negativeDirLimit * swipeThresholdDistanceCoeff)) {
                    swiped = true;
                    positiveDir = false;
                } else if ((distance > 0) && (distance >= positiveDirLimit * swipeThresholdDistanceCoeff)) {
                    swiped = true;
                    positiveDir = true;
                }
            }

            if (swiped) {
                if (!positiveDir && canSwipeNegativeDir) {
                    result = (horizontal) ? RESULT_SWIPED_LEFT : RESULT_SWIPED_UP;
                } else if (positiveDir && canSwipePositiveDir) {
                    result = (horizontal) ? RESULT_SWIPED_RIGHT : RESULT_SWIPED_DOWN;
                }
            }
        }

        if (LOCAL_LOGD) {
            Log.d(TAG, "swiping finished  --- result = " + result);
        }

        finishSwiping(result);
    }

    private boolean handleActionMoveWhileNotSwiping(RecyclerView rv, MotionEvent e) {
        if (mCheckingTouchSlop == RecyclerView.NO_ID) {
            return false;
        }

        final int dx = (int) (e.getX() + 0.5f) - mInitialTouchX;
        final int dy = (int) (e.getY() + 0.5f) - mInitialTouchY;

        final int scrollAxisDelta;
        final int swipeAxisDelta;

        if (mSwipeHorizontal) {
            scrollAxisDelta = dy;
            swipeAxisDelta = dx;
        } else {
            scrollAxisDelta = dx;
            swipeAxisDelta = dy;
        }

        if (Math.abs(scrollAxisDelta) > mTouchSlop) {
            // scrolling occurred
            mCheckingTouchSlop = RecyclerView.NO_ID;
            return false;
        }

        if (Math.abs(swipeAxisDelta) <= mTouchSlop) {
            return false;
        }

        // check swipeable direction mask
        boolean dirMasked;
        if (mSwipeHorizontal) {
            if (swipeAxisDelta < 0) {
                dirMasked = ((mSwipingItemReactionType & REACTION_MASK_START_SWIPE_LEFT) != 0);
            } else {
                dirMasked = ((mSwipingItemReactionType & REACTION_MASK_START_SWIPE_RIGHT) != 0);
            }
        } else {
            if (swipeAxisDelta < 0) {
                dirMasked = ((mSwipingItemReactionType & REACTION_MASK_START_SWIPE_UP) != 0);
            } else {
                dirMasked = ((mSwipingItemReactionType & REACTION_MASK_START_SWIPE_DOWN) != 0);
            }
        }

        if (dirMasked) {
            // masked
            mCheckingTouchSlop = RecyclerView.NO_ID;
            return false;
        }

        final RecyclerView.ViewHolder holder = CustomRecyclerViewUtils.findChildViewHolderUnderWithTranslation(rv, e.getX(), e.getY());

        if (holder == null || holder.getItemId() != mCheckingTouchSlop) {
            mCheckingTouchSlop = RecyclerView.NO_ID;
            return false;
        }

        return checkConditionAndStartSwiping(e, holder);
    }

    private void handleActionMoveWhileSwiping(MotionEvent e) {
        mLastTouchX = (int) (e.getX() + 0.5f);
        mLastTouchY = (int) (e.getY() + 0.5f);
        mVelocityTracker.addMovement(e);

        final int swipeDistanceX = mLastTouchX - mTouchedItemOffsetX;
        final int swipeDistanceY = mLastTouchY - mTouchedItemOffsetY;
        final int swipingItemPosition = getSwipingItemPosition();

        mSwipingItemOperator.update(swipingItemPosition, swipeDistanceX, swipeDistanceY);
    }

    private boolean checkConditionAndStartSwiping(MotionEvent e, RecyclerView.ViewHolder holder) {
        final int wrappedItemPosition = getWrappedItemPosition(holder);

        if (wrappedItemPosition == RecyclerView.NO_POSITION) {
            return false;
        }

        startSwiping(e, holder, wrappedItemPosition);

        return true;
    }

    private void startSwiping(MotionEvent e, RecyclerView.ViewHolder holder, int itemPosition) {
        if (LOCAL_LOGD) {
            Log.d(TAG, "swiping started");
        }

        mHandler.cancelLongPressDetection();

        mSwipingItem = holder;
        mSwipingItemPosition = itemPosition;
        mSwipingItemId = mWrapperAdapter.getItemId(itemPosition);
        mLastTouchX = (int) (e.getX() + 0.5f);
        mLastTouchY = (int) (e.getY() + 0.5f);
        mTouchedItemOffsetX = mLastTouchX;
        mTouchedItemOffsetY = mLastTouchY;
        mCheckingTouchSlop = RecyclerView.NO_ID;
        CustomRecyclerViewUtils.getLayoutMargins(holder.itemView, mSwipingItemMargins);

        mSwipingItemOperator = new SwipingItemOperator(this, mSwipingItem, mSwipingItemReactionType, mSwipeHorizontal);
        mSwipingItemOperator.start();

        mVelocityTracker.clear();
        mVelocityTracker.addMovement(e);

        mRecyclerView.getParent().requestDisallowInterceptTouchEvent(true);

        // raise onItemSwipeStarted() event
        if (mItemSwipeEventListener != null) {
            mItemSwipeEventListener.onItemSwipeStarted(itemPosition);
        }

        // raise onSwipeItemStarted() event
        mWrapperAdapter.onSwipeItemStarted(this, holder, mSwipingItemId);
    }

    private void finishSwiping(int result) {
        final RecyclerView.ViewHolder swipingItem = mSwipingItem;

        if (swipingItem == null) {
            return;
        }

        // cancel deferred request
        mHandler.removeDeferredCancelSwipeRequest();

        mHandler.cancelLongPressDetection();

        if (mRecyclerView != null && mRecyclerView.getParent() != null) {
            mRecyclerView.getParent().requestDisallowInterceptTouchEvent(false);
        }

        final int itemPosition = getSwipingItemPosition();

        mVelocityTracker.clear();

        mSwipingItem = null;
        mSwipingItemPosition = RecyclerView.NO_POSITION;
        mSwipingItemId = RecyclerView.NO_ID;
        mLastTouchX = 0;
        mLastTouchY = 0;
        mInitialTouchX = 0;
        mTouchedItemOffsetX = 0;
        mTouchedItemOffsetY = 0;
        mCheckingTouchSlop = RecyclerView.NO_ID;
        mSwipingItemReactionType = 0;

        if (mSwipingItemOperator != null) {
            mSwipingItemOperator.finish();
            mSwipingItemOperator = null;
        }

        final int slideDir = resultCodeToSlideDirection(result);
        SwipeResultAction resultAction = null;

        if (mWrapperAdapter != null) {
            resultAction = mWrapperAdapter.onSwipeItemFinished(swipingItem, itemPosition, result);
        }

        if (resultAction == null) {
            resultAction = new SwipeResultActionDefault(); // set default action
        }

        int afterReaction = resultAction.getResultActionType();

        verifyAfterReaction(result, afterReaction);

        //noinspection UnusedAssignment
        boolean slideAnimated = false;

        switch (afterReaction) {
            case AFTER_SWIPE_REACTION_MOVE_TO_ORIGIN:
                slideAnimated = mItemSlideAnimator.finishSwipeSlideToDefaultPosition(
                        swipingItem, mSwipeHorizontal, true, mReturnToDefaultPositionAnimationDuration,
                        itemPosition, resultAction);
                break;
            case AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION:
                slideAnimated = mItemSlideAnimator.finishSwipeSlideToOutsideOfWindow(
                        swipingItem, slideDir, true, mMoveToOutsideWindowAnimationDuration,
                        itemPosition, resultAction);
                break;
            case AFTER_SWIPE_REACTION_REMOVE_ITEM: {
                final RecyclerView.ItemAnimator itemAnimator = mRecyclerView.getItemAnimator();

                final long removeAnimationDuration = (itemAnimator != null) ? itemAnimator.getRemoveDuration() : 0;

                if (supportsViewPropertyAnimator()) {
                    final long moveAnimationDuration = (itemAnimator != null) ? itemAnimator.getMoveDuration() : 0;

                    final RemovingItemDecorator decorator = new RemovingItemDecorator(
                            mRecyclerView, swipingItem, result, removeAnimationDuration, moveAnimationDuration);

                    decorator.setMoveAnimationInterpolator(SwipeDismissItemAnimator.MOVE_INTERPOLATOR);
                    decorator.start();
                }

                slideAnimated = mItemSlideAnimator.finishSwipeSlideToOutsideOfWindow(
                        swipingItem, slideDir, true, removeAnimationDuration,
                        itemPosition, resultAction);
            }
            break;
            case AFTER_SWIPE_REACTION_DO_NOTHING:
                break;
            default:
                throw new IllegalStateException("Unknown after reaction type: " + afterReaction);
        }

        if (mWrapperAdapter != null) {
            mWrapperAdapter.onSwipeItemFinished2(swipingItem, itemPosition, result, afterReaction, resultAction);
        }

        // raise onItemSwipeFinished() event
        if (mItemSwipeEventListener != null) {
            mItemSwipeEventListener.onItemSwipeFinished(itemPosition, result, afterReaction);
        }

        // invoke onSwipeSlideItemAnimationEnd
        if (!slideAnimated) {
            resultAction.slideAnimationEnd();
        }
    }

    private static void verifyAfterReaction(int result, int afterReaction) {
        if ((afterReaction == AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION) ||
                (afterReaction == AFTER_SWIPE_REACTION_REMOVE_ITEM)) {
            switch (result) {
                case RESULT_SWIPED_LEFT:
                case RESULT_SWIPED_UP:
                case RESULT_SWIPED_RIGHT:
                case RESULT_SWIPED_DOWN:
                    break;
                default:
                    throw new IllegalStateException("Unexpected after reaction has been requested: result = " + result + ", afterReaction = " + afterReaction);
            }
        }
    }

    private static int resultCodeToSlideDirection(int result) {
        switch (result) {
            case RESULT_SWIPED_LEFT:
                return ItemSlidingAnimator.DIR_LEFT;
            case RESULT_SWIPED_UP:
                return ItemSlidingAnimator.DIR_UP;
            case RESULT_SWIPED_RIGHT:
                return ItemSlidingAnimator.DIR_RIGHT;
            case RESULT_SWIPED_DOWN:
                return ItemSlidingAnimator.DIR_DOWN;
            default:
                // NOTE: returned value should not be used.
                return ItemSlidingAnimator.DIR_LEFT;
        }
    }

    /*package*/
    static int getItemPosition(@Nullable RecyclerView.Adapter adapter, long itemId, int itemPositionGuess) {
        if (adapter == null)
            return RecyclerView.NO_POSITION;

        final int itemCount = adapter.getItemCount();
        if (itemPositionGuess >= 0 && itemPositionGuess < itemCount) {
            if (adapter.getItemId(itemPositionGuess) == itemId)
                return itemPositionGuess;
        }

        for (int i = 0; i < itemCount; i++) {
            if (adapter.getItemId(i) == itemId)
                return i;
        }

        return RecyclerView.NO_POSITION;
    }

    public void cancelSwipe() {
        cancelSwipe(false);
    }

    /*package*/ void cancelSwipe(boolean immediately) {
        handleActionUpOrCancel(null, false);

        if (immediately) {
            finishSwiping(RESULT_CANCELED);
        } else {
            if (isSwiping()) {
                mHandler.requestDeferredCancelSwipe();
            }
        }
    }

    /*package*/ boolean isAnimationRunning(RecyclerView.ViewHolder item) {
        return (mItemSlideAnimator != null) && (mItemSlideAnimator.isRunning(item));
    }

    private void slideItem(RecyclerView.ViewHolder holder, float amount, boolean proportionalAmount, boolean horizontal, boolean shouldAnimate) {
        if (amount == OUTSIDE_OF_THE_WINDOW_LEFT) {
            mItemSlideAnimator.slideToOutsideOfWindow(holder, ItemSlidingAnimator.DIR_LEFT, shouldAnimate, mMoveToOutsideWindowAnimationDuration);
        } else if (amount == OUTSIDE_OF_THE_WINDOW_TOP) {
            mItemSlideAnimator.slideToOutsideOfWindow(holder, ItemSlidingAnimator.DIR_UP, shouldAnimate, mMoveToOutsideWindowAnimationDuration);
        } else if (amount == OUTSIDE_OF_THE_WINDOW_RIGHT) {
            mItemSlideAnimator.slideToOutsideOfWindow(holder, ItemSlidingAnimator.DIR_RIGHT, shouldAnimate, mMoveToOutsideWindowAnimationDuration);
        } else if (amount == OUTSIDE_OF_THE_WINDOW_BOTTOM) {
            mItemSlideAnimator.slideToOutsideOfWindow(holder, ItemSlidingAnimator.DIR_DOWN, shouldAnimate, mMoveToOutsideWindowAnimationDuration);
        } else if (amount == 0.0f) {
            mItemSlideAnimator.slideToDefaultPosition(holder, horizontal, shouldAnimate, mReturnToDefaultPositionAnimationDuration);
        } else {
            mItemSlideAnimator.slideToSpecifiedPosition(holder, amount, proportionalAmount, horizontal, shouldAnimate, mMoveToSpecifiedPositionAnimationDuration);
        }
    }

    private int getWrappedItemPosition(RecyclerView.ViewHolder holder) {
        final RecyclerView.Adapter rootAdapter = mRecyclerView.getAdapter();
        final int rootItemPosition = CustomRecyclerViewUtils.getSynchronizedPosition(holder);

        return WrapperAdapterUtils.unwrapPosition(rootAdapter, mWrapperAdapter, rootItemPosition);
    }

    /**
     * Gets the duration of the "return to default position" animation
     *
     * @return Duration of the "return to default position" animation in milliseconds
     */
    public long getReturnToDefaultPositionAnimationDuration() {
        return mReturnToDefaultPositionAnimationDuration;
    }

    /**
     * Sets the duration of the "return to default position" animation
     *
     * @param duration Duration of the "return to default position" animation in milliseconds
     */
    public void setReturnToDefaultPositionAnimationDuration(long duration) {
        mReturnToDefaultPositionAnimationDuration = duration;
    }


    /**
     * Gets the duration of the "move to specified position" animation
     *
     * @return Duration of the "move to specified position" animation in milliseconds
     */
    public long getMoveToSpecifiedPositionAnimationDuration() {
        return mMoveToSpecifiedPositionAnimationDuration;
    }

    /**
     * Sets the duration of the "move to specified position" animation
     *
     * @param duration Duration of the "move to specified position" animation in milliseconds
     */
    public void setMoveToSpecifiedPositionAnimationDuration(long duration) {
        mMoveToSpecifiedPositionAnimationDuration = duration;
    }

    /**
     * Gets the duration of the "move to outside of the window" animation
     *
     * @return Duration of the "move to outside of the window" animation in milliseconds
     */
    public long getMoveToOutsideWindowAnimationDuration() {
        return mMoveToOutsideWindowAnimationDuration;
    }

    /**
     * Sets the duration of the "move to outside of the window" animation
     *
     * @param duration Duration of the "move to outside of the window" animation in milliseconds
     */
    public void setMoveToOutsideWindowAnimationDuration(long duration) {
        mMoveToOutsideWindowAnimationDuration = duration;
    }

    /**
     * Gets OnItemSwipeEventListener listener
     *
     * @return The listener object
     */
    @Nullable
    public OnItemSwipeEventListener getOnItemSwipeEventListener() {
        return mItemSwipeEventListener;
    }

    /**
     * Sets OnItemSwipeEventListener listener
     *
     * @param listener The listener object
     */
    public void setOnItemSwipeEventListener(@Nullable OnItemSwipeEventListener listener) {
        mItemSwipeEventListener = listener;
    }

    /*package*/ boolean swipeHorizontal() {
        return mSwipeHorizontal;
    }

    /*package*/ void applySlideItem(
            RecyclerView.ViewHolder holder, int itemPosition,
            float prevAmount, float amount, boolean proportionalAmount,
            boolean horizontal, boolean shouldAnimate, boolean isSwiping) {
        final SwipeableItemViewHolder holder2 = (SwipeableItemViewHolder) holder;
        final View containerView = SwipeableViewHolderUtils.getSwipeableContainerView(holder2);

        if (containerView == null) {
            return;
        }

        final int reqBackgroundType;

        if (amount == 0.0f) {
            if (prevAmount == 0.0f) {
                reqBackgroundType = DRAWABLE_SWIPE_NEUTRAL_BACKGROUND;
            } else {
                reqBackgroundType = determineBackgroundType(prevAmount, horizontal);
            }
        } else {
            reqBackgroundType = determineBackgroundType(amount, horizontal);
        }

        float adjustedAmount = amount;

        if (amount != 0.0f) {
            boolean isLimitProportional = holder2.isProportionalSwipeAmountModeEnabled();
            float minLimit = horizontal ? holder2.getMaxLeftSwipeAmount() : holder2.getMaxUpSwipeAmount();
            float maxLimit = horizontal ? holder2.getMaxRightSwipeAmount() : holder2.getMaxDownSwipeAmount();

            minLimit = adaptAmount(holder2, horizontal, minLimit, isLimitProportional, proportionalAmount);
            maxLimit = adaptAmount(holder2, horizontal, maxLimit, isLimitProportional, proportionalAmount);

            adjustedAmount = Math.max(adjustedAmount, minLimit);
            adjustedAmount = Math.min(adjustedAmount, maxLimit);
        }

        slideItem(holder, adjustedAmount, proportionalAmount, horizontal, shouldAnimate);
        mWrapperAdapter.onUpdateSlideAmount(holder, itemPosition, amount, proportionalAmount, horizontal, isSwiping, reqBackgroundType);
    }

    private static int determineBackgroundType(float amount, boolean horizontal) {
        if (horizontal) {
            return (amount < 0)
                    ? DRAWABLE_SWIPE_LEFT_BACKGROUND
                    : DRAWABLE_SWIPE_RIGHT_BACKGROUND;
        } else {
            return (amount < 0)
                    ? DRAWABLE_SWIPE_UP_BACKGROUND
                    : DRAWABLE_SWIPE_DOWN_BACKGROUND;
        }
    }

    /*package*/ void cancelPendingAnimations(RecyclerView.ViewHolder holder) {
        if (mItemSlideAnimator != null) {
            mItemSlideAnimator.endAnimation(holder);
        }
    }

    /*package*/ int getSwipeContainerViewTranslationX(RecyclerView.ViewHolder holder) {
        return mItemSlideAnimator.getSwipeContainerViewTranslationX(holder);
    }

    /*package*/ int getSwipeContainerViewTranslationY(RecyclerView.ViewHolder holder) {
        return mItemSlideAnimator.getSwipeContainerViewTranslationY(holder);
    }

    /*package*/ void handleOnLongPress(MotionEvent e) {
        RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForItemId(mCheckingTouchSlop);
        if (holder != null) {
            checkConditionAndStartSwiping(e, holder);
        }
    }

    /*package*/ int getSwipingItemPosition() {
        return mSwipingItemPosition;
    }

    /*package*/ int syncSwipingItemPosition() {
        return syncSwipingItemPosition(mSwipingItemPosition);
    }

    /*package*/ int syncSwipingItemPosition(int positionGuess) {
        mSwipingItemPosition = getItemPosition(mWrapperAdapter, mSwipingItemId, positionGuess);
        return mSwipingItemPosition;
    }

    /*package*/
    static float adaptAmount(SwipeableItemViewHolder holder, boolean horizontal, float srcAmount, boolean isSrcProportional, boolean isDestProportional) {
        float destAmount = srcAmount;

        if ((isSrcProportional ^ isDestProportional) && (srcAmount != 0.0f) && !isSpecialSwipeAmountValue(srcAmount)) {
            View v = SwipeableViewHolderUtils.getSwipeableContainerView(holder);
            float d = (horizontal) ? v.getWidth() : v.getHeight();

            if (isDestProportional) {
                d = (d != 0) ? (1 / d) : 0;
            }

            destAmount *= d;
        }

        return destAmount;
    }

    private static boolean isSpecialSwipeAmountValue(float amount) {
        return (amount == SwipeableItemConstants.OUTSIDE_OF_THE_WINDOW_LEFT) ||
                (amount == SwipeableItemConstants.OUTSIDE_OF_THE_WINDOW_RIGHT) ||
                (amount == SwipeableItemConstants.OUTSIDE_OF_THE_WINDOW_TOP) ||
                (amount == SwipeableItemConstants.OUTSIDE_OF_THE_WINDOW_BOTTOM);
    }

    private static boolean supportsViewPropertyAnimator() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    private static class InternalHandler extends Handler {
        private static final int MSG_LONGPRESS = 1;
        private static final int MSG_DEFERRED_CANCEL_SWIPE = 2;

        private RecyclerViewSwipeManager mHolder;
        private MotionEvent mDownMotionEvent;

        public InternalHandler(RecyclerViewSwipeManager holder) {
            mHolder = holder;
        }

        public void release() {
            removeCallbacks(null);
            mHolder = null;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LONGPRESS:
                    mHolder.handleOnLongPress(mDownMotionEvent);
                    break;
                case MSG_DEFERRED_CANCEL_SWIPE:
                    mHolder.cancelSwipe(true);
                    break;
            }
        }

        public void startLongPressDetection(MotionEvent e, int timeout) {
            cancelLongPressDetection();
            mDownMotionEvent = MotionEvent.obtain(e);
            sendEmptyMessageAtTime(MSG_LONGPRESS, e.getDownTime() + timeout);
        }

        public void cancelLongPressDetection() {
            removeMessages(MSG_LONGPRESS);
            if (mDownMotionEvent != null) {
                mDownMotionEvent.recycle();
                mDownMotionEvent = null;
            }
        }

        public void removeDeferredCancelSwipeRequest() {
            removeMessages(MSG_DEFERRED_CANCEL_SWIPE);
        }

        public void requestDeferredCancelSwipe() {
            if (isCancelSwipeRequested()) {
                return;
            }
            sendEmptyMessage(MSG_DEFERRED_CANCEL_SWIPE);
        }

        public boolean isCancelSwipeRequested() {
            return hasMessages(MSG_DEFERRED_CANCEL_SWIPE);
        }
    }
}
