package com.agileburo.anytype.feature_editor.data

import com.google.gson.JsonElement

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 20.03.2019.
 */
data class BlockModel(
    val id: String,
    val parentId: String = "",
    val content: JsonElement,
    val contentType: Int,
    val type: Int,
    val children: List<BlockModel>
)
