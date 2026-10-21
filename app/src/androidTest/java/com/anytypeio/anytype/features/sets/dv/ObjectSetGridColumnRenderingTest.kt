package com.anytypeio.anytype.features.sets.dv

import androidx.core.os.bundleOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVViewerRelation
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.presentation.MockBlockContentFactory.StubTextContent
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.utils.checkHasText
import com.anytypeio.anytype.test_utils.utils.checkIsRecyclerSize
import com.anytypeio.anytype.test_utils.utils.onItemView
import com.anytypeio.anytype.test_utils.utils.rVMatcher
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class ObjectSetGridColumnRenderingTest : TestObjectSetSetup() {


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
    fun shouldRenderAllColumnHeaderNamesBasedOnViewerRelations() {

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
        val relations = listOf(relation1, relation2, relation3, relation4, relation5)
        stubRelations(relations)

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
                relationLinks = relations.map { RelationLink(it.key, it.format) }
            )
        )

        val root = Block(
            id = ctx,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, dataview.id)
        )

        val set = listOf(root, header, title, dataview)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubSubscriptionEventChannel()
        stubSearchWithSubscription(listOf(ObjectWrapper.Basic(mapOf(
            Relations.ID to "fixture-record",
            Relations.NAME to "Fixture record",
            Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
        ))))
        stubOpenObjectSetWithRecord(
            set = set,
            details = defaultDetails
        )

        // TESTING

        val scenario = launchFragment(bundleOf(ObjectSetFragment.CONTEXT_ID_KEY to ctx))
        // Subscription/mapping runs on real IO dispatchers outside Espresso's idling resources.
        // Wait for the fixture record to be committed before asserting the five header cells.
        val deadline = android.os.SystemClock.uptimeMillis() + 5_000
        var recordsReady = false
        while (!recordsReady && android.os.SystemClock.uptimeMillis() < deadline) {
            scenario.onFragment { fragment ->
                recordsReady = (fragment.requireView().findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvRows).adapter?.itemCount ?: 0) > 0
            }
            if (!recordsReady) android.os.SystemClock.sleep(32)
        }
        org.junit.Assert.assertTrue("Fixture record must be committed before rendering assertions", recordsReady)

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
