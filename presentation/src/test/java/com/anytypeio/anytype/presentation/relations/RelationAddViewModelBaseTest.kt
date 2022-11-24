package com.anytypeio.anytype.presentation.relations

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.relations.model.RelationView
import com.anytypeio.anytype.presentation.relations.providers.FakeObjectRelationProvider
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class RelationAddViewModelBaseTest {

    @get:Rule
    internal val coroutineTestRule = CoroutinesTestRule()

    private val availableHidden = StubRelationObject(isHidden = true)
    private val available = StubRelationObject()
    private val availableRelations = listOf(available, availableHidden)

    private val relationsProvider = FakeObjectRelationProvider()

    @Test
    fun `no added relations - results are available without hidden`() {
        runTest {
            // SETUP
            val store = DefaultStoreOfRelations()
            val vm = createVM(store)

            // TESTING
            store.merge(availableRelations)
            vm.onStart()
            coroutineTestRule.testDispatcher.scheduler.runCurrent()
            vm.results.test {
                assertEquals(
                    actual = awaitItem(),
                    expected = listOf(
                        RelationView.Existing(
                            key = available.key,
                            name = available.name.orEmpty(),
                            format = available.format
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `added relations equal to available - results are empty`() {
        relationsProvider.relation = available
        runTest {
            // SETUP
            val store = DefaultStoreOfRelations()
            val vm = createVM(store)

            // TESTING
            store.merge(availableRelations)
            vm.onStart()
            vm.results.test {
                assertEquals(
                    actual = awaitItem(),
                    expected = emptyList()
                )
            }
        }
    }

    private fun createVM(
        storeOfRelations: StoreOfRelations
    ) = object : RelationAddViewModelBase(
        storeOfRelations = storeOfRelations,
        relationsProvider = relationsProvider
    ) {
        override fun sendAnalyticsEvent(length: Int) {}
    }

}