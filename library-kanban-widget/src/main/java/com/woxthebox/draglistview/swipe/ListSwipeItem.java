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
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

import com.woxthebox.draglistview.R;

public class ListSwipeItem extends RelativeLayout {

    private enum SwipeState {
        IDLE, // Item is not moving
        SWIPING, // Item is moving because the user is swiping with the finger
        ANIMATING // Item is animating
    }

    public enum SwipeDirection {
        LEFT, RIGHT, LEFT_AND_RIGHT, NONE
    }

    public enum SwipeInStyle {
        APPEAR, SLIDE
    }

    private View mLeftView;
    private View mRightView;
    private View mSwipeView;
    private RecyclerView.ViewHolder mViewHolder;
    private SwipeState mSwipeState = SwipeState.IDLE;
    private float mSwipeTranslationX;
    private float mStartSwipeTranslationX;
    private float mFlingSpeed;
    private boolean mSwipeStarted;
    private int mSwipeViewId;
    private int mLeftViewId;
    private int mRightViewId;
    private SwipeDirection mSwipeDirection = SwipeDirection.LEFT_AND_RIGHT;
    private SwipeInStyle mSwipeInStyle = SwipeInStyle.APPEAR;

    // Used to report swiped distance to listener. This is will be set at the start of the swipe and reset at the end.
    private ListSwipeHelper.OnSwipeListener mSwipeListener;

    public ListSwipeItem(Context context) {
        super(context);
    }

