package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.animation.doOnEnd
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.WidgetMultiSelectTopToolbarBinding
import com.anytypeio.anytype.core_utils.ext.dimen

class MultiSelectTopToolbarWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    val binding = WidgetMultiSelectTopToolbarBinding.inflate(
        LayoutInflater.from(context), this
    )

    val selectText get() : TextView = binding.tvToolbarTitle
    val doneButton get() : View = binding.btnDone

    fun setCellSelectionText(count: Int) {
        when {
            count == 1 -> {
                selectText.text = context.getString(R.string.one_selected_cell)
            }
            count > 1 -> {
                selectText.text = context.getString(R.string.number_selected_cells, count)
            }
            else -> {
                selectText.text = null
            }
        }
    }

    fun showWithAnimation() {
        if (translationY < 0) {
            ObjectAnimator.ofFloat(
                this,
                SELECT_BUTTON_ANIMATION_PROPERTY,
                0f
            ).apply {
                duration = SELECT_BUTTON_ANIMATION_DURATION
                interpolator = DecelerateInterpolator()
                start()
            }
        }
    }

    fun hideWithAnimation(action: () -> Unit) {
        if (translationY >= 0) {
            ObjectAnimator.ofFloat(
                this,
                SELECT_BUTTON_ANIMATION_PROPERTY,
                -context.dimen(R.dimen.dp_48)
            ).apply {
                duration = SELECT_BUTTON_ANIMATION_DURATION
                interpolator = DecelerateInterpolator()
                doOnEnd { action.invoke() }
                start()
            }
        }
    }

    companion object {
        const val SELECT_BUTTON_ANIMATION_PROPERTY = "translationY"
        const val SELECT_BUTTON_ANIMATION_DURATION = 200L
    }
}