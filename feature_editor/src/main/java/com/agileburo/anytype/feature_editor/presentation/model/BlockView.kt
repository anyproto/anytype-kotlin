package com.agileburo.anytype.feature_editor.presentation.model

import com.agileburo.anytype.feature_editor.domain.Content
import com.agileburo.anytype.feature_editor.domain.ContentType

data class BlockView(
    val id: String,
    val contentType: ContentType,
    val content: Content.Text
)