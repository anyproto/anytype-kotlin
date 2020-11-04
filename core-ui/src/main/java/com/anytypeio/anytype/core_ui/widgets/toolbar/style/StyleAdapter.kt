package com.anytypeio.anytype.core_ui.widgets.toolbar.style

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.page.editor.Markup
import com.anytypeio.anytype.presentation.page.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.page.editor.model.Alignment
import com.anytypeio.anytype.presentation.page.editor.styling.StyleConfig
import com.anytypeio.anytype.presentation.page.editor.styling.StylingEvent
import com.anytypeio.anytype.presentation.page.editor.styling.StylingType

class StyleAdapter(
    var props: ControlPanelState.Toolbar.Styling.Props?,
    private val visibleTypes: ArrayList<StylingType>,
    private val enabledMarkup: ArrayList<Markup.Type>,
    private val enabledAlignment: ArrayList<Alignment>,
    private val onStylingEvent: (StylingEvent) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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
            StylingType.STYLE.ordinal -> StyleTextViewHolder(
                view = inflater.inflate(
                    R.layout.block_style_toolbar_style,
                    parent,
                    false
                )
            )
            StylingType.TEXT_COLOR.ordinal -> StyleTextColorViewHolder(
                view = inflater.inflate(
                    R.layout.block_style_toolbar_color,
                    parent,
                    false
                )
            )
            StylingType.BACKGROUND.ordinal -> StyleBackgroundViewHolder(
                view = inflater.inflate(
                    R.layout.block_style_toolbar_background,
                    parent,
                    false
                )
            )
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is StyleTextViewHolder -> {
                holder.bind(onStylingEvent, enabledMarkup, enabledAlignment, props)
            }
            is StyleTextColorViewHolder -> {
                holder.bind(onStylingEvent, props?.color)
            }
            is StyleBackgroundViewHolder -> {
                holder.bind(onStylingEvent, props?.background)
            }
        }
    }

    override fun getItemCount(): Int = visibleTypes.size
    override fun getItemViewType(position: Int): Int = visibleTypes[position].getViewType()
}