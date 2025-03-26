package com.anytypeio.anytype.presentation.editor.editor

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubObjectType
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.model.EditorFooter
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

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

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun after() {
        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @ExperimentalTime
    @Test
    fun `should render featured relations block and show note header as footer`() = runTest {

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
            content = Block.Content.Smart,
            children = listOf(header.id)
        )

        val doc = listOf(page, header, featuredBlock)

        val objectTypeId = "objectTypeId"
        val objectTypeName = "objectTypeName"
        val objectTypeDescription = "objectTypeDesc"

        val r1 = StubRelationObject(name = "Ad")
        val r2 = StubRelationObject(name = "De")
        val r3 = StubRelationObject(name = "HJ")
        val objectRelations = listOf(r1, r2, r3)

        val objectType = StubObjectType(
            id = objectTypeId,
            name = "Object Type",
            recommendedFeaturedRelations = listOf(r1.id, r2.id, r3.id),
        )

        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val objectFields =
            mapOf(
                Relations.ID to root,
                r1.key to value1,
                r2.key to value2,
                r3.key to value3,
                Relations.TYPE to objectType.id,
                Relations.LAYOUT to ObjectType.Layout.NOTE.code.toDouble()
            )

        val objectTypeFields =
            mapOf(
                Relations.ID to objectTypeId,
                Relations.UNIQUE_KEY to objectType.uniqueKey,
                Relations.NAME to objectTypeName,
                Relations.DESCRIPTION to objectTypeDescription,
                Relations.RECOMMENDED_FEATURED_RELATIONS to listOf<>()
            )
        val customDetails = ObjectViewDetails(
            mapOf(
                root to objectFields,
                objectTypeId to objectTypeFields
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubSearchObjects()
        stubGetDefaultObjectType()
        stubOpenDocument(
            document = doc,
            details = customDetails
        )

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        val expected = listOf(
            BlockView.FeaturedRelation(
                id = featuredBlock.id,
                relations = listOf(
                    ObjectRelationView.ObjectType.Base(
                        id = objectType.id,
                        key = Relations.TYPE,
                        name = objectTypeName,
                        value = null,
                        featured = true,
                        type = objectTypeId,
                        system = true
                    )
                )
            )
        )

        assertEquals(
            expected = ViewState.Success(expected),
            actual = vm.state.value
        )

        vm.footers.test {
            val result = awaitItem()
            assertEquals(EditorFooter.Note, result)
            cancelAndConsumeRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `should render featured relations block and not show note header as footer`() = runTest {

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
            content = Block.Content.Smart,
            children = listOf(header.id)
        )

        val doc = listOf(page, header, featuredBlock)

        val objectTypeName = "objectTypeName"
        val objectTypeDescription = "objectTypeDesc"

        val r1 = StubRelationObject(name = "Ad")
        val r2 = StubRelationObject(name = "De")
        val r3 = StubRelationObject(name = "HJ")
        val objectRelations = listOf(r1, r2, r3)

        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val objectFields =
            mapOf(
                Relations.ID to root,
                r1.key to value1,
                r2.key to value2,
                r3.key to value3,
                Relations.TYPE to objType.id,
                Relations.FEATURED_RELATIONS to listOf(Relations.TYPE),
                Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
            )

        val customDetails = ObjectViewDetails(
            mapOf(
                root to objectFields,
                objType.id to objType.map
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubSearchObjects()
        stubGetDefaultObjectType()
        stubOpenDocument(
            document = doc,
            details = customDetails
        )

        val vm = buildViewModel()
        storeOfRelations.merge(objectRelations)

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        val expected = listOf(
            BlockView.FeaturedRelation(
                id = featuredBlock.id,
                relations = listOf(
                    ObjectRelationView.ObjectType.Base(
                        id = objType.id,
                        key = Relations.TYPE,
                        name = objectTypeName,
                        value = null,
                        featured = true,
                        type = objType.id,
                        system = true
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