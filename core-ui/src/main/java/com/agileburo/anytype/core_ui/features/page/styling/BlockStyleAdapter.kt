package com.agileburo.anytype.core_ui.features.page.styling

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.Color
import com.agileburo.anytype.core_ui.common.ViewType
import com.agileburo.anytype.core_ui.features.page.styling.BlockStyleAdapter.Page.*
import kotlinx.android.synthetic.main.block_style_toolbar_background.view.*
import kotlinx.android.synthetic.main.block_style_toolbar_color.view.*
import kotlinx.android.synthetic.main.block_style_toolbar_style.view.*

class BlockStyleAdapter(
    private val pages: List<Page> = listOf(STYLE, COLOR, BACKGROUND),
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
            COLOR.ordinal -> ViewHolder.ColorViewHolder(
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
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(onStylingEvent)

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        abstract fun bind(onStylingEvent: (StylingEvent) -> Unit)

        class StyleViewHolder(view: View) : ViewHolder(view) {

            private val bold = itemView.bold
            private val italic = itemView.italic
            private val strike = itemView.strikethrough
            private val code = itemView.code
            private val left = itemView.alignmentLeft
            private val middle = itemView.alignmentMiddle
            private val right = itemView.alignmentRight

            override fun bind(onStylingEvent: (StylingEvent) -> Unit) {
                bold.setOnClickListener {
                    onStylingEvent(StylingEvent.Markup.Bold)
                }

                italic.setOnClickListener {
                    onStylingEvent(StylingEvent.Markup.Italic)
                }

                strike.setOnClickListener {
                    onStylingEvent(StylingEvent.Markup.Strikethrough)
                }

                code.setOnClickListener {
                    onStylingEvent(StylingEvent.Markup.Code)
                }

                left.setOnClickListener {
                    onStylingEvent(StylingEvent.Alignment.Left)
                }

                middle.setOnClickListener {
                    onStylingEvent(StylingEvent.Alignment.Center)
                }

                right.setOnClickListener {
                    onStylingEvent(StylingEvent.Alignment.Right)
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

            override fun bind(onStylingEvent: (StylingEvent) -> Unit) {
                default.setOnClickListener {
                    onStylingEvent(StylingEvent.TextColor(color = Color.DEFAULT))
                }
                grey.setOnClickListener {
                    onStylingEvent(StylingEvent.TextColor(color = Color.GREY))
                }
                yellow.setOnClickListener {
                    onStylingEvent(StylingEvent.TextColor(color = Color.YELLOW))
                }
                orange.setOnClickListener {
                    onStylingEvent(StylingEvent.TextColor(color = Color.ORANGE))
                }
                red.setOnClickListener {
                    onStylingEvent(StylingEvent.TextColor(color = Color.RED))
                }
                pink.setOnClickListener {
                    onStylingEvent(StylingEvent.TextColor(color = Color.PINK))
                }
                purple.setOnClickListener {
                    onStylingEvent(StylingEvent.TextColor(color = Color.PURPLE))
                }
                blue.setOnClickListener {
                    onStylingEvent(StylingEvent.TextColor(color = Color.BLUE))
                }
                ice.setOnClickListener {
                    onStylingEvent(StylingEvent.TextColor(color = Color.ICE))
                }
                teal.setOnClickListener {
                    onStylingEvent(StylingEvent.TextColor(color = Color.TEAL))
                }
                green.setOnClickListener {
                    onStylingEvent(StylingEvent.TextColor(color = Color.GREEN))
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

            override fun bind(onStylingEvent: (StylingEvent) -> Unit) {
                default.setOnClickListener {
                    onStylingEvent(StylingEvent.BackgroundColor(color = Color.DEFAULT))
                }
                grey.setOnClickListener {
                    onStylingEvent(StylingEvent.BackgroundColor(color = Color.GREY))
                }
                yellow.setOnClickListener {
                    onStylingEvent(StylingEvent.BackgroundColor(color = Color.YELLOW))
                }
                orange.setOnClickListener {
                    onStylingEvent(StylingEvent.BackgroundColor(color = Color.ORANGE))
                }
                red.setOnClickListener {
                    onStylingEvent(StylingEvent.BackgroundColor(color = Color.RED))
                }
                pink.setOnClickListener {
                    onStylingEvent(StylingEvent.BackgroundColor(color = Color.PINK))
                }
                purple.setOnClickListener {
                    onStylingEvent(StylingEvent.BackgroundColor(color = Color.PURPLE))
                }
                blue.setOnClickListener {
                    onStylingEvent(StylingEvent.BackgroundColor(color = Color.BLUE))
                }
                ice.setOnClickListener {
                    onStylingEvent(StylingEvent.BackgroundColor(color = Color.ICE))
                }
                teal.setOnClickListener {
                    onStylingEvent(StylingEvent.BackgroundColor(color = Color.TEAL))
                }
                green.setOnClickListener {
                    onStylingEvent(StylingEvent.BackgroundColor(color = Color.GREEN))
                }
            }
        }
    }

    enum class Page : ViewType {
        STYLE {
            override fun getViewType(): Int = ordinal
        },
        COLOR {
            override fun getViewType(): Int = ordinal
        },
        BACKGROUND {
            override fun getViewType(): Int = ordinal
        }
    }

    sealed class StylingEvent {

        sealed class Alignment : StylingEvent() {
            object Left : Alignment()
            object Center : Alignment()
            object Right : Alignment()
        }

        sealed class Markup : StylingEvent() {
            object Bold : Markup()
            object Italic : Markup()
            object Strikethrough : Markup()
            object Code : Markup()
        }

        data class TextColor(val color: Color) : StylingEvent()
        data class BackgroundColor(val color: Color) : StylingEvent()
    }
}