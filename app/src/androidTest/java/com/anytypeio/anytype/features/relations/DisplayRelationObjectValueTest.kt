package com.anytypeio.anytype.features.relations

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.dataview.interactor.*
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.relations.AddFileToRecord
import com.anytypeio.anytype.mocking.MockDataFactory
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectValueProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectTypeProvider
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.RelationValueDVViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.relations.RelationValueBaseFragment
import com.anytypeio.anytype.utils.*
import com.anytypeio.anytype.utils.TestUtils.withRecyclerView
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
@LargeTest
class DisplayRelationObjectValueTest {

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var dispatcher: Dispatcher<Payload>

    @Mock
    lateinit var gateway: Gateway

    private lateinit var addRelationOption: AddDataViewRelationOption
    private lateinit var removeTagFromDataViewRecord: RemoveTagFromDataViewRecord
    private lateinit var removeStatusFromDataViewRecord: RemoveStatusFromDataViewRecord
    private lateinit var addTagToDataViewRecord: AddTagToDataViewRecord
    private lateinit var updateDataViewRecord: UpdateDataViewRecord
    private lateinit var updateDetail: UpdateDetail
    private lateinit var urlBuilder: UrlBuilder
    private lateinit var addFileToRecord: AddFileToRecord

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val root = MockDataFactory.randomUuid()
    private val state = MutableStateFlow(ObjectSet.init())
    private val session = ObjectSetSession()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        addRelationOption = AddDataViewRelationOption(repo)
        addTagToDataViewRecord = AddTagToDataViewRecord(repo)
        removeTagFromDataViewRecord = RemoveTagFromDataViewRecord(repo)
        removeStatusFromDataViewRecord = RemoveStatusFromDataViewRecord(repo)
        updateDataViewRecord = UpdateDataViewRecord(repo)
        updateDetail = UpdateDetail(repo)
        urlBuilder = UrlBuilder(gateway)
        addFileToRecord = AddFileToRecord(repo)
        TestRelationValueDVFragment.testVmFactory = RelationValueDVViewModel.Factory(
            relations = DataViewObjectRelationProvider(state),
            values = DataViewObjectValueProvider(state, session),
            details = object: ObjectDetailProvider {
                override fun provide(): Map<Id, Block.Fields> = state.value.details
            },
            types = object: ObjectTypeProvider {
                override fun provide(): List<ObjectType> = state.value.objectTypes
            },
            removeTagFromRecord = removeTagFromDataViewRecord,
            removeStatusFromDataViewRecord = removeStatusFromDataViewRecord,
            urlBuilder = urlBuilder,
            dispatcher = dispatcher,
            updateDataViewRecord = updateDataViewRecord,
            addFileToRecord = addFileToRecord
        )
    }

    @Test
    fun shouldDisplayEditButtonAndPlusButton() {
        // SETUP

        val relation = MockDataFactory.randomUuid()
        val target = MockDataFactory.randomUuid()

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to target,
            relation to emptyList<Id>()
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        state.value = ObjectSet(
            blocks = listOf(
                Block(
                    id = MockDataFactory.randomUuid(),
                    children = emptyList(),
                    fields = Block.Fields.empty(),
                    content = Block.Content.DataView(
                        relations = listOf(
                            Relation(
                                key = relation,
                                isMulti = true,
                                name = MockDataFactory.randomString(),
                                format = Relation.Format.OBJECT,
                                source = Relation.Source.values().random()
                            )
                        ),
                        viewers = listOf(viewer),
                        source = MockDataFactory.randomUuid()
                    )
                )
            ),
            viewerDb = mapOf(
                viewer.id to ObjectSet.ViewerData(
                    records = listOf(record),
                    total = 1
                )
            )
        )

        // TESTING

        launchFragment(
            bundleOf(
                RelationValueBaseFragment.CTX_KEY to root,
                RelationValueBaseFragment.RELATION_KEY to relation,
                RelationValueBaseFragment.TARGET_KEY to target
            )
        )

        // Checking that the buttons are invisible

        onView(withId(R.id.btnEditOrDone)).apply {
            check(matches(isDisplayed()))
        }

        onView(withId(R.id.btnAddValue)).apply {
            check(matches(isDisplayed()))
        }
    }

    @Test
    fun shouldSetRelationName() {

        // SETUP

        val name = "Object"

        val relation = MockDataFactory.randomUuid()
        val target = MockDataFactory.randomUuid()

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to target,
            relation to emptyList<Id>()
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        state.value = ObjectSet(
            blocks = listOf(
                Block(
                    id = MockDataFactory.randomUuid(),
                    children = emptyList(),
                    fields = Block.Fields.empty(),
                    content = Block.Content.DataView(
                        relations = listOf(
                            Relation(
                                key = relation,
                                isMulti = true,
                                name = name,
                                format = Relation.Format.OBJECT,
                                source = Relation.Source.values().random()
                            )
                        ),
                        viewers = listOf(viewer),
                        source = MockDataFactory.randomUuid()
                    )
                )
            ),
            viewerDb = mapOf(
                viewer.id to ObjectSet.ViewerData(
                    records = listOf(record),
                    total = 1
                )
            )
        )

        // TESTING

        launchFragment(
            bundleOf(
                RelationValueBaseFragment.CTX_KEY to root,
                RelationValueBaseFragment.RELATION_KEY to relation,
                RelationValueBaseFragment.TARGET_KEY to target
            )
        )

        // Checking that the name is set

        onView(withId(R.id.tvTagOrStatusRelationHeader)).apply {
            check(matches(withText(name)))
        }
    }

    @Test
    fun shouldRenderEmptyState() {

        // SETUP

        val name = "Object"

        val relationId = MockDataFactory.randomUuid()
        val targetId = MockDataFactory.randomUuid()

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to targetId,
            relationId to emptyList<Id>()
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        state.value = ObjectSet(
            blocks = listOf(
                Block(
                    id = MockDataFactory.randomUuid(),
                    children = emptyList(),
                    fields = Block.Fields.empty(),
                    content = Block.Content.DataView(
                        relations = listOf(
                            Relation(
                                key = relationId,
                                name = name,
                                format = Relation.Format.OBJECT,
                                source = Relation.Source.values().random()
                            )
                        ),
                        viewers = listOf(viewer),
                        source = MockDataFactory.randomUuid()
                    )
                )
            ),
            viewerDb = mapOf(
                viewer.id to ObjectSet.ViewerData(
                    records = listOf(record),
                    total = 1
                )
            )
        )

        // TESTING

        launchFragment(
            bundleOf(
                RelationValueBaseFragment.CTX_KEY to root,
                RelationValueBaseFragment.RELATION_KEY to relationId,
                RelationValueBaseFragment.TARGET_KEY to targetId
            )
        )

        val rvMatcher = withRecyclerView(R.id.recycler)

        onView(rvMatcher.atPositionOnView(0, R.id.tvEmptyMessage)).apply {
            check(matches(isDisplayed()))
        }
    }

    @Test
    fun shouldRenderTwoObjectsWithNames() {

        // SETUP

        val relationName = "Cast"
        val object1Name = "Charlie Chaplin"
        val object1Id = MockDataFactory.randomUuid()
        val object2Name = "Jean-Pierre Léaud"
        val object2Id = MockDataFactory.randomUuid()

        val objectType1 = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = "Director",
            relations = emptyList(),
            emoji = "",
            layout = ObjectType.Layout.values().random(),
            description = "",
            isHidden = false,
            smartBlockTypes = listOf()
        )

        val objectType2 = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = "Actor",
            relations = emptyList(),
            emoji = "",
            layout = ObjectType.Layout.values().random(),
            description = "",
            isHidden = false,
            smartBlockTypes = listOf()
        )

        val relationId = MockDataFactory.randomUuid()
        val recordId = MockDataFactory.randomUuid()

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to recordId,
            relationId to listOf(object1Id, object2Id)
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        state.value = ObjectSet(
            blocks = listOf(
                Block(
                    id = MockDataFactory.randomUuid(),
                    children = emptyList(),
                    fields = Block.Fields.empty(),
                    content = Block.Content.DataView(
                        relations = listOf(
                            Relation(
                                key = relationId,
                                name = relationName,
                                format = Relation.Format.OBJECT,
                                source = Relation.Source.values().random()
                            )
                        ),
                        viewers = listOf(viewer),
                        source = MockDataFactory.randomUuid()
                    )
                )
            ),
            viewerDb = mapOf(
                viewer.id to ObjectSet.ViewerData(
                    records = listOf(record),
                    total = 1
                )
            ),
            details = mapOf(
                object1Id to Block.Fields(
                    mapOf(
                        Block.Fields.NAME_KEY to object1Name,
                        Block.Fields.TYPE_KEY to objectType1.url,
                        "iconEmoji" to "👤"
                    )
                ),
                object2Id to Block.Fields(
                    mapOf(
                        Block.Fields.NAME_KEY to object2Name,
                        Block.Fields.TYPE_KEY to objectType2.url,
                        "iconEmoji" to "👤"
                    )
                )
            ),
            objectTypes = listOf(objectType1, objectType2)
        )

        // TESTING

        launchFragment(
            bundleOf(
                RelationValueBaseFragment.CTX_KEY to root,
                RelationValueBaseFragment.RELATION_KEY to relationId,
                RelationValueBaseFragment.TARGET_KEY to recordId
            )
        )

        val rvMatcher = withRecyclerView(R.id.recycler)

        onView(rvMatcher.atPositionOnView(0, R.id.tvTitle)).apply {
            check(matches(withText(object1Name)))
        }

        onView(rvMatcher.atPositionOnView(0, R.id.tvSubtitle)).apply {
            check(matches(withText(objectType1.name)))
        }

        onView(rvMatcher.atPositionOnView(1, R.id.tvTitle)).apply {
            check(matches(withText(object2Name)))
        }

        onView(rvMatcher.atPositionOnView(1, R.id.tvSubtitle)).apply {
            check(matches(withText(objectType2.name)))
        }
    }

    @Test
    fun shouldRenderTwoObjectsWithoutObjectTypes() {

        // SETUP

        val objectType1 = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = "Writer",
            relations = emptyList(),
            emoji = "",
            layout = ObjectType.Layout.PROFILE,
            description = "",
            isHidden = false,
            smartBlockTypes = listOf()
        )

        val objectType2 = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = "Writer",
            relations = emptyList(),
            emoji = "",
            layout = ObjectType.Layout.PROFILE,
            description = "",
            isHidden = false,
            smartBlockTypes = listOf()
        )

        val relationName = "Cast"
        val object1Name = "Charlie Chaplin"
        val object1Id = MockDataFactory.randomUuid()
        val object2Name = "Jean-Pierre Léaud"
        val object2Id = MockDataFactory.randomUuid()

        val relationId = MockDataFactory.randomUuid()
        val recordId = MockDataFactory.randomUuid()

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to recordId,
            relationId to listOf(object1Id, object2Id)
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        state.value = ObjectSet(
            blocks = listOf(
                Block(
                    id = MockDataFactory.randomUuid(),
                    children = emptyList(),
                    fields = Block.Fields.empty(),
                    content = Block.Content.DataView(
                        relations = listOf(
                            Relation(
                                key = relationId,
                                name = relationName,
                                format = Relation.Format.OBJECT,
                                source = Relation.Source.values().random()
                            )
                        ),
                        viewers = listOf(viewer),
                        source = MockDataFactory.randomUuid()
                    )
                )
            ),
            viewerDb = mapOf(
                viewer.id to ObjectSet.ViewerData(
                    records = listOf(record),
                    total = 1
                )
            ),
            details = mapOf(
                object1Id to Block.Fields(
                    mapOf(
                        Block.Fields.NAME_KEY to object1Name,
                        ObjectSetConfig.TYPE_KEY to objectType1.url,
                        "iconEmoji" to "👤"
                    )
                ),
                object2Id to Block.Fields(
                    mapOf(
                        Block.Fields.NAME_KEY to object2Name,
                        ObjectSetConfig.TYPE_KEY to objectType2.url,
                        "iconEmoji" to "👤"
                    )
                )
            ),
            objectTypes = listOf()
        )

        // TESTING

        launchFragment(
            bundleOf(
                RelationValueBaseFragment.CTX_KEY to root,
                RelationValueBaseFragment.RELATION_KEY to relationId,
                RelationValueBaseFragment.TARGET_KEY to recordId
            )
        )

        with(R.id.recycler.rVMatcher()) {
            onItemView(0, R.id.tvTitle).checkHasText(object1Name)
            onItemView(0, R.id.tvSubtitle).checkHasText(R.string.unknown_object_type)
            onItemView(1, R.id.tvTitle).checkHasText(object2Name)
            onItemView(1, R.id.tvSubtitle).checkHasText(R.string.unknown_object_type)
            checkIsRecyclerSize(2)
        }
    }

    @Test
    fun shouldRenderProfileObjectWithNameAndInitial() {

        // SETUP

        val relationName = "Writers"
        val object1Name = "Virginia Woolf"
        val object1Id = MockDataFactory.randomUuid()
        val object2Name = "Réné-Auguste Chateaubriand"
        val object2Id = MockDataFactory.randomUuid()

        val objectType1 = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = "Writer",
            relations = emptyList(),
            emoji = "",
            layout = ObjectType.Layout.PROFILE,
            description = "",
            isHidden = false,
            smartBlockTypes = listOf()
        )

        val objectType2 = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = "Writer",
            relations = emptyList(),
            emoji = "",
            layout = ObjectType.Layout.PROFILE,
            description = "",
            isHidden = false,
            smartBlockTypes = listOf()
        )

        val relationId = MockDataFactory.randomUuid()
        val recordId = MockDataFactory.randomUuid()

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to recordId,
            relationId to listOf(object1Id, object2Id)
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        state.value = ObjectSet(
            blocks = listOf(
                Block(
                    id = MockDataFactory.randomUuid(),
                    children = emptyList(),
                    fields = Block.Fields.empty(),
                    content = Block.Content.DataView(
                        relations = listOf(
                            Relation(
                                key = relationId,
                                name = relationName,
                                format = Relation.Format.OBJECT,
                                source = Relation.Source.values().random()
                            )
                        ),
                        viewers = listOf(viewer),
                        source = MockDataFactory.randomUuid()
                    )
                )
            ),
            viewerDb = mapOf(
                viewer.id to ObjectSet.ViewerData(
                    records = listOf(record),
                    total = 1
                )
            ),
            details = mapOf(
                object1Id to Block.Fields(
                    mapOf(
                        Block.Fields.NAME_KEY to object1Name,
                        Block.Fields.TYPE_KEY to objectType1.url
                    )
                ),
                object2Id to Block.Fields(
                    mapOf(
                        Block.Fields.NAME_KEY to object2Name,
                        Block.Fields.TYPE_KEY to objectType2.url
                    )
                )
            ),
            objectTypes = listOf(objectType1, objectType2)
        )

        // TESTING

        launchFragment(
            bundleOf(
                RelationValueBaseFragment.CTX_KEY to root,
                RelationValueBaseFragment.RELATION_KEY to relationId,
                RelationValueBaseFragment.TARGET_KEY to recordId
            )
        )

        val rvMatcher = withRecyclerView(R.id.recycler)

        onView(rvMatcher.atPositionOnView(0, R.id.initial)).apply {
            check(matches(withText(object1Name.first().toString())))
        }

        onView(rvMatcher.atPositionOnView(1, R.id.initial)).apply {
            check(matches(withText(object2Name.first().toString())))
        }
    }

    private fun launchFragment(args: Bundle): FragmentScenario<TestRelationValueDVFragment> {
        return launchFragmentInContainer<TestRelationValueDVFragment>(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }

}