package com.anytypeio.anytype.presentation.editor

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Document
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.editor.Editor
import com.anytypeio.anytype.domain.editor.Editor.Focus
import com.anytypeio.anytype.presentation.editor.editor.Proxy
import com.anytypeio.anytype.presentation.editor.editor.Store
import com.anytypeio.anytype.presentation.editor.editor.actions.ActionItemType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.selection.SelectionStateHolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface Editor {

    /**
     * Mode of user interaction with the editor.
     */
    sealed class Mode {

        /**
         * Default mode of interaction with the editor: content editing is fully enabled.
         */
        data object Edit : Mode()

        /**
         * Editor in preview state.
         * Currently used for templates, which can't be edited.
         */
        data object Read : Mode()

        /**
         * Editor in locked state.
         * Locked mode is toggled in object menu.
         */
        data object Locked: Mode()

        /**
         * Editor in select mode: when one or multiple blocks are selected.
         * To enter this mode, user long-clicks a block.
         */
        data object Select : Mode()

        /**
         * Editor in scroll-and-move mode: one or multiple blocks are selected, then moved to new position by scrolling.
         * @see [ActionItemType.SAM]
         */
        data object SAM : Mode()

        /**
         * Editor in search-on-page state: searching plain text through blocks.
         * @see [Block.Content.Text.Style]
         */
        data object Search : Mode()

        /**
         * Editor in text styling mode.
         */
        sealed class Styling : Mode() {
            /**
             * Enabled when user selects “style” option in BlockToolbar for focused block.
             * @property [target] id of the selected block
             * @property [cursor] cursor position before selection (we might need to restore cursor position after unselecting [target] block)
             */
            data class Single(
                val target: Id,
                val cursor: Int?
            ) : Styling()

            /**
             * Enabled when user selects multiple blocks and choose [ActionItemType.Style] from ActionToolbar
             * @property [targets] ids of the selected blocks.
             */
            data class Multi(val targets: Set<Id>) : Styling()
        }

        /**
         * Editor in simple table menu mode.
         * @property[tableId] - the id of the table block for which the menu is shown
         * and it is possible to select items only from this table.
         * Clicks on cells in other tabled blocks will be omitted.
         * @property[initialTargets] - last selected cells for the Tab Cell,
         * when changing Tabs will be used as selected state
         * @property[targets] - all of these cells are currently selected,
         * cell allocation is based on the given list
         * @property[tab] - currently selected Tab
         */
        data class Table(
            val tableId: Id,
            var initialTargets: Set<Id>,
            val tab: BlockView.Table.Tab,
            var targets: Set<Id>,
        ) : Mode()
    }

    class Storage {
        val document: DocumentProvider = DocumentProvider.Default()
        val views: Store<List<BlockView>> = Store.Screen()
        val focus: Store<Focus> = Store.Focus()
        val details: Store.Details = Store.Details()

        @Deprecated("legacy")
        val relations: Store.Relations = Store.Relations()
        val textSelection: Store<Editor.TextSelection> = Store.TextSelection()
        val objectRestrictions: Store.ObjectRestrictions = Store.ObjectRestrictions()
        val relationLinks: Store.RelationLinks = Store.RelationLinks()
    }

    class Proxer(
        val intents: Proxy.Intents = Proxy.Intents(),
        val changes: Proxy.Text.Changes = Proxy.Text.Changes(),
        val saves: Proxy.Text.Saves = Proxy.Text.Saves(),
        val payloads: Proxy.Payloads = Proxy.Payloads(),
        val errors: Proxy.Error = Proxy.Error(),
        val toasts: Proxy.Toast = Proxy.Toast()
    )

    class Memory(
        val selections: SelectionStateHolder
    )

    sealed class Restore {
        data class Selection(
            val target: Id,
            val range: IntRange,
        ) : Restore()
    }

    sealed class Event {
        sealed class Text : Event() {
            data class OnSelectionChanged(val id: Id, val selection: IntRange) : Text()
        }
    }

    interface DocumentProvider {
        fun get(): Document
        fun update(document: Document)
        fun observe() : Flow<Document>
        fun clear()
        class Default : DocumentProvider {
            val doc = MutableStateFlow<Document>(emptyList())
            override fun observe(): Flow<Document> = doc
            override fun get(): Document = doc.value
            override fun update(document: Document) { this.doc.tryEmit(document) }
            override fun clear() { this.doc.value = emptyList() }
        }
    }
}