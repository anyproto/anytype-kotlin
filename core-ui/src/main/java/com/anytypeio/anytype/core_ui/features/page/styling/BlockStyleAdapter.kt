package com.anytypeio.anytype.core_ui.features.page.styling

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.page.editor.ThemeColor
import com.anytypeio.anytype.presentation.page.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.page.editor.model.Alignment.*
import com.anytypeio.anytype.presentation.page.editor.styling.StylingEvent
import com.anytypeio.anytype.presentation.page.editor.styling.StylingEvent.*
import com.anytypeio.anytype.presentation.page.editor.styling.StylingType
import com.anytypeio.anytype.presentation.page.editor.styling.StylingType.*
import kotlinx.android.synthetic.main.block_style_toolbar_background.view.*
import kotlinx.android.synthetic.main.block_style_toolbar_color.view.*
import kotlinx.android.synthetic.main.block_style_toolbar_style.view.*
import timber.log.Timber

class BlockStyleAdapter(
    var props: ControlPanelState.Toolbar.Styling.Props? = null,
    private var pages: List<StylingType> = listOf(STYLE, TEXT_COLOR, BACKGROUND),
    private val onStylingEvent: (StylingEvent) -> Unit
) : RecyclerView.Adapter<BlockStyleAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            STYLE.ordinal -> ViewHolder.StyleViewHolder(
                view = inflater.inflate(
                    R.layout.block_style_toolbar_style,
                    parent,
                    false
                )
            )
            TEXT_COLOR.ordinal -> ViewHolder.ColorViewHolder(
                view = inflater.inflate(
                    R.layout.block_style_toolbar_color,
                    parent,
                    false
                )
            )
            BACKGROUND.ordinal -> ViewHolder.BackgroundViewHolder(
                view = inflater.inflate(
                    R.layout.block_style_toolbar_background,
                    parent,
                    false
                )
            )
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun getItemCount(): Int = pages.size
    override fun getItemViewType(position: Int): Int = pages[position].getViewType()
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(onStylingEvent, props)

    fun applyMarkupStylingMode() {
        Timber.d("Applying markup mode")
        pages = listOf(TEXT_COLOR, BACKGROUND)
        notifyDataSetChanged()
    }

    fun applyBlockStylingMode() {
        Timber.d("Applying block mode")
        pages = listOf(STYLE, TEXT_COLOR, BACKGROUND)
        notifyDataSetChanged()
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        abstract fun bind(
            onStylingEvent: (StylingEvent) -> Unit,
            target: ControlPanelState.Toolbar.Styling.Props? = null
        )

        class StyleViewHolder(view: View) : ViewHolder(view) {

            private val bold = itemView.bold
            private val italic = itemView.italic
            private val strike = itemView.strikethrough
            private val code = itemView.code
            private val link = itemView.setUrlButton
            private val left = itemView.alignmentLeft
            private val middle = itemView.alignmentMiddle
            private val right = itemView.alignmentRight

            override fun bind(
                onStylingEvent: (StylingEvent) -> Unit,
                target: ControlPanelState.Toolbar.Styling.Props?
            ) {

                left.isSelected = target?.alignment == null || target.alignment == START
                middle.isSelected = target?.alignment == CENTER
                right.isSelected = target?.alignment == END
                code.isSelected = target?.isCode ?: false
                bold.isSelected = target?.isBold ?: false
                italic.isSelected = target?.isItalic ?: false
                strike.isSelected = target?.isStrikethrough ?: false
                link.isSelected = target?.isLinked ?: false

                bold.setOnClickListener {
                    onStylingEvent(Markup.Bold)
                }

                italic.setOnClickListener {
                    onStylingEvent(Markup.Italic)
                }

                strike.setOnClickListener {
                    onStylingEvent(Markup.StrikeThrough)
                }

                code.setOnClickListener {
                    onStylingEvent(Markup.Code)
                }

                link.setOnClickListener {
                    onStylingEvent(Markup.Link)
                }

                left.setOnClickListener {
                    onStylingEvent(Alignment.Left)
                }

                middle.setOnClickListener {
                    onStylingEvent(Alignment.Center)
                }

                right.setOnClickListener {
                    onStylingEvent(Alignment.Right)
                }
            }
        }

        class ColorViewHolder(view: View) : ViewHolder(view) {

            private val default = itemView.textColorDefault
            private val grey = itemView.textColorGrey
            private val yellow = itemView.textColorYellow
            private val orange = itemView.textColorOrange
            private val red = itemView.textColorRed
            private val pink = itemView.textColorPink
            private val purple = itemView.textColorPurple
            private val blue = itemView.textColorBlue
            private val ice = itemView.textColorIce
            private val teal = itemView.textColorTeal
            private val green = itemView.textColorGreen

            override fun bind(
                onStylingEvent: (StylingEvent) -> Unit,
                target: ControlPanelState.Toolbar.Styling.Props?
            ) {
                default.isSelected = target?.color == ThemeColor.DEFAULT.title
                grey.isSelected = target?.color == ThemeColor.GREY.title
                yellow.isSelected = target?.color == ThemeColor.YELLOW.title
                orange.isSelected = target?.color == ThemeColor.ORANGE.title
                red.isSelected = target?.color == ThemeColor.RED.title
                pink.isSelected = target?.color == ThemeColor.PINK.title
                purple.isSelected = target?.color == ThemeColor.PURPLE.title
                blue.isSelected = target?.color == ThemeColor.BLUE.title
                ice.isSelected = target?.color == ThemeColor.ICE.title
                teal.isSelected = target?.color == ThemeColor.TEAL.title
                green.isSelected = target?.color == ThemeColor.GREEN.title

                default.setOnClickListener {
                    onStylingEvent(Coloring.Text(color = ThemeColor.DEFAULT))
                }
                grey.setOnClickListener {
                    onStylingEvent(Coloring.Text(color = ThemeColor.GREY))
                }
                yellow.setOnClickListener {
                    onStylingEvent(Coloring.Text(color = ThemeColor.YELLOW))
                }
                orange.setOnClickListener {
                    onStylingEvent(Coloring.Text(color = ThemeColor.ORANGE))
                }
                red.setOnClickListener {
                    onStylingEvent(Coloring.Text(color = ThemeColor.RED))
                }
                pink.setOnClickListener {
                    onStylingEvent(Coloring.Text(color = ThemeColor.PINK))
                }
                purple.setOnClickListener {
                    onStylingEvent(Coloring.Text(color = ThemeColor.PURPLE))
                }
                blue.setOnClickListener {
                    onStylingEvent(Coloring.Text(color = ThemeColor.BLUE))
                }
                ice.setOnClickListener {
                    onStylingEvent(Coloring.Text(color = ThemeColor.ICE))
                }
                teal.setOnClickListener {
                    onStylingEvent(Coloring.Text(color = ThemeColor.TEAL))
                }
                green.setOnClickListener {
                    onStylingEvent(Coloring.Text(color = ThemeColor.GREEN))
                }
            }
        }

        class BackgroundViewHolder(view: View) : ViewHolder(view) {

            private val default = itemView.backgroundColorDefault
            private val grey = itemView.backgroundColorGrey
            private val yellow = itemView.backgroundColorYellow
            private val orange = itemView.backgroundColorOrange
            private val red = itemView.backgroundColorRed
            private val pink = itemView.backgroundColorPink
            private val purple = itemView.backgroundColorPurple
            private val blue = itemView.backgroundColorBlue
            private val ice = itemView.backgroundColorIce
            private val teal = itemView.backgroundColorTeal
            private val green = itemView.backgroundColorGreen

            override fun bind(
                onStylingEvent: (StylingEvent) -> Unit,
                target: ControlPanelState.Toolbar.Styling.Props?
            ) {

                default.isSelected = target?.background == ThemeColor.DEFAULT.title
                grey.isSelected = target?.background == ThemeColor.GREY.title
                yellow.isSelected = target?.background == ThemeColor.YELLOW.title
                orange.isSelected = target?.background == ThemeColor.ORANGE.title
                red.isSelected = target?.background == ThemeColor.RED.title
                pink.isSelected = target?.background == ThemeColor.PINK.title
                purple.isSelected = target?.background == ThemeColor.PURPLE.title
                blue.isSelected = target?.background == ThemeColor.BLUE.title
                ice.isSelected = target?.background == ThemeColor.ICE.title
                teal.isSelected = target?.background == ThemeColor.TEAL.title
                green.isSelected = target?.background == ThemeColor.GREEN.title

                default.setOnClickListener {
                    onStylingEvent(Coloring.Background(color = ThemeColor.DEFAULT))
                }
                grey.setOnClickListener {
                    onStylingEvent(Coloring.Background(color = ThemeColor.GREY))
                }
                yellow.setOnClickListener {
                    onStylingEvent(Coloring.Background(color = ThemeColor.YELLOW))
                }
                orange.setOnClickListener {
                    onStylingEvent(Coloring.Background(color = ThemeColor.ORANGE))
                }
                red.setOnClickListener {
                    onStylingEvent(Coloring.Background(color = ThemeColor.RED))
                }
                pink.setOnClickListener {
                    onStylingEvent(Coloring.Background(color = ThemeColor.PINK))
                }
                purple.setOnClickListener {
                    onStylingEvent(Coloring.Background(color = ThemeColor.PURPLE))
                }
                blue.setOnClickListener {
                    onStylingEvent(Coloring.Background(color = ThemeColor.BLUE))
                }
                ice.setOnClickListener {
                    onStylingEvent(Coloring.Background(color = ThemeColor.ICE))
                }
                teal.setOnClickListener {
                    onStylingEvent(Coloring.Background(color = ThemeColor.TEAL))
                }
                green.setOnClickListener {
                    onStylingEvent(Coloring.Background(color = ThemeColor.GREEN))
                }
            }
        }
    }
}