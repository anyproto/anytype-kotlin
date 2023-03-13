package com.anytypeio.anytype.presentation.editor.editor

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.core_models.StubObjectType
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.render.parseThemeBackgroundColor
import com.anytypeio.anytype.presentation.mapper.toView
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import kotlinx.coroutines.test.runTest
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

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

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
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
            content = Block.Content.Smart(SmartBlockType.PAGE),
            children = listOf(header.id, featuredBlock.id, block.id)
        )

        val doc = listOf(page, header, title, block, featuredBlock)

        val objectTypeId = MockDataFactory.randomString()
        val objectTypeName = MockDataFactory.randomString()
        val objectTypeDescription = MockDataFactory.randomString()

        val r1 = MockTypicalDocumentFactory.relationObject("Ad")
        val r2 = MockTypicalDocumentFactory.relationObject("De")
        val r3 = MockTypicalDocumentFactory.relationObject("HJ")

        val relationObjectType = StubObjectType(
            id = objectTypeId,
            name = "Object Type"
        )

        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val objectFields = Block.Fields(
            mapOf(
                r1.key to value1,
                r2.key to value2,
                r3.key to value3,
                Relations.TYPE to objectTypeId,
                Relations.FEATURED_RELATIONS to listOf(Relations.TYPE, r3.key)
            )
        )

        val objectTypeFields = Block.Fields(
            mapOf(
                Relations.NAME to objectTypeName,
                Relations.DESCRIPTION to objectTypeDescription
            )
        )
        val customDetails = Block.Details(
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
            relations = emptyList()
        )

        storeOfRelations.merge(
            listOf(r1, r2, r3)
        )

        storeOfObjectTypes.merge(
            types = listOf(relationObjectType)
        )

        val vm = buildViewModel()

        vm.onStart(root)

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
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = block.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            )
        )

        assertEquals(
            expected = ViewState.Success(expected),
            actual = vm.state.value
        )
    }

    @Test
    fun `should not render featured relations when featured block not present`() = runTest {

        val title = MockTypicalDocumentFactory.title
        val header = MockTypicalDocumentFactory.header
        val block = MockTypicalDocumentFactory.a

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(SmartBlockType.PAGE),
            children = listOf(header.id, block.id)
        )

        val doc = listOf(page, header, title, block)

        val objectTypeId = MockDataFactory.randomString()
        val objectTypeName = MockDataFactory.randomString()
        val objectTypeDescription = MockDataFactory.randomString()

        val r1 = MockTypicalDocumentFactory.relationObject("Ad")
        val r2 = MockTypicalDocumentFactory.relationObject("De")
        val r3 = MockTypicalDocumentFactory.relationObject("HJ")

        val relationObjectType = StubObjectType(
            id = objectTypeId,
            name = "Object Type"
        )

        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val objectFields = Block.Fields(
            mapOf(
                r1.key to value1,
                r2.key to value2,
                r3.key to value3,
                Relations.TYPE to objectTypeId,
                Relations.FEATURED_RELATIONS to listOf(Relations.TYPE)
            )
        )

        val objectTypeFields = Block.Fields(
            mapOf(
                Relations.NAME to objectTypeName,
                Relations.DESCRIPTION to objectTypeDescription
            )
        )
        val customDetails = Block.Details(
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
            relations = emptyList()
        )

        storeOfRelations.merge(
            listOf(r1, r2, r3)
        )

        storeOfObjectTypes.merge(
            listOf(relationObjectType)
        )

        val vm = buildViewModel()

        vm.onStart(root)

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
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = block.parseThemeBackgroundColor()
                            )
                        )
                    } else {
                        emptyList()
                    }
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
            content = Block.Content.Smart(SmartBlockType.PAGE),
            children = listOf(header.id, featuredBlock.id, block.id)
        )

        val doc = listOf(page, header, title, block, featuredBlock)

        val objectTypeId = MockDataFactory.randomString()
        val objectTypeName = MockDataFactory.randomString()
        val objectTypeDescription = MockDataFactory.randomString()

        val r1 = MockTypicalDocumentFactory.relationObject("Ad")
        val r2 = MockTypicalDocumentFactory.relationObject("De")
        val r3 = MockTypicalDocumentFactory.relationObject("HJ")
        val relationObjectType = StubRelationObject(
            key = Block.Fields.TYPE_KEY,
            name = "Object Type",
            format = Relation.Format.OBJECT
        )


        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val objectFields = Block.Fields(
            mapOf(
                r1.key to value1,
                r2.key to value2,
                r3.key to value3,
                relationObjectType.key to objectTypeId,
                Relations.FEATURED_RELATIONS to emptyList<String>()
            )
        )

        val objectTypeFields = Block.Fields(
            mapOf(
                Relations.NAME to objectTypeName,
                Relations.DESCRIPTION to objectTypeDescription
            )
        )
        val customDetails = Block.Details(
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
            relations = emptyList()
        )

        storeOfRelations.merge(
            listOf(r1, r2, r3, relationObjectType)
        )

        val vm = buildViewModel()

        vm.onStart(root)

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
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = block.parseThemeBackgroundColor()
                            )
                        )
                    } else {
                        emptyList()
                    }
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
                content = Block.Content.Smart(SmartBlockType.PAGE),
                children = listOf(header.id, featuredBlock.id, block.id)
            )

            val doc = listOf(page, header, title, block, featuredBlock)

            val objectTypeId = MockDataFactory.randomString()
            val objectTypeName = "Movie"
            val objectTypeDescription = MockDataFactory.randomString()

            val r1 = MockTypicalDocumentFactory.relationObject("Ad")
            val r2 = MockTypicalDocumentFactory.relationObject("De")
            val r3 = MockTypicalDocumentFactory.relationObject("HJ")
            val relationObjectType = StubRelationObject(
                key = Block.Fields.TYPE_KEY,
                name = "Object Type",
                format = Relation.Format.OBJECT
            )

            val value1 = MockDataFactory.randomString()
            val value2 = MockDataFactory.randomString()
            val value3 = MockDataFactory.randomString()
            val objectFields = Block.Fields(
                mapOf(
                    r1.key to value1,
                    r2.key to value2,
                    r3.key to value3,
                    relationObjectType.key to objectTypeId,
                    Relations.FEATURED_RELATIONS to listOf(relationObjectType.key, r3.key)
                )
            )

            val objectTypeFields = Block.Fields(
                mapOf(
                    Relations.NAME to objectTypeName,
                    Relations.DESCRIPTION to objectTypeDescription
                )
            )
            val customDetails = Block.Details(
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
                relations = emptyList()
            )

            storeOfRelations.merge(
                listOf(r1, r2, relationObjectType)
            )

            val vm = buildViewModel()

            vm.onStart(root)

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
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = block.parseThemeBackgroundColor()
                            )
                        )
                    } else {
                        emptyList()
                    }
                )
            )

            assertEquals(
                expected = ViewState.Success(expected),
                actual = vm.state.value
            )
        }

    @Test
    fun `should not render relation in featured relations if corresponding relation is hidden`() =
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
                content = Block.Content.Smart(SmartBlockType.PAGE),
                children = listOf(header.id, featuredBlock.id, block.id)
            )

            val doc = listOf(page, header, title, block, featuredBlock)

            val objectTypeId = MockDataFactory.randomString()
            val objectTypeName = MockDataFactory.randomString()
            val objectTypeDescription = MockDataFactory.randomString()

            val relationObjectType = StubObjectType(
                id = objectTypeId,
                name = "Object Type"
            )

            val value1 = MockDataFactory.randomString()
            val value2 = MockDataFactory.randomString()
            val value3 = MockDataFactory.randomString()

            val objectFields = Block.Fields(
                mapOf(
                    r1.key to value1,
                    r2.key to value2,
                    r3.key to value3,
                    Relations.TYPE to objectTypeId,
                    Relations.FEATURED_RELATIONS to listOf(Relations.TYPE, r1.key, r2.key, r3.key)
                )
            )

            val objectTypeFields = Block.Fields(
                mapOf(
                    Relations.NAME to objectTypeName,
                    Relations.DESCRIPTION to objectTypeDescription
                )
            )
            val customDetails = Block.Details(
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
                relations = emptyList()
            )

            storeOfRelations.merge(
                listOf(r1, r2, r3)
            )

            storeOfObjectTypes.merge(
                listOf(relationObjectType)
            )

            val vm = buildViewModel()

            vm.onStart(root)

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
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = block.parseThemeBackgroundColor()
                            )
                        )
                    } else {
                        emptyList()
                    }
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
                content = Block.Content.Smart(SmartBlockType.PAGE),
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
            val objectFields = Block.Fields(
                mapOf(
                    r1.key to value1,
                    r2.key to value2,
                    r3.key to value3,
                    Relations.TYPE to objectTypeId,
                    Relations.FEATURED_RELATIONS to listOf(Relations.TYPE, r3.key)
                )
            )

            val customDetails = Block.Details(mapOf(root to objectFields))

            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubSearchObjects()
            stubOpenDocument(
                document = doc,
                details = customDetails,
                relations = emptyList()
            )

            storeOfRelations.merge(
                listOf(r1, r2, r3)
            )

            storeOfObjectTypes.merge(
                types = listOf(relationObjectType)
            )

            val vm = buildViewModel()

            vm.onStart(root)

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
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = block.parseThemeBackgroundColor()
                            )
                        )
                    } else {
                        emptyList()
                    }
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
            content = Block.Content.Smart(SmartBlockType.PAGE),
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
        val objectFields = Block.Fields(
            mapOf(
                r1.key to value1,
                r2.key to value2,
                r3.key to value3,
                Relations.TYPE to objectTypeId,
                Relations.FEATURED_RELATIONS to listOf(Relations.TYPE, r3.key)
            )
        )

        val objectTypeFields = Block.Fields(
            mapOf(
                Relations.IS_DELETED to true
            )
        )

        val customDetails =
            Block.Details(mapOf(root to objectFields, objectTypeId to objectTypeFields))

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubSearchObjects()
        stubOpenDocument(
            document = doc,
            details = customDetails,
            relations = emptyList()
        )

        storeOfRelations.merge(
            listOf(r1, r2, r3)
        )

        storeOfObjectTypes.merge(
            types = listOf(relationObjectType)
        )

        val vm = buildViewModel()

        vm.onStart(root)

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
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = block.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            )
        )

        assertEquals(
            expected = ViewState.Success(expected),
            actual = vm.state.value
        )
    }
}