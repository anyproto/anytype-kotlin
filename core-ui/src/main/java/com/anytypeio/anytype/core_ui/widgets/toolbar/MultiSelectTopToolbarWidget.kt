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
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class MultiSelectTopToolbarWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    val binding = WidgetMultiSelectTopToolbarBinding.inflate(
        LayoutInflater.from(context), this
    )

    val selectText get() : TextView = binding.tvToolbarTitle
    val doneButton get() : View = binding.btnDone

    fun setTableSelectionText(count: Int, tab: BlockView.Table.Tab) = with(context) {
        selectText.text = when {
            count == 1 -> when (tab) {
                BlockView.Table.Tab.CELL -> getString(R.string.one_selected_cell)
                BlockView.Table.Tab.COLUMN -> getString(R.string.one_selected_column)
                BlockView.Table.Tab.ROW -> getString(R.string.one_selected_row)
            }
            count > 1 -> when (tab) {
                BlockView.Table.Tab.CELL -> getString(R.string.number_selected_cells, count)
                BlockView.Table.Tab.COLUMN -> getString(R.string.number_selected_columns, count)
                BlockView.Table.Tab.ROW -> getString(R.string.number_selected_rows, count)
            }
            else -> null
        }
    }

    fun setBlockSelectionText(count: Int) = with(context) {
        selectText.text = when {
            count > 1 -> getString(R.string.number_selected_blocks, count)
            count == 1 -> getString(R.string.one_selected_block)
            else -> null
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