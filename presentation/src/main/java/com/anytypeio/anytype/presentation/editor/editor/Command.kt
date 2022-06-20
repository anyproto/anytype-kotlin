package com.anytypeio.anytype.presentation.editor.editor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_utils.ext.Mimetype

sealed class Command {

    data class OpenDocumentImagePicker(val mimeType: Mimetype) : Command()

    data class OpenDocumentEmojiIconPicker(
        val target: String
    ) : Command()

    data class OpenGallery(
        val mimeType: Mimetype
    ) : Command()

    data class OpenBookmarkSetter(
        val target: String,
        val context: String
    ) : Command()

    data class ScrollToActionMenu(val target: Id?) : Command()

    /**
     * @property [id] id of the file block
     */
    data class OpenFileByDefaultApp(
        val id: String,
        val mime: String,
        val uri: String
    ) : Command()

    object PopBackStack : Command()

    object ShowKeyboard : Command()
    object CloseKeyboard : Command()

    object ClearSearchInput : Command()

    data class Browse(
        val url: Url
    ) : Command()

    data class OpenDocumentMenu(
        val isArchived: Boolean,
        val isFavorite: Boolean,
        val isLocked: Boolean
    ) : Command()

    data class OpenProfileMenu(
        val isFavorite: Boolean,
        val isLocked: Boolean
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

    sealed class OpenObjectRelationScreen : Command() {
        data class RelationList(
            val ctx: String,
            val target: String?,
            val isLocked: Boolean
        ) : OpenObjectRelationScreen()

        data class RelationAdd(val ctx: String, val target: String) : OpenObjectRelationScreen()
        sealed class Value : OpenObjectRelationScreen() {
            data class Default(
                val ctx: Id,
                val target: Id,
                val relation: Id,
                val targetObjectTypes: List<Id>,
                val isLocked: Boolean
            ) : OpenObjectRelationScreen.Value()

            data class Text(
                val ctx: Id,
                val target: Id,
                val relation: Id,
                val isLocked: Boolean = false
            ) : OpenObjectRelationScreen.Value()

            data class Date(
                val ctx: Id,
                val target: Id,
                val relation: Id
            ) : OpenObjectRelationScreen.Value()
        }
    }

    object AddSlashWidgetTriggerToFocusedBlock : Command()
    object AddMentionWidgetTriggerToFocusedBlock : Command()

    data class OpenChangeObjectTypeScreen(
        val ctx: Id,
        val smartBlockType: SmartBlockType,
        val isDraft: Boolean = false
    ) : Command()

    data class OpenMoveToScreen(
        val ctx: Id,
        val blocks: List<Id>,
        val restorePosition: Int?,
        val restoreBlock: Id?
    ) : Command()

    data class OpenLinkToScreen(val target: Id, val position: Int?) : Command()

    data class OpenLinkToObjectOrWebScreen(val uri: String) : Command()

    data class OpenAddRelationScreen(
        val ctx: Id,
        val target: Id
    ) : Command()

    object ShowTextLinkMenu : Command()

    data class SaveTextToSystemClipboard(val text: String) : Command()

    /**
     * @param ctx - Object Id
     * @param block - LinkBlock Id
     */
    data class OpenObjectAppearanceSettingScreen(
        val ctx: Id,
        val block: Id
    ) : Command()

    data class ScrollToPosition(val pos: Int) : Command()
}