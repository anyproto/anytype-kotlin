package com.anytypeio.anytype.features.editor

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.emojifier.data.DefaultDocumentEmojiIconProvider
import com.anytypeio.anytype.features.editor.base.EditorTestSetup
import com.anytypeio.anytype.features.editor.base.TestEditorFragment
import com.anytypeio.anytype.mocking.MockDataFactory
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.utils.*
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class RelationBlockUITesting : EditorTestSetup() {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val args = bundleOf(EditorFragment.ID_KEY to root)

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
            text = "Relation Block UI Testing",
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
    fun shouldDisplayRelationBlockPlaceholderAtTheEnd() {

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Foo",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        // This should be rendered as placeholder because it's not related to any relation.

        val b = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.RelationBlock(key = null)
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, a.id, b.id)
        )

        val document = listOf(page, header, title, a, b)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document, defaultDetails)

        // TESTING

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(0, R.id.title).checkHasText(title.content<Block.Content.Text>().text)
            onItemView(1, R.id.textContent).checkHasText(a.content<Block.Content.Text>().text)
            onItemView(2, R.id.tvPlaceholder).checkHasText(R.string.set_new_relation)
            checkIsRecyclerSize(3)
        }
    }

    @Test
    fun shouldDisplayPrimitiveTextRelationBlocksAtTheEnd() {

        val relation1 = Relation(
            key = MockDataFactory.randomString(),
            name = "Company",
            format = Relation.Format.SHORT_TEXT,
            source = Relation.Source.values().random()
        )

        val relation2 = Relation(
            key = MockDataFactory.randomString(),
            name = "Year",
            format = Relation.Format.NUMBER,
            source = Relation.Source.values().random()
        )

        val relation3 = Relation(
            key = MockDataFactory.randomString(),
            name = "Phone",
            format = Relation.Format.PHONE,
            source = Relation.Source.values().random()
        )

        val relation4 = Relation(
            key = MockDataFactory.randomString(),
            name = "Website",
            format = Relation.Format.URL,
            source = Relation.Source.values().random()
        )

        val relation5 = Relation(
            key = MockDataFactory.randomString(),
            name = "Email",
            format = Relation.Format.EMAIL,
            source = Relation.Source.values().random()
        )

        val value1 = "Anytype"
        val value2 = 2021.01
        val value3 = "+00000000000"
        val value4 = "https://anytype.io/"
        val value5 = "team@anytype.io"

        val customDetails = Block.Details(
            mapOf(
                root to Block.Fields(
                    mapOf(
                        "iconEmoji" to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
                        relation1.key to value1,
                        relation2.key to value2,
                        relation3.key to value3,
                        relation4.key to value4,
                        relation5.key to value5,
                    )
                )
            )
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

        val block1 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.RelationBlock(key = relation1.key)
        )

        val block2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.RelationBlock(key = relation2.key)
        )

        val block3 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.RelationBlock(key = relation3.key)
        )

        val block4 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.RelationBlock(key = relation4.key)
        )

        val block5 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.RelationBlock(key = relation5.key)
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, paragraph.id, block1.id, block2.id, block3.id, block4.id, block5.id)
        )

        val document = listOf(page, header, title, paragraph, block1, block2, block3, block4, block5)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(
            document = document,
            details = customDetails,
            relations = listOf(relation1, relation2, relation3, relation4, relation5)
        )

        // TESTING

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            checkIsRecyclerSize(7)
            onItemView(0, R.id.title).checkHasText(title.content<Block.Content.Text>().text)
            onItemView(1, R.id.textContent).checkHasText(paragraph.content<Block.Content.Text>().text)
            onItemView(2, R.id.tvRelationTitle).checkHasText(relation1.name)
            onItemView(2, R.id.tvRelationValue).checkHasText(value1)
            onItemView(3, R.id.tvRelationTitle).checkHasText(relation2.name)
            onItemView(3, R.id.tvRelationValue).checkHasText(value2.toString())
            onItemView(4, R.id.tvRelationTitle).checkHasText(relation3.name)
            onItemView(4, R.id.tvRelationValue).checkHasText(value3)
            onItemView(5, R.id.tvRelationTitle).checkHasText(relation4.name)
            onItemView(5, R.id.tvRelationValue).checkHasText(value4)
            onItemView(6, R.id.tvRelationTitle).checkHasText(relation5.name)
            onItemView(6, R.id.tvRelationValue).checkHasText(value5)
        }
    }

    @Test
    fun shouldDisplayPrimitiveTextRelationBlocksWithBackgroundColorSetExceptForOneBlock() {

        val relation1 = Relation(
            key = MockDataFactory.randomString(),
            name = "Company",
            format = Relation.Format.SHORT_TEXT,
            source = Relation.Source.values().random()
        )

        val relation2 = Relation(
            key = MockDataFactory.randomString(),
            name = "Year",
            format = Relation.Format.NUMBER,
            source = Relation.Source.values().random()
        )

        val relation3 = Relation(
            key = MockDataFactory.randomString(),
            name = "Phone",
            format = Relation.Format.PHONE,
            source = Relation.Source.values().random()
        )

        val relation4 = Relation(
            key = MockDataFactory.randomString(),
            name = "Website",
            format = Relation.Format.URL,
            source = Relation.Source.values().random()
        )

        val relation5 = Relation(
            key = MockDataFactory.randomString(),
            name = "Email",
            format = Relation.Format.EMAIL,
            source = Relation.Source.values().random()
        )

        val value1 = "Anytype"
        val value2 = "2021"
        val value3 = "+00000000000"
        val value4 = "https://anytype.io/"
        val value5 = "team@anytype.io"

        val background1 = ThemeColor.PURPLE
        val background2 = ThemeColor.RED
        val background3 = ThemeColor.BLUE
        val background4 = ThemeColor.ORANGE

        val customDetails = Block.Details(
            mapOf(
                root to Block.Fields(
                    mapOf(
                        "iconEmoji" to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
                        relation1.key to value1,
                        relation2.key to value2,
                        relation3.key to value3,
                        relation4.key to value4,
                        relation5.key to value5,
                    )
                )
            )
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

        val block1 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            backgroundColor = background1.title,
            content = Block.Content.RelationBlock(
                key = relation1.key,
            )
        )

        val block2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            backgroundColor = background2.title,
            content = Block.Content.RelationBlock(
                key = relation2.key
            )
        )

        val block3 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            backgroundColor = background2.title,
            content = Block.Content.RelationBlock(
                key = relation3.key
            )
        )

        val block4 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            backgroundColor = background2.title,
            content = Block.Content.RelationBlock(
                key = relation4.key
            )
        )

        val block5 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.RelationBlock(key = relation5.key)
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, paragraph.id, block1.id, block2.id, block3.id, block4.id, block5.id)
        )

        val document = listOf(page, header, title, paragraph, block1, block2, block3, block4, block5)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(
            document = document,
            details = customDetails,
            relations = listOf(relation1, relation2, relation3, relation4, relation5)
        )

        // TESTING

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            checkIsRecyclerSize(7)
            onItem(2).checkHasBackgroundColor(background1.background)
            onItem(3).checkHasBackgroundColor(background2.background)
            onItem(4).checkHasBackgroundColor(background3.background)
            onItem(5).checkHasBackgroundColor(background4.background)
            onItem(6).checkHasNoBackground()
        }
    }

    @Test
    fun shouldDisplayOneStatusRelationBlockAtTheEnd() {

        val option = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Done",
            color = ThemeColor.PURPLE.title
        )

        val relation1 = Relation(
            key = MockDataFactory.randomString(),
            name = "Status",
            format = Relation.Format.STATUS,
            source = Relation.Source.values().random(),
            selections = listOf(option)
        )

        val customDetails = Block.Details(
            mapOf(
                root to Block.Fields(
                    mapOf(
                        "iconEmoji" to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
                        relation1.key to option.id
                    )
                )
            )
        )

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Foo",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val b = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.RelationBlock(key = relation1.key)
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, a.id, b.id)
        )

        val document = listOf(page, header, title, a, b)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(
            document = document,
            details = customDetails,
            relations = listOf(relation1)
        )

        // TESTING

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            checkIsRecyclerSize(3)
            onItemView(0, R.id.title).checkHasText(title.content<Block.Content.Text>().text)
            onItemView(1, R.id.textContent).checkHasText(a.content<Block.Content.Text>().text)
            onItemView(2, R.id.tvRelationTitle).checkHasText(relation1.name)
            onItemView(2, R.id.tvRelationValue).checkHasText(option.text)
        }
    }

    @Test
    fun shouldDisplayOneFileRelationBlockContainingTwoFiles() {

        val file1 = MockDataFactory.randomUuid()
        val file2 = MockDataFactory.randomUuid()

        val relation1 = Relation(
            key = MockDataFactory.randomString(),
            name = "Attachement",
            format = Relation.Format.FILE,
            source = Relation.Source.values().random(),
            selections = emptyList()
        )

        val customDetails = Block.Details(
            mapOf(
                root to Block.Fields(
                    mapOf(
                        "iconEmoji" to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
                        relation1.key to listOf(file1, file2)
                    )
                ),
                file1 to Block.Fields(
                    mapOf(
                        "name" to "Document",
                        "ext" to "pdf",
                        "mime" to "application/pdf"
                    )
                ),
                file2 to Block.Fields(
                    mapOf(
                        "name" to "Image",
                        "ext" to "jpg",
                        "mime" to "image/jpeg"
                    )
                )
            )
        )

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Foo",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val b = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.RelationBlock(key = relation1.key)
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, a.id, b.id)
        )

        val document = listOf(page, header, title, a, b)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(
            document = document,
            details = customDetails,
            relations = listOf(relation1)
        )

        // TESTING

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            checkIsRecyclerSize(3)
            onItemView(0, R.id.title).checkHasText(title.content<Block.Content.Text>().text)
            onItemView(1, R.id.textContent).checkHasText(a.content<Block.Content.Text>().text)
            onItemView(2, R.id.tvRelationTitle).checkHasText(relation1.name)
            onItemView(2, R.id.file0).checkIsDisplayed()
            onItemView(2, R.id.file1).checkIsDisplayed()
        }
    }

    @Test
    fun shouldSelectRelationBlockPlaceholder() {

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

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.RelationBlock(key = null)
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, paragraph.id, block.id)
        )

        val document = listOf(page, header, title, paragraph, block)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document, defaultDetails)

        // TESTING

        launchFragment(args)

        val rvMatcher = R.id.recycler.rVMatcher()

        with(rvMatcher) {
            checkIsRecyclerSize(3)
            onItemView(0, R.id.title).checkHasText(title.content<Block.Content.Text>().text)
            onItemView(1, R.id.textContent).checkHasText(paragraph.content<Block.Content.Text>().text)
            onItemView(1, R.id.textContent).checkIsNotSelected()
            onItemView(2, R.id.tvPlaceholder).checkHasText(R.string.set_new_relation)
            onItemView(2, R.id.tvPlaceholder).checkIsNotSelected()
        }

        rvMatcher.onItemView(1, R.id.textContent).perform(click())

        onView(withId(R.id.multiSelectModeButton)).perform(click())

        advance(EditorViewModel.DELAY_REFRESH_DOCUMENT_TO_ENTER_MULTI_SELECT_MODE)

        rvMatcher.onItemView(1, R.id.textContent).perform(click())
        rvMatcher.onItemView(2, R.id.placeholderContainer).perform(click())

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun shouldIndentRelationBlockPlaceholder() {

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.RelationBlock(key = null)
        )

        val paragraph = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = listOf(block.id),
            content = Block.Content.Text(
                text = "Foo",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, paragraph.id)
        )

        val document = listOf(page, header, title, paragraph, block)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document, defaultDetails)

        // TESTING

        launchFragment(args)

        val rvMatcher = R.id.recycler.rVMatcher()

        rvMatcher.onItemView(2, R.id.relationIcon).checkHasMarginStart(dimen = R.dimen.indent, coefficient = 1)
    }

    @Test
    fun shouldIndentPrimitiveTextRelationBlocksAtTheEnd() {

        val relation1 = Relation(
            key = MockDataFactory.randomString(),
            name = "Company",
            format = Relation.Format.SHORT_TEXT,
            source = Relation.Source.values().random()
        )

        val relation2 = Relation(
            key = MockDataFactory.randomString(),
            name = "Year",
            format = Relation.Format.NUMBER,
            source = Relation.Source.values().random()
        )

        val relation3 = Relation(
            key = MockDataFactory.randomString(),
            name = "Phone",
            format = Relation.Format.PHONE,
            source = Relation.Source.values().random()
        )

        val relation4 = Relation(
            key = MockDataFactory.randomString(),
            name = "Website",
            format = Relation.Format.URL,
            source = Relation.Source.values().random()
        )

        val relation5 = Relation(
            key = MockDataFactory.randomString(),
            name = "Email",
            format = Relation.Format.EMAIL,
            source = Relation.Source.values().random()
        )

        val value1 = "Anytype"
        val value2 = "2021"
        val value3 = "+00000000000"
        val value4 = "https://anytype.io/"
        val value5 = "team@anytype.io"

        val customDetails = Block.Details(
            mapOf(
                root to Block.Fields(
                    mapOf(
                        "iconEmoji" to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
                        relation1.key to value1,
                        relation2.key to value2,
                        relation3.key to value3,
                        relation4.key to value4,
                        relation5.key to value5,
                    )
                )
            )
        )

        val block1 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.RelationBlock(key = relation1.key)
        )

        val block2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.RelationBlock(key = relation2.key)
        )

        val block3 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.RelationBlock(key = relation3.key)
        )

        val block4 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.RelationBlock(key = relation4.key)
        )

        val block5 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.RelationBlock(key = relation5.key)
        )

        val paragraph = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = listOf(block1.id, block2.id, block3.id, block4.id, block5.id),
            content = Block.Content.Text(
                text = "Foo",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, paragraph.id)
        )

        val document = listOf(page, header, title, paragraph, block1, block2, block3, block4, block5)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(
            document = document,
            details = customDetails,
            relations = listOf(relation1, relation2, relation3, relation4, relation5)
        )

        // TESTING

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            checkIsRecyclerSize(7)
            onItemView(2, R.id.tvRelationTitle).checkHasPaddingLeft(R.dimen.indent, 1)
            onItemView(3, R.id.tvRelationTitle).checkHasPaddingLeft(R.dimen.indent, 1)
            onItemView(4, R.id.tvRelationTitle).checkHasPaddingLeft(R.dimen.indent, 1)
            onItemView(5, R.id.tvRelationTitle).checkHasPaddingLeft(R.dimen.indent, 1)
            onItemView(6, R.id.tvRelationTitle).checkHasPaddingLeft(R.dimen.indent, 1)
        }
    }

    @Test
    fun shouldIndentStatusRelationBlockAtTheEnd() {

        val option1 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            color = ThemeColor.BLUE.title,
            text = "In testing"
        )

        val relation1 = Relation(
            key = MockDataFactory.randomString(),
            name = "Status",
            format = Relation.Format.STATUS,
            source = Relation.Source.values().random(),
            selections = listOf(option1)
        )

        val customDetails = Block.Details(
            mapOf(
                root to Block.Fields(
                    mapOf(
                        "iconEmoji" to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
                        relation1.key to option1.id
                    )
                )
            )
        )

        val block1 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.RelationBlock(key = relation1.key)
        )

        val paragraph = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = listOf(block1.id),
            content = Block.Content.Text(
                text = "Foo",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, paragraph.id)
        )

        val document = listOf(page, header, title, paragraph, block1)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(
            document = document,
            details = customDetails,
            relations = listOf(relation1)
        )

        // TESTING

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            checkIsRecyclerSize(3)
            onItemView(2, R.id.tvRelationTitle).checkHasPaddingLeft(R.dimen.indent, 1)
        }
    }

    @Test
    fun shouldSelectAllBasicTextRelationBlocks() {

        val relation1 = Relation(
            key = MockDataFactory.randomString(),
            name = "Company",
            format = Relation.Format.SHORT_TEXT,
            source = Relation.Source.values().random()
        )

        val relation2 = Relation(
            key = MockDataFactory.randomString(),
            name = "Year",
            format = Relation.Format.NUMBER,
            source = Relation.Source.values().random()
        )

        val relation3 = Relation(
            key = MockDataFactory.randomString(),
            name = "Phone",
            format = Relation.Format.PHONE,
            source = Relation.Source.values().random()
        )

        val relation4 = Relation(
            key = MockDataFactory.randomString(),
            name = "Website",
            format = Relation.Format.URL,
            source = Relation.Source.values().random()
        )

        val relation5 = Relation(
            key = MockDataFactory.randomString(),
            name = "Email",
            format = Relation.Format.EMAIL,
            source = Relation.Source.values().random()
        )

        val value1 = "Anytype"
        val value2 = "2021"
        val value3 = "+00000000000"
        val value4 = "https://anytype.io/"
        val value5 = "team@anytype.io"

        val customDetails = Block.Details(
            mapOf(
                root to Block.Fields(
                    mapOf(
                        "iconEmoji" to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
                        relation1.key to value1,
                        relation2.key to value2,
                        relation3.key to value3,
                        relation4.key to value4,
                        relation5.key to value5,
                    )
                )
            )
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

        val block1 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.RelationBlock(key = relation1.key)
        )

        val block2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.RelationBlock(key = relation2.key)
        )

        val block3 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.RelationBlock(key = relation3.key)
        )

        val block4 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.RelationBlock(key = relation4.key)
        )

        val block5 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.RelationBlock(key = relation5.key)
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, paragraph.id, block1.id, block2.id, block3.id, block4.id, block5.id)
        )

        val document = listOf(page, header, title, paragraph, block1, block2, block3, block4, block5)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(
            document = document,
            details = customDetails,
            relations = listOf(relation1, relation2, relation3, relation4, relation5)
        )

        // TESTING

        launchFragment(args)

        val rvMatcher = R.id.recycler.rVMatcher()

        with(rvMatcher) {
            checkIsRecyclerSize(7)
            onItemView(1, R.id.textContent).checkIsNotSelected()
            onItemView(2, R.id.tvRelationTitle).checkIsNotSelected()
            onItemView(2, R.id.tvRelationValue).checkIsNotSelected()
            onItemView(3, R.id.tvRelationTitle).checkIsNotSelected()
            onItemView(3, R.id.tvRelationValue).checkIsNotSelected()
            onItemView(4, R.id.tvRelationTitle).checkIsNotSelected()
            onItemView(4, R.id.tvRelationValue).checkIsNotSelected()
            onItemView(5, R.id.tvRelationTitle).checkIsNotSelected()
            onItemView(5, R.id.tvRelationValue).checkIsNotSelected()
            onItemView(6, R.id.tvRelationTitle).checkIsNotSelected()
            onItemView(6, R.id.tvRelationValue).checkIsNotSelected()
        }

        rvMatcher.onItemView(1, R.id.textContent).perform(click())

        onView(withId(R.id.multiSelectModeButton)).perform(click())

        advance(EditorViewModel.DELAY_REFRESH_DOCUMENT_TO_ENTER_MULTI_SELECT_MODE)

        rvMatcher.onItemView(1, R.id.textContent).perform(click())
        rvMatcher.onItemView(2, R.id.content).perform(click())
        rvMatcher.onItemView(3, R.id.content).perform(click())
        rvMatcher.onItemView(4, R.id.content).perform(click())
        rvMatcher.onItemView(5, R.id.content).perform(click())
        rvMatcher.onItemView(6, R.id.content).perform(click())

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        with(rvMatcher) {
            checkIsRecyclerSize(7)
            onItemView(1, R.id.textContent).checkIsSelected()
            onItemView(2, R.id.content).checkIsSelected()
            onItemView(3, R.id.content).checkIsSelected()
            onItemView(4, R.id.content).checkIsSelected()
            onItemView(5, R.id.content).checkIsSelected()
            onItemView(6, R.id.content).checkIsSelected()
        }
    }

    // STUBBING & SETUP

    private fun launchFragment(args: Bundle): FragmentScenario<TestEditorFragment> {
        return launchFragmentInContainer<TestEditorFragment>(
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