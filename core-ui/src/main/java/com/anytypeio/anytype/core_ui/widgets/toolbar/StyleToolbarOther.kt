package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.presentation.page.editor.styling.StylingEvent
import kotlinx.android.synthetic.main.widget_block_style_other.view.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

class StyleToolbarOther @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CardView(context, attrs) {

    init {
        LayoutInflater
            .from(context)
            .inflate(R.layout.widget_block_style_other, this)
    }

    val actions : Flow<StylingEvent> = merge(
        bold.clicks().map { StylingEvent.Markup.Bold },
        italic.clicks().map { StylingEvent.Markup.Italic },
        strikethrough.clicks().map { StylingEvent.Markup.StrikeThrough },
        code.clicks().map { StylingEvent.Markup.Code },
        alignmentLeft.clicks().map { StylingEvent.Alignment.Left },
        alignmentMiddle.clicks().map { StylingEvent.Alignment.Center },
        alignmentRight.clicks().map { StylingEvent.Alignment.Right },
        setUrl.clicks().map { StylingEvent.Markup.Link }
    )
}