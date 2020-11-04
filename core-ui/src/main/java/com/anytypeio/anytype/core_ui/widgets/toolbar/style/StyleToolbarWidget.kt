package com.anytypeio.anytype.core_ui.widgets.toolbar.style

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_ui.widgets.toolbar.BlockStyleToolbarWidget
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.page.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.page.editor.styling.StyleConfig
import com.anytypeio.anytype.presentation.page.editor.styling.StylingEvent
import com.anytypeio.anytype.presentation.page.editor.styling.StylingMode
import com.anytypeio.anytype.presentation.page.editor.styling.StylingType
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.widget_block_style_toolbar_new.view.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import timber.log.Timber
import kotlin.properties.Delegates

class StyleToolbarWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val channel = Channel<StylingEvent>()

    private val blockStyleAdapter = StyleAdapter(
        props = null,
        visibleTypes = arrayListOf(),
        enabledAlignment = arrayListOf(),
        enabledMarkup = arrayListOf()
    ) { event ->
        Timber.d("Styling Event : $event")
        channel.offer(event)
    }

    val events = channel.consumeAsFlow()

    var mode: StylingMode by Delegates.observable(StylingMode.BLOCK) { _, _, _ -> }

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_block_style_toolbar_new, this)
        setup()
    }

    private fun setup() {
        pager.adapter = blockStyleAdapter
        TabLayoutMediator(tabLayout, pager) { tab, position ->
            val viewType = blockStyleAdapter.getItemViewType(position)
            val customView = LayoutInflater.from(context).inflate(R.layout.tab_item_style_toolbar, null)
            (customView.rootView as TextView).text = getTabTitle(viewType)
            tab.customView = customView
        }.attach()
    }

    fun update(config: StyleConfig, props: ControlPanelState.Toolbar.Styling.Props?) {
        blockStyleAdapter.updateConfig(config, props)
        blockStyleAdapter.notifyDataSetChanged()
    }

    fun showWithAnimation() {
        ObjectAnimator.ofFloat(this, BlockStyleToolbarWidget.ANIMATED_PROPERTY, 0f).apply {
            duration = BlockStyleToolbarWidget.ANIMATION_DURATION
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    fun hideWithAnimation() {
        ObjectAnimator.ofFloat(
            this,
            BlockStyleToolbarWidget.ANIMATED_PROPERTY,
            context.dimen(com.anytypeio.anytype.core_ui.R.dimen.dp_203)
        ).apply {
            duration = BlockStyleToolbarWidget.ANIMATION_DURATION
            interpolator = AccelerateInterpolator()
            start()
        }
    }

    private fun getTabTitle(viewType: Int) =
        when (viewType) {
            StylingType.STYLE.ordinal -> context.getString(R.string.text)
            StylingType.TEXT_COLOR.ordinal -> context.getString(R.string.color)
            StylingType.BACKGROUND.ordinal -> context.getString(R.string.background)
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }

    fun closeButtonClicks() = close.clicks()
}