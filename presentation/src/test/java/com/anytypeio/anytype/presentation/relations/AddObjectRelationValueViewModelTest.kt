package com.anytypeio.anytype.presentation.relations

import FakeGateWay
import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.StubRelation
import com.anytypeio.anytype.core_models.StubRelationOption
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.relations.providers.FakeObjectDetailsProvider
import com.anytypeio.anytype.presentation.relations.providers.FakeObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.FakeObjectTypesProvider
import com.anytypeio.anytype.presentation.relations.providers.FakeObjectValueProvider
import com.anytypeio.anytype.presentation.sets.RelationValueBaseViewModel.RelationValueView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class AddObjectRelationValueViewModelTest {

    @get:Rule
    internal val coroutineTestRule = CoroutinesTestRule()

    private val option = StubRelationOption()
    private val relation = StubRelation(
        format = Relation.Format.TAG,
        selections = listOf(option)
    )

    private val relationsProvider = FakeObjectRelationProvider

    @Test
    fun `query is empty - there is only tag view`() {
        val viewModel = createViewModel()
        relationsProvider.relation = relation

        runTest {
            viewModel.ui.test {
                viewModel.onStart("", "")
                // first item is emmit in constructor
                val actual = listOf(awaitItem(), awaitItem())[1]
                assertEquals(
                    expected = listOf(
                        RelationValueView.Tag(
                            id = option.id,
                            name = option.text,
                            color = option.color,
                            isSelected = false
                        )
                    ),
                    actual = actual
                )
            }
        }
    }

    @Test
    fun `query doesn't contain tag - there is only create view`() {
        val viewModel = createViewModel()
        relationsProvider.relation = relation

        runTest {
            viewModel.ui.test {
                viewModel.onStart("", "")
                val query = MockDataFactory.randomString() + option.text
                viewModel.onFilterInputChanged(query)
                // first item is emmit in constructor, second - at onStart
                val actual = listOf(awaitItem(), awaitItem(), awaitItem())[2]
                assertEquals(
                    expected = listOf(
                        RelationValueView.Create(query),
                    ),
                    actual = actual
                )
            }
        }
    }

    @Test
    fun `query contains tag parts but doesn't equal - there are create and tag views`() {
        val viewModel = createViewModel()
        relationsProvider.relation = relation

        runTest {
            viewModel.ui.test {
                viewModel.onStart("", "")

                val query = option.text.substring(1)
                viewModel.onFilterInputChanged(query)
                // first item is emmit in constructor, second - at onStart
                val actual = listOf(awaitItem(), awaitItem(), awaitItem())[2]
                assertEquals(
                    expected = listOf(
                        RelationValueView.Create(query),
                        RelationValueView.Tag(
                            id = option.id,
                            name = option.text,
                            color = option.color,
                            isSelected = false
                        )
                    ),
                    actual = actual
                )
            }
        }
    }

    @Test
    fun `query equals tag - there is only tag view`() {
        val viewModel = createViewModel()
        relationsProvider.relation = relation

        runTest {
            viewModel.ui.test {
                viewModel.onStart("", "")
                // first item is emmit in constructor, second - at onStart
                val actual = listOf(awaitItem(), awaitItem())[1]
                assertEquals(
                    expected = listOf(
                        RelationValueView.Tag(
                            id = option.id,
                            name = option.text,
                            color = option.color,
                            isSelected = false
                        )
                    ),
                    actual = actual
                )

                val query = option.text
                viewModel.onFilterInputChanged(query)

                // because `ui` has distinct under the hood
                expectNoEvents()
            }
        }
    }

    private fun createViewModel(): AddObjectRelationValueViewModel = object :
        AddObjectRelationValueViewModel(
            FakeObjectValueProvider,
            FakeObjectDetailsProvider,
            relationsProvider,
            FakeObjectTypesProvider,
            UrlBuilder(FakeGateWay)
        ) {}
}