package com.anytypeio.anytype.features.sets.dv

import androidx.core.os.bundleOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVViewerRelation
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.emojifier.data.DefaultDocumentEmojiIconProvider
import com.anytypeio.anytype.presentation.MockBlockContentFactory.StubTextContent
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.utils.checkHasChildViewCount
import com.anytypeio.anytype.test_utils.utils.checkHasChildViewWithText
import com.anytypeio.anytype.test_utils.utils.checkHasText
import com.anytypeio.anytype.test_utils.utils.checkIsRecyclerSize
import com.anytypeio.anytype.test_utils.utils.onItemView
import com.anytypeio.anytype.test_utils.utils.rVMatcher
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class ObjectSetGridPrimitiveRelationTest : TestObjectSetSetup() {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    override val title: Block = Block(
        id = MockDataFactory.randomUuid(),
        content = StubTextContent(
            style = Block.Content.Text.Style.TITLE,
            text = "Data View UI Testing",
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    @Before
    override fun setup() {
        super.setup()
    }

    @Test
    fun shouldRenderAllObjectPrimitiveRelationsValuesFromTwoRecords() {

        val type = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            emoji = MockDataFactory.randomString(),
            layout = ObjectType.Layout.BASIC,
            relationLinks = emptyList(),
            description = "",
            isHidden = false,
            smartBlockTypes = listOf(),
            isArchived = false,
            isReadOnly = false
        )

        val relation1 = Relation(
            key = MockDataFactory.randomString(),
            name = "Description",
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

        val object1value1 = "Operating environment for the new Internet"
        val object1value2 = "2021"
        val object1value3 = "+00000000000"
        val object1value4 = "https://anytype.io/"
        val object1value5 = "team@anytype.io"

        val object2value1 = "A peer-to-peer hypermedia protocol designed to make the web faster, safer, and more open."
        val object2value2 = "2021"
        val object2value3 = "+00000000000"
        val object2value4 = "https://ipfs.io/"
        val object2value5 = "team@ipfs.io"

        val record1 = mapOf(
            ObjectSetConfig.ID_KEY to MockDataFactory.randomUuid(),
            ObjectSetConfig.TYPE_KEY to type.url,
            ObjectSetConfig.NAME_KEY to "Anytype",
            "iconEmoji" to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
            relation1.key to object1value1,
            relation2.key to object1value2,
            relation3.key to object1value3,
            relation4.key to object1value4,
            relation5.key to object1value5,
        )

        val record2 = mapOf(
            ObjectSetConfig.ID_KEY to MockDataFactory.randomUuid(),
            ObjectSetConfig.TYPE_KEY to type.url,
            ObjectSetConfig.NAME_KEY to "IPFS",
            "iconEmoji" to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
            relation1.key to object2value1,
            relation2.key to object2value2,
            relation3.key to object2value3,
            relation4.key to object2value4,
            relation5.key to object2value5,
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = "Default Grid View",
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = listOf(
                DVViewerRelation(
                    key = relation1.key,
                    isVisible = true
                ),
                DVViewerRelation(
                    key = relation2.key,
                    isVisible = true
                ),
                DVViewerRelation(
                    key = relation3.key,
                    isVisible = true
                ),
                DVViewerRelation(
                    key = relation4.key,
                    isVisible = true
                ),
                DVViewerRelation(
                    key = relation5.key,
                    isVisible = true
                )
            ),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        val dataview = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            fields = Block.Fields.empty(),
            content = Block.Content.DataView(
                viewers = listOf(viewer),
                
            )
        )

        val root = Block(
            id = ctx,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, dataview.id)
        )

        val set = listOf(root, header, title, dataview)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObjectSetWithRecord(
            set = set,
            relations = listOf(relation1, relation2, relation3, relation4, relation5),
            details = defaultDetails,
            objectTypes = listOf(type)
        )

        // TESTING

        launchFragment(bundleOf(ObjectSetFragment.CONTEXT_ID_KEY to ctx))

        with(R.id.rvRows.rVMatcher()) {
            checkIsRecyclerSize(2)
            onItemView(0, R.id.rowCellRecycler)
                .checkHasChildViewCount(5)
//                .checkHasChildViewWithText(
//                    pos = 0,
//                    text = "",
//                    target = R.id.tvText
//                )
                .checkHasChildViewWithText(
                    pos = 0,
                    text = object1value1,
                    target = R.id.tvText
                )
                .checkHasChildViewWithText(
                    pos = 1,
                    text = object1value2,
                    target = R.id.tvText
                )
                .checkHasChildViewWithText(
                    pos = 2,
                    text = object1value3,
                    target = R.id.tvText
                )
                .checkHasChildViewWithText(
                    pos = 3,
                    text = object1value4,
                    target = R.id.tvText
                )
                .checkHasChildViewWithText(
                    pos = 4,
                    text = object1value5,
                    target = R.id.tvText
                )

            onItemView(0, R.id.tvTitle).checkHasText("Anytype")

            onItemView(1, R.id.rowCellRecycler)
                .checkHasChildViewCount(5)
                .checkHasChildViewWithText(
                    pos = 0,
                    text = object2value1,
                    target = R.id.tvText
                )
                .checkHasChildViewWithText(
                    pos = 1,
                    text = object2value2,
                    target = R.id.tvText
                )
                .checkHasChildViewWithText(
                    pos = 2,
                    text = object2value3,
                    target = R.id.tvText
                )
                .checkHasChildViewWithText(
                    pos = 3,
                    text = object2value4,
                    target = R.id.tvText
                )
                .checkHasChildViewWithText(
                    pos = 4,
                    text = object2value5,
                    target = R.id.tvText
                )

            onItemView(1, R.id.tvTitle).checkHasText("IPFS")
        }
    }
}