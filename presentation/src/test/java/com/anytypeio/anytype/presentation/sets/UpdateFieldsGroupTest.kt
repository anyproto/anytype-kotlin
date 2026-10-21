package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.Event.Command.DataView.UpdateView.DVViewerFields
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Regression for the live "Color columns" / "Group by" bug: a viewer-update event
 * (DVViewerFields) must carry the Kanban group fields, so the board re-colors / re-groups
 * without reopening the set. Mirrors how hideIcon / coverFit already round-trip.
 */
class UpdateFieldsGroupTest {

    private val boardViewer = DVViewer(
        id = "v1",
        name = "Board",
        type = Block.Content.DataView.Viewer.Type.BOARD,
        sorts = emptyList(),
        filters = emptyList(),
        viewerRelations = emptyList(),
        groupRelationKey = null,
        groupBackgroundColors = false
    )

    private fun fields(
        groupRelationKey: String?,
        groupBackgroundColors: Boolean
    ) = DVViewerFields(
        name = "Board",
        type = Block.Content.DataView.Viewer.Type.BOARD,
        coverRelationKey = "",
        hideIcon = false,
        cardSize = Block.Content.DataView.Viewer.Size.SMALL,
        coverFit = false,
        defaultTemplateId = null,
        defaultObjectTypeId = null,
        groupRelationKey = groupRelationKey,
        groupBackgroundColors = groupBackgroundColors
    )

    @Test
    fun `updateFields applies groupBackgroundColors and groupRelationKey from the event`() {
        val updated = boardViewer.updateFields(
            fields(groupRelationKey = "status", groupBackgroundColors = true)
        )
        assertEquals("status", updated.groupRelationKey)
        assertEquals(true, updated.groupBackgroundColors)
    }

    @Test
    fun `updateFields clears groupBackgroundColors when the event turns it off`() {
        val on = boardViewer.copy(groupRelationKey = "status", groupBackgroundColors = true)
        val updated = on.updateFields(
            fields(groupRelationKey = "status", groupBackgroundColors = false)
        )
        assertEquals(false, updated.groupBackgroundColors)
    }
}
