package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key

sealed class ObjectSetCommand {

    sealed class Modal : ObjectSetCommand() {

        data class Menu(
            val ctx: Id,
            val isArchived: Boolean,
            val isFavorite: Boolean
        ) : Modal()

        data class CreateViewer(
            val ctx: String,
            val target: Id
        ) : Modal()

        data class EditDataViewViewer(
            val ctx: Id,
            val dataview: Id,
            val viewer: Id,
            val name: String
        ) : Modal()

        data class ManageViewer(val ctx: Id, val dataview: Id) : Modal()

        data class OpenSettings(
            val ctx: Id,
            val dv: Id,
            val viewer: Id
        ) : Modal()

        data class ModifyViewerFilters(
            val ctx: Id
        ) : Modal()

        data class ModifyViewerSorts(
            val ctx: Id
        ) : Modal()

        data class EditGridTextCell(
            val ctx: Id,
            val relationKey: Id,
            val recordId: Id
        ) : Modal()

        data class EditGridDateCell(
            val ctx: Id,
            val relationKey: Key,
            val objectId: Id
        ) : Modal()

        data class EditRelationCell(
            val ctx: Id,
            val dataview: Id,
            val target: Id,
            val relationKey: Key,
            val targetObjectTypes: List<Id>,
            val viewer: Id
        ) : Modal()

        data class ViewerCustomizeScreen(
            val ctx: Id,
            val viewer: Id
        ) : Modal()

        data class SetNameForCreatedObject(val ctx: Id, val target: Id) : Modal()

        data class CreateBookmark(val ctx: Id) : Modal()

        data class OpenIconActionMenu(
            val target: Id
        ) : Modal()

        data class OpenCoverActionMenu(
            val ctx: Id
        ) : Modal()

        data class OpenDataViewSelectSourceScreen(
            val selectedTypes: List<Id>
        ) : Modal()

        object OpenEmptyDataViewSelectSourceScreen: Modal()
    }

    sealed class Intent : ObjectSetCommand() {
        data class GoTo(val url: String) : Intent()
        data class MailTo(val email: String) : Intent()
        data class Call(val phone: String) : Intent()
    }
}