package com.anytypeio.anytype.features.relations

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_utils.const.DateConst
import com.anytypeio.anytype.core_utils.ext.toTimeSeconds
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.GetDateObjectByTimestamp
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.primitives.FieldParserImpl
import com.anytypeio.anytype.domain.relations.AddRelationToObject
import com.anytypeio.anytype.domain.relations.AddToFeaturedRelations
import com.anytypeio.anytype.domain.relations.DeleteRelationFromObject
import com.anytypeio.anytype.domain.relations.RemoveFromFeaturedRelations
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.objects.LockedStateProvider
import com.anytypeio.anytype.presentation.relations.ObjectRelationListViewModelFactory
import com.anytypeio.anytype.presentation.relations.RelationListViewModel
import com.anytypeio.anytype.presentation.relations.providers.RelationListProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.utils.checkHasText
import com.anytypeio.anytype.test_utils.utils.checkHasTextColor
import com.anytypeio.anytype.test_utils.utils.checkIsRecyclerSize
import com.anytypeio.anytype.test_utils.utils.onItemView
import com.anytypeio.anytype.test_utils.utils.rVMatcher
import com.anytypeio.anytype.test_utils.utils.resources
import com.anytypeio.anytype.ui.relations.ObjectRelationListFragment
import com.anytypeio.anytype.utils.CoroutinesTestRule
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
@LargeTest
class ObjectRelationListTest {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val spaceId = MockDataFactory.randomUuid()

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var dispatcher: Dispatcher<Payload>

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var storeOfRelations: StoreOfRelations

    @Mock
    lateinit var relationListProvider: RelationListProvider

    @Mock
    lateinit var addRelationToObject: AddRelationToObject

    @Mock
    lateinit var lockedStateProvider: LockedStateProvider

    @Mock
    lateinit var analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate

    private lateinit var updateDetail: UpdateDetail
    private lateinit var addToFeaturedRelations: AddToFeaturedRelations
    private lateinit var removeFromFeaturedRelations: RemoveFromFeaturedRelations
    private lateinit var deleteRelationFromObject: DeleteRelationFromObject

    private val ctx = MockDataFactory.randomUuid()
    private val storage = Editor.Storage()

    lateinit var urlBuilder: UrlBuilder

    lateinit var fieldParser: FieldParser

    @Mock
    lateinit var logger: Logger

    @Mock
    lateinit var dateProvider: DateProvider

    @Mock
    lateinit var getDateObjectByTimestamp: GetDateObjectByTimestamp

