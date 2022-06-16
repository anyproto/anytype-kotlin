package com.anytypeio.anytype.core_ui.features.editor.decoration

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.veryLight
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class EditorDecorationContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val graphicOffsetValue =
        resources.getDimension(R.dimen.default_graphic_container_width).toInt()
    private val graphicOffsetValueExtra =
        resources.getDimension(R.dimen.default_graphic_container_right_offset).toInt()
    private val defaultIndentOffset = resources.getDimension(R.dimen.default_indent).toInt()
    private val highlightBottomOffset =
        resources.getDimension(R.dimen.default_highlight_content_margin_top).toInt()
    private val totalGraphicOffset =
        (defaultIndentOffset + graphicOffsetValue + graphicOffsetValueExtra)
    private val defaultHeaderOneExtraSpaceTop =
        resources.getDimension(R.dimen.default_header_one_extra_space_top)
            .toInt()
    private val defaultHeaderOneExtraSpaceBottom =
        resources.getDimension(R.dimen.default_header_one_extra_space_bottom)
            .toInt()
    private val defaultHeaderTwoExtraSpaceTop =
        resources.getDimension(R.dimen.default_header_two_extra_space_top)
            .toInt()
    private val defaultHeaderTwoExtraSpaceBottom =
        resources.getDimension(R.dimen.default_header_two_extra_space_bottom)
            .toInt()
    private val defaultHeaderThreeExtraSpaceTop =
        resources.getDimension(R.dimen.default_header_three_extra_space_top)
            .toInt()
    private val defaultHeaderThreeExtraSpaceBottom =
        resources.getDimension(R.dimen.default_header_three_extra_space_bottom)
            .toInt()
    private val defaultGraphicContainerWidth = resources.getDimensionPixelSize(R.dimen.default_graphic_container_width)

    fun decorate(
        decorations: List<BlockView.Decoration>,
        content: View? = null
    ) {
        if (childCount > 0) removeAllViews()
        var bottomOffset = 0
        var topOffset = 0
        var isPreviousHighlight = false
        var previousOffset = 0

        decorations.forEachIndexed { indent, decor ->

            when (indent) {
                0 -> {
                    previousOffset = 0
                }
                else -> {
                    previousOffset += if (isPreviousHighlight) {
                        totalGraphicOffset
                    } else {
                        defaultIndentOffset
                    }
                }
            }

            // Drawing background

            if (decor.background != ThemeColor.DEFAULT) {
                val lp = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                ).apply {

                    marginStart = previousOffset

                    // Offsets for extra space related to certain styles.

                    if (indent == decorations.lastIndex) {
                        when (decor.style) {
                            BlockView.Decoration.Style.Header.H1 -> {
                                topMargin = defaultHeaderOneExtraSpaceTop
                                bottomOffset += defaultHeaderOneExtraSpaceBottom
                            }
                            BlockView.Decoration.Style.Header.H2 -> {
                                topMargin = defaultHeaderTwoExtraSpaceTop
                                bottomOffset += defaultHeaderTwoExtraSpaceBottom
                            }
                            BlockView.Decoration.Style.Header.H3 -> {
                                topMargin = defaultHeaderThreeExtraSpaceTop
                                bottomOffset += defaultHeaderThreeExtraSpaceBottom
                            }
                            else -> {
                                // Do nothing
                            }
                        }
                    }

                    bottomMargin = bottomOffset
                }

                addView(
                    DecorationWidget.Background(
                        context = context,
                        background = resources.veryLight(decor.background, 0)
                    ),
                    lp
                )
            }

            // Drawing highlight line inside box

            if (decor.style is BlockView.Decoration.Style.Highlight) {
                if (decor.style is BlockView.Decoration.Style.Highlight.End) {
                    bottomOffset += highlightBottomOffset
                }
                val highlight = DecorationWidget.Highlight(context = context)
                val lm = LayoutParams(
                    defaultGraphicContainerWidth,
                    LayoutParams.MATCH_PARENT
                ).apply {
                    marginStart = when (indent) {
                        0 -> {
                            defaultIndentOffset
                        }
                        else -> {
                            previousOffset + defaultIndentOffset
                        }
                    }
                    bottomMargin = bottomOffset
                }

                addView(
                    highlight,
                    lm
                )
                isPreviousHighlight = true
            } else {
                isPreviousHighlight = false
            }

            // Optional offset for content
            // TODO this code will be removed
            // Instead of applying margin here, return offset. ViewGroup, containing content, should handle this offset.
            if (indent == decorations.lastIndex) {
                content?.updateLayoutParams<LayoutParams> {
                    bottomMargin = bottomOffset
                }
            }
        }
    }
}