package com.anytypeio.anytype.presentation.relations

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.StubRelation
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.dataview.interactor.ObjectRelationList
import com.anytypeio.anytype.presentation.relations.model.RelationView
import com.anytypeio.anytype.presentation.relations.providers.FakeObjectRelationProvider
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class RelationAddViewModelBaseTest {

    @get:Rule
    internal val coroutineTestRule = CoroutinesTestRule()

    private val availableHidden = StubRelation(isHidden = true)
    private val available = StubRelation()
    private val availableRelations = listOf(available, availableHidden)
    private val blockRepository = mock<BlockRepository> {
        onBlocking { relationListAvailable(any()) }.thenReturn(availableRelations)
    }

    private val relationsProvider = FakeObjectRelationProvider()
    private val vm = createVM()


    @Test
    fun `no added relations - results are available without hidden`() {
        runTest {
            vm.onStart("")
            coroutineTestRule.testDispatcher.scheduler.runCurrent()
            vm.results.test {
                assertEquals(
                    actual = awaitItem(),
                    expected = listOf(
                        RelationView.Existing(
                            id = available.key,
                            name = available.name,
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
            vm.onStart("")
            vm.results.test {
                assertEquals(
                    actual = awaitItem(),
                    expected = emptyList()
                )
            }
        }
    }

    private fun createVM() = object : RelationAddViewModelBase(
        objectRelationList = ObjectRelationList(
            repo = blockRepository,
            dispatchers = AppCoroutineDispatchers(
                io = coroutineTestRule.testDispatcher,
                computation = coroutineTestRule.testDispatcher,
                main = coroutineTestRule.testDispatcher
            )
        ),
        relationsProvider = relationsProvider
    ) {
        override fun sendAnalyticsEvent(length: Int) {}
    }

}