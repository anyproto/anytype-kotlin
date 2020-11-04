package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.viewpager2.widget.ViewPager2
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.page.styling.BlockStyleAdapter
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.page.editor.control.ControlPanelState.Toolbar.Styling.Props
import com.anytypeio.anytype.presentation.page.editor.styling.StylingEvent
import com.anytypeio.anytype.presentation.page.editor.styling.StylingMode
import com.anytypeio.anytype.presentation.page.editor.styling.StylingType
import kotlinx.android.synthetic.main.widget_block_style_toolbar.view.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlin.properties.Delegates

class BlockStyleToolbarWidget : ConstraintLayout {

    var props: Props? by Delegates.observable<Props?>(null) { prop, old, new ->
        if (new != old && new != null) {
            blockStyleAdapter.props = new
            blockStyleAdapter.notifyDataSetChanged()
        }
    }

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
        setup()
    }

    private val channel = Channel<StylingEvent>()

    private val blockStyleAdapter = BlockStyleAdapter { event -> channel.offer(event) }

    val events = channel.consumeAsFlow()

    private val callback = object : ViewPager2.OnPageChangeCallback() {

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            when (position) {
                0 -> {
                    if (mode == StylingMode.MARKUP) {
                        blockStyle.isSelected = false
                        blockColor.isSelected = true
                        blockBackground.isSelected = false
                        channel.offer(StylingEvent.Sliding.Color)
                    } else {
                        blockStyle.isSelected = true
                        blockColor.isSelected = false
                        blockBackground.isSelected = false
                    }
                }
                1 -> {
                    if (mode == StylingMode.MARKUP) {
                        blockStyle.isSelected = false
                        blockColor.isSelected = false
                        blockBackground.isSelected = true
                        channel.offer(StylingEvent.Sliding.Background)
                    } else {
                        blockStyle.isSelected = false
                        blockColor.isSelected = true
                        blockBackground.isSelected = false
                    }
                }
                2 -> {
                    blockStyle.isSelected = false
                    blockColor.isSelected = false
                    blockBackground.isSelected = true
                }
            }
        }
    }

    var mode: StylingMode by Delegates.observable(StylingMode.BLOCK) { _, old, new ->
        if (new != old) {
            if (new == StylingMode.MARKUP) {
                blockStyleAdapter.applyMarkupStylingMode()
                blockColor.isSelected = true
                ConstraintSet().apply {
                    clone(context, R.layout.widget_block_style_toolbar_markup)
                    applyTo(root)
                }
            } else {
                blockStyleAdapter.applyBlockStylingMode()
                ConstraintSet().apply {
                    clone(context, R.layout.widget_block_style_toolbar)
                    applyTo(root)
                }
            }
        }
    }

    private fun inflate() {
        LayoutInflater.from(context).inflate(R.layout.widget_block_style_toolbar, this)
    }

    private fun setup() {
        pager.apply {
            adapter = blockStyleAdapter
            registerOnPageChangeCallback(callback)
        }

        blockStyle.isSelected = true

        blockStyle.setOnClickListener {
            proceedWithOpeningBlockStylePage()
        }

        blockColor.setOnClickListener {
            proceedWithOpeningTextColorPage()
        }

        blockBackground.setOnClickListener {
            proceedWithOpeningBlockBackgroundPage()
        }
    }

    fun applyStylingType(type: StylingType) {
        when (type) {
            StylingType.STYLE -> proceedWithOpeningBlockStylePage(smoothScroll = false)
            StylingType.TEXT_COLOR -> proceedWithOpeningTextColorPage(smoothScroll = false)
            StylingType.BACKGROUND -> proceedWithOpeningBlockBackgroundPage(smoothScroll = false)
        }
    }

    fun showWithAnimation() {
        ObjectAnimator.ofFloat(this, ANIMATED_PROPERTY, 0f).apply {
            duration = ANIMATION_DURATION
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    fun hideWithAnimation() {
        ObjectAnimator.ofFloat(this, ANIMATED_PROPERTY, context.dimen(R.dimen.dp_203)).apply {
            duration = ANIMATION_DURATION
            interpolator = AccelerateInterpolator()
            start()
        }
    }

    private fun proceedWithOpeningBlockBackgroundPage(smoothScroll: Boolean = true) {
        if (mode == StylingMode.MARKUP)
            pager.setCurrentItem(PAGE_BACKGROUND_INDEX, smoothScroll)
        else
            pager.setCurrentItem(PAGE_BACKGROUND_INDEX, smoothScroll)
    }

    private fun proceedWithOpeningBlockStylePage(smoothScroll: Boolean = true) {
        pager.setCurrentItem(PAGE_STYLE_INDEX, smoothScroll)
    }

    private fun proceedWithOpeningTextColorPage(smoothScroll: Boolean = true) {
        if (mode == StylingMode.MARKUP)
            pager.setCurrentItem(PAGE_COLOR_INDEX.dec(), smoothScroll)
        else
            pager.setCurrentItem(PAGE_COLOR_INDEX, smoothScroll)
    }

    fun closeButtonClicks() = close.clicks()

    companion object {
        const val PAGE_STYLE_INDEX = 0
        const val PAGE_COLOR_INDEX = 1
        const val PAGE_BACKGROUND_INDEX = 2

        const val ANIMATION_DURATION = 100L
        const val ANIMATED_PROPERTY = "translationY"
    }
}