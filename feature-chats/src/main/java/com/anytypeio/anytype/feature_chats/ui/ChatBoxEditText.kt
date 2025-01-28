package com.anytypeio.anytype.feature_chats.ui

import android.content.Context
import android.text.Editable
import android.text.style.UnderlineSpan
import androidx.appcompat.widget.AppCompatEditText
import com.anytypeio.anytype.presentation.editor.editor.Markup

class ChatBoxEditText : AppCompatEditText {

    constructor(context: Context) : super(context) {
        maxLines = 5
        background = null
        setHint("Write a message")
    }

    fun setEffect(
        effect: Effect
    ) {
        when(effect) {
            is Effect.InsertMention -> {
                val editable = text as Editable
                val start = selectionStart.dec()
                editable.replace(
                    selectionStart.dec(),
                    selectionEnd,
                    effect.name + " "
                )
                editable.setSpan(
                    UnderlineSpan(),
                    start,
                    start + effect.name.length,
                    Markup.DEFAULT_SPANNABLE_FLAG
                )
            }
        }
    }
}