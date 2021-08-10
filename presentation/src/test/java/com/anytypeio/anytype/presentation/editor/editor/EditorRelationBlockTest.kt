package com.anytypeio.anytype.presentation.editor.editor

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.mapper.toView
import com.anytypeio.anytype.presentation.relations.DocumentRelationView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.TXT
import com.jraska.livedata.test
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorRelationBlockTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val title = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Text(
            style = Block.Content.Text.Style.TITLE,
            text = MockDataFactory.randomString(),
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

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
    fun test() {

        // SETUP

        val relation = Relation(
            key = MockDataFactory.randomString(),
            name = "Album's title",
            format = Relation.Format.SHORT_TEXT,
            source = Relation.Source.values().random()
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
            relations = listOf(relation)
        )

        val vm = buildViewModel()

        vm.onStart(root)

        // TESTING

        vm.state.test().apply {
            assertValue(
                ViewState.Success(
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
                            mode = BlockView.Mode.EDIT
                        ),
                        BlockView.Relation.Related(
                            id = b.id,
                            view = DocumentRelationView.Default(
                                relationId = relation.key,
                                name = relation.name,
                                value = value
                            )
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `should render block relation placeholder when relation is not present`() {

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

        val r1 = MockTypicalDocumentFactory.relation("Ad")
        val r2 = MockTypicalDocumentFactory.relation("De")
        val r3 = MockTypicalDocumentFactory.relation("HJ")
        val relationObjectType = Relation(
            key = Block.Fields.TYPE_KEY,
            name = "Object Type",
            format = Relation.Format.OBJECT,
            source = Relation.Source.DERIVED
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
                Block.Fields.NAME_KEY to objectTypeName,
                Block.Fields.DESCRIPTION_KEY to objectTypeDescription
            )
        )
        val customDetails = Block.Details(
            mapOf(
                root to objectFields,
                objectTypeId to objectTypeFields
            )
        )

        stubInterceptEvents()
        stubGetObjectTypes(objectTypes = listOf())
        stubOpenDocument(
            document = doc,
            details = customDetails,
            relations = listOf(r1, r2, r3, relationObjectType)
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
                    backgroundColor = block.content<Block.Content.Text>().backgroundColor,
                    color = block.content<Block.Content.Text>().color,
                    text = block.content<Block.Content.Text>().text,
                    alignment = block.content<Block.Content.Text>().align?.toView(),
                    number = 1
                ),
                BlockView.Relation.Placeholder(
                    id = relationBlock.id,
                    indent = 0,
                    isSelected = false
                )
            )

        vm.state.test().assertValue(ViewState.Success(expected))
    }

    @Test
    fun `should render block relation when relation is present`() {

        val title = MockTypicalDocumentFactory.title
        val header = MockTypicalDocumentFactory.header
        val block = MockTypicalDocumentFactory.a

        val r1 = MockTypicalDocumentFactory.relation("Ad")
        val r2 = MockTypicalDocumentFactory.relation("De")
        val r3 = MockTypicalDocumentFactory.relation("HJ")
        val relationObjectType = Relation(
            key = Block.Fields.TYPE_KEY,
            name = "Object Type",
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
                Block.Fields.NAME_KEY to objectTypeName,
                Block.Fields.DESCRIPTION_KEY to objectTypeDescription
            )
        )
        val customDetails = Block.Details(
            mapOf(
                root to objectFields,
                objectTypeId to objectTypeFields
            )
        )

        stubInterceptEvents()
        stubGetObjectTypes(objectTypes = listOf())
        stubOpenDocument(
            document = doc,
            details = customDetails,
            relations = listOf(r1, r2, r3, relationObjectType)
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
                    backgroundColor = block.content<Block.Content.Text>().backgroundColor,
                    color = block.content<Block.Content.Text>().color,
                    text = block.content<Block.Content.Text>().text,
                    alignment = block.content<Block.Content.Text>().align?.toView(),
                    number = 1
                ),
                BlockView.Relation.Related(
                    id = relationBlock.id,
                    indent = 0,
                    isSelected = false,
                    view = DocumentRelationView.Default(
                        relationId = r2.key,
                        name = r2.name,
                        value = value2,
                        isFeatured = false
                    )
                )
            )

        vm.state.test().assertValue(ViewState.Success(expected))
    }

    @Test
    fun `should render block-relation with hidden relation as placeholder`() {

        val title = MockTypicalDocumentFactory.title
        val header = MockTypicalDocumentFactory.header
        val block = MockTypicalDocumentFactory.a

        val r1 = MockTypicalDocumentFactory.relation("Ad")
        val r2 = MockTypicalDocumentFactory.relation(name = "De", isHidden = true)
        val r3 = MockTypicalDocumentFactory.relation("HJ")

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
                Block.Fields.NAME_KEY to objectTypeName,
                Block.Fields.DESCRIPTION_KEY to objectTypeDescription
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
        stubGetObjectTypes(objectTypes = listOf())
        stubOpenDocument(
            document = doc,
            details = customDetails,
            relations = listOf(r1, r2, r3, relationObjectType)
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
                    backgroundColor = block.content<Block.Content.Text>().backgroundColor,
                    color = block.content<Block.Content.Text>().color,
                    text = block.content<Block.Content.Text>().text,
                    alignment = block.content<Block.Content.Text>().align?.toView(),
                    number = 1
                ),
                BlockView.Relation.Placeholder(
                    id = relationBlock.id,
                    indent = 0,
                    isSelected = false
                )
            )

        vm.state.test().assertValue(ViewState.Success(expected))
    }
}