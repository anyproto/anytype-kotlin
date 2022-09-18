package com.anytypeio.anytype.presentation.relations

import app.cash.turbine.test
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.StubRelation
import com.anytypeio.anytype.core_models.StubRelationOption
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.presentation.relations.RelationValueView.Option.Tag
import com.anytypeio.anytype.presentation.relations.add.AddOptionsRelationProvider
import com.anytypeio.anytype.presentation.relations.add.BaseAddOptionsRelationViewModel
import com.anytypeio.anytype.presentation.relations.providers.FakeObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.FakeObjectValueProvider
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class BaseAddOptionsRelationViewModelTest {

    @get:Rule
    internal val coroutineTestRule = CoroutinesTestRule()

    private val notSelectedOption = StubRelationOption()
    private val selectedOption = StubRelationOption()
    private val relation = StubRelation(
        format = Relation.Format.TAG,
        selections = listOf(notSelectedOption, selectedOption)
    )
    private val relationId = relation.key
    private val targetId = "stubTargetId"
    private val relationsProvider = FakeObjectRelationProvider()
    private val valuesProvider = FakeObjectValueProvider(
        mapOf(
            targetId to mapOf(relationId to listOf(selectedOption.id))
        )
    )

    @Mock
    lateinit var setObjectDetails: UpdateDetail

    @Mock
    lateinit var dispatcher: Dispatcher<Payload>

    @Mock
    lateinit var analytics: Analytics

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `query is empty - there is only tag view`() {
        val viewModel = createViewModel()
        relationsProvider.relation = relation

        runTest {
            viewModel.ui.test {
                viewModel.onStart(targetId, relationId)
                // first item is emmit in constructor
                val actual = listOf(awaitItem(), awaitItem())[1]
                assertEquals(
                    expected = listOf(tag(notSelectedOption)),
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
                viewModel.onStart(targetId, relationId)
                val query = MockDataFactory.randomString() + notSelectedOption.text
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
                viewModel.onStart(targetId, relationId)

                val query = notSelectedOption.text.substring(1)
                viewModel.onFilterInputChanged(query)
                // first item is emmit in constructor, second - at onStart
                val actual = listOf(awaitItem(), awaitItem(), awaitItem())[2]
                assertEquals(
                    expected = listOf(
                        RelationValueView.Create(query),
                        tag(notSelectedOption)
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
                viewModel.onStart(targetId, relationId)
                // first item is emmit in constructor, second - at onStart
                val actual = listOf(awaitItem(), awaitItem())[1]
                assertEquals(
                    expected = listOf(tag(notSelectedOption)),
                    actual = actual
                )

                val query = notSelectedOption.text
                viewModel.onFilterInputChanged(query)

                // because `ui` has distinct under the hood
                expectNoEvents()
            }
        }
    }

    @Test
    fun `query equals selected tag - there is only empty view`() {
        val viewModel = createViewModel()
        relationsProvider.relation = relation

        runTest {
            viewModel.ui.test {
                viewModel.onStart(targetId, relationId)
                // first item is emmit in constructor, second - at onStart
                val actual = listOf(awaitItem(), awaitItem())[1]
                assertEquals(
                    expected = listOf(tag(notSelectedOption)),
                    actual = actual
                )

                val query = selectedOption.text
                viewModel.onFilterInputChanged(query)

                assertEquals(
                    expected = listOf(RelationValueView.Empty),
                    actual = awaitItem()
                )
            }
        }
    }

    private fun tag(option: Relation.Option) = Tag(
        id = option.id,
        name = option.text,
        color = option.color,
        isSelected = false,
        isCheckboxShown = true
    )

    private fun createViewModel(): BaseAddOptionsRelationViewModel = object :
        BaseAddOptionsRelationViewModel(
            optionsProvider = AddOptionsRelationProvider(),
            values = valuesProvider,
            relations = relationsProvider,
            dispatcher = dispatcher,
            setObjectDetail = setObjectDetails,
            analytics = analytics
        ) {}
}