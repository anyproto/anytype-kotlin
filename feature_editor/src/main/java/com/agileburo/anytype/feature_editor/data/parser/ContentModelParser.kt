package com.agileburo.anytype.feature_editor.data.parser

import com.agileburo.anytype.feature_editor.data.ContentModel
import com.agileburo.anytype.feature_editor.domain.BlockType
import com.google.gson.Gson
import com.google.gson.JsonElement

class ContentModelParser(val gson : Gson) {

    fun parse(json : JsonElement, blockType : BlockType) : ContentModel {
        return when(blockType) {
            BlockType.Editable -> {
                gson.fromJson(json, ContentModel.Text::class.java)
            }
            BlockType.Page -> {
                gson.fromJson(json, ContentModel.Page::class.java)
            }
            BlockType.BookMark -> {
                gson.fromJson(json, ContentModel.Bookmark::class.java)
            }
            BlockType.Image -> {
                gson.fromJson(json, ContentModel.Image::class.java)
            }
            else -> TODO()
        }
    }

}
