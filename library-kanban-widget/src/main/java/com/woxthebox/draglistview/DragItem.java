/*
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

import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

public class DragItem {
    protected static final int ANIMATION_DURATION = 250;
    private View mDragView;
    private View mRealDragView;

    private float mOffsetX;
    private float mOffsetY;
    private float mPosX;
    private float mPosY;
    private float mPosTouchDx;
    private float mPosTouchDy;
    private float mAnimationDx;
    private float mAnimationDy;
    private boolean mCanDragHorizontally = true;
    private boolean mSnapToTouch = true;

    DragItem(Context context) {
        mDragView = new View(context);
        hide();
    }

    public DragItem(Context context, int layoutId) {
        mDragView = View.inflate(context, layoutId, null);
        hide();
    }

    public void onBindDragView(View clickedView, View dragView) {
        Bitmap bitmap = Bitmap.createBitmap(clickedView.getWidth(), clickedView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        clickedView.draw(canvas);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            dragView.setBackground(new BitmapDrawable(clickedView.getResources(), bitmap));
        } else {
            //noinspection deprecation
            dragView.setBackgroundDrawable(new BitmapDrawable(clickedView.getResources(), bitmap));
        }
    }

    public void onMeasureDragView(View clickedView, View dragView) {
        dragView.setLayoutParams(new FrameLayout.LayoutParams(clickedView.getMeasuredWidth(), clickedView.getMeasuredHeight()));
        int widthSpec = View.MeasureSpec.makeMeasureSpec(clickedView.getMeasuredWidth(), View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(clickedView.getMeasuredHeight(), View.MeasureSpec.EXACTLY);
        dragView.measure(widthSpec, heightSpec);
    }

    public void onStartDragAnimation(View dragView) {
    }

    public void onEndDragAnimation(View dragView) {
    }

    boolean canDragHorizontally() {
        return mCanDragHorizontally;
    }

    void setCanDragHorizontally(boolean canDragHorizontally) {
        mCanDragHorizontally = canDragHorizontally;
    }

    boolean isSnapToTouch() {
        return mSnapToTouch;
    }

    protected void setSnapToTouch(boolean snapToTouch) {
        mSnapToTouch = snapToTouch;
    }

    View getDragItemView() {
        return mDragView;
    }

    View getRealDragView() {
        return mRealDragView;
    }

    private void show() {
        mDragView.setVisibility(View.VISIBLE);
    }

    void hide() {
        mDragView.setVisibility(View.GONE);
        mRealDragView = null;
    }

    boolean isDragging() {
        return mDragView.getVisibility() == View.VISIBLE;
    }

    void startDrag(View startFromView, float touchX, float touchY) {
        show();
        mRealDragView = startFromView;
        onBindDragView(startFromView, mDragView);
        onMeasureDragView(startFromView, mDragView);
        onStartDragAnimation(mDragView);

        float startX = startFromView.getX() - (mDragView.getMeasuredWidth() - startFromView.getMeasuredWidth()) / 2 + mDragView
                .getMeasuredWidth() / 2;
        float startY = startFromView.getY() - (mDragView.getMeasuredHeight() - startFromView.getMeasuredHeight()) / 2 + mDragView
                .getMeasuredHeight() / 2;

        if (mSnapToTouch) {
            mPosTouchDx = 0;
            mPosTouchDy = 0;
            setPosition(touchX, touchY);
            setAnimationDx(startX - touchX);
            setAnimationDY(startY - touchY);

            PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("AnimationDx", mAnimationDx, 0);
            PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("AnimationDY", mAnimationDy, 0);
            ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(this, pvhX, pvhY);
            anim.setInterpolator(new DecelerateInterpolator());
            anim.setDuration(ANIMATION_DURATION);
            anim.start();
        } else {
            mPosTouchDx = startX - touchX;
            mPosTouchDy = startY - touchY;
            setPosition(touchX, touchY);
        }
    }

    void endDrag(View endToView, AnimatorListenerAdapter listener) {
        onEndDragAnimation(mDragView);

        float endX = endToView.getX() - (mDragView.getMeasuredWidth() - endToView.getMeasuredWidth()) / 2 + mDragView
                .getMeasuredWidth() / 2;
        float endY = endToView.getY() - (mDragView.getMeasuredHeight() - endToView.getMeasuredHeight()) / 2 + mDragView
                .getMeasuredHeight() / 2;
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("X", mPosX, endX);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("Y", mPosY, endY);
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(this, pvhX, pvhY);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setDuration(ANIMATION_DURATION);
        anim.addListener(listener);
        anim.start();
    }

    @SuppressWarnings("WeakerAccess")
    void setAnimationDx(float x) {
        mAnimationDx = x;
        updatePosition();
    }

    @SuppressWarnings("WeakerAccess")
    void setAnimationDY(float y) {
        mAnimationDy = y;
        updatePosition();
    }

    @SuppressWarnings("unused")
    void setX(float x) {
        mPosX = x;
        updatePosition();
    }

    @SuppressWarnings("unused")
    void setY(float y) {
        mPosY = y;
        updatePosition();
    }

    float getX() {
        return mPosX;
    }

    float getY() {
        return mPosY;
    }

    void setPosition(float touchX, float touchY) {
        mPosX = touchX + mPosTouchDx;
        mPosY = touchY + mPosTouchDy;
        updatePosition();
    }

    void setOffset(float offsetX, float offsetY) {
        mOffsetX = offsetX;
        mOffsetY = offsetY;
        updatePosition();
    }

    private void updatePosition() {
        if (mCanDragHorizontally) {
            mDragView.setX(mPosX + mOffsetX + mAnimationDx - mDragView.getMeasuredWidth() / 2);
        }

        mDragView.setY(mPosY + mOffsetY + mAnimationDy - mDragView.getMeasuredHeight() / 2);
        mDragView.invalidate();
    }
}
