package com.agileburo.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.extensions.drawable
import com.agileburo.anytype.core_ui.layout.SpacingItemDecoration
import com.agileburo.anytype.core_ui.widgets.toolbar.ColorToolbarWidget.BackgroundColorAdapter
import com.agileburo.anytype.core_ui.widgets.toolbar.ColorToolbarWidget.TextColorAdapter
import kotlinx.android.synthetic.main.item_toolbar_text_color.view.*
import kotlinx.android.synthetic.main.widget_color_toolbar.view.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow

/**
 * This toolbar widget provides user with the ability to color block content.
 * User can color a text or hightlight a text inside some block.
 * Available background and text colors are rendered as scrollable lists.
 * @see TextColorAdapter
 * @see BackgroundColorAdapter
 */
class ColorToolbarWidget : LinearLayout {

    var state: State = State.IDLE

    private val channel = Channel<Click>()

    sealed class Click {
        data class OnTextColorClicked(val color: Int) : Click()
        data class OnBackgroundColorClicked(val color: Int) : Click()
    }

    fun observeClicks(): Flow<Click> = channel.consumeAsFlow()

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
        orientation = VERTICAL
        setBackgroundResource(R.color.default_bottom_detail_toolbar_background_color)
        setupAdapters()
    }

    private fun inflate() {
        LayoutInflater.from(context).inflate(R.layout.widget_color_toolbar, this)
    }

    private fun setupAdapters() {

        val decoration = DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL)

        val divider = context.drawable(R.drawable.rectangle_color_toolbar_horizontal_divider)

        val spacing = SpacingItemDecoration(
            firstItemSpacingStart = context
                .resources
                .getDimension(R.dimen.default_toolbar_color_item_spacing_first)
                .toInt(),
            lastItemSpacingEnd = context
                .resources
                .getDimension(R.dimen.default_toolbar_color_item_spacing_last)
                .toInt()
        )

        decoration.setDrawable(divider)

        textColorRecycler.apply {
            setHasFixedSize(true)

            layoutManager = provideLayoutManager()

            addItemDecoration(spacing)

            addItemDecoration(decoration)

            adapter = TextColorAdapter(
                onTextColorClicked = { channel.sendBlocking(Click.OnTextColorClicked(it)) },
                colors = resources.getIntArray(R.array.toolbar_color_text_colors).toList()
            )
        }

        backgroundColorRecycler.apply {

            setHasFixedSize(true)

            layoutManager = provideLayoutManager()

            addItemDecoration(spacing)

            addItemDecoration(decoration)

            adapter = BackgroundColorAdapter(
                onBackgroundColorClicked = { channel.sendBlocking(Click.OnBackgroundColorClicked(it)) },
                colors = resources.getIntArray(R.array.toolbar_color_background_colours).toList()
            )
        }
    }

    private fun provideLayoutManager(): LinearLayoutManager {
        return LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
    }

    /**
     * Adapter for rendering list of avalaible text colors.
     * @property colors immutable list of colors represented by integers.
     */
    class TextColorAdapter(
        private val colors: List<Int>,
        private val onTextColorClicked: (Int) -> Unit
    ) : RecyclerView.Adapter<TextColorAdapter.ViewHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup, viewType: Int
        ) = ViewHolder(
            view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_toolbar_text_color, parent, false)
        )

        override fun getItemCount() = colors.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(
                color = colors[position],
                isFirst = position == 0,
                isLast = position == colors.lastIndex,
                onTextColorClicked = onTextColorClicked
            )
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            private val placeholder = itemView.placeholder

            fun bind(
                color: Int,
                isFirst: Boolean,
                isLast: Boolean,
                onTextColorClicked: (Int) -> Unit
            ) {
                placeholder.setTextColor(color)
                setBackground(isFirst, isLast)
                itemView.setOnClickListener { onTextColorClicked(color) }
            }

            private fun setBackground(isFirst: Boolean, isLast: Boolean) {
                when {
                    isFirst -> {
                        itemView
                            .setBackgroundResource(R.drawable.rectangle_toolbar_color_first_item)
                    }
                    isLast -> {
                        itemView
                            .setBackgroundResource(R.drawable.rectangle_toolbar_color_last_item)
                    }
                    else -> {
                        itemView
                            .setBackgroundResource(R.drawable.rectangle_toolbar_color_default_item)
                    }
                }
            }
        }
    }

    /**
     * Adapter for rendering list of avalaible colors to highlight some text.
     * @property colors immutable list of background colors represented by integers.
     */
    class BackgroundColorAdapter(
        private val colors: List<Int>,
        private val onBackgroundColorClicked: (Int) -> Unit
    ) : RecyclerView.Adapter<BackgroundColorAdapter.ViewHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup, viewType: Int
        ) = ViewHolder(
            view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_toolbar_background_color, parent, false)
        )

        override fun getItemCount() = colors.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(
                color = colors[position],
                isFirst = position == 0,
                isLast = position == colors.lastIndex,
                onBackgroundColorClicked = onBackgroundColorClicked
            )
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            private val placeholder = itemView.placeholder

            fun bind(
                color: Int,
                isFirst: Boolean,
                isLast: Boolean,
                onBackgroundColorClicked: (Int) -> Unit
            ) {
                placeholder.backgroundTintList = ColorStateList.valueOf(color)
                setBackground(isFirst, isLast)
                itemView.setOnClickListener { onBackgroundColorClicked(color) }
            }

            private fun setBackground(isFirst: Boolean, isLast: Boolean) {
                when {
                    isFirst -> {
                        itemView
                            .setBackgroundResource(R.drawable.rectangle_toolbar_color_first_item)
                    }
                    isLast -> {
                        itemView
                            .setBackgroundResource(R.drawable.rectangle_toolbar_color_last_item)
                    }
                    else -> {
                        itemView
                            .setBackgroundResource(R.drawable.rectangle_toolbar_color_default_item)
                    }
                }
            }
        }
    }

    enum class State { IDLE, BLOCK, SELECTION }
}