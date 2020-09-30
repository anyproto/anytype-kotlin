package com.anytypeio.anytype.presentation.page.editor

import com.anytypeio.anytype.core_ui.features.page.BlockDimensions
import com.anytypeio.anytype.core_ui.features.page.BlockView
import com.anytypeio.anytype.domain.common.Id
import com.anytypeio.anytype.domain.common.Url

sealed class Command {

    data class OpenDocumentIconActionMenu(
        val target: String,
        val image: String?,
        val emoji: String?
    ) : Command()

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

    object OpenAddBlockPanel : Command()

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

    data class OpenActionBar(
        val block: BlockView,
        val dimensions: BlockDimensions
    ) : Command()

    data class Browse(
        val url: Url
    ) : Command()

    object OpenDocumentMenu : Command()

    object AlertDialog : Command()
}