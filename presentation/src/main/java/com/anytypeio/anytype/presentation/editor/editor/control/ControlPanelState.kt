package com.anytypeio.anytype.presentation.editor.editor.control

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashWidgetState
import com.anytypeio.anytype.presentation.editor.editor.styling.StyleToolbarState
import com.anytypeio.anytype.presentation.editor.editor.table.SimpleTableWidgetItem
import com.anytypeio.anytype.presentation.editor.markup.MarkupStyleDescriptor
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.objects.ObjectTypeView

/**
 * Control panels are UI-elements that allow user to interact with blocks on a page.
 * Each panel is currently represented as a toolbar.
 * @property mainToolbar block-toolbar state (main toolbar state)
 * @property styleTextToolbar styling toolbar state
 */
data class ControlPanelState(
    val navigationToolbar: Toolbar.Navigation = Toolbar.Navigation.reset(),
    val mainToolbar: Toolbar.Main = Toolbar.Main.reset(),
    val styleTextToolbar: Toolbar.Styling = Toolbar.Styling.reset(),
    val styleExtraToolbar: Toolbar.Styling.Extra = Toolbar.Styling.Extra.reset(),
    val styleColorBackgroundToolbar: Toolbar.Styling.ColorBackground = Toolbar.Styling.ColorBackground.reset(),
    val styleBackgroundToolbar: Toolbar.Styling.Background = Toolbar.Styling.Background.reset(),
    val markupMainToolbar: Toolbar.MarkupMainToolbar = Toolbar.MarkupMainToolbar.reset(),
    val markupColorToolbar: Toolbar.MarkupColorToolbar = Toolbar.MarkupColorToolbar.reset(),
    val multiSelect: Toolbar.MultiSelect = Toolbar.MultiSelect.reset(),
    val mentionToolbar: Toolbar.MentionToolbar = Toolbar.MentionToolbar.reset(),
    val slashWidget: Toolbar.SlashWidget = Toolbar.SlashWidget.reset(),
    val searchToolbar: Toolbar.SearchToolbar = Toolbar.SearchToolbar.reset(),
    val simpleTableWidget: Toolbar.SimpleTableWidget = Toolbar.SimpleTableWidget.reset(),
) {

    sealed class Toolbar {

        /**
         * General property that defines whether this toolbar is visible or not.
         */
        abstract val isVisible: Boolean

        /**
         * Navigation bottom toolbar allowing user open Search, Naviagation Screens
         * or add new Document on Dashboard
         *
         * @property isVisible
         */
        data class Navigation(
            override val isVisible: Boolean
        ) : Toolbar() {
            companion object {
                fun reset(): Navigation = Navigation(false)
            }
        }

        /**
         * Main toolbar allowing user-interface for CRUD-operations on block/page content.
         * @property isVisible defines whether the toolbar is visible or not
         */
        data class Main(
            override val isVisible: Boolean,
            val targetBlockType: TargetBlockType = TargetBlockType.Any
        ) : Toolbar() {

            sealed interface TargetBlockType {
                /**
                 * Toolbar is shown for Any block type except Title
                 */
                object Any: TargetBlockType
                /**
                 * Toolbar is shown for Any block type except Title
                 */
                object Title: TargetBlockType

                object Cell: TargetBlockType

                object Description : TargetBlockType
            }

            companion object {
                fun reset(): Main = Main(false, TargetBlockType.Any)
            }
        }

        /**
         * Main toolbar allowing user-interface for markup operations.
         * @property isVisible defines whether the toolbar is visible or not
         */
        data class MarkupMainToolbar(
            override val isVisible: Boolean,
            val style: MarkupStyleDescriptor? = null,
            val supportedTypes: List<Markup.Type> = emptyList(),
            val isTextColorSelected: Boolean = false,
            val isBackgroundColorSelected: Boolean = false
        ) : Toolbar() {
            companion object {
                fun reset(): MarkupMainToolbar = MarkupMainToolbar(
                    isVisible = false,
                    style = null,
                    isTextColorSelected = false,
                    isBackgroundColorSelected = false
                )
            }
        }

        /**
         * Main toolbar allowing user-interface for markup operations.
         * @property isVisible defines whether the toolbar is visible or not
         */
        data class MarkupColorToolbar(
            override val isVisible: Boolean = false
        ) : Toolbar() {
            companion object {
                fun reset(): MarkupColorToolbar = MarkupColorToolbar(false)
            }
        }

        /**
         * Basic color toolbar state.
         * @property isVisible defines whether the toolbar is visible or not
         */
        data class Styling(
            override val isVisible: Boolean,
            val state: StyleToolbarState.Text
        ) : Toolbar() {

            companion object {
                fun reset() = Styling(
                    isVisible = false,
                    state = StyleToolbarState.Text.empty()
                )
            }

            data class Extra(
                override val isVisible: Boolean,
                val state: StyleToolbarState.Other,
                val navigatedFromStylingTextToolbar: Boolean,
                val navigatedFromCellsMenu: Boolean
            ) : Toolbar() {
                companion object {
                    fun reset() = Extra(
                        isVisible = false,
                        state = StyleToolbarState.Other.empty(),
                        navigatedFromStylingTextToolbar = false,
                        navigatedFromCellsMenu = false
                    )
                }
            }

            data class ColorBackground(
                override val isVisible: Boolean,
                val state: StyleToolbarState.ColorBackground,
                val navigatedFromStylingTextToolbar: Boolean,
                val navigatedFromCellsMenu: Boolean
            ) : Toolbar() {
                companion object {
                    fun reset() = ColorBackground(
                        isVisible = false,
                        state = StyleToolbarState.ColorBackground.empty(),
                        navigatedFromStylingTextToolbar = false,
                        navigatedFromCellsMenu = false
                    )
                }
            }

            data class Background(
                override val isVisible: Boolean,
                val state: StyleToolbarState.Background
            ) : Toolbar() {
                companion object {
                    fun reset() =
                        Background(isVisible = false, state = StyleToolbarState.Background.empty())
                }
            }
        }

        /**
         * Basic multi select mode toolbar state.
         * @property isVisible defines whether we are in multi select mode or not
         * @property count number of selected blocks
         */
        data class MultiSelect(
            override val isVisible: Boolean,
            val isScrollAndMoveEnabled: Boolean = false,
            val isQuickScrollAndMoveMode: Boolean = false,
            val count: Int = 0
        ) : Toolbar() {
            companion object {
                fun reset(): MultiSelect = MultiSelect(
                    isVisible = false
                )
            }
        }

        /**
         * Toolbar with list of mentions and add new page item.
         * @property isVisible defines whether the toolbar is visible or not
         * @property mentionFrom first position of the mentionFilter in text
         * @property mentionFilter sequence of symbol @ and characters, using for filtering mentions
         * @property cursorCoordinate y coordinate bottom of the cursor, using for define top border of the toolbar
         * @property mentions list of all mentions
         */
        data class MentionToolbar(
            override val isVisible: Boolean,
            val mentionFrom: Int?,
            val mentionFilter: String?,
            val cursorCoordinate: Int?,
            val updateList: Boolean = false,
            val mentions: List<DefaultObjectView> = emptyList()
        ) : Toolbar() {
            companion object {
                fun reset(): MentionToolbar = MentionToolbar(
                    isVisible = false,
                    mentionFilter = null,
                    mentionFrom = null,
                    cursorCoordinate = null,
                    mentions = emptyList(),
                    updateList = false
                )
            }
        }

        /**
         * Search toolbar.
         */
        data class SearchToolbar(
            override val isVisible: Boolean
        ) : Toolbar() {
            companion object {
                fun reset(): SearchToolbar = SearchToolbar(false)
            }
        }

        data class SlashWidget(
            override val isVisible: Boolean,
            val from: Int? = null,
            val filter: String? = null,
            val cursorCoordinate: Int? = null,
            val updateList: Boolean = false,
            val items: List<String> = emptyList(),
            val widgetState: SlashWidgetState? = null
        ) : Toolbar() {
            companion object {
                fun reset(): SlashWidget = SlashWidget(
                    isVisible = false,
                    filter = null,
                    from = null,
                    cursorCoordinate = null,
                    items = emptyList(),
                    updateList = false,
                    widgetState = null
                )
            }
        }

        data class SimpleTableWidget(
            override val isVisible: Boolean,
            val tableId: Id,
            val items: List<SimpleTableWidgetItem> = emptyList(),
            val selectedCount: Int,
            val tab: BlockView.Table.Tab
        ) : Toolbar() {
            companion object {
                fun reset(): SimpleTableWidget = SimpleTableWidget(
                    isVisible = false,
                    tableId = "",
                    items = emptyList(),
                    selectedCount = 0,
                    tab = BlockView.Table.Tab.CELL
                )
            }
        }
    }

    /**
     * Block currently associated with this panel.
     * @property id id of the focused block
     */
    data class Focus(
        val id: String,
        val type: Type
    ) {
        enum class Type {
            P, H1, H2, H3, H4, TITLE, QUOTE, CODE_SNIPPET, BULLET, NUMBERED, TOGGLE, CHECKBOX, BOOKMARK
        }
    }

    companion object {

        /**
         * Factory function for creating initial state.
         */
        fun init(): ControlPanelState = ControlPanelState(
            navigationToolbar = Toolbar.Navigation(
                isVisible = true
            ),
            mainToolbar = Toolbar.Main.reset(),
            markupMainToolbar = Toolbar.MarkupMainToolbar(
                isVisible = false
            ),
            markupColorToolbar = Toolbar.MarkupColorToolbar(),
            multiSelect = Toolbar.MultiSelect(
                isVisible = false,
                count = 0
            ),
            styleTextToolbar = Toolbar.Styling(
                isVisible = false,
                state = StyleToolbarState.Text.empty()
            ),
            mentionToolbar = Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                updateList = false,
                mentionFrom = null,
                mentions = emptyList()
            ),
            searchToolbar = Toolbar.SearchToolbar(
                isVisible = false
            ),
            slashWidget = Toolbar.SlashWidget.reset(),
            simpleTableWidget = Toolbar.SimpleTableWidget.reset()
        )
    }
}