package com.anytypeio.anytype.core_ui.widgets.toolbar.style

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.databinding.BlockStyleToolbarBackgroundBinding
import com.anytypeio.anytype.core_ui.databinding.BlockStyleToolbarColorBinding
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.editor.editor.model.Alignment
import com.anytypeio.anytype.presentation.editor.editor.styling.StyleConfig
import com.anytypeio.anytype.presentation.editor.editor.styling.StylingEvent
import com.anytypeio.anytype.presentation.editor.editor.styling.StylingType

class StyleAdapter(
    var props: ControlPanelState.Toolbar.Styling.Props?,
    private val visibleTypes: ArrayList<StylingType>,
    private val enabledMarkup: ArrayList<Markup.Type>,
    private val enabledAlignment: ArrayList<Alignment>,
    private val onStylingEvent: (StylingEvent) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    @Deprecated("Maybe legacy, maybe not.")
    fun updateConfig(config: StyleConfig, props: ControlPanelState.Toolbar.Styling.Props?) {
        visibleTypes.clear()
        visibleTypes.addAll(config.visibleTypes)
        enabledMarkup.clear()
        enabledMarkup.addAll(config.enabledMarkup)
        enabledAlignment.clear()
        enabledAlignment.addAll(config.enabledAlignment)
        this.props = props
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
                holder.bind(onStylingEvent, props?.color)
            }
            is StyleBackgroundViewHolder -> {
                holder.bind(onStylingEvent, props?.background)
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