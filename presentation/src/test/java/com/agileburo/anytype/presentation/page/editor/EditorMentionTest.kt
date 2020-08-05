package com.agileburo.anytype.presentation.page.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.MentionEvent
import com.agileburo.anytype.core_ui.widgets.toolbar.adapter.Mention
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.presentation.page.PageViewModel
import com.agileburo.anytype.presentation.util.CoroutinesTestRule
import com.jraska.livedata.test
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorMentionTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `should update text with cursor position`() {

        val mentionTrigger = "@a"
        val from = 11
        val givenText = "page about $mentionTrigger music"
        val mentionText = "Avant-Garde Jazz"
        val mentionHash = "ryew78yfhiuwehudc"

        val a = Block(
            id = "dfhkshfjkhsdjhfjkhsjkd",
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = givenText,
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 0,
                            endInclusive = 3
                        ),
                        type = Block.Content.Text.Mark.Type.BOLD
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 5,
                            endInclusive = 9
                        ),
                        type = Block.Content.Text.Mark.Type.ITALIC
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 14,
                            endInclusive = 18
                        ),
                        type = Block.Content.Text.Mark.Type.STRIKETHROUGH
                    )
                ),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(a.id)
        )

        val document = listOf(page, a)

        stubOpenDocument(document)
        stubObserveEvents()

        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onBlockFocusChanged(
                id = a.id,
                hasFocus = true
            )
            onSelectionChanged(
                id = a.id,
                selection = IntRange(12, 12)
            )
            onMentionEvent(
                MentionEvent.MentionSuggestStart(
                    cursorCoordinate = 500,
                    mentionStart = from
                )
            )
            onMentionSuggestClick(
                mention = Mention(
                    id = mentionHash,
                    emoji = null,
                    image = null,
                    title = mentionText
                ),
                mentionTrigger = mentionTrigger
            )
        }

        vm.state.test().apply {
            assertValue(
                ViewState.Success(
                    blocks = listOf(
                        BlockView.Title(
                            id = root,
                            isFocused = false,
                            text = null,
                            mode = BlockView.Mode.EDIT
                        ),
                        BlockView.Paragraph(
                            id = a.id,
                            cursor = 28,
                            isSelected = false,
                            isFocused = true,
                            marks = listOf(
                                Markup.Mark(
                                    from = 0,
                                    to = 3,
                                    type = Markup.Type.BOLD
                                ),
                                Markup.Mark(
                                    from = 5,
                                    to = 9,
                                    type = Markup.Type.ITALIC
                                ),
                                Markup.Mark(
                                    from = 29,
                                    to = 33,
                                    type = Markup.Type.STRIKETHROUGH
                                ),
                                Markup.Mark(
                                    from = from,
                                    to = from + mentionText.length,
                                    type = Markup.Type.MENTION,
                                    param = mentionHash
                                )
                            ),
                            backgroundColor = null,
                            color = null,
                            indent = 0,
                            text = "page about Avant-Garde Jazz  music",
                            mode = BlockView.Mode.EDIT
                        )
                    )
                )
            )
        }

        clearPendingCoroutines()
    }

    private fun clearPendingCoroutines() {
        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }
}