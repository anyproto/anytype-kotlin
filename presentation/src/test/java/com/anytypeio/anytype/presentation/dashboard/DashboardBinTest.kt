package com.anytypeio.anytype.presentation.dashboard

import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.search.Subscriptions
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import kotlin.test.assertEquals

class DashboardBinTest : DashboardTestSetup() {

    private val objectIds = listOf(
        MockDataFactory.randomUuid(),
        MockDataFactory.randomUuid(),
        MockDataFactory.randomUuid(),
        MockDataFactory.randomUuid(),
    )

    private val objects: List<Map<String, Any?>> = listOf(
        mapOf(
            Relations.ID to objectIds[0]
        ),
        mapOf(
            Relations.ID to objectIds[1]
        ),
        mapOf(
            Relations.ID to objectIds[2]
        ),
        mapOf(
            Relations.ID to objectIds[3]
        )
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should enter select mode when block is clicked in bin tab`() {

        // SETUP

        val dashboard = Block(
            id = config.home,
            content = Block.Content.Smart(SmartBlockType.HOME),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        stubGetConfig(Either.Right(config))

        stubObserveEvents(params = InterceptEvents.Params(context = config.home))

        stubOpenDashboard(
            payload = Payload(
                context = config.home,
                events = listOf(
                    Event.Command.ShowObject(
                        root = config.home,
                        context = config.home,
                        blocks = listOf(dashboard),
                        type = SmartBlockType.HOME
                    )
                )
            )
        )

        stubGetProfile()

        repo.stub {
            onBlocking {
                searchObjectsWithSubscription(
                    subscription = Subscriptions.SUBSCRIPTION_ARCHIVED,
                    keys = ObjectSearchConstants.defaultKeys,
                    filters = ObjectSearchConstants.filterTabArchive,
                    sorts = ObjectSearchConstants.sortTabArchive,
                    limit = 0,
                    offset = 0,
                    afterId = null,
                    beforeId = null
                )
            } doReturn SearchResult(
                results = objects.map {
                    ObjectWrapper.Basic(it)
                },
                dependencies = emptyList()
            )
        }

        vm = buildViewModel()

        vm.onStart()

        // TESTING

        val expectedBeforeSelection = listOf(
            DashboardView.Document(
                id = objectIds[0],
                isArchived = true,
                target = objectIds[0]
            ),
            DashboardView.Document(
                id = objectIds[1],
                isArchived = true,
                target = objectIds[1]
            ),
            DashboardView.Document(
                id = objectIds[2],
                isArchived = true,
                target = objectIds[2]
            ),
            DashboardView.Document(
                id = objectIds[3],
                isArchived = true,
                target = objectIds[3]
            )
        )

        assertEquals(
            expected = expectedBeforeSelection,
            actual = vm.archived.value
        )

        assertEquals(
            expected = HomeDashboardViewModel.Mode.DEFAULT,
            actual = vm.mode.value
        )

        vm.onTabObjectClicked(
            target = objectIds[0],
            tab = HomeDashboardViewModel.TAB.BIN,
            isLoading = false
        )

        val expectedAfterSelection = expectedBeforeSelection.map { obj ->
            if (obj.id == objectIds[0])
                obj.copy(isSelected = true)
            else
                obj
        }

        assertEquals(
            expected = expectedAfterSelection,
            actual = vm.archived.value
        )

        assertEquals(
            expected = HomeDashboardViewModel.Mode.SELECTION,
            actual = vm.mode.value
        )
    }

    @Test
    fun `should toggle selection on object click in bin tab`() {

        // SETUP

        val dashboard = Block(
            id = config.home,
            content = Block.Content.Smart(SmartBlockType.HOME),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        stubGetConfig(Either.Right(config))

        stubObserveEvents(params = InterceptEvents.Params(context = config.home))

        stubOpenDashboard(
            payload = Payload(
                context = config.home,
                events = listOf(
                    Event.Command.ShowObject(
                        root = config.home,
                        context = config.home,
                        blocks = listOf(dashboard),
                        type = SmartBlockType.HOME
                    )
                )
            )
        )

//        stubSearchObjects(
//            params = SearchObjects.Params(
//                filters = ObjectSearchConstants.filterTabArchive,
//                sorts = ObjectSearchConstants.sortTabArchive
//            ),
//            objects = objects
//        )

        stubGetProfile()

        repo.stub {
            onBlocking {
                searchObjectsWithSubscription(
                    subscription = Subscriptions.SUBSCRIPTION_ARCHIVED,
                    keys = ObjectSearchConstants.defaultKeys,
                    filters = ObjectSearchConstants.filterTabArchive,
                    sorts = ObjectSearchConstants.sortTabArchive,
                    limit = 0,
                    offset = 0,
                    afterId = null,
                    beforeId = null
                )
            } doReturn SearchResult(
                results = objects.map {
                    ObjectWrapper.Basic(it)
                },
                dependencies = emptyList()
            )
        }

        vm = buildViewModel()

        vm.onStart()

        // TESTING

        val expectedBeforeSelection = listOf(
            DashboardView.Document(
                id = objectIds[0],
                isArchived = true,
                target = objectIds[0]
            ),
            DashboardView.Document(
                id = objectIds[1],
                isArchived = true,
                target = objectIds[1]
            ),
            DashboardView.Document(
                id = objectIds[2],
                isArchived = true,
                target = objectIds[2]
            ),
            DashboardView.Document(
                id = objectIds[3],
                isArchived = true,
                target = objectIds[3]
            )
        )

        assertEquals(
            expected = expectedBeforeSelection,
            actual = vm.archived.value
        )

        assertEquals(
            expected = HomeDashboardViewModel.Mode.DEFAULT,
            actual = vm.mode.value
        )

        // Clicking on the first object, in order to enter select mode

        vm.onTabObjectClicked(
            target = objectIds[0],
            tab = HomeDashboardViewModel.TAB.BIN,
            isLoading = false
        )

        // Clicking on the second object, in order select it

        vm.onTabObjectClicked(
            target = objectIds[1],
            tab = HomeDashboardViewModel.TAB.BIN,
            isLoading = false
        )

        // Checking that two object are selected

        val expectedAfterSelection = expectedBeforeSelection.map { obj ->
            if (obj.id == objectIds[0] || obj.id == objectIds[1])
                obj.copy(isSelected = true)
            else
                obj
        }

        assertEquals(
            expected = expectedAfterSelection,
            actual = vm.archived.value
        )

        // Clicking on the second object, in order unselect it

        vm.onTabObjectClicked(
            target = objectIds[1],
            tab = HomeDashboardViewModel.TAB.BIN,
            isLoading = false
        )

        // Checking that only the first object is selected now

        val expectedAfterUnselect = expectedBeforeSelection.map { obj ->
            if (obj.id == objectIds[0])
                obj.copy(isSelected = true)
            else
                obj
        }

        assertEquals(
            expected = expectedAfterUnselect,
            actual = vm.archived.value
        )
    }

    @Test
    fun `should exit select mode if no object is selected in bin tab`() {

        // SETUP

        val dashboard = Block(
            id = config.home,
            content = Block.Content.Smart(SmartBlockType.HOME),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        stubGetConfig(Either.Right(config))

        stubObserveEvents(params = InterceptEvents.Params(context = config.home))

        stubOpenDashboard(
            payload = Payload(
                context = config.home,
                events = listOf(
                    Event.Command.ShowObject(
                        root = config.home,
                        context = config.home,
                        blocks = listOf(dashboard),
                        type = SmartBlockType.HOME
                    )
                )
            )
        )

        stubGetProfile()

        repo.stub {
            onBlocking {
                searchObjectsWithSubscription(
                    subscription = Subscriptions.SUBSCRIPTION_ARCHIVED,
                    keys = ObjectSearchConstants.defaultKeys,
                    filters = ObjectSearchConstants.filterTabArchive,
                    sorts = ObjectSearchConstants.sortTabArchive,
                    limit = 0,
                    offset = 0,
                    afterId = null,
                    beforeId = null
                )
            } doReturn SearchResult(
                results = objects.map {
                    ObjectWrapper.Basic(it)
                },
                dependencies = emptyList()
            )
        }

        vm = buildViewModel()

        vm.onStart()

        // TESTING

        val expectedBeforeSelection = listOf(
            DashboardView.Document(
                id = objectIds[0],
                isArchived = true,
                target = objectIds[0]
            ),
            DashboardView.Document(
                id = objectIds[1],
                isArchived = true,
                target = objectIds[1]
            ),
            DashboardView.Document(
                id = objectIds[2],
                isArchived = true,
                target = objectIds[2]
            ),
            DashboardView.Document(
                id = objectIds[3],
                isArchived = true,
                target = objectIds[3]
            )
        )

        assertEquals(
            expected = expectedBeforeSelection,
            actual = vm.archived.value
        )

        assertEquals(
            expected = HomeDashboardViewModel.Mode.DEFAULT,
            actual = vm.mode.value
        )

        // Clicking on the first object, in order to enter select mode

        vm.onTabObjectClicked(
            target = objectIds[0],
            tab = HomeDashboardViewModel.TAB.BIN,
            isLoading = false
        )

        // Checking that this first object is selected

        val expectedAfterSelection = expectedBeforeSelection.map { obj ->
            if (obj.id == objectIds[0])
                obj.copy(isSelected = true)
            else
                obj
        }

        assertEquals(
            expected = expectedAfterSelection,
            actual = vm.archived.value
        )

        // Checking that bin tab is now in select mode

        assertEquals(
            expected = HomeDashboardViewModel.Mode.SELECTION,
            actual = vm.mode.value
        )

        // Clicking on the first object again to unselect it

        vm.onTabObjectClicked(
            target = objectIds[0],
            tab = HomeDashboardViewModel.TAB.BIN,
            isLoading = false
        )

        // Checking that no object is selected now

        val expectedAfterUnselect = expectedBeforeSelection

        assertEquals(
            expected = expectedAfterUnselect,
            actual = vm.archived.value
        )

        // Checking that bin tab is not select mode

        assertEquals(
            expected = HomeDashboardViewModel.Mode.DEFAULT,
            actual = vm.mode.value
        )
    }

    private fun stubGetProfile() {
        getProfile.stub {
            onBlocking {
                observe(
                    keys = any(),
                    subscription = any(),
                    dispatcher = any()
                )
            } doReturn emptyFlow()
        }
    }
}