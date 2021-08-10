package com.anytypeio.anytype.presentation.editor.editor.listener

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.editor.editor.BlockDimensions
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

sealed class ListenerType {

    sealed class Bookmark : ListenerType() {
        data class View(val item: BlockView.Media.Bookmark) : Bookmark()
        data class Placeholder(val target: String) : Bookmark()
        data class Error(val item: BlockView.Error.Bookmark) : Bookmark()
    }

    sealed class File : ListenerType() {
        data class View(val target: String) : File()
        data class Placeholder(val target: String) : File()
        data class Upload(val target: String) : File()
        data class Error(val target: String) : File()
    }

    sealed class Picture: ListenerType() {
        data class View(val target: String) : Picture()
        data class Placeholder(val target: String) : Picture()
        data class Upload(val target: String) : Picture()
        data class Error(val target: String) : Picture()
    }

    sealed class Video : ListenerType() {
        data class View(val target: String) : Video()
        data class Placeholder(val target: String) : Video()
        data class Upload(val target: String) : Video()
        data class Error(val target: String) : Video()
    }

    sealed class Code : ListenerType() {
        data class SelectLanguage(val target: String) : Code()
    }

    data class LongClick(val target: String, val dimensions: BlockDimensions) : ListenerType()

    data class EditableBlock(val target: String) : ListenerType()

    object TitleBlock : ListenerType()

    data class Page(val target: String) : ListenerType()

    data class Mention(val target: String) : ListenerType()

    data class DividerClick(val target: String) : ListenerType()

    sealed class Relation : ListenerType() {
        data class Placeholder(val target: Id) : Relation()
        data class Related(val value: BlockView.Relation) : Relation()
        data class ObjectType(val type: String) : Relation()
    }
}