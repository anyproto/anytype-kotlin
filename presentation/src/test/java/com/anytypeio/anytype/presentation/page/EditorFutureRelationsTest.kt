package com.anytypeio.anytype.presentation.page

import MockDataFactory
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.mapper.toView
import com.anytypeio.anytype.presentation.page.editor.EditorPresentationTestSetup
import com.anytypeio.anytype.presentation.page.editor.ViewState
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import com.anytypeio.anytype.presentation.relations.DocumentRelationView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.jraska.livedata.test
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorFutureRelationsTest : EditorPresentationTestSetup() {

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
        MockitoAnnotations.initMocks(this)
    }

    @After
    fun after() {
        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun `should render object type and text relation as featured relation`() {

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
                relationObjectType.key to objectTypeId,
                Block.Fields.FEATURED_RELATIONS_KEY to listOf(relationObjectType.key, r3.key)
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
                BlockView.FeaturedRelation(
                    id = featuredBlock.id,
                    relations = listOf(
                        DocumentRelationView.ObjectType(
                            relationId = objectTypeId,
                            name = objectTypeName,
                            isFeatured = true
                        ),
                        DocumentRelationView.Default(
                            relationId = r3.key,
                            name = r3.name,
                            value = value3,
                            isFeatured = true
                        )
                    )
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
                )
            )

        vm.state.test().assertValue(ViewState.Success(expected))
    }

    @Test
    fun `should not render featured relations when featured block not present`() {

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
                relationObjectType.key to objectTypeId,
                Block.Fields.FEATURED_RELATIONS_KEY to listOf(relationObjectType.key)
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
                )
            )

        vm.state.test().assertValue(ViewState.Success(expected))
    }

    @Test
    fun `should not render featured relations when list of ids is empty`() {

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
                relationObjectType.key to objectTypeId,
                Block.Fields.FEATURED_RELATIONS_KEY to emptyList<String>()
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
                )
            )

        vm.state.test().assertValue(ViewState.Success(expected))
    }

    @Test
    fun `should not render text featured relation when appropriate relation is not present`() {

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
                relationObjectType.key to objectTypeId,
                Block.Fields.FEATURED_RELATIONS_KEY to listOf(relationObjectType.key, r3.key)
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
            relations = listOf(r1, r2, relationObjectType)
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
                BlockView.FeaturedRelation(
                    id = featuredBlock.id,
                    relations = listOf(
                        DocumentRelationView.ObjectType(
                            relationId = objectTypeId,
                            name = objectTypeName,
                            isFeatured = true
                        )
                    )
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
                )
            )

        vm.state.test().assertValue(ViewState.Success(expected))
    }
}