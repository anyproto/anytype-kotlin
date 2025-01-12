package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubObjectView
import com.anytypeio.anytype.core_models.StubSmartBlock
import com.anytypeio.anytype.core_models.permissions.EditBlocksPermission
import com.anytypeio.anytype.core_models.permissions.toObjectPermissions
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class ObjectPermissionsTest {

    @Test
    fun `Unlocked, not archived, participant can edit, BASIC layout`() {
        // GIVEN: An ObjectView with:
        //  - root block is unlocked
        //  - BASIC layout
        //  - no object restrictions
        //  - participant can edit
        val rootBlockId = "root-block"
        val objectView = StubObjectView(
            blocks = listOf(
                StubSmartBlock(
                    id = rootBlockId
                )
            ),
            root = rootBlockId,
            details = mapOf(
                rootBlockId to mapOf(
                    Relations.ID to rootBlockId,
                    Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
                )
            )
        )

        // WHEN
        val permissions = objectView.toObjectPermissions(participantCanEdit = true)

        // THEN
        assertTrue(permissions.canArchive)
        assertFalse(permissions.canDelete)
        assertTrue(permissions.canChangeType)
        assertTrue(permissions.canUndoRedo)
        assertTrue(permissions.canChangeLayout)
        assertTrue(permissions.canEditRelationValues)
        assertTrue(permissions.canEditRelationsList)
        assertTrue(permissions.canEditBlocks)
        assertTrue(permissions.canEditDetails)
        assertTrue(permissions.editBlocks is EditBlocksPermission.Edit)
        assertTrue(permissions.canDuplicate)
        assertTrue(permissions.canLinkItself)
        assertTrue(permissions.canLock)
        assertTrue(permissions.canChangeIcon)
        assertTrue(permissions.canChangeCover)
        assertTrue(permissions.canMakeAsTemplate)
        assertTrue(permissions.canApplyTemplates)
        assertTrue(permissions.canFavorite)
    }

    @Test
    fun `Unlocked, not archived, participant cannot edit`() {
        // GIVEN: An ObjectView with:
        //  - root block is unlocked
        //  - BASIC layout
        //  - no object restrictions
        //  - participant can edit
        val rootBlockId = "root-block"
        val objectView = StubObjectView(
            blocks = listOf(
                StubSmartBlock(
                    id = rootBlockId
                )
            ),
            root = rootBlockId,
            details = mapOf(
                rootBlockId to mapOf(
                    Relations.ID to rootBlockId,
                    Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
                )
            )
        )

        // WHEN
        val permissions = objectView.toObjectPermissions(participantCanEdit = false)

        // THEN
        assertFalse(permissions.canArchive)
        assertFalse(permissions.canDelete)
        assertFalse(permissions.canChangeType)
        assertFalse(permissions.canUndoRedo)
        assertFalse(permissions.canChangeLayout)
        assertFalse(permissions.canEditRelationValues)
        assertFalse(permissions.canEditRelationsList)
        assertFalse(permissions.canEditBlocks)
        assertFalse(permissions.canEditDetails)
        assertTrue(permissions.editBlocks == EditBlocksPermission.ReadOnly)
        assertFalse(permissions.canDuplicate)
        assertFalse(permissions.canLinkItself)
        assertFalse(permissions.canLock)
        assertFalse(permissions.canChangeIcon)
        assertFalse(permissions.canChangeCover)
        assertFalse(permissions.canMakeAsTemplate)
        assertFalse(permissions.canApplyTemplates)
        assertFalse(permissions.canFavorite)
    }

    @Test
    fun `Locked, not archived, participant can edit, BASIC layout`() {
        // GIVEN: An ObjectView with:
        //  - root block is locked
        //  - BASIC layout
        //  - participant can edit
        //  - no additional object restrictions
        val rootBlockId = "root-block"
        val objectView = StubObjectView(
            blocks = listOf(
                StubSmartBlock(
                    id = rootBlockId,
                    fields = Block.Fields(
                        mapOf(
                            Block.Fields.IS_LOCKED_KEY to true
                        )
                    )
                )
            ),
            root = rootBlockId,
            details = mapOf(
                rootBlockId to mapOf(
                    Relations.ID to rootBlockId,
                    Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
                )
            )
        )

        // WHEN
        val permissions = objectView.toObjectPermissions(participantCanEdit = true)

        // THEN
        // Because the block is locked, editing blocks is read-only, but other actions may be allowed.
        assertTrue(permissions.canArchive)
        assertFalse(permissions.canDelete)
        assertFalse(permissions.canEditBlocks)
        assertTrue(permissions.editBlocks == EditBlocksPermission.ReadOnly)
        assertFalse(permissions.canChangeType)
        assertFalse(permissions.canChangeIcon)
        assertFalse(permissions.canChangeCover)
        assertTrue(permissions.canFavorite)
    }

    @Test
    fun `Archived, participant can edit, BASIC layout`() {
        // GIVEN: An ObjectView with:
        //  - root block is unlocked
        //  - BASIC layout
        //  - object is archived
        //  - participant can edit
        val rootBlockId = "root-block"
        val objectView = StubObjectView(
            blocks = listOf(
                StubSmartBlock(id = rootBlockId)
            ),
            root = rootBlockId,
            details = mapOf(
                rootBlockId to mapOf(
                    Relations.ID to rootBlockId,
                    Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
                    Relations.IS_ARCHIVED to true
                )
            )
        )

        // WHEN
        val permissions = objectView.toObjectPermissions(participantCanEdit = true)

        // THEN
        // An archived object can often be deleted, but not re-archived or edited.
        assertFalse(permissions.canArchive, "Cannot archive an already archived object.")
        assertTrue(permissions.canDelete, "Archived object can typically be deleted if user can edit.")
        assertFalse(permissions.canEditBlocks, "Archived objects are read-only for blocks.")
        assertTrue(permissions.editBlocks == EditBlocksPermission.ReadOnly)
        assertFalse(permissions.canChangeLayout, "Changing layout is disallowed if object is archived.")
        assertFalse(permissions.canChangeIcon, "Changing icon is disallowed if object is archived.")
        assertFalse(permissions.canMakeAsTemplate, "Cannot make an archived object into a template.")
    }

    @Test
    fun `Archived, participant cannot edit, BASIC layout`() {
        // GIVEN: An ObjectView with:
        //  - root block is unlocked
        //  - BASIC layout
        //  - object is archived
        //  - participant cannot edit
        val rootBlockId = "root-block"
        val objectView = StubObjectView(
            blocks = listOf(
                StubSmartBlock(id = rootBlockId)
            ),
            root = rootBlockId,
            details = mapOf(
                rootBlockId to mapOf(
                    Relations.ID to rootBlockId,
                    Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
                    Relations.IS_ARCHIVED to true
                )
            )
        )

        // WHEN
        val permissions = objectView.toObjectPermissions(participantCanEdit = false)

        // THEN
        // Participant can't edit => effectively all edit actions are false.
        assertFalse(permissions.canArchive, "Already archived and user can't edit => no re-archiving.")
        assertFalse(permissions.canDelete, "Cannot delete if participant can't edit.")
        assertFalse(permissions.canEditBlocks, "Can't edit blocks if participant can't edit overall.")
        assertTrue(permissions.editBlocks == EditBlocksPermission.ReadOnly)
        assertFalse(permissions.canMakeAsTemplate, "Cannot make a template if you have no edit rights.")
        assertFalse(permissions.canChangeIcon, "No editing privileges => cannot change icon.")
        assertFalse(permissions.canChangeCover, "No editing privileges => cannot change cover.")
        assertFalse(permissions.canDuplicate, "No editing privileges => cannot duplicate.")
    }

    @Test
    fun `Unlocked, not archived, participant can edit, NOTE layout`() {
        // GIVEN: An ObjectView with:
        //  - root block is unlocked
        //  - NOTE layout
        //  - participant can edit
        val rootBlockId = "root-block"
        val objectView = StubObjectView(
            blocks = listOf(
                StubSmartBlock(id = rootBlockId)
            ),
            root = rootBlockId,
            details = mapOf(
                rootBlockId to mapOf(
                    Relations.ID to rootBlockId,
                    Relations.LAYOUT to ObjectType.Layout.NOTE.code.toDouble(),
                )
            )
        )

        // WHEN
        val permissions = objectView.toObjectPermissions(participantCanEdit = true)

        // THEN
        // NOTE layout is part of undoRedoLayouts, so undo/redo is allowed if unlocked & not archived.
        assertTrue(permissions.canArchive, "Unlocked + participant can edit => can archive.")
        assertFalse(permissions.canDelete, "Not archived => can't delete.")
        assertTrue(permissions.canUndoRedo, "NOTE layout supports undo/redo.")
        assertTrue(permissions.canEditBlocks, "Not locked + not archived + can edit => can edit blocks.")
        assertTrue(permissions.canEditDetails, "No restrictions => can edit details.")
        assertTrue(permissions.canFavorite, "Favoriting is typically allowed if participant can edit.")
        // etc.
    }

    @Test
    fun `Unlocked, not archived, participant can edit, BASIC layout, with DELETE & RELATIONS & BLOCKS restrictions`() {
        // GIVEN: An ObjectView with:
        //  - root block is unlocked
        //  - BASIC layout
        //  - objectRestrictions contains DELETE, RELATIONS, and BLOCKS
        //  - participant can edit
        val rootBlockId = "root-block"
        val objectView = StubObjectView(
            blocks = listOf(
                StubSmartBlock(
                    id = rootBlockId
                )
            ),
            root = rootBlockId,
            details = mapOf(
                rootBlockId to mapOf(
                    Relations.ID to rootBlockId,
                    Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
                    Relations.IS_ARCHIVED to false
                )
            ),
            objectRestrictions = listOf(
                ObjectRestriction.DELETE,
                ObjectRestriction.RELATIONS,
                ObjectRestriction.BLOCKS,
                ObjectRestriction.DETAILS
            )
        )

        // WHEN
        val permissions = objectView.toObjectPermissions(participantCanEdit = true)

        // THEN
        // Because of DELETE restriction => cannot archive or delete.
        assertFalse(permissions.canArchive, "DELETE restriction => cannot archive.")
        assertFalse(permissions.canDelete, "DELETE restriction => cannot delete.")
        // Because of BLOCKS restriction => cannot edit blocks.
        assertFalse(permissions.canEditBlocks, "BLOCKS restriction => cannot edit blocks.")
        assertTrue(permissions.editBlocks == EditBlocksPermission.ReadOnly)
        // Because of RELATIONS restriction => cannot edit relations.
        assertFalse(permissions.canEditRelationsList)
        // Because of DETAILS restriction => cannot edit relations values.
        assertFalse(permissions.canEditRelationValues)
        // However, participant can still do other changes if not restricted:
        assertTrue(permissions.canChangeType, "Still allowed to change type unless restricted specifically.")
        assertTrue(permissions.canUndoRedo, "BASIC layout => supports undo/redo if user can edit.")
        assertTrue(permissions.canFavorite, "Favoriting is not restricted by DELETE, BLOCKS, or RELATIONS.")
    }

}