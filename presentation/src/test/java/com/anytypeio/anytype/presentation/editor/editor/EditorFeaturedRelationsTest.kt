package com.anytypeio.anytype.presentation.editor.editor

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.core_models.StubObjectType
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.render.parseThemeBackgroundColor
import com.anytypeio.anytype.presentation.mapper.toView
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorFeaturedRelationsTest : EditorPresentationTestSetup() {

    @get:Rule
    val timberTestRule: TimberTestRule = TimberTestRule.builder()
        .minPriority(Log.DEBUG)
        .showThread(true)
        .showTimestamp(false)
        .onlyLogWhenTestFails(true)
        .build()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
    }

    @After
    fun after() {
        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun `should render object type and text relation as featured relation`() = runTest {

        val title = MockTypicalDocumentFactory.title
        val header = MockTypicalDocumentFactory.header
        val block = MockTypicalDocumentFactory.a
        val featuredBlock = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.FeaturedRelations
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, featuredBlock.id, block.id)
        )

        val doc = listOf(page, header, title, block, featuredBlock)

        val objectTypeId = MockDataFactory.randomString()
        val objectTypeKey = MockDataFactory.randomString()
        val objectTypeName = MockDataFactory.randomString()
        val objectTypeDescription = MockDataFactory.randomString()

        val r1 = MockTypicalDocumentFactory.relationObject("Ad")
        val r2 = MockTypicalDocumentFactory.relationObject("De")
        val r3 = MockTypicalDocumentFactory.relationObject("HJ")
        val relationType = StubRelationObject(
            id = objectTypeId,
            key = Relations.TYPE,
            name = "Type relation",
            format = Relation.Format.LONG_TEXT
        )

        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val objectFields =
            mapOf(
                Relations.ID to root,
                r1.key to value1,
                r2.key to value2,
                r3.key to value3,
                Relations.TYPE to objectTypeId,
                Relations.FEATURED_RELATIONS to listOf(Relations.TYPE, r3.key)
            )

        val objectTypeFields =
            mapOf(
                Relations.ID to objectTypeId,
                Relations.UNIQUE_KEY to objectTypeKey,
                Relations.NAME to objectTypeName,
                Relations.DESCRIPTION to objectTypeDescription
            )

        val customDetails = ObjectViewDetails(
            mapOf(
                root to objectFields,
                objectTypeId to objectTypeFields
            )
        )

        stubInterceptEvents()
        stubSpaceManager()
        stubInterceptThreadStatus()
        stubSearchObjects()
        stubOpenDocument(
            document = doc,
            details = customDetails,

        )

        storeOfRelations.merge(
            listOf(r1, r2, r3, relationType)
        )

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                isFocused = false,
                text = title.content<Block.Content.Text>().text,
                emoji = null
            ),
            BlockView.FeaturedRelation(
                id = featuredBlock.id,
                relations = listOf(
                    ObjectRelationView.ObjectType.Base(
                        id = objectTypeId,
                        key = Relations.TYPE,
                        name = objectTypeName,
                        featured = true,
                        type = objectTypeId,
                        system = true
                    ),
                    ObjectRelationView.Default(
                        id = r3.id,
                        key = r3.key,
                        name = r3.name.orEmpty(),
                        value = value3,
                        featured = true,
                        format = Relation.Format.SHORT_TEXT,
                        system = false
                    )
                )
            ),
            BlockView.Text.Numbered(
                isFocused = false,
                id = block.id,
                marks = emptyList(),
                background = block.parseThemeBackgroundColor(),
                text = block.content<Block.Content.Text>().text,
                alignment = block.content<Block.Content.Text>().align?.toView(),
                number = 1,
                decorations = listOf(
                    BlockView.Decoration(
                        background = block.parseThemeBackgroundColor()
                    )
                )
            )
        )

        val test = vm.state.test()

        val first = test.awaitValue()
        val second = test.awaitValue()

        second.assertValue(ViewState.Success(expected))
    }

    @Test
    fun `should not render featured relations when featured block not present`() = runTest {

        val title = MockTypicalDocumentFactory.title
        val header = MockTypicalDocumentFactory.header
        val block = MockTypicalDocumentFactory.a

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, block.id)
        )

        val doc = listOf(page, header, title, block)

        val objectTypeId = MockDataFactory.randomString()
        val objectTypeKey = MockDataFactory.randomString()
        val objectTypeName = MockDataFactory.randomString()
        val objectTypeDescription = MockDataFactory.randomString()

        val r1 = MockTypicalDocumentFactory.relationObject("Ad")
        val r2 = MockTypicalDocumentFactory.relationObject("De")
        val r3 = MockTypicalDocumentFactory.relationObject("HJ")

        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val objectFields =
            mapOf(
                r1.key to value1,
                r2.key to value2,
                r3.key to value3,
                Relations.TYPE to objectTypeId,
                Relations.FEATURED_RELATIONS to listOf(Relations.TYPE)
            )


        val objectTypeFields =
            mapOf(
                Relations.ID to objectTypeId,
                Relations.UNIQUE_KEY to objectTypeKey,
                Relations.NAME to objectTypeName,
                Relations.DESCRIPTION to objectTypeDescription
            )

        val customDetails = ObjectViewDetails(
            mapOf(
                root to objectFields,
                objectTypeId to objectTypeFields
            )
        )

        stubInterceptEvents()
        stubSearchObjects()
        stubOpenDocument(
            document = doc,
            details = customDetails,

        )

        storeOfRelations.merge(
            listOf(r1, r2, r3)
        )

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        val expected =
            listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    isFocused = false,
                    text = title.content<Block.Content.Text>().text,
                    emoji = null
                ),
                BlockView.Text.Numbered(
                    isFocused = false,
                    id = block.id,
                    marks = emptyList(),
                    background = block.parseThemeBackgroundColor(),
                    text = block.content<Block.Content.Text>().text,
                    alignment = block.content<Block.Content.Text>().align?.toView(),
                    number = 1,
                    decorations = listOf(
                        BlockView.Decoration(
                            background = block.parseThemeBackgroundColor()
                        )
                    )
                )
            )

        vm.state.test().assertValue(ViewState.Success(expected))
    }

    @Test
    fun `should not render featured relations when list of ids is empty`() = runTest {

        val title = MockTypicalDocumentFactory.title
        val header = MockTypicalDocumentFactory.header
        val block = MockTypicalDocumentFactory.a
        val featuredBlock = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.FeaturedRelations
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, featuredBlock.id, block.id)
        )

        val doc = listOf(page, header, title, block, featuredBlock)

        val objectTypeId = MockDataFactory.randomString()
        val objectTypeKey = MockDataFactory.randomString()
        val objectTypeName = MockDataFactory.randomString()
        val objectTypeDescription = MockDataFactory.randomString()

        val r1 = MockTypicalDocumentFactory.relationObject("Ad")
        val r2 = MockTypicalDocumentFactory.relationObject("De")
        val r3 = MockTypicalDocumentFactory.relationObject("HJ")

        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val objectFields =
            mapOf(
                r1.key to value1,
                r2.key to value2,
                r3.key to value3,
                Relations.TYPE to objectTypeId,
                Relations.FEATURED_RELATIONS to emptyList<String>()
            )

        val objectTypeFields =
            mapOf(
                Relations.ID to objectTypeId,
                Relations.UNIQUE_KEY to objectTypeKey,
                Relations.NAME to objectTypeName,
                Relations.DESCRIPTION to objectTypeDescription
            )
        val customDetails = ObjectViewDetails(
            mapOf(
                root to objectFields,
                objectTypeId to objectTypeFields
            )
        )

        stubInterceptEvents()
        stubSearchObjects()
        stubOpenDocument(
            document = doc,
            details = customDetails,

        )

        storeOfRelations.merge(
            listOf(r1, r2, r3)
        )

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        val expected =
            listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    isFocused = false,
                    text = title.content<Block.Content.Text>().text,
                    emoji = null
                ),
                BlockView.Text.Numbered(
                    isFocused = false,
                    id = block.id,
                    marks = emptyList(),
                    background = block.parseThemeBackgroundColor(),
                    text = block.content<Block.Content.Text>().text,
                    alignment = block.content<Block.Content.Text>().align?.toView(),
                    number = 1,
                    decorations = listOf(
                        BlockView.Decoration(
                            background = block.parseThemeBackgroundColor()
                        )
                    )
                )
            )

        vm.state.test().assertValue(ViewState.Success(expected))
    }

    @Test
    fun `should not render text featured relation when appropriate relation is not present`() =
        runTest {

            val title = MockTypicalDocumentFactory.title
            val header = MockTypicalDocumentFactory.header
            val block = MockTypicalDocumentFactory.a
            val featuredBlock = Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                children = emptyList(),
                content = Block.Content.FeaturedRelations
            )

            val page = Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart,
                children = listOf(header.id, featuredBlock.id, block.id)
            )

            val doc = listOf(page, header, title, block, featuredBlock)

            val objectTypeId = MockDataFactory.randomString()
            val objectTypeKey = MockDataFactory.randomString()
            val objectTypeName = "Movie"
            val objectTypeDescription = MockDataFactory.randomString()

            val r1 = MockTypicalDocumentFactory.relationObject("Ad")
            val r2 = MockTypicalDocumentFactory.relationObject("De")
            val r3 = MockTypicalDocumentFactory.relationObject("HJ")
            val relationType = StubRelationObject(
                id = objectTypeId,
                key = Relations.TYPE,
                name = "Type relation",
                format = Relation.Format.LONG_TEXT
            )

            val value1 = MockDataFactory.randomString()
            val value2 = MockDataFactory.randomString()
            val value3 = MockDataFactory.randomString()
            val objectFields =
                mapOf(
                    Relations.ID to root,
                    r1.key to value1,
                    r2.key to value2,
                    r3.key to value3,
                    Relations.TYPE to objectTypeId,
                    Relations.FEATURED_RELATIONS to listOf(Relations.TYPE, r3.key)
                )


            val objectTypeFields =
                mapOf(
                    Relations.ID to objectTypeId,
                    Relations.UNIQUE_KEY to objectTypeKey,
                    Relations.NAME to objectTypeName,
                    Relations.DESCRIPTION to objectTypeDescription
                )

            val customDetails = ObjectViewDetails(
                mapOf(
                    root to objectFields,
                    objectTypeId to objectTypeFields
                )
            )

            stubInterceptEvents()
            stubSearchObjects()
            stubOpenDocument(
                document = doc,
                details = customDetails,

            )

            storeOfRelations.merge(
                listOf(r1, r2, relationType)
            )

            val vm = buildViewModel()

            vm.onStart(id = root, space = defaultSpace)

            advanceUntilIdle()

            val expected = listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    isFocused = false,
                    text = title.content<Block.Content.Text>().text,
                    emoji = null
                ),
                BlockView.FeaturedRelation(
                    id = featuredBlock.id,
                    relations = listOf(
                        ObjectRelationView.ObjectType.Base(
                            id = objectTypeId,
                            key = Relations.TYPE,
                            name = objectTypeName,
                            featured = true,
                            type = objectTypeId,
                            system = true
                        )
                    )
                ),
                BlockView.Text.Numbered(
                    isFocused = false,
                    id = block.id,
                    marks = emptyList(),
                    background = block.parseThemeBackgroundColor(),
                    text = block.content<Block.Content.Text>().text,
                    alignment = block.content<Block.Content.Text>().align?.toView(),
                    number = 1,
                    decorations = listOf(
                        BlockView.Decoration(
                            background = block.parseThemeBackgroundColor()
                        )
                    )
                )
            )

            assertEquals(
                expected = ViewState.Success(expected),
                actual = vm.state.value
            )
        }

    @Test
    fun `should render relation in featured relations if corresponding relation is hidden`() =
        runTest {

            val title = MockTypicalDocumentFactory.title
            val header = MockTypicalDocumentFactory.header
            val block = MockTypicalDocumentFactory.a

            val r1 = MockTypicalDocumentFactory.relationObject(name = "Ad", isHidden = false)
            val r2 = MockTypicalDocumentFactory.relationObject(name = "De", isHidden = true)
            val r3 = MockTypicalDocumentFactory.relationObject(name = "HJ", isHidden = true)

            val featuredBlock = Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                children = emptyList(),
                content = Block.Content.FeaturedRelations
            )

            val page = Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart,
                children = listOf(header.id, featuredBlock.id, block.id)
            )

            val doc = listOf(page, header, title, block, featuredBlock)

            val objectTypeId = MockDataFactory.randomString()
            val objectTypeKey = MockDataFactory.randomString()
            val objectTypeName = MockDataFactory.randomString()
            val objectTypeDescription = MockDataFactory.randomString()

            val relationType = StubRelationObject(
                id = objectTypeId,
                key = Relations.TYPE,
                name = "Type relation",
                format = Relation.Format.LONG_TEXT
            )

            val value1 = MockDataFactory.randomString()
            val value2 = MockDataFactory.randomString()
            val value3 = MockDataFactory.randomString()

            val objectFields =
                mapOf(
                    Relations.ID to root,
                    r1.key to value1,
                    r2.key to value2,
                    r3.key to value3,
                    Relations.TYPE to objectTypeId,
                    Relations.FEATURED_RELATIONS to listOf(Relations.TYPE, r1.key, r2.key, r3.key)
                )

            val objectTypeFields =
                mapOf(
                    Relations.ID to objectTypeId,
                    Relations.UNIQUE_KEY to objectTypeKey,
                    Relations.NAME to objectTypeName,
                    Relations.DESCRIPTION to objectTypeDescription
                )

            val customDetails = ObjectViewDetails(
                mapOf(
                    root to objectFields,
                    objectTypeId to objectTypeFields
                )
            )

            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubSearchObjects()
            stubOpenDocument(
                document = doc,
                details = customDetails,

            )

            storeOfRelations.merge(
                listOf(r1, r2, r3, relationType)
            )

            val vm = buildViewModel()

            vm.onStart(id = root, space = defaultSpace)

            advanceUntilIdle()

            val expected = listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    isFocused = false,
                    text = title.content<Block.Content.Text>().text,
                    emoji = null
                ),
                BlockView.FeaturedRelation(
                    id = featuredBlock.id,
                    relations = listOf(
                        ObjectRelationView.ObjectType.Base(
                            id = objectTypeId,
                            key = Relations.TYPE,
                            name = objectTypeName,
                            featured = true,
                            type = objectTypeId,
                            system = true
                        ),
                        ObjectRelationView.Default(
                            id = r1.id,
                            key = r1.key,
                            name = r1.name.orEmpty(),
                            value = value1,
                            featured = true,
                            format = Relation.Format.SHORT_TEXT,
                            system = false
                        ),
                        ObjectRelationView.Default(
                            id = r2.id,
                            key = r2.key,
                            name = r2.name.orEmpty(),
                            value = value2,
                            featured = true,
                            format = Relation.Format.SHORT_TEXT,
                            system = false
                        ),
                        ObjectRelationView.Default(
                            id = r3.id,
                            key = r3.key,
                            name = r3.name.orEmpty(),
                            value = value3,
                            featured = true,
                            format = Relation.Format.SHORT_TEXT,
                            system = false
                        )
                    )
                ),
                BlockView.Text.Numbered(
                    isFocused = false,
                    id = block.id,
                    marks = emptyList(),
                    background = block.parseThemeBackgroundColor(),
                    text = block.content<Block.Content.Text>().text,
                    alignment = block.content<Block.Content.Text>().align?.toView(),
                    number = 1,
                    decorations = listOf(
                        BlockView.Decoration(
                            background = block.parseThemeBackgroundColor()
                        )
                    )
                )
            )

            assertEquals(
                expected = ViewState.Success(expected),
                actual = vm.state.value
            )
        }

    @Test
    fun `should render deleted object type as featured relation when type is not present in details`() =
        runTest {

            val title = MockTypicalDocumentFactory.title
            val header = MockTypicalDocumentFactory.header
            val block = MockTypicalDocumentFactory.a
            val featuredBlock = Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                children = emptyList(),
                content = Block.Content.FeaturedRelations
            )

            val page = Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart,
                children = listOf(header.id, featuredBlock.id, block.id)
            )

            val doc = listOf(page, header, title, block, featuredBlock)

            val objectTypeId = MockDataFactory.randomString()

            val r1 = MockTypicalDocumentFactory.relationObject("Ad")
            val r2 = MockTypicalDocumentFactory.relationObject("De")
            val r3 = MockTypicalDocumentFactory.relationObject("HJ")

            val value1 = MockDataFactory.randomString()
            val value2 = MockDataFactory.randomString()
            val value3 = MockDataFactory.randomString()
            val objectFields =
                mapOf(
                    Relations.ID to root,
                    r1.key to value1,
                    r2.key to value2,
                    r3.key to value3,
                    Relations.TYPE to objectTypeId,
                    Relations.FEATURED_RELATIONS to listOf(Relations.TYPE, r3.key)
                )

            val relationType = StubRelationObject(
                id = objectTypeId,
                key = Relations.TYPE,
                name = "Type relation",
                format = Relation.Format.LONG_TEXT
            )

            val customDetails = ObjectViewDetails(mapOf(root to objectFields))

            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubSearchObjects()
            stubOpenDocument(
                document = doc,
                details = customDetails,

            )

            storeOfRelations.merge(
                listOf(r1, r2, r3, relationType)
            )

            val vm = buildViewModel()

            vm.onStart(id = root, space = defaultSpace)

            advanceUntilIdle()

            val expected = listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    isFocused = false,
                    text = title.content<Block.Content.Text>().text,
                    emoji = null
                ),
                BlockView.FeaturedRelation(
                    id = featuredBlock.id,
                    relations = listOf(
                        ObjectRelationView.ObjectType.Deleted(
                            id = objectTypeId,
                            key = Relations.TYPE,
                            featured = true,
                            system = true
                        ),
                        ObjectRelationView.Default(
                            id = r3.id,
                            key = r3.key,
                            name = r3.name.orEmpty(),
                            value = value3,
                            featured = true,
                            format = Relation.Format.SHORT_TEXT,
                            system = false
                        )
                    )
                ),
                BlockView.Text.Numbered(
                    isFocused = false,
                    id = block.id,
                    marks = emptyList(),
                    background = block.parseThemeBackgroundColor(),
                    text = block.content<Block.Content.Text>().text,
                    alignment = block.content<Block.Content.Text>().align?.toView(),
                    number = 1,
                    decorations = listOf(
                        BlockView.Decoration(
                            background = block.parseThemeBackgroundColor()
                        )
                    )
                )
            )

            assertEquals(
                expected = ViewState.Success(expected),
                actual = vm.state.value
            )
        }

    @Test
    fun `should render deleted object type as featured relation when flag is deleted`() = runTest {

        val title = MockTypicalDocumentFactory.title
        val header = MockTypicalDocumentFactory.header
        val block = MockTypicalDocumentFactory.a
        val featuredBlock = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.FeaturedRelations
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, featuredBlock.id, block.id)
        )

        val doc = listOf(page, header, title, block, featuredBlock)

        val objectTypeId = MockDataFactory.randomString()

        val r1 = MockTypicalDocumentFactory.relationObject("Ad")
        val r2 = MockTypicalDocumentFactory.relationObject("De")
        val r3 = MockTypicalDocumentFactory.relationObject("HJ")

        val relationObjectType = StubObjectType(
            id = objectTypeId
        )

        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val objectFields =
            mapOf(
                Relations.ID to root,
                r1.key to value1,
                r2.key to value2,
                r3.key to value3,
                Relations.TYPE to objectTypeId,
                Relations.FEATURED_RELATIONS to listOf(Relations.TYPE, r3.key)
            )

        val objectTypeFields =
            mapOf(
                Relations.IS_DELETED to true
            )

        val customDetails =
            ObjectViewDetails(mapOf(root to objectFields, objectTypeId to objectTypeFields))

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubSearchObjects()
        stubOpenDocument(
            document = doc,
            details = customDetails,

        )

        val relationType = StubRelationObject(
            id = objectTypeId,
            key = Relations.TYPE,
            name = "Type relation",
            format = Relation.Format.LONG_TEXT
        )

        storeOfRelations.merge(
            listOf(r1, r2, r3, relationType)
        )

        storeOfObjectTypes.merge(
            types = listOf(relationObjectType)
        )

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                isFocused = false,
                text = title.content<Block.Content.Text>().text,
                emoji = null
            ),
            BlockView.FeaturedRelation(
                id = featuredBlock.id,
                relations = listOf(
                    ObjectRelationView.ObjectType.Deleted(
                        id = objectTypeId,
                        key = Relations.TYPE,
                        featured = true,
                        system = true
                    ),
                    ObjectRelationView.Default(
                        id = r3.id,
                        key = r3.key,
                        name = r3.name.orEmpty(),
                        value = value3,
                        featured = true,
                        format = Relation.Format.SHORT_TEXT,
                        system = false
                    )
                )
            ),
            BlockView.Text.Numbered(
                isFocused = false,
                id = block.id,
                marks = emptyList(),
                background = block.parseThemeBackgroundColor(),
                text = block.content<Block.Content.Text>().text,
                alignment = block.content<Block.Content.Text>().align?.toView(),
                number = 1,
                decorations = listOf(
                    BlockView.Decoration(
                        background = block.parseThemeBackgroundColor()
                    )
                )
            )
        )

        assertEquals(
            expected = ViewState.Success(expected),
            actual = vm.state.value
        )
    }

    @Test
    fun `should render backlinks and links as featured relations`() = runTest {

        val title = MockTypicalDocumentFactory.title
        val header = MockTypicalDocumentFactory.header
        val block = MockTypicalDocumentFactory.a
        val featuredBlock = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.FeaturedRelations
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, featuredBlock.id, block.id)
        )

        val doc = listOf(page, header, title, block, featuredBlock)

        val backlinksRelation =
            StubRelationObject(uniqueKey = Relations.BACKLINKS, key = Relations.BACKLINKS)
        val linksRelation = StubRelationObject(uniqueKey = Relations.LINKS, key = Relations.LINKS)

        val objBacklinks1 = StubObject("objBacklinks1")
        val objBacklinks2 = StubObject("objBacklinks2")
        val objBacklinks3 = StubObject("objBacklinks3")

        val objLinksTo1 = StubObject("objLinksTo1")
        val objLinksTo2 = StubObject("objLinksTo2")

        val objectDetails = ObjectViewDetails(
            mapOf(
                root to
                        mapOf(
                            Relations.ID to root,
                            Relations.TYPE to MockDataFactory.randomString(),
                            Relations.FEATURED_RELATIONS to listOf(
                                Relations.BACKLINKS,
                                Relations.LINKS
                            ),
                            Relations.BACKLINKS to listOf(
                                objBacklinks1.id,
                                objBacklinks2.id,
                                objBacklinks3.id
                            ),
                            Relations.LINKS to listOf(objLinksTo1.id, objLinksTo2.id)
                        ),
                objBacklinks1.id to
                        mapOf(Relations.ID to objBacklinks1.id),
                objBacklinks2.id to
                        mapOf(Relations.ID to objBacklinks2.id),
                objBacklinks3.id to
                        mapOf(Relations.ID to objBacklinks3.id),
                objLinksTo1.id to
                        mapOf(Relations.ID to objLinksTo1.id),
                objLinksTo2.id to
                        mapOf(Relations.ID to objLinksTo2.id)
            )
        )

        storeOfRelations.merge(
            listOf(backlinksRelation, linksRelation)
        )

        stubGetNetworkMode()
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubSearchObjects()
        stubOpenDocument(
            document = doc,
            details = objectDetails,

        )

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                isFocused = false,
                text = title.content<Block.Content.Text>().text,
                emoji = null
            ),
            BlockView.FeaturedRelation(
                id = featuredBlock.id,
                relations = listOf(
                    ObjectRelationView.Links.Backlinks(
                        id = backlinksRelation.id,
                        key = backlinksRelation.key,
                        name = backlinksRelation.name.orEmpty(),
                        featured = true,
                        system = true,
                        count = 3
                    ),
                    ObjectRelationView.Links.From(
                        id = linksRelation.id,
                        key = linksRelation.key,
                        name = linksRelation.name.orEmpty(),
                        featured = true,
                        system = true,
                        count = 2
                    )
                )
            ),
            BlockView.Text.Numbered(
                isFocused = false,
                id = block.id,
                marks = emptyList(),
                background = block.parseThemeBackgroundColor(),
                text = block.content<Block.Content.Text>().text,
                alignment = block.content<Block.Content.Text>().align?.toView(),
                number = 1,
                decorations = listOf(
                    BlockView.Decoration(
                        background = block.parseThemeBackgroundColor()
                    )
                )
            )
        )

        coroutineTestRule.advanceUntilIdle()

        assertEquals(
            expected = ViewState.Success(expected),
            actual = vm.state.value
        )
    }

    @Test
    fun `should not render backlinks and links as featured relations, when no sub objects are present`() =
        runTest {

            val title = MockTypicalDocumentFactory.title
            val header = MockTypicalDocumentFactory.header
            val block = MockTypicalDocumentFactory.a
            val featuredBlock = Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                children = emptyList(),
                content = Block.Content.FeaturedRelations
            )

            val page = Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart,
                children = listOf(header.id, featuredBlock.id, block.id)
            )

            val doc = listOf(page, header, title, block, featuredBlock)

            val backlinksRelation =
                StubRelationObject(uniqueKey = Relations.BACKLINKS, key = Relations.BACKLINKS)
            val linksRelation =
                StubRelationObject(uniqueKey = Relations.LINKS, key = Relations.LINKS)

            val objectDetails = ObjectViewDetails(
                mapOf(
                    root to
                            mapOf(
                                Relations.TYPE to MockDataFactory.randomString(),
                                Relations.FEATURED_RELATIONS to listOf(
                                    Relations.BACKLINKS,
                                    Relations.LINKS
                                )
                            )
                )
            )

            storeOfRelations.merge(
                listOf(backlinksRelation, linksRelation)
            )

            stubGetNetworkMode()
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubSearchObjects()
            stubOpenDocument(
                document = doc,
                details = objectDetails,

            )

            val vm = buildViewModel()

            vm.onStart(id = root, space = defaultSpace)

            advanceUntilIdle()

            val expected = listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    isFocused = false,
                    text = title.content<Block.Content.Text>().text,
                    emoji = null
                ),
                BlockView.Text.Numbered(
                    isFocused = false,
                    id = block.id,
                    marks = emptyList(),
                    background = block.parseThemeBackgroundColor(),
                    text = block.content<Block.Content.Text>().text,
                    alignment = block.content<Block.Content.Text>().align?.toView(),
                    number = 1,
                    decorations = listOf(
                        BlockView.Decoration(
                            background = block.parseThemeBackgroundColor()
                        )
                    )
                )
            )

            assertEquals(
                expected = ViewState.Success(expected),
                actual = vm.state.value
            )
        }
}