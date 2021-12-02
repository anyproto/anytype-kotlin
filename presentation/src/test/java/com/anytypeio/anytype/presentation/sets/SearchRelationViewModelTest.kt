package com.anytypeio.anytype.presentation.sets

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.anytypeio.anytype.core_models.DVViewerRelation
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.dataview.interactor.AddDataViewViewerSort
import com.anytypeio.anytype.presentation.MockRelationFactory
import com.anytypeio.anytype.presentation.MockRelationFactory.relationCustomNumber
import com.anytypeio.anytype.presentation.MockRelationFactory.relationCustomText
import com.anytypeio.anytype.presentation.MockRelationFactory.relationDone
import com.anytypeio.anytype.presentation.MockRelationFactory.relationFeaturedRelations
import com.anytypeio.anytype.presentation.MockRelationFactory.relationIconEmoji
import com.anytypeio.anytype.presentation.MockRelationFactory.relationIconImage
import com.anytypeio.anytype.presentation.MockRelationFactory.relationLastModifiedDate
import com.anytypeio.anytype.presentation.MockRelationFactory.relationName
import com.anytypeio.anytype.presentation.MockRelationFactory.relationStatus
import com.anytypeio.anytype.presentation.MockRelationFactory.relationTag
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime

class SearchRelationViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val root = MockDataFactory.randomUuid()
    private val dataViewId = MockDataFactory.randomString()
    private val session = ObjectSetSession()

    @Mock
    lateinit var addDataViewViewerSort: AddDataViewViewerSort

    private val dispatcher = Dispatcher.Default<Payload>()

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
    }

    @ExperimentalTime
    @Test
    fun `relation Name and Done should be visible`() = runBlocking {
        val state = MutableStateFlow(
            MockObjectSetFactory.makeDefaultObjectSet(
                dataViewId = dataViewId,
                relations = MockRelationFactory.getAllRelations(),
                viewerRelations = listOf(
                    DVViewerRelation(
                        key = relationName.key,
                        isVisible = true
                    ),
                    DVViewerRelation(
                        key = relationLastModifiedDate.key,
                        isVisible = true
                    ),
                    DVViewerRelation(
                        key = relationCustomNumber.key,
                        isVisible = true
                    ),
                    DVViewerRelation(
                        key = relationCustomText.key,
                        isVisible = true
                    ),
                    DVViewerRelation(
                        key = relationDone.key,
                        isVisible = true
                    ),
                    DVViewerRelation(
                        key = relationStatus.key,
                        isVisible = true
                    ),
                    DVViewerRelation(
                        key = relationTag.key,
                        isVisible = true
                    )
                )
            )
        )

        val vm = buildViewModel(state)

        val expected = listOf(
            SimpleRelationView(
                key = relationName.key,
                title = relationName.name,
                format = ColumnView.Format.SHORT_TEXT,
                isVisible = true,
                isHidden = true,
                isReadonly = false,
                isDefault = true
            ),
            SimpleRelationView(
                key = relationLastModifiedDate.key,
                title = relationLastModifiedDate.name,
                format = ColumnView.Format.DATE,
                isVisible = true,
                isHidden = false,
                isReadonly = true,
                isDefault = false
            ),
            SimpleRelationView(
                key = relationCustomNumber.key,
                title = relationCustomNumber.name,
                format = ColumnView.Format.NUMBER,
                isVisible = true,
                isHidden = false,
                isReadonly = false,
                isDefault = false
            ),
            SimpleRelationView(
                key = relationCustomText.key,
                title = relationCustomText.name,
                format = ColumnView.Format.LONG_TEXT,
                isVisible = true,
                isHidden = false,
                isReadonly = false,
                isDefault = false
            ),
            SimpleRelationView(
                key = relationDone.key,
                title = relationDone.name,
                format = ColumnView.Format.CHECKBOX,
                isVisible = true,
                isHidden = true,
                isReadonly = false,
                isDefault = true
            ),
            SimpleRelationView(
                key = relationStatus.key,
                title = relationStatus.name,
                format = ColumnView.Format.STATUS,
                isVisible = true,
                isHidden = false,
                isReadonly = false,
                isDefault = false
            ),
            SimpleRelationView(
                key = relationTag.key,
                title = relationTag.name,
                format = ColumnView.Format.TAG,
                isVisible = true,
                isHidden = false,
                isReadonly = false,
                isDefault = false
            )
        )

        vm.views.test {
            delay(100)
            assertEquals(expected = expected, actual = awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `relation with format Emoji, File, Relation should not be visible`() = runBlocking {
        val state = MutableStateFlow(
            MockObjectSetFactory.makeDefaultObjectSet(
                dataViewId = dataViewId,
                relations = MockRelationFactory.getAllRelations(),
                viewerRelations = listOf(
                    DVViewerRelation(
                        key = relationName.key,
                        isVisible = true
                    ),
                    DVViewerRelation(
                        key = relationIconEmoji.key,
                        isVisible = true
                    ),
                    DVViewerRelation(
                        key = relationCustomNumber.key,
                        isVisible = true
                    ),
                    DVViewerRelation(
                        key = relationCustomText.key,
                        isVisible = true
                    ),
                    DVViewerRelation(
                        key = relationIconImage.key,
                        isVisible = true
                    ),
                    DVViewerRelation(
                        key = relationFeaturedRelations.key,
                        isVisible = true
                    )
                )
            )
        )

        val vm = buildViewModel(state)

        val expected = listOf(
            SimpleRelationView(
                key = relationName.key,
                title = relationName.name,
                format = ColumnView.Format.SHORT_TEXT,
                isVisible = true,
                isHidden = true,
                isReadonly = false,
                isDefault = true
            ),
            SimpleRelationView(
                key = relationCustomNumber.key,
                title = relationCustomNumber.name,
                format = ColumnView.Format.NUMBER,
                isVisible = true,
                isHidden = false,
                isReadonly = false,
                isDefault = false
            ),
            SimpleRelationView(
                key = relationCustomText.key,
                title = relationCustomText.name,
                format = ColumnView.Format.LONG_TEXT,
                isVisible = true,
                isHidden = false,
                isReadonly = false,
                isDefault = false
            )
        )

        vm.views.test {
            delay(100)
            assertEquals(expected = expected, actual = awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    private fun buildViewModel(state: MutableStateFlow<ObjectSet>): SelectSortRelationViewModel {
        return SelectSortRelationViewModel(
            objectSetState = state,
            session = session,
            addDataViewViewerSort = addDataViewViewerSort,
            dispatcher = dispatcher
        )
    }
}