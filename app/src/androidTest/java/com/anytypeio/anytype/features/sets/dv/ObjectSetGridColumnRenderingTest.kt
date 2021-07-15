package com.anytypeio.anytype.features.sets.dv

import androidx.core.os.bundleOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.mocking.MockDataFactory
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import com.anytypeio.anytype.utils.checkHasText
import com.anytypeio.anytype.utils.checkIsRecyclerSize
import com.anytypeio.anytype.utils.onItemView
import com.anytypeio.anytype.utils.rVMatcher
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class ObjectSetGridColumnRenderingTest : TestObjectSetSetup() {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    override val title: Block = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Text(
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
    fun shouldRenderAllColumnHeaderNamesBasedOnViewerRelations() {

        val type = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            emoji = MockDataFactory.randomString(),
            layout = ObjectType.Layout.BASIC,
            relations = emptyList(),
            description = "",
            isHidden = false,
            smartBlockTypes = listOf()
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
                relations = listOf(relation1, relation2, relation3, relation4, relation5),
                viewers = listOf(viewer),
                source = MockDataFactory.randomUuid()
            )
        )

        val root = Block(
            id = ctx,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(SmartBlockType.SET),
            children = listOf(header.id, dataview.id)
        )

        val set = listOf(root, header, title, dataview)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubSetActiveViewer()
        stubOpenObjectSetWithRecord(
            set = set,
            relations = listOf(relation1, relation2, relation3, relation4, relation5),
            details = defaultDetails,
            viewer = viewer.id,
            dataview = dataview.id,
            records = emptyList(),
            total = 1,
            objectTypes = listOf(type)
        )

        // TESTING

        launchFragment(bundleOf(ObjectSetFragment.CONTEXT_ID_KEY to ctx))

        with(R.id.rvHeader.rVMatcher()) {
            checkIsRecyclerSize(5)
            onItemView(0, R.id.cellText).checkHasText(relation1.name)
            onItemView(1, R.id.cellText).checkHasText(relation2.name)
            onItemView(2, R.id.cellText).checkHasText(relation3.name)
            onItemView(3, R.id.cellText).checkHasText(relation4.name)
            onItemView(4, R.id.cellText).checkHasText(relation5.name)
        }
    }
}