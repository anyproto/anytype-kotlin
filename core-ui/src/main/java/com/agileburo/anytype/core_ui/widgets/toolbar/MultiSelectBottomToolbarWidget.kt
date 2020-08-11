package com.agileburo.anytype.core_ui.widgets.toolbar

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.reactive.clicks
import com.agileburo.anytype.core_utils.ext.dimen
import com.agileburo.anytype.core_utils.ext.invisible
import com.agileburo.anytype.core_utils.ext.visible
import kotlinx.android.synthetic.main.layout_bottom_multi_select_toolbar.view.*

class MultiSelectBottomToolbarWidget : ConstraintLayout {

    constructor(
        context: Context
    ) : this(context, null)

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : this(context, attrs, 0)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        inflate()
    }

    var isShowing = false

    fun inflate() {
        LayoutInflater.from(context).inflate(R.layout.layout_bottom_multi_select_toolbar, this)
    }

    fun doneClicks() = done.clicks()
    fun deleteClicks() = delete.clicks()
    fun turnIntoClicks() = convert.clicks()

    // Temporary button usage for copying.
    fun copyClicks() = more.clicks()

    fun enterScrollAndMove() = enterScrollAndMove.clicks()
    fun applyScrollAndMoveClicks() = move1.clicks()
    fun exitScrollAndMoveClicks() = cancel1.clicks()

    fun showWithAnimation() {
        ObjectAnimator.ofFloat(this, ANIMATED_PROPERTY, 0f).apply {
            duration = ANIMATION_DURATION
            interpolator = DecelerateInterpolator()
            start()
            doOnEnd {
                isShowing = true
            }
        }
    }

    fun hideWithAnimation() {
        ObjectAnimator.ofFloat(
            this,
            ANIMATED_PROPERTY,
            context.dimen(R.dimen.default_toolbar_height)
        ).apply {
            duration = ANIMATION_DURATION
            interpolator = AccelerateInterpolator()
            start()
            doOnEnd {
                isShowing = false
            }
        }
    }

    fun hideMultiSelectControls() {
        controls.invisible()
    }

    fun showScrollAndMoveModeControls() {
        scrollandMoveControls.visible()
        done.invisible()
    }

    fun hideScrollAndMoveModeControls() {
        scrollandMoveControls.invisible()
        done.visible()
    }

    fun update(count: Int) {
        if (count == 0) {
            title.visible()
            controls.invisible()
        } else {
            title.invisible()
            controls.visible()
        }
    }

    companion object {
        const val ANIMATION_DURATION = 100L
        const val ANIMATED_PROPERTY = "translationY"
    }
}