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
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_utils.const.DateConst
import com.anytypeio.anytype.core_utils.ext.toTimeSeconds
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.dataview.interactor.ObjectRelationList
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.mocking.MockDataFactory
import com.anytypeio.anytype.presentation.page.Editor
import com.anytypeio.anytype.presentation.page.editor.DetailModificationManager
import com.anytypeio.anytype.presentation.page.editor.ThemeColor
import com.anytypeio.anytype.presentation.relations.ObjectRelationListViewModelFactory
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.relations.RelationListFragment
import com.anytypeio.anytype.utils.*
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.text.SimpleDateFormat
import java.util.*

@RunWith(AndroidJUnit4::class)
@LargeTest
class ObjectRelationListTest {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var dispatcher: Dispatcher<Payload>

    @Mock
    lateinit var detailModificationManager: DetailModificationManager

    private lateinit var objectRelationList: ObjectRelationList
    private lateinit var updateDetail: UpdateDetail

    private val ctx = MockDataFactory.randomUuid()
    private val storage = Editor.Storage()

    lateinit var urlBuilder: UrlBuilder

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        urlBuilder = UrlBuilder(gateway)
        objectRelationList = ObjectRelationList(repo)
        updateDetail = UpdateDetail(repo)
        TestRelationListFragment.testVmFactory = ObjectRelationListViewModelFactory(
            stores = storage,
            urlBuilder = urlBuilder,
            objectRelationList = objectRelationList,
            dispatcher = dispatcher,
            detailModificationManager = detailModificationManager,
            updateDetail = updateDetail
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
            RelationListFragment.ARG_CTX to ctx,
            RelationListFragment.ARG_MODE to RelationListFragment.MODE_LIST
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
            RelationListFragment.ARG_CTX to ctx,
            RelationListFragment.ARG_MODE to RelationListFragment.MODE_LIST
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
            RelationListFragment.ARG_CTX to ctx,
            RelationListFragment.ARG_MODE to RelationListFragment.MODE_LIST
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
                RelationListFragment.ARG_CTX to ctx,
                RelationListFragment.ARG_MODE to RelationListFragment.MODE_LIST
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
            RelationListFragment.ARG_CTX to ctx,
            RelationListFragment.ARG_MODE to RelationListFragment.MODE_LIST
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
            RelationListFragment.ARG_CTX to ctx,
            RelationListFragment.ARG_MODE to RelationListFragment.MODE_LIST
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
            color = color1.title
        )

        val option2 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Done",
            color = color2.title
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
            RelationListFragment.ARG_CTX to ctx,
            RelationListFragment.ARG_MODE to RelationListFragment.MODE_LIST
        ))

        // TESTING

        with(R.id.recycler.rVMatcher()) {
            onItemView(0, R.id.tvSectionName).checkHasText(R.string.other_relations)
            onItemView(1, R.id.tvRelationTitle).checkHasText(name1)
            onItemView(1, R.id.tvRelationValue).checkHasText(option1.text)
            onItemView(1, R.id.tvRelationValue).checkHasTextColor(color1.text)
            onItemView(2, R.id.tvRelationTitle).checkHasText(name2)
            onItemView(2, R.id.tvRelationValue).checkHasText(option2.text)
            onItemView(2, R.id.tvRelationValue).checkHasTextColor(color2.text)
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
            color = color1.title
        )

        val option2 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Journalist",
            color = color2.title
        )

        val option3 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Politik",
            color = color3.title
        )

        val option4 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Critic",
            color = color4.title
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
            RelationListFragment.ARG_CTX to ctx,
            RelationListFragment.ARG_MODE to RelationListFragment.MODE_LIST
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
            RelationListFragment.ARG_CTX to ctx,
            RelationListFragment.ARG_MODE to RelationListFragment.MODE_LIST
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

    private fun launchFragment(args: Bundle): FragmentScenario<TestRelationListFragment> {
        return launchFragmentInContainer(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }
}