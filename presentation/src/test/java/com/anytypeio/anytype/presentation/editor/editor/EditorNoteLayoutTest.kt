package com.anytypeio.anytype.presentation.editor.editor

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.model.EditorFooter
import com.anytypeio.anytype.presentation.relations.DocumentRelationView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import kotlinx.coroutines.runBlocking
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime

class EditorNoteLayoutTest : EditorPresentationTestSetup() {

    @get:Rule
    val timberTestRule: TimberTestRule = TimberTestRule.builder()
        .minPriority(Log.DEBUG)
        .showThread(true)
        .showTimestamp(false)
        .onlyLogWhenTestFails(true)
        .build()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @After
    fun after() {
        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @ExperimentalTime
    @Test
    fun `should render featured relations block and show note header as footer`() = runBlocking {

        val featuredBlock = Block(
            id = "featuredRelations",
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.FeaturedRelations
        )

        val header = Block(
            id = "header",
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.HEADER
            ),
            fields = Block.Fields.empty(),
            children = listOf(featuredBlock.id)
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(SmartBlockType.PAGE),
            children = listOf(header.id)
        )

        val doc = listOf(page, header, featuredBlock)

        val objectTypeId = "objectTypeId"
        val objectTypeName = "objectTypeName"
        val objectTypeDescription = "objectTypeDesc"

        val r1 = MockTypicalDocumentFactory.relation("Ad")
        val r2 = MockTypicalDocumentFactory.relation("De")
        val r3 = MockTypicalDocumentFactory.relation("HJ")
        val relationObjectType = Relation(
            key = Block.Fields.TYPE_KEY,
            name = "Object Type",
            format = Relation.Format.OBJECT,
            source = Relation.Source.DERIVED
        )


        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val objectFields = Block.Fields(
            mapOf(
                r1.key to value1,
                r2.key to value2,
                r3.key to value3,
                relationObjectType.key to objectTypeId,
                Relations.FEATURED_RELATIONS to listOf(relationObjectType.key),
                Relations.LAYOUT to ObjectType.Layout.NOTE.code.toDouble()
            )
        )

        val objectTypeFields = Block.Fields(
            mapOf(
                Relations.NAME to objectTypeName,
                Relations.DESCRIPTION to objectTypeDescription
            )
        )
        val customDetails = Block.Details(
            mapOf(
                root to objectFields,
                objectTypeId to objectTypeFields
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubGetObjectTypes(objectTypes = listOf())
        stubGetDefaultObjectType()
        stubOpenDocument(
            document = doc,
            details = customDetails,
            relations = listOf(r1, r2, r3, relationObjectType)
        )

        val vm = buildViewModel()

        vm.onStart(root)

        val expected = listOf(
            BlockView.FeaturedRelation(
                id = featuredBlock.id,
                relations = listOf(
                    DocumentRelationView.ObjectType(
                        relationId = relationObjectType.key,
                        name = objectTypeName,
                        value = null,
                        isFeatured = true,
                        type = objectTypeId
                    )
                )
            )
        )

        vm.state.test().assertValue(ViewState.Success(expected))

        vm.footers.test {
            val result = awaitItem()
            assertEquals(EditorFooter.Note, result)
            cancelAndConsumeRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `should render featured relations block and not show note header as footer`() = runBlocking {

        val featuredBlock = Block(
            id = "featuredRelations",
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.FeaturedRelations
        )

        val header = Block(
            id = "header",
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.HEADER
            ),
            fields = Block.Fields.empty(),
            children = listOf(featuredBlock.id)
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(SmartBlockType.PAGE),
            children = listOf(header.id)
        )

        val doc = listOf(page, header, featuredBlock)

        val objectTypeId = "objectTypeId"
        val objectTypeName = "objectTypeName"
        val objectTypeDescription = "objectTypeDesc"

        val r1 = MockTypicalDocumentFactory.relation("Ad")
        val r2 = MockTypicalDocumentFactory.relation("De")
        val r3 = MockTypicalDocumentFactory.relation("HJ")
        val relationObjectType = Relation(
            key = Block.Fields.TYPE_KEY,
            name = "Object Type",
            format = Relation.Format.OBJECT,
            source = Relation.Source.DERIVED
        )


        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val objectFields = Block.Fields(
            mapOf(
                r1.key to value1,
                r2.key to value2,
                r3.key to value3,
                relationObjectType.key to objectTypeId,
                Relations.FEATURED_RELATIONS to listOf(relationObjectType.key),
                Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
            )
        )

        val objectTypeFields = Block.Fields(
            mapOf(
                Relations.NAME to objectTypeName,
                Relations.DESCRIPTION to objectTypeDescription
            )
        )
        val customDetails = Block.Details(
            mapOf(
                root to objectFields,
                objectTypeId to objectTypeFields
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubGetObjectTypes(objectTypes = listOf())
        stubGetDefaultObjectType()
        stubOpenDocument(
            document = doc,
            details = customDetails,
            relations = listOf(r1, r2, r3, relationObjectType)
        )

        val vm = buildViewModel()

        vm.onStart(root)

        val expected = listOf(
            BlockView.FeaturedRelation(
                id = featuredBlock.id,
                relations = listOf(
                    DocumentRelationView.ObjectType(
                        relationId = relationObjectType.key,
                        name = objectTypeName,
                        value = null,
                        isFeatured = true,
                        type = objectTypeId
                    )
                )
            )
        )

        vm.state.test().assertValue(ViewState.Success(expected))

        vm.footers.test {
            val result = awaitItem()
            assertEquals(EditorFooter.None, result)
            cancelAndConsumeRemainingEvents()
        }
    }

}