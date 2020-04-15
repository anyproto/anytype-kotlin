package com.agileburo.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.features.page.styling.BlockStyleAdapter
import kotlinx.android.synthetic.main.widget_block_style_toolbar.view.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow

class BlockStyleToolbarWidget : ConstraintLayout {

    private val channel = Channel<BlockStyleAdapter.StylingEvent>()

    val events = channel.consumeAsFlow()

    private val callback = object : ViewPager2.OnPageChangeCallback() {

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            when (position) {
                PAGE_STYLE_INDEX -> {
                    blockStyle.isSelected = true
                    blockBackground.isSelected = false
                    blockColor.isSelected = false
                }
                PAGE_COLOR_INDEX -> {
                    blockStyle.isSelected = false
                    blockBackground.isSelected = false
                    blockColor.isSelected = true
                }
                PAGE_BACKGROUND_INDEX -> {
                    blockStyle.isSelected = false
                    blockBackground.isSelected = true
                    blockColor.isSelected = false
                }
            }
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

    private fun inflate() {
        LayoutInflater.from(context).inflate(R.layout.widget_block_style_toolbar, this)
    }

    private fun setup() {
        pager.apply {
            adapter = BlockStyleAdapter { event -> channel.offer(event) }
            registerOnPageChangeCallback(callback)
        }

        blockStyle.isSelected = true

        blockStyle.setOnClickListener {
            pager.setCurrentItem(PAGE_STYLE_INDEX, true)
        }

        blockColor.setOnClickListener {
            pager.setCurrentItem(PAGE_COLOR_INDEX, true)
        }

        blockBackground.setOnClickListener {
            pager.setCurrentItem(PAGE_BACKGROUND_INDEX, true)
        }
    }

    companion object {
        const val PAGE_STYLE_INDEX = 0
        const val PAGE_COLOR_INDEX = 1
        const val PAGE_BACKGROUND_INDEX = 2
    }
}