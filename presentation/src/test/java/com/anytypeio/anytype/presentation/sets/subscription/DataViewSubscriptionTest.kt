package com.anytypeio.anytype.presentation.sets.subscription

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType.Layout
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubDataViewView
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.search.DataViewSubscriptionContainer
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DataViewSubscriptionTest {

    private val container = mock<DataViewSubscriptionContainer>()
    private val storeOfRelations = mock<StoreOfRelations>()
    private val subscription = DefaultDataViewSubscription(container)

    private val page = MockDataFactory.randomUuid()

    @Test
    fun `an inline collection subscribes to the collection it targets`() = runTest {
        val target = MockDataFactory.randomUuid()
        val inline = dataView(target = target)
        val state = ObjectState.DataView(
            root = page,
            blocks = listOf(inline),
            details = ObjectViewDetails(
                details = layoutOf(page, Layout.BASIC) + layoutOf(target, Layout.COLLECTION)
            )
        )

        assertEquals(target, subscribe(state, inline.id).collection)
    }

    @Test
    fun `a collection object subscribes to itself`() = runTest {
        val dataView = dataView()
        val state = ObjectState.DataView(
            root = page,
            blocks = listOf(dataView),
            details = ObjectViewDetails(details = layoutOf(page, Layout.COLLECTION))
        )

        assertEquals(page, subscribe(state, dataView.id).collection)
    }

    private suspend fun subscribe(
        state: ObjectState.DataView,
        blockId: Id
    ): DataViewSubscriptionContainer.Params {
        whenever(container.observe(any())).thenReturn(emptyFlow())
        subscription.startDataViewSubscription(
            context = page,
            space = MockDataFactory.randomUuid(),
            state = state,
            blockId = blockId,
            currentViewerId = null,
            offset = 0,
            storeOfRelations = storeOfRelations
        )
        val params = argumentCaptor<DataViewSubscriptionContainer.Params>()
        verify(container).observe(params.capture())
        return params.firstValue
    }

    private fun dataView(target: Id = "") = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.DataView(
            viewers = listOf(StubDataViewView()),
            targetObjectId = target
        ),
        fields = Block.Fields.empty(),
        children = emptyList()
    )

    private fun layoutOf(id: Id, layout: Layout) = mapOf(
        id to mapOf(Relations.ID to id, Relations.LAYOUT to layout.code.toDouble())
    )
}
