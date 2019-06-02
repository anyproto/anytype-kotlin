package com.agileburo.anytype.feature_editor.data

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 25.03.2019.
 */
sealed class ContentModel {

    data class Text(
        val text: String = "",
        val marks: List<MarkModel> = emptyList(),
        val number : Int? = null,
        val checked : Boolean? = null
    ) : ContentModel()

    data class Page(
        val id : String
    ) : ContentModel()

    data class Bookmark(
        val bookMark : BookmarkModel
    ) : ContentModel()

    data class BookmarkModel(
        val type : String,
        val url : String,
        val title : String,
        val description : String,
        val site : String,
        val icon : String,
        val images : List<ImageModel>
    )

    data class Image(
        val original : OriginalImageModel
    ) : ContentModel()

    data class OriginalImageModel(
        val key : String,
        val type : String,
        val name : String,
        val size : Int,
        val time : Long
    )

    data class ImageModel(val url : String)

}
