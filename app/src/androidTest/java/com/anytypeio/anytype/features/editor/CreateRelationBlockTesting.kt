package com.anytypeio.anytype.features.editor

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.action.ViewActions.click
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.domain.block.interactor.CreateBlock
import com.anytypeio.anytype.emojifier.data.DefaultDocumentEmojiIconProvider
import com.anytypeio.anytype.features.editor.base.EditorTestSetup
import com.anytypeio.anytype.features.editor.base.TestPageFragment
import com.anytypeio.anytype.mocking.MockDataFactory
import com.anytypeio.anytype.presentation.page.editor.model.UiBlock
import com.anytypeio.anytype.ui.page.PageFragment
import com.anytypeio.anytype.utils.*
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class CreateRelationBlockTesting : EditorTestSetup() {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val args = bundleOf(PageFragment.ID_KEY to root)

    private val defaultDetails = Block.Details(
        mapOf(
            root to Block.Fields(
                mapOf(
                    "iconEmoji" to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random()
                )
            )
        )
    )

    private val title = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Text(
            style = Block.Content.Text.Style.TITLE,
            text = "CreateRelationBlockTesting",
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    private val header = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    @Before
    override fun setup() {
        super.setup()
    }

    @Test
    fun shouldAddNewRelationBlockPlaceholderWithOnCreateAfterOnAddRelationCommand() {

        val new = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.RelationBlock(
                key = null
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val paragraph = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Foo",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, paragraph.id)
        )

        val document = listOf(page, header, title, paragraph)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document, defaultDetails)
        stubCreateBlock(
            params = CreateBlock.Params(
                context = root,
                target = paragraph.id,
                position = Position.BOTTOM,
                prototype = Block.Prototype.Relation("")
            ),
            events = listOf(
                Event.Command.UpdateStructure(
                    context = root,
                    id = root,
                    children = page.children + listOf(new.id)
                ),
                Event.Command.AddBlock(
                    context = root,
                    blocks = listOf(new)
                )
            )
        )

        // TESTING

        val fragment = launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            checkIsRecyclerSize(2)
            onItemView(0, R.id.title).checkHasText(title.content<Block.Content.Text>().text)
            onItemView(
                1,
                R.id.textContent
            ).checkHasText(paragraph.content<Block.Content.Text>().text)
        }

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(click())
        }

        Thread.sleep(200)

        fragment.onFragment { fr ->
            fr.onAddBlockClicked(UiBlock.RELATION)
        }

        Thread.sleep(200)

        with(R.id.recycler.rVMatcher()) {
            checkIsRecyclerSize(3)
            onItemView(0, R.id.title).checkHasText(title.content<Block.Content.Text>().text)
            onItemView(
                1,
                R.id.textContent
            ).checkHasText(paragraph.content<Block.Content.Text>().text)
            onItemView(2, R.id.tvPlaceholder).checkHasText(R.string.set_new_relation)
        }
    }

    @Test
    fun shouldAddNewRelationBlockPlaceholderWithOnCreateAfterOnAddRelationCommandAfterTitle() {

        val new = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.RelationBlock(
                key = null
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id)
        )

        val document = listOf(page, header, title)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document, defaultDetails)
        stubCreateBlock(
            params = CreateBlock.Params(
                context = root,
                target = title.id,
                position = Position.BOTTOM,
                prototype = Block.Prototype.Relation("")
            ),
            events = listOf(
                Event.Command.UpdateStructure(
                    context = root,
                    id = root,
                    children = page.children + listOf(new.id)
                ),
                Event.Command.AddBlock(
                    context = root,
                    blocks = listOf(new)
                )
            )
        )

        // TESTING

        val fragment = launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            checkIsRecyclerSize(1)
            onItemView(0, R.id.title).checkHasText(title.content<Block.Content.Text>().text)
        }

        with(R.id.recycler.rVMatcher()) {
            onItemView(0, R.id.title).perform(click())
        }

        Thread.sleep(200)

        fragment.onFragment { fr ->
            fr.onAddBlockClicked(UiBlock.RELATION)
        }

        Thread.sleep(100)

        with(R.id.recycler.rVMatcher()) {
            checkIsRecyclerSize(2)
            onItemView(0, R.id.title).checkHasText(title.content<Block.Content.Text>().text)
            onItemView(1, R.id.tvPlaceholder).checkHasText(R.string.set_new_relation)
        }
    }

    @Test
    fun shouldAddNewRelationBlockPlaceholderWithOnReplaceAfterOnAddRelationCommand() {

        val new = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.RelationBlock(
                key = null
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val paragraph = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, paragraph.id)
        )

        val document = listOf(page, header, title, paragraph)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document, defaultDetails)
        stubReplaceBlock(
            command = Command.Replace(
                context = root,
                target = paragraph.id,
                prototype = Block.Prototype.Relation("")
            ),
            events = listOf(
                Event.Command.UpdateStructure(
                    context = root,
                    id = root,
                    children = listOf(page.id, header.id, new.id)
                ),
                Event.Command.AddBlock(
                    context = root,
                    blocks = listOf(new)
                ),
                Event.Command.DeleteBlock(
                    context = root,
                    targets = listOf(paragraph.id)
                )
            )
        )

        // TESTING

        val fragment = launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            checkIsRecyclerSize(2)
            onItemView(0, R.id.title).checkHasText(title.content<Block.Content.Text>().text)
            onItemView(1, R.id.textContent).checkHasText("")
        }

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(click())
        }

        Thread.sleep(200)

        fragment.onFragment { fr ->
            fr.onAddBlockClicked(UiBlock.RELATION)
        }

        Thread.sleep(100)

        with(R.id.recycler.rVMatcher()) {
            checkIsRecyclerSize(2)
            onItemView(0, R.id.title).checkHasText(title.content<Block.Content.Text>().text)
            onItemView(1, R.id.tvPlaceholder).checkHasText(R.string.set_new_relation)
        }
    }

    // STUBBING & SETUP

    private fun launchFragment(args: Bundle): FragmentScenario<TestPageFragment> {
        return launchFragmentInContainer<TestPageFragment>(
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

