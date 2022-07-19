package com.anytypeio.anytype.core_ui.features.editor.decoration

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import com.anytypeio.anytype.core_ui.R

interface DecorationWidget {

    class Background @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        background: Int
    ) : View(context, attrs), DecorationWidget {
        init {
            setBackgroundColor(background)
        }
    }

    class Highlight @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
    ) : FrameLayout(context, attrs), DecorationWidget {
        init {
            val lp = LayoutParams(
                resources.getDimensionPixelSize(R.dimen.highlight_line_width),
                LayoutParams.MATCH_PARENT
            ).apply {
                gravity = Gravity.CENTER
            }
            val line = View(context).apply {
                setBackgroundResource(R.color.block_highlight_divider)
            }
            addView(line, lp)
        }
    }

    class EndingCallout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        background: Int
    ) : View(context, attrs), DecorationWidget {
        init {
            setBackgroundResource(R.drawable.rect_callout_end)
            (this.background as GradientDrawable).setColor(background)
        }
    }
}