package com.agileburo.anytype.feature_editor.presentation.model

import com.agileburo.anytype.feature_editor.domain.ContentType

data class BlockView(
    val id: String,
    val contentType: ContentType,
    val content: Content.Text,
    val needClearFocus: Boolean = false
) {

    sealed class Content {
        data class Text(
            var text: CharSequence,
            val param: ContentParam
        ) : Content()
    }

    data class ContentParam(val map: Map<String, Any?>) {
        val number: Int by map
        val checked: Boolean by map
    }
}