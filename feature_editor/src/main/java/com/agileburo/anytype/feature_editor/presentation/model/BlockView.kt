package com.agileburo.anytype.feature_editor.presentation.model

import com.agileburo.anytype.feature_editor.domain.ContentType
import com.agileburo.anytype.feature_editor.domain.Mark

data class BlockView(
    val id: String,
    val contentType: ContentType,
    val content: Content.Text,
    val needClearFocus: Boolean = false
) {

    sealed class Content {
        data class Text(
            val text : CharSequence,
            val param : ContentParam,
            val marks : List<Mark>
        ) : Content()
    }

    data class ContentParam(val map : Map<String, Any?>) {
        val number: Int by map
        val checked: Boolean by map
    }

}