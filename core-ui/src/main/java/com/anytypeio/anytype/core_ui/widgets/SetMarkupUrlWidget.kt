package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.reactive.clicks
import kotlinx.android.synthetic.main.widget_set_markup_url.view.*

class SetMarkupUrlWidget : LinearLayout {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init()
    }

    fun doneClicks() = setMarkupDoneButton.clicks()

    fun init() {
        LayoutInflater.from(context).inflate(R.layout.widget_set_markup_url, this)
        orientation = HORIZONTAL
        clearUrlButton.setOnClickListener {
            setUrlMarkupTextInput.text = null
        }
    }

}