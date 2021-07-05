package com.anytypeio.anytype.presentation.page.editor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.domain.status.SyncStatus
import com.anytypeio.anytype.presentation.page.editor.model.BlockView

sealed class Command {

    @Deprecated("Obsolete screen. To be deleted.")
    data class OpenDocumentIconActionMenu(
        val target: String,
        val image: String?,
        val emoji: String?
    ) : Command()

    @Deprecated("Obsolete screen. To be deleted.")
    data class OpenProfileIconActionMenu(
        val target: String,
        val image: String?,
        val name: String?
    ) : Command()

    data class OpenDocumentEmojiIconPicker(
        val target: String
    ) : Command()

    data class OpenGallery(
        val mediaType: String
    ) : Command()

    data class OpenBookmarkSetter(
        val target: String,
        val context: String
    ) : Command()

    data class OpenAddBlockPanel(val ctx: Id) : Command()

    data class Measure(val target: Id) : Command()

    data class OpenTurnIntoPanel(
        val target: Id,
        val excludedCategories: List<String> = emptyList(),
        val excludedTypes: List<String> = emptyList()
    ) : Command()

    data class OpenMultiSelectTurnIntoPanel(
        val excludedCategories: List<String> = emptyList(),
        val excludedTypes: List<String> = emptyList()
    ) : Command()

    data class RequestDownloadPermission(
        val id: String
    ) : Command()

    object PopBackStack : Command()

    object CloseKeyboard : Command()

    object ClearSearchInput : Command()

    data class OpenActionBar(
        val block: BlockView,
        val dimensions: BlockDimensions
    ) : Command()

    data class Browse(
        val url: Url
    ) : Command()

    data class OpenDocumentMenu(
        val status: SyncStatus,
        val title: String?,
        val emoji: String?,
        val image: String?,
        val isDeleteAllowed: Boolean,
        val isLayoutAllowed: Boolean,
        val isDetailsAllowed: Boolean,
        val isArchived: Boolean,
        val isRelationsAllowed: Boolean,
        val isDownloadAllowed: Boolean
    ) : Command()

    data class OpenProfileMenu(
        val status: SyncStatus,
        val title: String?,
        val emoji: String?,
        val image: String?,
        val isDeleteAllowed: Boolean,
        val isLayoutAllowed: Boolean,
        val isDetailsAllowed: Boolean,
        val isRelationsAllowed: Boolean,
        val isDownloadAllowed: Boolean
    ) : Command()

    data class OpenCoverGallery(val ctx: String) : Command()
    data class OpenObjectLayout(val ctx: String) : Command()

    object AlertDialog : Command()

    data class OpenFullScreenImage(
        val target: Id,
        val url: Url
    ) : Command()

    sealed class Dialog : Command() {
        data class SelectLanguage(val target: String) : Dialog()
    }

    sealed class OpenObjectRelationScreen: Command(){
        data class List(val ctx: String, val target: String?) : OpenObjectRelationScreen()
        data class Add(val ctx: String, val target: String) : OpenObjectRelationScreen()
        sealed class Value : OpenObjectRelationScreen() {
            data class Default(val ctx: Id, val target: Id, val relation: Id) : OpenObjectRelationScreen.Value()
            data class Text(val ctx: Id, val target: Id, val relation: Id) : OpenObjectRelationScreen.Value()
            data class Date(val ctx: Id, val target: Id, val relation: Id) : OpenObjectRelationScreen.Value()
        }
    }

    object AddSlashWidgetTriggerToFocusedBlock: Command()

    data class OpenChangeObjectTypeScreen(val ctx: Id, val smartBlockType: SmartBlockType): Command()
}