    public ListSwipeItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ListSwipeItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ListSwipeItem);

        mSwipeViewId = a.getResourceId(R.styleable.ListSwipeItem_swipeViewId, -1);
        mLeftViewId = a.getResourceId(R.styleable.ListSwipeItem_leftViewId, -1);
        mRightViewId = a.getResourceId(R.styleable.ListSwipeItem_rightViewId, -1);

        a.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSwipeView = findViewById(mSwipeViewId);
        mLeftView = findViewById(mLeftViewId);
        mRightView = findViewById(mRightViewId);

        if (mLeftView != null) {
            mLeftView.setVisibility(View.INVISIBLE);
        }
        if (mRightView != null) {
            mRightView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void setTag(Object tag) {
        super.setTag(tag);
        // If view holder is recyclable then reset as this view might be used to another card
        if (mViewHolder != null && mViewHolder.isRecyclable()) {
            resetSwipe(false);
        }
    }

    public void setSwipeInStyle(SwipeInStyle style) {
        mSwipeInStyle = style;
    }

    public void setSupportedSwipeDirection(SwipeDirection swipeDirection) {
        mSwipeDirection = swipeDirection;
    }

    public SwipeDirection getSupportedSwipeDirection() {
        return mSwipeDirection;
    }

    void setSwipeListener(ListSwipeHelper.OnSwipeListener listener) {
        mSwipeListener = listener;
    }

    SwipeDirection getSwipedDirection() {
        if (mSwipeState != SwipeState.IDLE) {
            return SwipeDirection.NONE;
        }

        if (mSwipeView.getTranslationX() == -getMeasuredWidth()) {
            return SwipeDirection.LEFT;
        } else if (mSwipeView.getTranslationX() == getMeasuredWidth()) {
            return SwipeDirection.RIGHT;
        }
        return SwipeDirection.NONE;
    }

    boolean isAnimating() {
        return mSwipeState == SwipeState.ANIMATING;
    }

    boolean isSwipeStarted() {
        return mSwipeStarted;
    }

    void setFlingSpeed(float speed) {
        mFlingSpeed = speed;
    }

    void swipeTranslationByX(float dx) {
        setSwipeTranslationX(mSwipeTranslationX + dx);
    }

    void setSwipeTranslationX(float x) {
        // Based on supported swipe direction reset the x position
        if ((mSwipeDirection == SwipeDirection.LEFT && x > 0) || (mSwipeDirection == SwipeDirection.RIGHT && x < 0) || mSwipeDirection == SwipeDirection.NONE) {
            x = 0;
        }

        mSwipeTranslationX = x;
        mSwipeTranslationX = Math.min(mSwipeTranslationX, getMeasuredWidth());
        mSwipeTranslationX = Math.max(mSwipeTranslationX, -getMeasuredWidth());

        mSwipeView.setTranslationX(mSwipeTranslationX);
        if (mSwipeListener != null) {
            mSwipeListener.onItemSwiping(this, mSwipeTranslationX);
        }

        if (mSwipeTranslationX < 0) {
            if (mSwipeInStyle == SwipeInStyle.SLIDE) {
                mRightView.setTranslationX(getMeasuredWidth() + mSwipeTranslationX);
            }
            mRightView.setVisibility(View.VISIBLE);
            mLeftView.setVisibility(View.INVISIBLE);
        } else if (mSwipeTranslationX > 0) {
            if (mSwipeInStyle == SwipeInStyle.SLIDE) {
                mLeftView.setTranslationX(-getMeasuredWidth() + mSwipeTranslationX);
            }
            mLeftView.setVisibility(View.VISIBLE);
            mRightView.setVisibility(View.INVISIBLE);
        } else {
            mRightView.setVisibility(View.INVISIBLE);
            mLeftView.setVisibility(View.INVISIBLE);
        }
    }

    void animateToSwipeTranslationX(float x, Animator.AnimatorListener... listeners) {
        if (x == mSwipeTranslationX) {
            return;
        }

        mSwipeState = SwipeState.ANIMATING;
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "SwipeTranslationX", mSwipeTranslationX, x);
        animator.setDuration(250);
        animator.setInterpolator(new DecelerateInterpolator());
        for (Animator.AnimatorListener listener : listeners) {
            if (listener != null) {
                animator.addListener(listener);
            }
        }
        animator.start();
    }

    void resetSwipe(boolean animate) {
        if (isAnimating() || !mSwipeStarted) {
            return;
        }

        if (mSwipeTranslationX != 0) {
            if (animate) {
                animateToSwipeTranslationX(0, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mSwipeState = SwipeState.IDLE;
                        mSwipeListener = null;
                    }
                });
            } else {
                setSwipeTranslationX(0);
                mSwipeState = SwipeState.IDLE;
                mSwipeListener = null;
            }
        } else {
            mSwipeListener = null;
        }

        if (mViewHolder != null && !mViewHolder.isRecyclable()) {
            mViewHolder.setIsRecyclable(true);
        }

        mViewHolder = null;
        mFlingSpeed = 0;
        mStartSwipeTranslationX = 0;
        mSwipeStarted = false;
    }

    void handleSwipeUp(Animator.AnimatorListener listener) {
        if (isAnimating() || !mSwipeStarted) {
            return;
        }

        AnimatorListenerAdapter idleListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mSwipeState = SwipeState.IDLE;
                if (mSwipeTranslationX == 0) {
                    resetSwipe(false);
                }
                if (mViewHolder != null) {
                    mViewHolder.setIsRecyclable(true);
                }
            }
        };

        if (mFlingSpeed == 0 && Math.abs(mStartSwipeTranslationX - mSwipeTranslationX) < getMeasuredWidth() / 3) {
            // Bounce back
            animateToSwipeTranslationX(mStartSwipeTranslationX, idleListener, listener);
        } else {
            // Animate to end
            float newX = getTranslateToXPosition(mStartSwipeTranslationX, mSwipeTranslationX, mFlingSpeed);
            animateToSwipeTranslationX(newX, idleListener, listener);
        }
        mStartSwipeTranslationX = 0;
        mFlingSpeed = 0;
    }

    private float getTranslateToXPosition(float startTranslationX, float currentTranslationX, float flingSpeed) {
        if (flingSpeed == 0 && Math.abs(startTranslationX - currentTranslationX) < getMeasuredWidth() / 3) {
            // Bounce back
            return startTranslationX;
        } else if (currentTranslationX < 0) {
            // Swiping done side
            if (flingSpeed > 0) {
                return 0;
            } else {
                return -getMeasuredWidth();
            }
        } else if (startTranslationX == 0) {
            // Swiping action side from start position
            if (flingSpeed < 0) {
                return 0;
            } else {
                return getMeasuredWidth();
            }
        } else {
            // Swiping action side from action position
            if (flingSpeed > 0) {
                return getMeasuredWidth();
            } else {
                return 0;
            }
        }
    }

    void handleSwipeMoveStarted(ListSwipeHelper.OnSwipeListener listener) {
        mStartSwipeTranslationX = mSwipeTranslationX;
        mSwipeListener = listener;
    }

    void handleSwipeMove(float dx, RecyclerView.ViewHolder viewHolder) {
        if (isAnimating()) {
            return;
        }
        mSwipeState = SwipeState.SWIPING;
        if (!mSwipeStarted) {
            mSwipeStarted = true;
            mViewHolder = viewHolder;
            mViewHolder.setIsRecyclable(false);
        }
        swipeTranslationByX(dx);
    }
}
