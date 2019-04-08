package com.agileburo.anytype.feature_editor.data

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 25.03.2019.
 */
data class ContentModel(
    val text: String = "",
    val marks: List<MarkModel> = emptyList(),
    val number : Int? = null,
    val checked : Boolean? = null
)