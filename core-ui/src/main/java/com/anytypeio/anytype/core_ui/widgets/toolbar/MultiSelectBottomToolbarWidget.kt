package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.LayoutBottomMultiSelectToolbarBinding
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible


class MultiSelectBottomToolbarWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    val binding = LayoutBottomMultiSelectToolbarBinding.inflate(
        LayoutInflater.from(context), this
    )

    var isShowing = false

    fun doneClicks() = binding.done.clicks()
    fun deleteClicks() = binding.delete.clicks()
    fun turnIntoClicks() = binding.turnInto.clicks()
    fun styleClicks() = binding.multiStyle.clicks()
    fun copyClicks() = binding.copy.clicks()

    fun enterScrollAndMove() = binding.enterScrollAndMove.clicks()
    fun applyScrollAndMoveClicks() = binding.move1.clicks()
    fun exitScrollAndMoveClicks() = binding.cancel1.clicks()

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
        binding.controls.invisible()
    }

    fun showScrollAndMoveModeControls() {
        binding.scrollandMoveControls.visible()
        binding.done.invisible()
    }

    fun hideScrollAndMoveModeControls() {
        binding.scrollandMoveControls.invisible()
        binding.done.visible()
    }

    fun update(count: Int) {
        if (count == 0) {
            binding.title.visible()
            binding.controls.invisible()
        } else {
            binding.title.invisible()
            binding.controls.visible()
        }
    }

    companion object {
        const val ANIMATION_DURATION = 100L
        const val ANIMATED_PROPERTY = "translationY"
    }
}