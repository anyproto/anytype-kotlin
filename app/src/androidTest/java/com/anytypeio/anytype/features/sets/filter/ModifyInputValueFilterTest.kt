package com.anytypeio.anytype.features.sets.filter

import android.os.Bundle
import android.text.InputType
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.mocking.MockDataFactory
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.filter.FilterViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.sets.modals.filter.ModifyFilterFromInputFieldValueFragment
import com.anytypeio.anytype.utils.CoroutinesTestRule
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

@RunWith(AndroidJUnit4::class)
@LargeTest
class ModifyInputValueFilterTest {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var gateway: Gateway

    lateinit var updateDataViewViewer: UpdateDataViewViewer
    lateinit var searchObjects: SearchObjects
    lateinit var urlBuilder: UrlBuilder

    private val root = MockDataFactory.randomUuid()
    private val session = ObjectSetSession()
    private val state = MutableStateFlow(ObjectSet.init())
    private val dispatcher = Dispatcher.Default<Payload>()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        updateDataViewViewer = UpdateDataViewViewer(repo)
        searchObjects = SearchObjects(repo)
        urlBuilder = UrlBuilder(gateway)
        TestModifyFilterFromInputFieldValueFragment.testVmFactory = FilterViewModel.Factory(
            objectSetState = state,
            session = session,
            updateDataViewViewer = updateDataViewViewer,
            dispatcher = dispatcher,
            searchObjects = searchObjects,
            urlBuilder = urlBuilder
        )
    }

    @Test
    fun shouldTypeTextThenClickActionButtonToApplyChanges() {

        val relationKey = MockDataFactory.randomUuid()

        val target = MockDataFactory.randomUuid()

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to target,
            relationKey to emptyList<String>()
        )

        // Defining viewer containing one filter

        val initialFilterText = "Foo"
        val textToType = "Bar"

        val filter = DVFilter(
            relationKey = relationKey,
            value = initialFilterText,
            condition = DVFilterCondition.EQUAL
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = listOf(filter),
            sorts = emptyList(),
            viewerRelations = listOf(
                Block.Content.DataView.Viewer.ViewerRelation(
                    key = relationKey,
                    isVisible = true
                )
            ),
            type = Block.Content.DataView.Viewer.Type.values().random()
        )

        val relation = Relation(
            key = relationKey,
            defaultValue = null,
            isHidden = false,
            isReadOnly = false,
            isMulti = true,
            name = MockDataFactory.randomString(),
            source = Relation.Source.values().random(),
            format = Relation.Format.LONG_TEXT,
            selections = emptyList()
        )

        val dv = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            fields = Block.Fields.empty(),
            content = Block.Content.DataView(
                relations = listOf(relation),
                viewers = listOf(viewer),
                source = MockDataFactory.randomUuid()
            )
        )

        state.value = ObjectSet(
            blocks = listOf(dv),
            viewerDb = mapOf(
                viewer.id to ObjectSet.ViewerData(
                    records = listOf(record),
                    total = 1
                )
            )
        )

        // Launching fragment

        val scenario = launchFragment(
            bundleOf(
                ModifyFilterFromInputFieldValueFragment.CTX_KEY to root,
                ModifyFilterFromInputFieldValueFragment.IDX_KEY to 0,
                ModifyFilterFromInputFieldValueFragment.RELATION_KEY to relationKey,
            )
        )

        // Veryfying that the initial filter text is visibile to our user

        val inputFieldInteraction = onView(withId(R.id.enterTextValueInputField))

        inputFieldInteraction.check(matches(withText(initialFilterText)))


        // Checking input type

        inputFieldInteraction.check(matches(withInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE)))

        // Typing additional text before pressing action button

        inputFieldInteraction.perform(
            typeText(textToType)
        )

        inputFieldInteraction.perform(
            closeSoftKeyboard()
        )

        Thread.sleep(1000)

        // Clicking to apply button, in order to save filter changes

        onView(withId(R.id.btnBottomAction)).apply {
            perform(click())
        }

        // Veryfying that the appropriate request was made

        verifyBlocking(repo, times(1)) {
            updateDataViewViewer(
                context = root,
                target = dv.id,
                viewer = viewer.copy(
                    filters = listOf(filter.copy(value = initialFilterText + textToType))
                )
            )
        }
    }

    @Test
    fun shouldTypeNumberFilterTextThenClickActionButtonToApplyChanges() {

        val relationKey = MockDataFactory.randomUuid()

        val target = MockDataFactory.randomUuid()

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to target,
            relationKey to emptyList<String>()
        )

        // Defining viewer containing one filter

        val initialFilterText = "1"
        val textToType = "2"

        val filter = DVFilter(
            relationKey = relationKey,
            value = initialFilterText,
            condition = DVFilterCondition.EQUAL
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = listOf(filter),
            sorts = emptyList(),
            viewerRelations = listOf(
                Block.Content.DataView.Viewer.ViewerRelation(
                    key = relationKey,
                    isVisible = true
                )
            ),
            type = Block.Content.DataView.Viewer.Type.values().random()
        )

        val relation = Relation(
            key = relationKey,
            defaultValue = null,
            isHidden = false,
            isReadOnly = false,
            isMulti = true,
            name = MockDataFactory.randomString(),
            source = Relation.Source.values().random(),
            format = Relation.Format.NUMBER,
            selections = emptyList()
        )

        val dv = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            fields = Block.Fields.empty(),
            content = Block.Content.DataView(
                relations = listOf(relation),
                viewers = listOf(viewer),
                source = MockDataFactory.randomUuid()
            )
        )

        state.value = ObjectSet(
            blocks = listOf(dv),
            viewerDb = mapOf(
                viewer.id to ObjectSet.ViewerData(
                    records = listOf(record),
                    total = 1
                )
            )
        )

        // Launching fragment

        launchFragment(
            bundleOf(
                ModifyFilterFromInputFieldValueFragment.CTX_KEY to root,
                ModifyFilterFromInputFieldValueFragment.IDX_KEY to 0,
                ModifyFilterFromInputFieldValueFragment.RELATION_KEY to relationKey,
            )
        )

        // Veryfying that the initial filter text is visibile to our user

        val inputFieldInteraction = onView(withId(R.id.enterTextValueInputField))

        inputFieldInteraction.check(matches(withText(initialFilterText)))

        // Checking input type

        inputFieldInteraction.check(matches(withInputType(InputType.TYPE_CLASS_NUMBER
                or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED)))

        // Typing additional text before pressing action button

        inputFieldInteraction.perform(
            typeText(textToType)
        )

        inputFieldInteraction.perform(
            closeSoftKeyboard()
        )

        // Clicking to apply button, in order to save filter changes

        Thread.sleep(1000)

        onView(withId(R.id.btnBottomAction)).apply {
            perform(click())
        }

        // Veryfying that the appropriate request was made

        verifyBlocking(repo, times(1)) {
            updateDataViewViewer(
                context = root,
                target = dv.id,
                viewer = viewer.copy(
                    filters = listOf(filter.copy(value = initialFilterText + textToType))
                )
            )
        }
    }

    private fun launchFragment(args: Bundle): FragmentScenario<TestModifyFilterFromInputFieldValueFragment> {
        return launchFragmentInContainer<TestModifyFilterFromInputFieldValueFragment>(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }
}