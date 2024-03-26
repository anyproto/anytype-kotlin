package com.anytypeio.anytype.features.editor

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.emojifier.data.DefaultDocumentEmojiIconProvider
import com.anytypeio.anytype.features.editor.base.EditorTestSetup
import com.anytypeio.anytype.features.editor.base.TestEditorFragment
import com.anytypeio.anytype.presentation.MockBlockContentFactory
import com.anytypeio.anytype.presentation.MockBlockFactory
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.utils.checkHasViewGroupChildWithText
import com.anytypeio.anytype.test_utils.utils.matchView
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class FeaturedRelationTesting : EditorTestSetup() {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    private val args = bundleOf(EditorFragment.CTX_KEY to root)

    private val title = MockBlockFactory.text(
        content = MockBlockContentFactory.StubTextContent(
            style = Block.Content.Text.Style.TITLE,
            text = "Relation Block UI Testing",
            marks = emptyList()
        )
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
    fun shouldRenderThreePrimitiveFeaturedRelations() {

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
                        "featuredRelations" to listOf(
                            relation1.key,
                            relation4.key,
                            relation5.key
                        )
                    )
                )
            )
        )

        val paragraph = MockBlockFactory.paragraph()

        val block1 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.FeaturedRelations
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, paragraph.id, block1.id)
        )

        val document = listOf(page, header, title, paragraph, block1)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubAnalytics()
        stubUpdateText()
        stubOpenDocument(
            document = document,
            details = customDetails,
            relations = listOf(relation1, relation2, relation3, relation4, relation5)
        )

        // TESTING

        launchFragment(args)

        R.id.featuredRelationRoot.matchView().apply {
            checkHasViewGroupChildWithText(1, value1)
            checkHasViewGroupChildWithText(3, value4)
            checkHasViewGroupChildWithText(5, value5)
        }
    }

    @Test
    fun shouldRenderTwoPrimitiveFeaturedRelationsAndExcludeDescriptionRelation() {

        val relation1 = Relation(
            key = Relations.DESCRIPTION,
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
                        "featuredRelations" to listOf(
                            relation1.key,
                            relation4.key,
                            relation5.key
                        )
                    )
                )
            )
        )

        val paragraph = MockBlockFactory.paragraph(text = "Foo")

        val block1 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.FeaturedRelations
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, paragraph.id, block1.id)
        )

        val document = listOf(page, header, title, paragraph, block1)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubAnalytics()
        stubUpdateText()
        stubOpenDocument(
            document = document,
            details = customDetails,
            relations = listOf(relation1, relation2, relation3, relation4, relation5)
        )

        // TESTING

        launchFragment(args)

        R.id.featuredRelationRoot.matchView().apply {
            checkHasViewGroupChildWithText(1, value4)
            checkHasViewGroupChildWithText(3, value5)
        }
    }

    private fun launchFragment(args: Bundle): FragmentScenario<TestEditorFragment> {
        return launchFragmentInContainer(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }
}