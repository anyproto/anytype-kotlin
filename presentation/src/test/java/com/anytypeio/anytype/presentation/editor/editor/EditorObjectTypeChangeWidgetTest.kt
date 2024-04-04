package com.anytypeio.anytype.presentation.editor.editor

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorObjectTypeChangeWidgetTest : EditorPresentationTestSetup() {

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
    val coroutineTestRule = CoroutinesTestRule()

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

    @Test
    fun `should show widget on object with select type internal flag`() {

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
            children = listOf(header.id, paragraph.id)
        )

        val doc = listOf(page, header, paragraph, featuredBlock)

        val objectDetails = Block.Fields(
            mapOf(
                Relations.SPACE_ID to defaultSpace,
                Relations.TYPE to ObjectTypeIds.NOTE,
                Relations.LAYOUT to ObjectType.Layout.NOTE.code.toDouble(),
                Relations.INTERNAL_FLAGS to listOf(1.0)
            )
        )

        val detailsList = Block.Details(details = mapOf(root to objectDetails))

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubSearchObjects()
        stubGetDefaultObjectType(type = ObjectTypeIds.NOTE)
        stubOpenDocument(
            document = doc,
            details = detailsList
        )
        stubGetObjectTypes(listOf())

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        val objectTypesWidget = vm.typesWidgetState.value

        assertNotNull(objectTypesWidget)
        assertTrue(objectTypesWidget.visible)
    }

    @Test
    fun `should not show widget on note object with empty internal flags`() {

        val paragraph = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "F",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

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
            children = listOf(header.id, paragraph.id)
        )

        val doc = listOf(page, header, paragraph, featuredBlock)

        val objectDetails = Block.Fields(
            mapOf(
                "type" to ObjectTypeIds.NOTE,
                "layout" to ObjectType.Layout.NOTE.code.toDouble()
            )
        )

        val detailsList = Block.Details(details = mapOf(root to objectDetails))

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubSearchObjects()
        stubGetDefaultObjectType(type = ObjectTypeIds.NOTE)
        stubOpenDocument(
            document = doc,
            details = detailsList
        )

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        val objectTypesWidget = vm.typesWidgetState.value

        assertNotNull(objectTypesWidget)
        assertFalse(objectTypesWidget.visible)
    }
}