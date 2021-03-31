package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.setEmojiOrNull
import com.anytypeio.anytype.core_ui.extensions.setImageOrNull
import kotlinx.android.synthetic.main.widget_icon.view.*

class IconWidget : FrameLayout {

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
        parse(attrs)
        init()
    }

    private fun inflate() {
        LayoutInflater.from(context).inflate(R.layout.widget_icon, this)
    }

    private fun parse(set: AttributeSet?) {
        if (set != null) {
            val attributes = context.obtainStyledAttributes(set, R.styleable.IconWidget, 0, 0)
            val emojiSize = attributes.getDimensionPixelSize(R.styleable.IconWidget_emoji_size, 0)
            emojiIcon.updateLayoutParams<LayoutParams> {
                this.width = emojiSize
                this.height = emojiSize
            }
            attributes.recycle()
        }
    }

    private fun init() {
        setBackgroundResource(R.drawable.rectangle_default_page_logo_background)
    }

    fun bind(
        emoji: String?,
        image: String?
    ) {
        emojiIcon.setEmojiOrNull(emoji)
        imageIcon.setImageOrNull(image)
    }
}