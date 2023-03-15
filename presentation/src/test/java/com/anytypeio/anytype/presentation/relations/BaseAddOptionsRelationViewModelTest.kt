package com.anytypeio.anytype.presentation.relations

import app.cash.turbine.test
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.core_models.StubRelationOptionObject
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.options.GetOptions
import com.anytypeio.anytype.presentation.relations.RelationValueView.Option.Tag
import com.anytypeio.anytype.presentation.relations.add.AddOptionsRelationProvider
import com.anytypeio.anytype.presentation.relations.add.BaseAddOptionsRelationViewModel
import com.anytypeio.anytype.presentation.relations.providers.FakeObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.FakeObjectValueProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

@ExperimentalCoroutinesApi
class BaseAddOptionsRelationViewModelTest {

    @get:Rule
    internal val coroutineTestRule = CoroutinesTestRule()

    private val notSelectedOption = StubRelationOptionObject()
    private val selectedOption = StubRelationOptionObject()
    private val relation = StubRelationObject(
        format = Relation.Format.TAG
    )
    private val relationKey = relation.key
    private val targetId = "stubTargetId"
    private val relationsProvider = FakeObjectRelationProvider()
    private val valuesProvider = FakeObjectValueProvider(
        mapOf(
            targetId to mapOf(relationKey to listOf(selectedOption.id))
        )
    )

    private val ctx = MockDataFactory.randomUuid()

    @Mock
    lateinit var setObjectDetails: UpdateDetail

    @Mock
    lateinit var dispatcher: Dispatcher<Payload>

    @Mock
    lateinit var objectDetailProvider: ObjectDetailProvider

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var getOptions: GetOptions

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `query is empty - there is only tag view`() = runTest {

        // SETUP
        val viewModel = createViewModel()
        relationsProvider.relation = relation

        getOptions.stub {
            onBlocking {
                invoke(any())
            } doReturn Either.Right(
                listOf(
                    notSelectedOption
                )
            )
        }

        // TESTING

        viewModel.ui.test {
            viewModel.onStart(
                ctx = ctx,
                target = targetId,
                relationKey = relationKey
            )
            // first item is emmit in constructor
            val actual = listOf(awaitItem(), awaitItem())[1]
            assertEquals(
                expected = listOf(tag(notSelectedOption)),
                actual = actual
            )
        }
    }

    @Test
    fun `query doesn't contain tag - there is only create view`() = runTest {

        // SETUP

        val viewModel = createViewModel()
        relationsProvider.relation = relation

        getOptions.stub {
            onBlocking {
                invoke(any())
            } doReturn Either.Right(
                listOf(
                    notSelectedOption
                )
            )
        }

        // TESTING

        viewModel.ui.test {
            viewModel.onStart(
                ctx = ctx,
                target = targetId,
                relationKey = relationKey
            )
            val query = MockDataFactory.randomString() + notSelectedOption.name.orEmpty()
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

    @Test
    fun `query contains tag parts but doesn't equal - there are create and tag views`() = runTest {

        // SETUP

        val viewModel = createViewModel()
        relationsProvider.relation = relation

        getOptions.stub {
            onBlocking {
                invoke(any())
            } doReturn Either.Right(
                listOf(
                    notSelectedOption
                )
            )
        }

        // TESTING

        viewModel.ui.test {
            viewModel.onStart(
                ctx = ctx,
                target = targetId,
                relationKey = relationKey
            )

            val query = notSelectedOption.name.orEmpty().substring(1)
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

    @Test
    fun `query equals tag - there is only tag view`() = runTest {

        // SETUP

        val viewModel = createViewModel()
        relationsProvider.relation = relation

        getOptions.stub {
            onBlocking {
                invoke(any())
            } doReturn Either.Right(
                listOf(
                    notSelectedOption
                )
            )
        }

        // TESTING

        viewModel.ui.test {
            viewModel.onStart(
                ctx = ctx,
                target = targetId,
                relationKey = relationKey
            )
            // first item is emmit in constructor, second - at onStart
            val actual = listOf(awaitItem(), awaitItem())[1]
            assertEquals(
                expected = listOf(tag(notSelectedOption)),
                actual = actual
            )

            val query = notSelectedOption.name.orEmpty()
            viewModel.onFilterInputChanged(query)

            // because `ui` has distinct under the hood
            expectNoEvents()
        }
    }

    @Test
    fun `query equals selected tag - there is only one view to create new option`() = runTest {

        // SETUP

        val viewModel = createViewModel()
        relationsProvider.relation = relation

        getOptions.stub {
            onBlocking {
                invoke(any())
            } doReturn Either.Right(
                listOf(
                    notSelectedOption
                )
            )
        }

        // TESTING

        viewModel.ui.test {
            viewModel.onStart(
                ctx = ctx,
                target = targetId,
                relationKey = relationKey
            )
            val actual = listOf(awaitItem(), awaitItem())[1]

            assertEquals(
                expected = listOf(tag(notSelectedOption)),
                actual = actual
            )

            val query = selectedOption.name.orEmpty()
            viewModel.onFilterInputChanged(query)

            assertEquals(
                expected = listOf(RelationValueView.Create(name = query)),
                actual = awaitItem()
            )
        }
    }

    private fun tag(option: ObjectWrapper.Option) = Tag(
        id = option.id,
        name = option.name.orEmpty(),
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
            analytics = analytics,
            detailsProvider = objectDetailProvider,
            getOptions = getOptions
        ) {}
}