    @Mock
    lateinit var stringResourceProvider: StringResourceProvider

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        urlBuilder = UrlBuilder(gateway)
        updateDetail = UpdateDetail(repo)
        fieldParser = FieldParserImpl(
            logger = logger,
            dateProvider = dateProvider,
            getDateObjectByTimestamp = getDateObjectByTimestamp,
            stringResourceProvider = stringResourceProvider
        )
        addToFeaturedRelations = AddToFeaturedRelations(repo)
        removeFromFeaturedRelations = RemoveFromFeaturedRelations(repo)
        deleteRelationFromObject = DeleteRelationFromObject(repo)
        TestObjectRelationListFragment.testVmFactory = ObjectRelationListViewModelFactory(
            vmParams = RelationListViewModel.VmParams(
                spaceId = SpaceId(spaceId)
            ),
            lockedStateProvider = lockedStateProvider,
            relationListProvider = relationListProvider,
            urlBuilder = urlBuilder,
            dispatcher = dispatcher,
            updateDetail = updateDetail,
            addToFeaturedRelations = addToFeaturedRelations,
            removeFromFeaturedRelations = removeFromFeaturedRelations,
            deleteRelationFromObject = deleteRelationFromObject,
            analytics = analytics,
            storeOfRelations = storeOfRelations,
            addRelationToObject = addRelationToObject,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            fieldParser = fieldParser
        )
    }

    @Test(expected = RuntimeException::class)
    fun shouldThrowAnExceptionIfArgsNotProvided() {
        launchFragment(bundleOf())
    }

    @Test
    fun shouldDisplayOneRelationWithoutValueInsideOtherRelationsSection() {

        // SETUP

        val name = "Description"

        val relation = Relation(
            key = MockDataFactory.randomUuid(),
            format = Relation.Format.LONG_TEXT,
            source = Relation.Source.values().random(),
            name = name
        )

        runBlocking {
            storage.relations.update(
                listOf(relation)
            )
        }

        launchFragment(bundleOf(
            ObjectRelationListFragment.ARG_CTX to ctx,
            ObjectRelationListFragment.ARG_MODE to ObjectRelationListFragment.MODE_LIST
        ))

        // TESTING

        with(R.id.recycler.rVMatcher()) {
            onItemView(0, R.id.tvSectionName).checkHasText(R.string.other_relations)
            onItemView(1, R.id.tvRelationTitle).checkHasText(name)
            onItemView(1, R.id.tvRelationValue).checkHasText("")
            checkIsRecyclerSize(2)
        }
    }

    @Test
    fun shouldDisplayOnlyFirstRelationBecauseSecondIsHiddenInsideOtherRelationsSection() {

        // SETUP

        val name1 = "Description"
        val name2 = "Identifier"

        val relation1 = Relation(
            key = MockDataFactory.randomUuid(),
            format = Relation.Format.LONG_TEXT,
            source = Relation.Source.values().random(),
            isHidden = true,
            name = name1
        )

        val relation2 = Relation(
            key = MockDataFactory.randomUuid(),
            format = Relation.Format.LONG_TEXT,
            source = Relation.Source.values().random(),
            name = name2
        )

        runBlocking {
            storage.relations.update(
                listOf(relation1, relation2)
            )
        }

        launchFragment(bundleOf(
            ObjectRelationListFragment.ARG_CTX to ctx,
            ObjectRelationListFragment.ARG_MODE to ObjectRelationListFragment.MODE_LIST
        ))

        // TESTING

        with(R.id.recycler.rVMatcher()) {
            onItemView(0, R.id.tvSectionName).checkHasText(R.string.other_relations)
            onItemView(1, R.id.tvRelationTitle).checkHasText(name2)
            onItemView(1, R.id.tvRelationValue).checkHasText("")
            checkIsRecyclerSize(2)
        }
    }

    @Test
    fun shouldDisplayTwoRelationsWithoutValueInsideOtherRelationsSection() {

        // SETUP

        val name1 = "Description"
        val name2 = "Comment"

        val relation1 = Relation(
            key = MockDataFactory.randomUuid(),
            format = Relation.Format.LONG_TEXT,
            source = Relation.Source.values().random(),
            name = name1
        )

        val relation2 = Relation(
            key = MockDataFactory.randomUuid(),
            format = Relation.Format.LONG_TEXT,
            source = Relation.Source.values().random(),
            name = name2
        )

        runBlocking {
            storage.relations.update(
                listOf(relation1, relation2)
            )
        }

        launchFragment(bundleOf(
            ObjectRelationListFragment.ARG_CTX to ctx,
            ObjectRelationListFragment.ARG_MODE to ObjectRelationListFragment.MODE_LIST
        ))

        // TESTING

        with(R.id.recycler.rVMatcher()) {
            onItemView(0, R.id.tvSectionName).checkHasText(R.string.other_relations)
            onItemView(1, R.id.tvRelationTitle).checkHasText(name1)
            onItemView(1, R.id.tvRelationValue).checkHasText("")
            onItemView(2, R.id.tvRelationTitle).checkHasText(name2)
            onItemView(2, R.id.tvRelationValue).checkHasText("")
            checkIsRecyclerSize(3)
        }
    }

    @Test
    fun shouldDisplayTwoRelationsWithValuesInsideOtherRelationsSection() {

        // SETUP

        val name1 = "Description"
        val name2 = "Comment"
        val value1 = "A mountain is an elevated portion of the Earth's crust, generally with steep sides that show significant exposed bedrock."
        val value2 = "We've never seen that mountain before."

        val relation1 = Relation(
            key = MockDataFactory.randomUuid(),
            format = Relation.Format.LONG_TEXT,
            source = Relation.Source.values().random(),
            name = name1
        )

        val relation2 = Relation(
            key = MockDataFactory.randomUuid(),
            format = Relation.Format.LONG_TEXT,
            source = Relation.Source.values().random(),
            name = name2
        )

        val relations = listOf(relation1, relation2)
        val details = Block.Details(
            mapOf(
                ctx to Block.Fields(
                    mapOf(
                        relation1.key to value1,
                        relation2.key to value2,
                    )
                )
            )
        )

        runBlocking {
            storage.relations.update(relations)
            storage.details.update(details)
        }

        launchFragment(
            bundleOf(
                ObjectRelationListFragment.ARG_CTX to ctx,
                ObjectRelationListFragment.ARG_MODE to ObjectRelationListFragment.MODE_LIST
            )
        )

        // TESTING

        with(R.id.recycler.rVMatcher()) {
            onItemView(0, R.id.tvSectionName).checkHasText(R.string.other_relations)
            onItemView(1, R.id.tvRelationTitle).checkHasText(name1)
            onItemView(1, R.id.tvRelationValue).checkHasText(value1)
            onItemView(2, R.id.tvRelationTitle).checkHasText(name2)
            onItemView(2, R.id.tvRelationValue).checkHasText(value2)
            checkIsRecyclerSize(3)
        }
    }

    @Test
    fun shouldDisplayTwoObjectRelationsWithNameAndAvatarInitialsInsideOtherRelationsSection() {

        // SETUP

        val name1 = "Assignee"
        val target1: Id = MockDataFactory.randomUuid()
        val username1 = "Konstantin"

        val name2 = "Created by"
        val target2: Id = MockDataFactory.randomUuid()
        val username2 = "Roman"

        val relation1 = Relation(
            key = MockDataFactory.randomUuid(),
            format = Relation.Format.OBJECT,
            source = Relation.Source.values().random(),
            name = name1
        )

        val relation2 = Relation(
            key = MockDataFactory.randomUuid(),
            format = Relation.Format.OBJECT,
            source = Relation.Source.values().random(),
            name = name2
        )

        val relations = listOf(relation1, relation2)

        val details = Block.Details(
            mapOf(
                ctx to Block.Fields(
                    mapOf(
                        relation1.key to target1,
                        relation2.key to target2,
                    )
                ),
                target1 to Block.Fields(
                    mapOf(
                        Block.Fields.NAME_KEY to username1
                    )
                ),
                target2 to Block.Fields(
                    mapOf(
                        Block.Fields.NAME_KEY to username2
                    )
                )
            )
        )

        runBlocking {
            storage.relations.update(relations)
            storage.details.update(details)
        }

        launchFragment(bundleOf(
            ObjectRelationListFragment.ARG_CTX to ctx,
            ObjectRelationListFragment.ARG_MODE to ObjectRelationListFragment.MODE_LIST
        ))

        // TESTING

        with(R.id.recycler.rVMatcher()) {
            onItemView(0, R.id.tvSectionName).checkHasText(R.string.other_relations)
            onItemView(1, R.id.tvRelationTitle).checkHasText(name1)
            onItemView(1, R.id.obj0).check(matches(hasDescendant(withText(username1))))
            onItemView(2, R.id.tvRelationTitle).checkHasText(name2)
            onItemView(2, R.id.obj0).check(matches(hasDescendant(withText(username2))))
            checkIsRecyclerSize(3)
        }
    }

    @Test
    fun shouldDisplayTwoDateRelationsInsideOtherRelationsSection() {

        // SETUP

        val format = SimpleDateFormat(DateConst.DEFAULT_DATE_FORMAT, Locale.getDefault())

        val name1 = "Date of birth"
        val date1 = System.currentTimeMillis()
        val date1Screen = format.format(Date(date1))

        val name2 = "Last modified at"
        val date2 = System.currentTimeMillis()
        val date2Screen = format.format(Date(date2))

        val relation1 = Relation(
            key = MockDataFactory.randomUuid(),
            format = Relation.Format.DATE,
            source = Relation.Source.values().random(),
            name = name1,
        )

        val relation2 = Relation(
            key = MockDataFactory.randomUuid(),
            format = Relation.Format.DATE,
            source = Relation.Source.values().random(),
            name = name2
        )

        val relations = listOf(relation1, relation2)

        val details = Block.Details(
            mapOf(
                ctx to Block.Fields(
                    mapOf(
                        relation1.key to date1.toTimeSeconds(),
                        relation2.key to date2.toTimeSeconds(),
                    )
                )
            )
        )

        runBlocking {
            storage.relations.update(relations)
            storage.details.update(details)
        }

        launchFragment(bundleOf(
            ObjectRelationListFragment.ARG_CTX to ctx,
            ObjectRelationListFragment.ARG_MODE to ObjectRelationListFragment.MODE_LIST
        ))

        // TESTING

        with(R.id.recycler.rVMatcher()) {
            onItemView(0, R.id.tvSectionName).checkHasText(R.string.other_relations)
            onItemView(1, R.id.tvRelationTitle).checkHasText(name1)
            onItemView(1, R.id.tvRelationValue).checkHasText(date1Screen)
            onItemView(2, R.id.tvRelationTitle).checkHasText(name2)
            onItemView(2, R.id.tvRelationValue).checkHasText(date2Screen)
            checkIsRecyclerSize(3)
        }
    }

    @Test
    fun shouldDisplayTwoStatusRelationsInsideOtherRelationsSection() {

        // SETUP

        val color1 = ThemeColor.RED
        val color2 = ThemeColor.TEAL

        val option1 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "In progress",
            color = color1.code
        )

        val option2 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Done",
            color = color2.code
        )

        val name1 = "Status 1"
        val name2 = "Status 2"

        val relation1 = Relation(
            key = MockDataFactory.randomUuid(),
            format = Relation.Format.STATUS,
            source = Relation.Source.values().random(),
            name = name1,
            selections = listOf(option1)
        )

        val relation2 = Relation(
            key = MockDataFactory.randomUuid(),
            format = Relation.Format.STATUS,
            source = Relation.Source.values().random(),
            name = name2,
            selections = listOf(option2)
        )

        val relations = listOf(relation1, relation2)

        val details = Block.Details(
            mapOf(
                ctx to Block.Fields(
                    mapOf(
                        relation1.key to option1.id,
                        relation2.key to option2.id,
                    )
                )
            )
        )

        runBlocking {
            storage.relations.update(relations)
            storage.details.update(details)
        }

        launchFragment(bundleOf(
            ObjectRelationListFragment.ARG_CTX to ctx,
            ObjectRelationListFragment.ARG_MODE to ObjectRelationListFragment.MODE_LIST
        ))

        // TESTING

        with(R.id.recycler.rVMatcher()) {
            onItemView(0, R.id.tvSectionName).checkHasText(R.string.other_relations)
            onItemView(1, R.id.tvRelationTitle).checkHasText(name1)
            onItemView(1, R.id.tvRelationValue).checkHasText(option1.text)
            onItemView(1, R.id.tvRelationValue).checkHasTextColor(resources.dark(color1))
            onItemView(2, R.id.tvRelationTitle).checkHasText(name2)
            onItemView(2, R.id.tvRelationValue).checkHasText(option2.text)
            onItemView(2, R.id.tvRelationValue).checkHasTextColor(resources.dark(color2))
            checkIsRecyclerSize(3)
        }
    }

    @Test
    fun shouldDisplayFourTagRelationsInsideOtherRelationsSection() {

        // SETUP

        val color1 = ThemeColor.RED
        val color2 = ThemeColor.TEAL
        val color3 = ThemeColor.ICE
        val color4 = ThemeColor.PURPLE

        val name = "Role"

        val option1 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Essayist",
            color = color1.code
        )

        val option2 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Journalist",
            color = color2.code
        )

        val option3 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Politik",
            color = color3.code
        )

        val option4 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Critic",
            color = color4.code
        )

        val relation = Relation(
            key = MockDataFactory.randomUuid(),
            format = Relation.Format.TAG,
            source = Relation.Source.values().random(),
            name = name,
            selections = listOf(option1, option2, option3, option4)
        )

        val relations = listOf(relation)

        val details = Block.Details(
            mapOf(
                ctx to Block.Fields(
                    mapOf(
                        relation.key to listOf(option1.id, option2.id, option3.id, option4.id)
                    )
                )
            )
        )

        runBlocking {
            storage.relations.update(relations)
            storage.details.update(details)
        }

        launchFragment(bundleOf(
            ObjectRelationListFragment.ARG_CTX to ctx,
            ObjectRelationListFragment.ARG_MODE to ObjectRelationListFragment.MODE_LIST
        ))

        // TESTING

        with(R.id.recycler.rVMatcher()) {
            onItemView(0, R.id.tvSectionName).checkHasText(R.string.other_relations)
            onItemView(1, R.id.tvRelationTitle).checkHasText(name)
            onItemView(1, R.id.tag0).check(matches((withText(option1.text))))
            onItemView(1, R.id.tag1).check(matches((withText(option2.text))))
            onItemView(1, R.id.tag2).check(matches((withText(option3.text))))
            onItemView(1, R.id.tag3).check(matches((withText(option4.text))))
            checkIsRecyclerSize(2)
        }
    }

    @Test
    fun shouldDisplayTwoFileRelationsInsideOtherRelationsSection() {

        // SETUP

        val name = "Attachement"

        val relation = Relation(
            key = MockDataFactory.randomUuid(),
            format = Relation.Format.FILE,
            source = Relation.Source.values().random(),
            name = name,
            selections = emptyList()
        )

        val relations = listOf(relation)

        val file1 = MockDataFactory.randomUuid()
        val file2 = MockDataFactory.randomUuid()

        val details = Block.Details(
            mapOf(
                ctx to Block.Fields(
                    mapOf(
                        relation.key to listOf(file1, file2)
                    )
                ),
                file1 to Block.Fields(
                    mapOf(
                        "name" to "Document",
                        "ext" to "pdf",
                        "mime" to "application/pdf"
                    )
                ),
                file2 to Block.Fields(
                    mapOf(
                        "name" to "Image",
                        "ext" to "jpg",
                        "mime" to "image/jpeg"
                    )
                )
            )
        )

        runBlocking {
            storage.relations.update(relations)
            storage.details.update(details)
        }

        launchFragment(bundleOf(
            ObjectRelationListFragment.ARG_CTX to ctx,
            ObjectRelationListFragment.ARG_MODE to ObjectRelationListFragment.MODE_LIST
        ))

        // TESTING

        with(R.id.recycler.rVMatcher()) {
            onItemView(0, R.id.tvSectionName).checkHasText(R.string.other_relations)
            onItemView(1, R.id.tvRelationTitle).checkHasText(name)
            onItemView(1, R.id.file0).check(matches(hasDescendant(withText("Document"))))
            onItemView(1, R.id.file1).check(matches(hasDescendant(withText("Image"))))
            checkIsRecyclerSize(2)
        }
    }

    private fun launchFragment(args: Bundle): FragmentScenario<TestObjectRelationListFragment> {
        return launchFragmentInContainer(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }
}