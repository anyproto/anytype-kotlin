package com.anytypeio.anytype.features.editor

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.features.editor.base.EditorTestSetup
import com.anytypeio.anytype.features.editor.base.TestEditorFragment
import com.anytypeio.anytype.presentation.MockBlockContentFactory.StubTextContent
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.ValueClassAnswer
import com.anytypeio.anytype.test_utils.utils.checkHasText
import com.anytypeio.anytype.test_utils.utils.onItemView
import com.anytypeio.anytype.test_utils.utils.rVMatcher
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.utils.CoroutinesTestRule
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

//Todo Не работает, доделать!
@RunWith(AndroidJUnit4::class)
@LargeTest
class MentionUpdateTesting : EditorTestSetup() {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val args = bundleOf(EditorFragment.ID_KEY to root)

    @Before
    override fun setup() {
        super.setup()
    }

    @Test
    fun shouldSetCursorAtTheEndOfDescription() {
        val title = Block(
            id = "title",
            content = StubTextContent(
                text = "Title ",
                style = Block.Content.Text.Style.TITLE,
                marks = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )
        val header = Block(
            id = "header",
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.HEADER
            ),
            fields = Block.Fields.empty(),
            children = listOf(title.id)
        )

        val mentionTarget = MockDataFactory.randomUuid()
        val originalText = "w46HX9"
        val updatedText = "WLzNG8Zq27aZ1"

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = StubTextContent(
                text = "Start $originalText end",
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(0, 5),
                        type = Block.Content.Text.Mark.Type.BOLD
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(6, 6 + originalText.length),
                        type = Block.Content.Text.Mark.Type.MENTION,
                        param = mentionTarget
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(10, 13),
                        type = Block.Content.Text.Mark.Type.STRIKETHROUGH
                    )
                ),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )
        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, block.id)
        )
        val document = listOf(page, header, title, block)

        val customDetails = Block.Details()

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubUpdateText()
        openPage.stub {
            onBlocking { execute(any()) } doAnswer ValueClassAnswer(
                Result.Success(
                    Payload(
                        context = root,
                        events = listOf(
                            Event.Command.ShowObject(
                                context = root,
                                root = root,
                                details = customDetails,
                                relations = emptyList(),
                                blocks = document,
                                objectRestrictions = emptyList()
                            ),
                            Event.Command.Details.Amend(
                                context = root,
                                target = mentionTarget,
                                details = mapOf(Block.Fields.NAME_KEY to updatedText)
                            )
                        )
                    )
                )
            )
        }

        // TESTING

        launchFragment(args = args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).apply {
                checkHasText("Start $updatedText end")
            }
        }

        // Release pending coroutines
        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    // STUBBING & SETUP

    private fun launchFragment(args: Bundle): FragmentScenario<TestEditorFragment> {
        return launchFragmentInContainer(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }

    /**
     * Moves coroutines clock time.
     */
    private fun advance(millis: Long) {
        coroutineTestRule.advanceTime(millis)
    }
}