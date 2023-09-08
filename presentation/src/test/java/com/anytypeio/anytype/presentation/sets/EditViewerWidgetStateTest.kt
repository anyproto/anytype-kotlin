package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.presentation.collections.MockSet
import com.anytypeio.anytype.presentation.sets.main.ObjectSetViewModelTestSetup
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class EditViewerWidgetStateTest : ObjectSetViewModelTestSetup() {

    private lateinit var closable: AutoCloseable
    private lateinit var viewModel: ObjectSetViewModel
    private lateinit var mockObjectSet: MockSet

    val templateView = StubObject(objectType = ObjectTypeIds.PAGE)

    @Before
    fun setup() {
        closable = MockitoAnnotations.openMocks(this)
        viewModel = givenViewModel()
        runTest {
            mockObjectSet = MockSet(context = root, setOfValue = ObjectTypeIds.PAGE)
            val pageTypeMap = mapOf(
                Relations.ID to ObjectTypeIds.PAGE,
                Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
                Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
                Relations.NAME to MockDataFactory.randomString()
            )
            stubWorkspaceManager(mockObjectSet.workspaceId)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubOpenObject(
                doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataViewWith3Views),
                details = mockObjectSet.details
            )
            stubSubscriptionResults(
                subscription = mockObjectSet.subscriptionId,
                workspace = mockObjectSet.workspaceId,
                storeOfRelations = storeOfRelations,
                keys = mockObjectSet.dvKeys,
                sources = listOf(ObjectTypeIds.PAGE),
                dvFilters = mockObjectSet.filters
            )
            stubStoreOfObjectTypes(pageTypeMap)
            stubTemplatesContainer(
                type = ObjectTypeIds.PAGE,
                templates = listOf(templateView)
            )
        }
    }

    @After
    fun after() {
        rule.advanceTime()
        closable.close()
    }

    @Test
    fun `default object type for viewer should be PAGE`() {

        //DataView with 3 Views: View1(Grid), View2(Gallery), View3(List)
        //View1(Grid) is active

        session.currentViewerId.value = mockObjectSet.viewerGrid.id

        // TESTING
        viewModel.onStart(ctx = root)
        viewModel.onViewersWidgetAction(action = ViewersWidgetUi.Action.Edit(mockObjectSet.viewerGallery.id))

        // VERIFY
        val expected = ViewerEditWidgetUi(
            showWidget = true,

        )

        assertEquals(
            expected = ObjectTypeIds.AUDIO,
            actual = viewModel.viewerEditWidgetState.value.defaultObjectType?.id
        )
    }
}