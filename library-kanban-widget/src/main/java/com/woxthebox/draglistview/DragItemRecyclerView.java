/**
 * Copyright 2014 Magnus Woxblom
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.woxthebox.draglistview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class DragItemRecyclerView extends RecyclerView implements AutoScroller.AutoScrollListener {

    public interface DragItemListener {
        void onDragStarted(int itemPosition, float x, float y);

        void onDragging(int itemPosition, float x, float y);

        void onDragEnded(int newItemPosition);
    }

    public interface DragItemCallback {
        boolean canDragItemAtPosition(int dragPosition);

        boolean canDropItemAtPosition(int dropPosition);
    }

    private enum DragState {
        DRAG_STARTED, DRAGGING, DRAG_ENDED
    }

    private AutoScroller mAutoScroller;
    private DragItemListener mListener;
    private DragItemCallback mDragCallback;
    private DragState mDragState = DragState.DRAG_ENDED;
    private DragItemAdapter mAdapter;
    private DragItem mDragItem;
    private Drawable mDropTargetBackgroundDrawable;
    private Drawable mDropTargetForegroundDrawable;
    private long mDragItemId = NO_ID;
    private boolean mHoldChangePosition;
    private int mDragItemPosition;
    private int mTouchSlop;
    private float mStartY;
    private boolean mClipToPadding;
    private boolean mCanNotDragAboveTop;
    private boolean mCanNotDragBelowBottom;
    private boolean mScrollingEnabled = true;
    private boolean mDisableReorderWhenDragging;
    private boolean mDragEnabled = true;

    public DragItemRecyclerView(Context context) {
        super(context);
        init();
    }

    public DragItemRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    public DragItemRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mAutoScroller = new AutoScroller(getContext(), this);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        addItemDecoration(new ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, State state) {
                super.onDraw(c, parent, state);
                drawDecoration(c, parent, mDropTargetBackgroundDrawable);
            }

            @Override
            public void onDrawOver(Canvas c, RecyclerView parent, State state) {
                super.onDrawOver(c, parent, state);
                drawDecoration(c, parent, mDropTargetForegroundDrawable);
            }

            private void drawDecoration(Canvas c, RecyclerView parent, Drawable drawable) {
                if (mAdapter == null || mAdapter.getDropTargetId() == NO_ID || drawable == null) {
                    return;
                }

                for (int i = 0; i < parent.getChildCount(); i++) {
                    View item = parent.getChildAt(i);
                    int pos = getChildAdapterPosition(item);
                    if (pos != NO_POSITION && mAdapter.getItemId(pos) == mAdapter.getDropTargetId()) {
                        drawable.setBounds(item.getLeft(), item.getTop(), item.getRight(), item.getBottom());
                        drawable.draw(c);
                    }
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!mScrollingEnabled) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float diffY = Math.abs(event.getY() - mStartY);
                if (diffY > mTouchSlop * 0.5) {
                    // Steal event from parent as we now only want to scroll in the list
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
        }
        return super.onInterceptTouchEvent(event);
    }

    void setDragEnabled(boolean enabled) {
        mDragEnabled = enabled;
    }

    boolean isDragEnabled() {
        return mDragEnabled;
    }

    void setCanNotDragAboveTopItem(boolean canNotDragAboveTop) {
        mCanNotDragAboveTop = canNotDragAboveTop;
    }

    void setCanNotDragBelowBottomItem(boolean canNotDragBelowBottom) {
        mCanNotDragBelowBottom = canNotDragBelowBottom;
    }

    void setScrollingEnabled(boolean scrollingEnabled) {
        mScrollingEnabled = scrollingEnabled;
    }

    void setDisableReorderWhenDragging(boolean disableReorder) {
        mDisableReorderWhenDragging = disableReorder;
    }

    public void setDropTargetDrawables(Drawable backgroundDrawable, Drawable foregroundDrawable) {
        mDropTargetBackgroundDrawable = backgroundDrawable;
        mDropTargetForegroundDrawable = foregroundDrawable;
    }

    void setDragItemListener(DragItemListener listener) {
        mListener = listener;
    }

    void setDragItemCallback(DragItemCallback callback) {
        mDragCallback = callback;
    }

    void setDragItem(DragItem dragItem) {
        mDragItem = dragItem;
    }

    boolean isDragging() {
        return mDragState != DragState.DRAG_ENDED;
    }

    long getDragItemId() {
        return mDragItemId;
    }

    @Override
    public void setClipToPadding(boolean clipToPadding) {
        super.setClipToPadding(clipToPadding);
        mClipToPadding = clipToPadding;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (!isInEditMode()) {
            if (!(adapter instanceof DragItemAdapter)) {
                throw new RuntimeException("Adapter must extend DragItemAdapter");
            }
            if (!adapter.hasStableIds()) {
                throw new RuntimeException("Adapter must have stable ids");
            }
        }
        super.setAdapter(adapter);
        mAdapter = (DragItemAdapter) adapter;
    }

    @Override
    public void swapAdapter(Adapter adapter, boolean r) {
        if (!isInEditMode()) {
            if (!(adapter instanceof DragItemAdapter)) {
                throw new RuntimeException("Adapter must extend DragItemAdapter");
            }
            if (!adapter.hasStableIds()) {
                throw new RuntimeException("Adapter must have stable ids");
            }
        }
        super.swapAdapter(adapter, r);
        mAdapter = (DragItemAdapter) adapter;
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
        if (!(layout instanceof LinearLayoutManager)) {
            throw new RuntimeException("Layout must be an instance of LinearLayoutManager");
        }
    }

    @Override
    public void onAutoScrollPositionBy(int dx, int dy) {
        if (isDragging()) {
            scrollBy(dx, dy);
            updateDragPositionAndScroll();
        } else {
            mAutoScroller.stopAutoScroll();
        }
    }

    @Override
    public void onAutoScrollColumnBy(int columns) {
    }

    /**
     * Returns the child view under the specific x,y coordinate.
     * This method will take margins of the child into account when finding it.
     */
    public View findChildView(float x, float y) {
        final int count = getChildCount();
        if (y <= 0 && count > 0) {
            return getChildAt(0);
        }

        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
            if (x >= child.getLeft() - params.leftMargin && x <= child.getRight() + params.rightMargin
                    && y >= child.getTop() - params.topMargin && y <= child.getBottom() + params.bottomMargin) {
                return child;
            }
        }

        return null;
    }

    private boolean shouldChangeItemPosition(int newPos) {
        // Check if drag position is changed and valid and that we are not in a hold position state
        if (mHoldChangePosition || mDragItemPosition == NO_POSITION || mDragItemPosition == newPos) {
            return false;
        }
        // If we are not allowed to drag above top or bottom and new pos is 0 or item count then return false
        if ((mCanNotDragAboveTop && newPos == 0) || (mCanNotDragBelowBottom && newPos == mAdapter.getItemCount() - 1)) {
            return false;
        }
        // Check with callback if we are allowed to drop at this position
        if (mDragCallback != null && !mDragCallback.canDropItemAtPosition(newPos)) {
            return false;
        }
        return true;
    }

    private void updateDragPositionAndScroll() {
        View view = findChildView(mDragItem.getX(), mDragItem.getY());
        int newPos = getChildLayoutPosition(view);
        if (newPos == NO_POSITION || view == null) {
            return;
        }

        // If using a LinearLayoutManager and the new view has a bigger height we need to check if passing centerY as well.
        // If not doing this extra check the bigger item will move back again when dragging slowly over it.
        boolean linearLayoutManager = getLayoutManager() instanceof LinearLayoutManager && !(getLayoutManager() instanceof GridLayoutManager);
        if (linearLayoutManager) {
            MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
            int viewHeight = view.getMeasuredHeight() + params.topMargin + params.bottomMargin;
            int viewCenterY = view.getTop() - params.topMargin + viewHeight / 2;
            boolean dragDown = mDragItemPosition < getChildLayoutPosition(view);
            boolean movedPassedCenterY = dragDown ? mDragItem.getY() > viewCenterY : mDragItem.getY() < viewCenterY;

            // If new height is bigger then current and not passed centerY then reset back to current position
            if (viewHeight > mDragItem.getDragItemView().getMeasuredHeight() && !movedPassedCenterY) {
                newPos = mDragItemPosition;
            }
        }

        LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
        if (shouldChangeItemPosition(newPos)) {
            if (mDisableReorderWhenDragging) {
                mAdapter.setDropTargetId(mAdapter.getItemId(newPos));
                mAdapter.notifyDataSetChanged();
            } else {
                int pos = layoutManager.findFirstVisibleItemPosition();
                View posView = layoutManager.findViewByPosition(pos);
                mAdapter.changeItemPosition(mDragItemPosition, newPos);
                mDragItemPosition = newPos;

                // Since notifyItemMoved scrolls the list we need to scroll back to where we were after the position change.
                if (layoutManager.getOrientation() == LinearLayoutManager.VERTICAL) {
                    int topMargin = ((MarginLayoutParams) posView.getLayoutParams()).topMargin;
                    layoutManager.scrollToPositionWithOffset(pos, posView.getTop() - topMargin);
                } else {
                    int leftMargin = ((MarginLayoutParams) posView.getLayoutParams()).leftMargin;
                    layoutManager.scrollToPositionWithOffset(pos, posView.getLeft() - leftMargin);
                }
            }
        }

        boolean lastItemReached = false;
        boolean firstItemReached = false;
        int top = mClipToPadding ? getPaddingTop() : 0;
        int bottom = mClipToPadding ? getHeight() - getPaddingBottom() : getHeight();
        int left = mClipToPadding ? getPaddingLeft() : 0;
        int right = mClipToPadding ? getWidth() - getPaddingRight() : getWidth();
        ViewHolder lastChild = findViewHolderForLayoutPosition(mAdapter.getItemCount() - 1);
        ViewHolder firstChild = findViewHolderForLayoutPosition(0);

        // Check if first or last item has been reached
        if (layoutManager.getOrientation() == LinearLayoutManager.VERTICAL) {
            if (lastChild != null && lastChild.itemView.getBottom() <= bottom) {
                lastItemReached = true;
            }
            if (firstChild != null && firstChild.itemView.getTop() >= top) {
                firstItemReached = true;
            }
        } else {
            if (lastChild != null && lastChild.itemView.getRight() <= right) {
                lastItemReached = true;
            }
            if (firstChild != null && firstChild.itemView.getLeft() >= left) {
                firstItemReached = true;
            }
        }

        // Start auto scroll if at the edge
        if (layoutManager.getOrientation() == LinearLayoutManager.VERTICAL) {
            if (mDragItem.getY() > getHeight() - view.getHeight() / 2 && !lastItemReached) {
                mAutoScroller.startAutoScroll(AutoScroller.ScrollDirection.UP);
            } else if (mDragItem.getY() < view.getHeight() / 2 && !firstItemReached) {
                mAutoScroller.startAutoScroll(AutoScroller.ScrollDirection.DOWN);
            } else {
                mAutoScroller.stopAutoScroll();
            }
        } else {
            if (mDragItem.getX() > getWidth() - view.getWidth() / 2 && !lastItemReached) {
                mAutoScroller.startAutoScroll(AutoScroller.ScrollDirection.LEFT);
            } else if (mDragItem.getX() < view.getWidth() / 2 && !firstItemReached) {
                mAutoScroller.startAutoScroll(AutoScroller.ScrollDirection.RIGHT);
            } else {
                mAutoScroller.stopAutoScroll();
            }
        }
    }

    boolean startDrag(View itemView, long itemId, float x, float y) {
        int dragItemPosition = mAdapter.getPositionForItemId(itemId);
        if (!mDragEnabled || (mCanNotDragAboveTop && dragItemPosition == 0)
                || (mCanNotDragBelowBottom && dragItemPosition == mAdapter.getItemCount() - 1)) {
            return false;
        }

        if (mDragCallback != null && !mDragCallback.canDragItemAtPosition(dragItemPosition)) {
            return false;
        }

        // If a drag is starting the parent must always be allowed to intercept
        getParent().requestDisallowInterceptTouchEvent(false);
        mDragState = DragState.DRAG_STARTED;
        mDragItemId = itemId;
        mDragItem.startDrag(itemView, x, y);
        mDragItemPosition = dragItemPosition;
        updateDragPositionAndScroll();

        mAdapter.setDragItemId(mDragItemId);
        mAdapter.notifyDataSetChanged();
        if (mListener != null) {
            mListener.onDragStarted(mDragItemPosition, mDragItem.getX(), mDragItem.getY());
        }

        invalidate();
        return true;
    }

    void onDragging(float x, float y) {
        if (mDragState == DragState.DRAG_ENDED) {
            return;
        }

        mDragState = DragState.DRAGGING;
        mDragItemPosition = mAdapter.getPositionForItemId(mDragItemId);
        mDragItem.setPosition(x, y);

        if (!mAutoScroller.isAutoScrolling()) {
            updateDragPositionAndScroll();
        }

        if (mListener != null) {
            mListener.onDragging(mDragItemPosition, x, y);
        }
        invalidate();
    }

    void onDragEnded() {
        // Need check because sometimes the framework calls drag end twice in a row
        if (mDragState == DragState.DRAG_ENDED) {
            return;
        }

        mAutoScroller.stopAutoScroll();
        setEnabled(false);

        if (mDisableReorderWhenDragging) {
            int newPos = mAdapter.getPositionForItemId(mAdapter.getDropTargetId());
            if (newPos != NO_POSITION) {
                mAdapter.swapItems(mDragItemPosition, newPos);
                mDragItemPosition = newPos;
            }
            mAdapter.setDropTargetId(NO_ID);
        }

        // Post so layout is done before we start end animation
        post(new Runnable() {
            @Override
            public void run() {
                // Sometimes the holder will be null if a holder has not yet been set for the position
                final RecyclerView.ViewHolder holder = findViewHolderForAdapterPosition(mDragItemPosition);
                if (holder != null) {
                    if (getItemAnimator() != null) {
                        getItemAnimator().endAnimation(holder);
                    }
                    mDragItem.endDrag(holder.itemView, new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            holder.itemView.setAlpha(1);
                            onDragItemAnimationEnd();
                        }
                    });
                } else {
                    onDragItemAnimationEnd();
                }
            }
        });
    }

    private void onDragItemAnimationEnd() {
        mAdapter.setDragItemId(NO_ID);
        mAdapter.setDropTargetId(NO_ID);
        mAdapter.notifyDataSetChanged();

        mDragState = DragState.DRAG_ENDED;
        if (mListener != null) {
            mListener.onDragEnded(mDragItemPosition);
        }

        mDragItemId = NO_ID;
        mDragItem.hide();
        setEnabled(true);
        invalidate();
    }

    int getDragPositionForY(float y) {
        View child = findChildView(0, y);
        int pos;
        if (child == null && getChildCount() > 0) {
            // If child is null and child count is not 0 it means that an item was
            // dragged below the last item in the list, then put it after that item
            pos = getChildLayoutPosition(getChildAt(getChildCount() - 1)) + 1;
        } else {
            pos = getChildLayoutPosition(child);
        }

        // If pos is NO_POSITION it means that the child has not been laid out yet,
        // this only happens for pos 0 as far as I know
        if (pos == NO_POSITION) {
            pos = 0;
        }
        return pos;
    }

    void addDragItemAndStart(float y, Object item, long itemId) {
        int pos = getDragPositionForY(y);

        mDragState = DragState.DRAG_STARTED;
        mDragItemId = itemId;
        mAdapter.setDragItemId(mDragItemId);
        mAdapter.addItem(pos, item);
        mDragItemPosition = pos;

        mHoldChangePosition = true;
        postDelayed(new Runnable() {
            @Override
            public void run() {
                mHoldChangePosition = false;
            }
        }, getItemAnimator().getMoveDuration());

        invalidate();
    }

    Object removeDragItemAndEnd() {
        if (mDragItemPosition == NO_POSITION) {
            return null;
        }
        mAutoScroller.stopAutoScroll();
        Object item = mAdapter.removeItem(mDragItemPosition);
        mAdapter.setDragItemId(NO_ID);
        mDragState = DragState.DRAG_ENDED;
        mDragItemId = NO_ID;

        invalidate();
        return item;
    }
}
