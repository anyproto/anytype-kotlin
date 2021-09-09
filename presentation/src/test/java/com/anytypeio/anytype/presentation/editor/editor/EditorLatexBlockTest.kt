package com.anytypeio.anytype.presentation.editor.editor

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.presentation.MockBlockFactory
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.TXT
import com.jraska.livedata.test
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorLatexBlockTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    val title = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Text(
            text = MockDataFactory.randomString(),
            style = Block.Content.Text.Style.TITLE,
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    val header = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    @Test
    fun `should render latex as unsupported block`() {

        // SETUP

        val latex = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Latex
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, latex.id)
        )

        val document = listOf(page, header, title, latex)

        stubOpenDocument(document)
        stubInterceptEvents()
        stubInterceptThreadStatus()

        val vm = buildViewModel()

        vm.onStart(root)

        // TESTING

        vm.state.test().apply {
            assertValue(
                ViewState.Success(
                    blocks = listOf(
                        BlockView.Title.Basic(
                            id = title.id,
                            isFocused = false,
                            text = title.content<TXT>().text,
                            mode = BlockView.Mode.EDIT
                        ),
                        BlockView.Unsupported(
                            id = latex.id,
                            isSelected = false,
                            indent = 0
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `should render latex as unsupported block with indent level equal to 1`() {

        // SETUP

        val latex = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Latex
        )

        val p = MockBlockFactory.paragraph(
            children = listOf(latex.id)
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, p.id)
        )

        val document = listOf(page, header, title, p, latex)

        stubOpenDocument(document)
        stubInterceptEvents()
        stubInterceptThreadStatus()

        val vm = buildViewModel()

        vm.onStart(root)

        // TESTING

        vm.state.test().apply {
            assertValue(
                ViewState.Success(
                    blocks = listOf(
                        BlockView.Title.Basic(
                            id = title.id,
                            isFocused = false,
                            text = title.content<TXT>().text,
                            mode = BlockView.Mode.EDIT
                        ),
                        BlockView.Text.Paragraph(
                            id = p.id,
                            isFocused = false,
                            text = p.content<TXT>().text,
                            mode = BlockView.Mode.EDIT
                        ),
                        BlockView.Unsupported(
                            id = latex.id,
                            isSelected = false,
                            indent = 1
                        )
                    )
                )
            )
        }
    }
}