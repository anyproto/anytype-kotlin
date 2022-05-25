package com.anytypeio.anytype.core_ui.widgets

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible

class ScrollAndMoveHintWidget : FrameLayout {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init()
    }

    fun init() {
        LayoutInflater.from(context).inflate(R.layout.widget_sam_hint, this)
    }

    fun showWithAnimation() {
        ObjectAnimator.ofFloat(
            this,
            ANIMATED_PROPERTY,
            TRANSLATION_INVISIBLE,
            TRANSLATION_VISIBLE
        ).apply {
            duration = ANIMATION_DURATION
            interpolator = DecelerateInterpolator()
            doOnStart { visible() }
            start()
        }
    }

    fun hideWithAnimation() {
        ObjectAnimator.ofFloat(
            this,
            ANIMATED_PROPERTY,
            TRANSLATION_VISIBLE,
            TRANSLATION_INVISIBLE
        ).apply {
            duration = ANIMATION_DURATION
            interpolator = AccelerateInterpolator()
            doOnEnd { invisible() }
            start()
        }
    }

    companion object {
        const val ANIMATION_DURATION = 300L
        const val ANIMATED_PROPERTY = "translationY"
        const val TRANSLATION_VISIBLE = 0f
        const val TRANSLATION_INVISIBLE = -1000f
    }
}