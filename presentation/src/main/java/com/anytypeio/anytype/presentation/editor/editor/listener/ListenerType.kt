package com.anytypeio.anytype.presentation.editor.editor.listener

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.editor.editor.BlockDimensions
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import com.anytypeio.anytype.presentation.sets.model.ObjectView

sealed interface ListenerType {

    sealed class Bookmark : ListenerType {
        data class View(val item: BlockView.Media.Bookmark) : Bookmark()
        data class Placeholder(val target: String) : Bookmark()
        data class Upload(val target: String) : File()
        data class Error(val item: BlockView.Error.Bookmark) : Bookmark()
    }

    sealed class File : ListenerType {
        data class View(val target: String) : File()
        data class Placeholder(val target: String) : File()
        data class Upload(val target: String) : File()
        data class Error(val target: String) : File()
    }

    sealed class Picture: ListenerType {
        data class View(val target: String) : Picture()
        data class Placeholder(val target: String) : Picture()
        data class Upload(val target: String) : Picture()
        data class Error(val target: String) : Picture()
    }

    sealed class Video : ListenerType {
        data class View(val target: String) : Video()
        data class Placeholder(val target: String) : Video()
        data class Upload(val target: String) : Video()
        data class Error(val target: String) : Video()
    }

    sealed class Code : ListenerType {
        data class SelectLanguage(val target: String) : Code()
    }

    sealed interface Callout : ListenerType {
        data class Icon(val blockId: String): Callout
    }

    data class LongClick(val target: String, val dimensions: BlockDimensions = BlockDimensions()) : ListenerType
    data class CellLongClick(val tableId: Id) : ListenerType

    data class EditableBlock(val target: String) : ListenerType

    object TitleBlock : ListenerType
    object ProfileImageIcon : ListenerType

    data class LinkToObject(val target: String) : ListenerType
    data class LinkToObjectArchived(val target: String) : ListenerType
    data class LinkToObjectDeleted(val target: String) : ListenerType
    data class LinkToObjectLoading(val target: String) : ListenerType
    data class LinkToObjectCheckboxUpdate(val target: Id, val isChecked: Boolean) : ListenerType

    data class Mention(val target: String) : ListenerType

    data class DividerClick(val target: String) : ListenerType

    data class Latex(val id: Id) : ListenerType

    sealed class Relation : ListenerType {
        data class Placeholder(val target: Id) : Relation()
        data class Related(val value: BlockView.Relation) : Relation()
        data class Featured(val relation: ObjectRelationView) : Relation()
        data class SetQuery(val queries: List<ObjectView> = emptyList()) : Relation()
        data class ChangeQueryByRelation(val msg: String) : Relation()
        data class ObjectType(val relation: ObjectRelationView, val viewId: Int): Relation()
    }

    data class TableOfContentsItem(val target: Id, val item: Id) : ListenerType
    data class TableOfContents(val target: Id) : ListenerType
    data class TableEmptyCell(val cell: BlockView.Table.Cell) : ListenerType
    data class TableTextCell(val cell: BlockView.Table.Cell) : ListenerType

    data class DataViewClick(val target: Id) : ListenerType
}