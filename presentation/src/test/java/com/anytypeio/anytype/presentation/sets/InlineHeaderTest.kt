package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType.Layout
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubDataViewView
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock

class InlineHeaderTest {

    @Test
    fun `an inline data view is headed by the object it queries, read-only and without cover`() = runTest {
        val page = MockDataFactory.randomUuid()
        val target = MockDataFactory.randomUuid()
        val inline = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.DataView(
                viewers = listOf(StubDataViewView()),
                targetObjectId = target
            ),
            fields = Block.Fields.empty(),
            children = emptyList()
        )
        val state = ObjectState.DataView(
            root = page,
            blocks = listOf(inline),
            details = ObjectViewDetails(
                details = mapOf(
                    page to mapOf(
                        Relations.ID to page,
                        Relations.NAME to "Project page",
                        Relations.LAYOUT to Layout.BASIC.code.toDouble(),
                        Relations.COVER_ID to "page-cover"
                    ),
                    target to mapOf(
                        Relations.ID to target,
                        Relations.NAME to "Tasks",
                        Relations.LAYOUT to Layout.SET.code.toDouble()
                    )
                )
            )
        )

        val header = state.inlineHeader(
            blockId = inline.id,
            urlBuilder = mock<UrlBuilder>(),
            storeOfObjectTypes = mock<StoreOfObjectTypes>()
        )

        assertIs<SetOrCollectionHeaderState.Default>(header)
        assertEquals("Tasks", header.title.text)
        assertTrue(header.isReadOnlyMode)
        assertNull(header.title.coverImage)
    }
}
