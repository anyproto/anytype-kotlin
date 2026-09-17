package com.anytypeio.anytype.presentation.sets.state

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType.Layout
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubDataViewView
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Test

class ObjectStateDataViewTest {

    @Test
    fun `a collection object holds its own objects`() {
        val dataView = dataView()
        val state = objectState(layout = Layout.COLLECTION, blocks = listOf(dataView))

        assertTrue(state.isCollection(dataView.id))
    }

    @Test
    fun `a set object queries its sources`() {
        val dataView = dataView()
        val state = objectState(layout = Layout.SET, blocks = listOf(dataView))

        assertFalse(state.isCollection(dataView.id))
    }

    @Test
    fun `a set converted to a collection still reports the set layout`() {
        val dataView = dataView(isCollection = true)
        val state = objectState(layout = Layout.SET, blocks = listOf(dataView))

        assertTrue(state.isCollection(dataView.id))
    }

    @Test
    fun `a collection answers once its block is deleted`() {
        val state = objectState(layout = Layout.COLLECTION)

        assertTrue(state.isCollection(MockDataFactory.randomUuid()))
    }

    @Test
    fun `an inline block pointing at a collection holds its own objects`() {
        val target = MockDataFactory.randomUuid()
        val dataView = dataView(target = target)
        val state = objectState(
            layout = Layout.BASIC,
            blocks = listOf(dataView),
            targets = mapOf(target to Layout.COLLECTION)
        )

        assertTrue(state.isCollection(dataView.id))
    }

    @Test
    fun `an inline block pointing at a set queries its sources`() {
        val target = MockDataFactory.randomUuid()
        val dataView = dataView(target = target)
        val state = objectState(
            layout = Layout.BASIC,
            blocks = listOf(dataView),
            targets = mapOf(target to Layout.SET)
        )

        assertFalse(state.isCollection(dataView.id))
    }

    @Test
    fun `a type object shows the objects of its own type`() {
        assertTrue(objectState(layout = Layout.OBJECT_TYPE).isTypeSet)
    }

    @Test
    fun `a set object is not a type set`() {
        assertFalse(objectState(layout = Layout.SET).isTypeSet)
    }

    @Test
    fun `a page hosting an inline block is not a type set`() {
        assertFalse(objectState(layout = Layout.BASIC).isTypeSet)
    }

    private val root = MockDataFactory.randomUuid()

    private fun dataView(
        isCollection: Boolean = false,
        target: Id = ""
    ) = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.DataView(
            viewers = listOf(StubDataViewView()),
            targetObjectId = target,
            isCollection = isCollection
        ),
        fields = Block.Fields.empty(),
        children = emptyList()
    )

    /**
     * The subclass carries no meaning for the predicates under test and is on its way out, so
     * every case above is stated as a set.
     */
    private fun objectState(
        layout: Layout,
        blocks: List<Block> = emptyList(),
        targets: Map<Id, Layout> = emptyMap()
    ) = ObjectState.DataView(
        root = root,
        blocks = blocks,
        details = ObjectViewDetails(
            details = (mapOf(root to layout) + targets).mapValues { (id, layout) ->
                mapOf(Relations.ID to id, Relations.LAYOUT to layout.code.toDouble())
            }
        )
    )
}
