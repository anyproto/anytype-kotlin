package com.anytypeio.anytype.core_ui.features.editor.decoration

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.FrameLayout
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.veryLight
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Decoration

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
    private val defaultGraphicContainerWidth = resources.getDimensionPixelSize(R.dimen.default_graphic_container_width)

    private val calloutInternalBottomPadding = resources.getDimension(R.dimen.default_callout_internal_bottom_padding).toInt()
    private val calloutBackgroundOffset = resources.getDimension(R.dimen.callout_background_offset)
    private val calloutExtraSpaceBottom = resources.getDimension(R.dimen.callout_block_extra_space_bottom).toInt()
    private val totalCalloutOffset = totalGraphicOffset
    private val defaultCalloutColor = resources.getColor(R.color.palette_very_light_grey, null)

    fun decorate(
        decorations: List<Decoration>,
        onApplyContentOffset: ((rect: Rect) -> Unit)? = null
    ) {
        if (childCount > 0) removeAllViews()
        val rect = Rect()
        var isPreviousHighlight = false
        var isPreviousCallout = false

        decorations.forEachIndexed { indent, decor ->

            val isLastDecoration = indent == decorations.lastIndex

            when (indent) {
                0 -> {
                    rect.left = 0
                }
                else -> {
                    rect.left += if (isPreviousHighlight) {
                        totalGraphicOffset
                    } else if (isPreviousCallout) {
                        totalCalloutOffset
                    } else {
                        defaultIndentOffset
                    }
                }
            }

            // Drawing background

            if (decor.background != ThemeColor.DEFAULT && !decor.style.isCard() && !decor.style.isCode()) {
                val lp = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                ).apply {
                    marginStart = rect.left
                    bottomMargin = rect.bottom
                    marginEnd = rect.right
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

            if (decor.style is Decoration.Style.Highlight && decor.style !is Decoration.Style.Highlight.Itself) {
                if (decor.style is Decoration.Style.Highlight.End) {
                    rect.bottom += highlightBottomOffset
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
                            rect.left + defaultIndentOffset
                        }
                    }
                }

                addView(
                    highlight,
                    lm
                )
                isPreviousHighlight = true
            } else {
                isPreviousHighlight = false
            }

            // Drawing callout background

            if (decor.style is Decoration.Style.Callout) {
                rect.right = defaultIndentOffset
                if (decor.style !is Decoration.Style.Callout.Start) {
                    if (!isLastDecoration) {
                        val lp = LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.MATCH_PARENT
                        ).apply {
                            marginStart = if (indent == 0) rect.left + defaultIndentOffset else rect.left
                            bottomMargin = rect.bottom
                            marginEnd = rect.right
                        }
                        if (decor.style is Decoration.Style.Callout.End) {
                            addView(
                                DecorationWidget.EndingCallout(
                                    context = context,
                                    background = resources.veryLight(decor.background, defaultCalloutColor)
                                ),
                                lp
                            )
                        } else {
                            addView(
                                DecorationWidget.Background(
                                    context = context,
                                    background = resources.veryLight(decor.background, defaultCalloutColor)
                                ),
                                lp
                            )
                        }
                    }
                }
                if (decor.style is Decoration.Style.Callout.End) {
                    rect.bottom += calloutInternalBottomPadding
                }
                if (decor.style is Decoration.Style.Callout.Full && isLastDecoration) {
                    rect.bottom += calloutExtraSpaceBottom
                }
                isPreviousCallout = true
            } else {
                isPreviousCallout = false
            }

            if (isLastDecoration) { onApplyContentOffset?.invoke(rect) }
        }
    }
}