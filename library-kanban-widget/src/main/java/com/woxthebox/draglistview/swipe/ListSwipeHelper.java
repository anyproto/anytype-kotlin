/*
 * Copyright 2017 Magnus Woxblom
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

package com.woxthebox.draglistview.swipe;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class ListSwipeHelper extends RecyclerView.OnScrollListener implements RecyclerView.OnItemTouchListener {

    public static abstract class OnSwipeListenerAdapter implements OnSwipeListener {
        @Override
        public void onItemSwipeStarted(ListSwipeItem item) {
        }

        @Override
        public void onItemSwipeEnded(ListSwipeItem item, ListSwipeItem.SwipeDirection swipedDirection) {
        }

        @Override
        public void onItemSwiping(ListSwipeItem item, float swipedDistanceX) {
        }
    }

    public interface OnSwipeListener {
        void onItemSwipeStarted(ListSwipeItem item);

        void onItemSwipeEnded(ListSwipeItem item, ListSwipeItem.SwipeDirection swipedDirection);

        void onItemSwiping(ListSwipeItem item, float swipedDistanceX);
    }

    private GestureListener mGestureListener;
    private GestureDetector mGestureDetector;
    private ListSwipeItem mSwipeView;
    private RecyclerView mRecyclerView;
    private OnSwipeListener mSwipeListener;
    private int mTouchSlop;

    public ListSwipeHelper(Context applicationContext, OnSwipeListener listener) {
        mSwipeListener = listener;
        mGestureListener = new GestureListener();
        mGestureDetector = new GestureDetector(applicationContext, mGestureListener);
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent event) {
        handleTouch(rv, event);
        return mGestureListener.isSwipeStarted();
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent event) {
        handleTouch(rv, event);
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        resetSwipedViews(null);
    }

    public void resetSwipedViews(View exceptionView) {
        int childCount = mRecyclerView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = mRecyclerView.getChildAt(i);
            if (view instanceof ListSwipeItem && view != exceptionView) {
                ((ListSwipeItem) view).resetSwipe(true);
            }
        }
    }

    private void handleTouch(RecyclerView rv, MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                View swipeView = rv.findChildViewUnder(event.getX(), event.getY());
                if (swipeView instanceof ListSwipeItem &&
                        ((ListSwipeItem) swipeView).getSupportedSwipeDirection() != ListSwipeItem.SwipeDirection.NONE) {
                    mSwipeView = (ListSwipeItem) swipeView;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mSwipeView != null) {
                    final ListSwipeItem endingSwipeView = mSwipeView;
                    endingSwipeView.handleSwipeUp(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (endingSwipeView.isSwipeStarted()) {
                                resetSwipedViews(endingSwipeView);
                            }

                            if (mSwipeListener != null) {
                                mSwipeListener.onItemSwipeEnded(endingSwipeView, endingSwipeView.getSwipedDirection());
                            }
                        }
                    });
                } else {
                    resetSwipedViews(null);
                }
                mSwipeView = null;
                mRecyclerView.requestDisallowInterceptTouchEvent(false);
                break;
        }
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    public void detachFromRecyclerView() {
        if (mRecyclerView != null) {
            mRecyclerView.removeOnItemTouchListener(this);
            mRecyclerView.removeOnScrollListener(this);
        }
        mRecyclerView = null;
    }

    public void attachToRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mRecyclerView.addOnItemTouchListener(this);
        mRecyclerView.addOnScrollListener(this);
        mTouchSlop = ViewConfiguration.get(mRecyclerView.getContext()).getScaledTouchSlop();
    }

    public void setSwipeListener(ListSwipeHelper.OnSwipeListener listener) {
        mSwipeListener = listener;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private boolean mSwipeStarted;

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (e1 == null || e2 == null || mSwipeView == null || mRecyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE) {
                return false;
            }

            final float diffX = Math.abs(e2.getX() - e1.getX());
            final float diffY = Math.abs(e2.getY() - e1.getY());
            if (!mSwipeStarted && diffX > mTouchSlop * 2 && diffX * 0.5f > diffY) {
                mSwipeStarted = true;
                mRecyclerView.requestDisallowInterceptTouchEvent(true);
                mSwipeView.handleSwipeMoveStarted(mSwipeListener);
                if (mSwipeListener != null) {
                    mSwipeListener.onItemSwipeStarted(mSwipeView);
                }
            }

            if (mSwipeStarted) {
                mSwipeView.handleSwipeMove(-distanceX, mRecyclerView.getChildViewHolder(mSwipeView));
            }

            return mSwipeStarted;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            mSwipeStarted = false;
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (!canStartSwipe(e1, e2)) {
                return false;
            }

            mSwipeView.setFlingSpeed(velocityX);
            return true;
        }

        boolean isSwipeStarted() {
            return mSwipeStarted;
        }

        private boolean canStartSwipe(MotionEvent e1, MotionEvent e2) {
            return !(e1 == null || e2 == null || mSwipeView == null || mRecyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE
                    || mSwipeView.getSupportedSwipeDirection() == ListSwipeItem.SwipeDirection.NONE);
        }
    }
}
