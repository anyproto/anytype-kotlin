package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.render.parseThemeBackgroundColor
import com.anytypeio.anytype.presentation.mapper.toView
import com.anytypeio.anytype.presentation.relations.DocumentRelationView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.TXT
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class EditorRelationBlockTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val title = StubTitle()

    private val header = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun test() = runTest {

        // SETUP

        val relation = StubRelationObject(
            key = MockDataFactory.randomString(),
            name = "Album's title",
            format = Relation.Format.SHORT_TEXT
        )

        val value = "Safe as milk"

        val customDetails = Block.Details(mapOf(root to Block.Fields(mapOf(relation.key to value))))

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Foo",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val b = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.RelationBlock(key = relation.key)
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, a.id, b.id)
        )

        val document = listOf(page, header, title, a, b)

        stubInterceptEvents()
        stubOpenDocument(
            document = document,
            details = customDetails,
            relations = emptyList()
        )

        val vm = buildViewModel()

        storeOfRelations.merge(
            relations = listOf(relation)
        )

        vm.onStart(root)

        // TESTING

        val expected = ViewState.Success(
            blocks = listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    isFocused = false,
                    text = title.content<TXT>().text,
                    mode = BlockView.Mode.EDIT
                ),
                BlockView.Text.Paragraph(
                    id = a.id,
                    text = a.content<Block.Content.Text>().text,
                    mode = BlockView.Mode.EDIT,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = a.parseThemeBackgroundColor()
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Relation.Related(
                    id = b.id,
                    view = DocumentRelationView.Default(
                        relationId = relation.id,
                        relationKey = relation.key,
                        name = relation.name.orEmpty(),
                        value = value,
                        format = relation.format
                    ),
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = b.parseThemeBackgroundColor()
                            )
                        )
                    } else {
                        emptyList()
                    }
                )
            )
        )

        assertEquals(
            expected = expected,
            actual = vm.state.value
        )
    }

    @Test
    fun `should render block relation placeholder when relation is not present`() = runTest {

        val title = MockTypicalDocumentFactory.title
        val header = MockTypicalDocumentFactory.header
        val block = MockTypicalDocumentFactory.a
        val relationBlock = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.RelationBlock(key = MockDataFactory.randomString())
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(SmartBlockType.PAGE),
            children = listOf(header.id, block.id, relationBlock.id)
        )

        val doc = listOf(page, header, title, block, relationBlock)

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
                relationObjectType.key to objectTypeId
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
                ),
                BlockView.Relation.Placeholder(
                    id = relationBlock.id,
                    indent = 0,
                    isSelected = false,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = relationBlock.parseThemeBackgroundColor()
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
    fun `should render block relation when relation is present`() = runTest {

        val title = MockTypicalDocumentFactory.title
        val header = MockTypicalDocumentFactory.header
        val block = MockTypicalDocumentFactory.a

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


        val relationBlock = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.RelationBlock(key = r2.key)
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(SmartBlockType.PAGE),
            children = listOf(header.id, block.id, relationBlock.id)
        )

        val doc = listOf(page, header, title, block, relationBlock)

        val objectTypeId = MockDataFactory.randomString()
        val objectTypeName = MockDataFactory.randomString()
        val objectTypeDescription = MockDataFactory.randomString()

        val objectFields = Block.Fields(
            mapOf(
                r1.key to value1,
                r2.key to value2,
                r3.key to value3,
                relationObjectType.key to objectTypeId
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
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = block.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Relation.Related(
                id = relationBlock.id,
                indent = 0,
                isSelected = false,
                view = DocumentRelationView.Default(
                    relationId = r2.id,
                    relationKey = r2.key,
                    name = r2.name.orEmpty(),
                    value = value2,
                    isFeatured = false,
                    format = r2.format
                ),
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = relationBlock.parseThemeBackgroundColor()
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
    fun `should render block-relation with hidden relation as placeholder`() = runTest {

        val title = MockTypicalDocumentFactory.title
        val header = MockTypicalDocumentFactory.header
        val block = MockTypicalDocumentFactory.a

        val r1 = StubRelationObject(name = "Ad")
        val r2 = StubRelationObject(name = "De", isHidden = true)
        val r3 = StubRelationObject(name = "HJ")
        val objectRelations = listOf(r1, r2, r3)

        val relationObjectType = Relation(
            key = Block.Fields.TYPE_KEY,
            name = "Radio Station",
            format = Relation.Format.OBJECT,
            source = Relation.Source.DERIVED
        )

        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()

        val relationBlock = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.RelationBlock(key = r2.key)
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(SmartBlockType.PAGE),
            children = listOf(header.id, block.id, relationBlock.id)
        )

        val doc = listOf(page, header, title, block, relationBlock)

        val objectTypeId = MockDataFactory.randomString()
        val objectTypeName = MockDataFactory.randomString()
        val objectTypeDescription = MockDataFactory.randomString()

        val objectFields = Block.Fields(
            mapOf(
                r1.key to value1,
                r2.key to value2,
                r3.key to value3,
                relationObjectType.key to objectTypeId
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
            relationLinks = objectRelations.map {
                RelationLink(key = it.key, format = it.relationFormat)
            }
        )

        val vm = buildViewModel()
        storeOfRelations.merge(objectRelations)

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
                ),
                BlockView.Relation.Placeholder(
                    id = relationBlock.id,
                    indent = 0,
                    isSelected = false,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = relationBlock.parseThemeBackgroundColor()
                            )
                        )
                    } else {
                        emptyList()
                    }
                )
            )

        vm.state.test().assertValue(ViewState.Success(expected))
    }
}