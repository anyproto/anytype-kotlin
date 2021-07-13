package com.anytypeio.anytype.presentation.sets.main

import MockDataFactory
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_models.restrictions.DataViewRestriction
import com.anytypeio.anytype.core_models.restrictions.DataViewRestrictions
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.model.CellView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class ObjectSetCellTest : ObjectSetViewModelTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @get:Rule
    val timberTestRule: TimberTestRule = TimberTestRule.builder()
        .minPriority(Log.DEBUG)
        .showThread(true)
        .showTimestamp(false)
        .onlyLogWhenTestFails(true)
        .build()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @After
    fun after() {
        coroutineTestRule.advanceTime(100)
    }

    val title = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Text(
            style = Block.Content.Text.Style.TITLE,
            text = MockDataFactory.randomString(),
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    val header = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    val relations = listOf(
        Relation(
            key = MockDataFactory.randomString(),
            name = MockDataFactory.randomString(),
            source = Relation.Source.DETAILS,
            defaultValue = null,
            format = Relation.Format.LONG_TEXT,
            isHidden = false,
            isMulti = false,
            isReadOnly = false,
            selections = emptyList()
        ),
        Relation(
            key = MockDataFactory.randomString(),
            name = MockDataFactory.randomString(),
            source = Relation.Source.DETAILS,
            defaultValue = null,
            format = Relation.Format.LONG_TEXT,
            isHidden = false,
            isMulti = false,
            isReadOnly = true,
            selections = emptyList()
        )
    )

    val vrelations = relations.map { relation ->
        DVViewerRelation(
            key = relation.key,
            isVisible = true
        )
    }

    val firstRecordId = "firstRecordId"
    val secondRecordId = "secondRecordId"
    val firstRecordName = MockDataFactory.randomString()
    val secondRecordName = MockDataFactory.randomString()
    val firstRecordType = MockDataFactory.randomString()
    val secondRecordType = MockDataFactory.randomString()

    val firstRecord = mapOf(
        ObjectSetConfig.ID_KEY to firstRecordId,
        ObjectSetConfig.NAME_KEY to firstRecordName,
        ObjectSetConfig.TYPE_KEY to firstRecordType,
        relations[0].key to MockDataFactory.randomString(),
        relations[1].key to MockDataFactory.randomString()
    )

    val secondRecord = mapOf(
        ObjectSetConfig.ID_KEY to secondRecordId,
        ObjectSetConfig.NAME_KEY to secondRecordName,
        ObjectSetConfig.TYPE_KEY to secondRecordType,
        relations[0].key to MockDataFactory.randomString(),
        relations[1].key to MockDataFactory.randomString()
    )

    val initialRecords = listOf(firstRecord, secondRecord)

    val viewer1 = DVViewer(
        id = MockDataFactory.randomUuid(),
        filters = emptyList(),
        sorts = emptyList(),
        type = Block.Content.DataView.Viewer.Type.GRID,
        name = MockDataFactory.randomString(),
        viewerRelations = vrelations
    )

    val viewer2 = DVViewer(
        id = MockDataFactory.randomUuid(),
        filters = emptyList(),
        sorts = emptyList(),
        type = Block.Content.DataView.Viewer.Type.GRID,
        name = MockDataFactory.randomString(),
        viewerRelations = vrelations.mapIndexed { index, viewerRelation ->
            if (index == 0)
                viewerRelation.copy(isVisible = false)
            else
                viewerRelation
        }
    )

    val dv = Block(
        id = MockDataFactory.randomUuid(),
        content = DV(
            source = MockDataFactory.randomString(),
            relations = relations,
            viewers = listOf(viewer1, viewer2)
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    @Test
    fun `should show error toast when clicked on read only cell`() = runBlocking {

        val dvRestrictions = listOf<DataViewRestrictions>(
            DataViewRestrictions(
                block = dv.id,
                restrictions = listOf(DataViewRestriction.VIEWS)
            )
        )

        stubInterceptEvents()
        stubOpenObjectSet(
            doc = listOf(
                header,
                title,
                dv
            ),
            dataViewRestrictions = dvRestrictions
        )

        stubSetActiveViewer()
        stubUpdateDataViewViewer()

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

        val cell = CellView.Description(
            id = firstRecordId,
            key = relations[1].key,
            text = firstRecord.get(relations[1].key).toString()
        )

        vm.onGridCellClicked(cell = cell)

        val result = vm.toasts.stream().first()

        assertEquals(ObjectSetViewModel.NOT_ALLOWED_CELL, result)
    }

    @Test
    fun `should show error toast on open readonly object as page 1`() = runBlocking {

        val objectTypePage = "_otpage"
        val objectTypeImage = "_otimage"
        val objectTypeSet = "_otset"

        val objectTypes = listOf(
            ObjectType(
                url = objectTypePage,
                name = "Page",
                relations = listOf(),
                layout = ObjectType.Layout.BASIC,
                emoji = "",
                smartBlockTypes = listOf(SmartBlockType.PAGE),
                isHidden = false,
                description = "page"
            ),
            ObjectType(
                url = objectTypeImage,
                name = "Image",
                relations = listOf(),
                layout = ObjectType.Layout.IMAGE,
                emoji = "",
                smartBlockTypes = listOf(SmartBlockType.FILE),
                isHidden = false,
                description = "image"
            ),
            ObjectType(
                url = objectTypeSet,
                name = "Set",
                relations = listOf(),
                layout = ObjectType.Layout.SET,
                emoji = "",
                smartBlockTypes = listOf(SmartBlockType.SET),
                isHidden = false,
                description = "set"
            )
        )

        stubInterceptEvents()
        stubOpenObjectSet(
            doc = listOf(
                header,
                title,
                dv
            ),
            objectTypes = objectTypes
        )

        stubSetActiveViewer()
        stubUpdateDataViewViewer()

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

        vm.onObjectClicked(id = "dsadas", types = listOf())

        val result = vm.toasts.stream().first()

        assertEquals(ObjectSetViewModel.OBJECT_TYPE_UNKNOWN, result)
    }

    @Test
    fun `should show error toast on open readonly object as page 2`() = runBlocking {

        val objectTypePage = "_otpage"
        val objectTypeImage = "_otimage"
        val objectTypeSet = "_otset"

        val objectTypes = listOf(
            ObjectType(
                url = objectTypePage,
                name = "Page",
                relations = listOf(),
                layout = ObjectType.Layout.BASIC,
                emoji = "",
                smartBlockTypes = listOf(SmartBlockType.PAGE),
                isHidden = false,
                description = "page"
            ),
            ObjectType(
                url = objectTypeImage,
                name = "Image",
                relations = listOf(),
                layout = ObjectType.Layout.IMAGE,
                emoji = "",
                smartBlockTypes = listOf(SmartBlockType.FILE),
                isHidden = false,
                description = "image"
            ),
            ObjectType(
                url = objectTypeSet,
                name = "Set",
                relations = listOf(),
                layout = ObjectType.Layout.SET,
                emoji = "",
                smartBlockTypes = listOf(SmartBlockType.SET),
                isHidden = false,
                description = "set"
            )
        )

        stubInterceptEvents()
        stubOpenObjectSet(
            doc = listOf(
                header,
                title,
                dv
            ),
            objectTypes = objectTypes
        )

        stubSetActiveViewer()
        stubUpdateDataViewViewer()

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

        vm.onObjectClicked(id = "dsadas", types = listOf("p6Oa"))

        val result = vm.toasts.stream().first()

        assertEquals(ObjectSetViewModel.OBJECT_TYPE_UNKNOWN, result)
    }

    @Test
    fun `should proceed with open page when open readonly object as page`() = runBlocking {

        val objectTypePage = "_otpage"
        val objectTypeImage = "_otimage"
        val objectTypeSet = "_otset"
        val objectId = "UnI9A1"

        val objectTypes = listOf(
            ObjectType(
                url = objectTypePage,
                name = "Page",
                relations = listOf(),
                layout = ObjectType.Layout.BASIC,
                emoji = "",
                smartBlockTypes = listOf(SmartBlockType.PAGE),
                isHidden = false,
                description = "page"
            ),
            ObjectType(
                url = objectTypeImage,
                name = "Image",
                relations = listOf(),
                layout = ObjectType.Layout.IMAGE,
                emoji = "",
                smartBlockTypes = listOf(SmartBlockType.FILE),
                isHidden = false,
                description = "image"
            ),
            ObjectType(
                url = objectTypeSet,
                name = "Set",
                relations = listOf(),
                layout = ObjectType.Layout.SET,
                emoji = "",
                smartBlockTypes = listOf(SmartBlockType.SET),
                isHidden = false,
                description = "set"
            )
        )

        stubInterceptEvents()
        stubOpenObjectSet(
            doc = listOf(
                header,
                title,
                dv
            ),
            objectTypes = objectTypes
        )

        stubSetActiveViewer()
        stubUpdateDataViewViewer()

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

        vm.onObjectClicked(id = objectId, types = listOf(objectTypePage))

        val result = vm.navigation.value!!.peekContent()

        assertEquals(AppNavigation.Command.OpenPage(id = objectId), result)
    }

    @Test
    fun `should proceed with open set when open readonly object as set`() = runBlocking {

        val objectTypePage = "_otpage"
        val objectTypeImage = "_otimage"
        val objectTypeSet = "_otset"
        val objectId = "UnI9A1"

        val objectTypes = listOf(
            ObjectType(
                url = objectTypePage,
                name = "Page",
                relations = listOf(),
                layout = ObjectType.Layout.BASIC,
                emoji = "",
                smartBlockTypes = listOf(SmartBlockType.PAGE),
                isHidden = false,
                description = "page"
            ),
            ObjectType(
                url = objectTypeImage,
                name = "Image",
                relations = listOf(),
                layout = ObjectType.Layout.IMAGE,
                emoji = "",
                smartBlockTypes = listOf(SmartBlockType.FILE),
                isHidden = false,
                description = "image"
            ),
            ObjectType(
                url = objectTypeSet,
                name = "Set",
                relations = listOf(),
                layout = ObjectType.Layout.SET,
                emoji = "",
                smartBlockTypes = listOf(SmartBlockType.SET),
                isHidden = false,
                description = "set"
            )
        )

        stubInterceptEvents()
        stubOpenObjectSet(
            doc = listOf(
                header,
                title,
                dv
            ),
            objectTypes = objectTypes
        )

        stubSetActiveViewer()
        stubUpdateDataViewViewer()

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

        vm.onObjectClicked(id = objectId, types = listOf(objectTypeSet, objectTypeImage))

        val result = vm.navigation.value!!.peekContent()

        assertEquals(AppNavigation.Command.OpenObjectSet(target = objectId), result)
    }
}