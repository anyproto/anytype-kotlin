package com.anytypeio.anytype.core_ui.widgets.toolbar.style

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.databinding.BlockStyleToolbarBackgroundBinding
import com.anytypeio.anytype.core_ui.databinding.BlockStyleToolbarColorBinding
import com.anytypeio.anytype.presentation.editor.editor.styling.StyleToolbarState
import com.anytypeio.anytype.presentation.editor.editor.styling.StylingEvent

class StyleAdapter(
    private var state: StyleToolbarState.ColorBackground,
    private val onStylingEvent: (StylingEvent) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun update(state: StyleToolbarState.ColorBackground) {
        this.state = state
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            HOLDER_TEXT_COLOR -> StyleTextColorViewHolder(
                binding = BlockStyleToolbarColorBinding.inflate(
                    inflater, parent, false
                )
            )
            HOLDER_BACKGROUND_COLOR -> StyleBackgroundViewHolder(
                binding = BlockStyleToolbarBackgroundBinding.inflate(
                    inflater, parent, false
                )
            )
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is StyleTextColorViewHolder -> {
                holder.bind(onStylingEvent, state.color)
            }
            is StyleBackgroundViewHolder -> {
                holder.bind(onStylingEvent, state.background)
            }
        }
    }

    override fun getItemCount(): Int = 2
    override fun getItemViewType(position: Int): Int = position

    companion object {
        const val HOLDER_TEXT_COLOR = 0
        const val HOLDER_BACKGROUND_COLOR = 1
    }
}