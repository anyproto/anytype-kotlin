package com.agileburo.anytype.feature_editor.data

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 20.03.2019.
 */
data class BlockModel(
    val id: String = "",
    val parentId: String = "",
    val content: String = "",
    var children: MutableList<BlockModel>
)