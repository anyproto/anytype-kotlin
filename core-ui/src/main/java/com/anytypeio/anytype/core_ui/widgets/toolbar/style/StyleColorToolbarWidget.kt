package com.anytypeio.anytype.core_ui.widgets.toolbar.style

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.editor.editor.styling.StyleConfig
import com.anytypeio.anytype.presentation.editor.editor.styling.StylingEvent
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.widget_block_style_toolbar_new.view.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import timber.log.Timber

class StyleColorToolbarWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CardView(context, attrs) {

    private val channel = Channel<StylingEvent>()

    private val blockStyleAdapter = StyleAdapter(
        props = null,
        visibleTypes = arrayListOf(),
        enabledAlignment = arrayListOf(),
        enabledMarkup = arrayListOf()
    ) { event ->
        Timber.d("Styling Event : $event")
        channel.trySend(event)
    }

    val events = channel.consumeAsFlow()

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_block_style_toolbar_colors, this)
        setup()
    }

    private fun setup() {
        val inflater = LayoutInflater.from(context)
        pager.adapter = blockStyleAdapter
        TabLayoutMediator(tabLayout, pager) { tab, position ->
            tab.customView = inflater.inflate(R.layout.tab_item_style_toolbar, null).apply {
                rootView.findViewById<TextView>(R.id.tabText).text = when (position) {
                    0 -> context.getString(R.string.color)
                    1 -> context.getString(R.string.background)
                    else -> throw IllegalStateException("Unexpected position: $position")
                }
            }
        }.attach()
    }

    fun update(config: StyleConfig, props: ControlPanelState.Toolbar.Styling.Props?) {
        blockStyleAdapter.updateConfig(config, props)
        blockStyleAdapter.notifyDataSetChanged()
    }
}