package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ViewerLayoutWidgetStateTest {

    private fun relation(
        key: String,
        format: RelationFormat,
        name: String,
        isHidden: Boolean = false
    ): ObjectWrapper.Relation = ObjectWrapper.Relation(
        map = mapOf(
            // ID is required for the store to index relations and for `isValid`.
            Relations.ID to key,
            Relations.RELATION_KEY to key,
            Relations.RELATION_FORMAT to format.code.toDouble(),
            Relations.NAME to name,
            Relations.IS_HIDDEN to isHidden
        )
    )

    private suspend fun store(vararg relations: ObjectWrapper.Relation): StoreOfRelations =
        DefaultStoreOfRelations().apply { merge(relations.toList()) }

    private fun viewer(
        groupRelationKey: String? = null,
        groupBackgroundColors: Boolean = false
    ): Block.Content.DataView.Viewer = Block.Content.DataView.Viewer(
        id = "view-1",
        name = "View",
        type = Block.Content.DataView.Viewer.Type.BOARD,
        sorts = emptyList(),
        filters = emptyList(),
        viewerRelations = emptyList(),
        groupRelationKey = groupRelationKey,
        groupBackgroundColors = groupBackgroundColors
    )

    @Test
    fun `groupByItems includes only status, tag and checkbox relations`() = runTest {
        val status = relation("k_status", RelationFormat.STATUS, "Status")
        val tag = relation("k_tag", RelationFormat.TAG, "Tag")
        val checkbox = relation("k_done", RelationFormat.CHECKBOX, "Done")
        val text = relation("k_text", RelationFormat.SHORT_TEXT, "Text")
        val storeOfRelations = store(status, tag, checkbox, text)
        val links = listOf(
            RelationLink("k_status", RelationFormat.STATUS),
            RelationLink("k_tag", RelationFormat.TAG),
            RelationLink("k_done", RelationFormat.CHECKBOX),
            RelationLink("k_text", RelationFormat.SHORT_TEXT)
        )

        val result = ViewerLayoutWidgetUi.init().updateState(
            viewer = viewer(groupRelationKey = "k_tag"),
            storeOfRelations = storeOfRelations,
            relationLinks = links
        )

        assertEquals(
            listOf("k_status", "k_tag", "k_done"),
            result.groupByItems.map { it.relationKey.key }
        )
        assertTrue(result.groupByItems.single { it.relationKey.key == "k_tag" }.isChecked)
        assertEquals(false, result.groupByItems.single { it.relationKey.key == "k_status" }.isChecked)
    }

    @Test
    fun `groupByItems excludes hidden relations`() = runTest {
        val hidden = relation("k_hidden", RelationFormat.STATUS, "Hidden", isHidden = true)
        val visible = relation("k_status", RelationFormat.STATUS, "Status")
        val storeOfRelations = store(hidden, visible)
        val links = listOf(
            RelationLink("k_hidden", RelationFormat.STATUS),
            RelationLink("k_status", RelationFormat.STATUS)
        )

        val result = ViewerLayoutWidgetUi.init().updateState(
            viewer = viewer(),
            storeOfRelations = storeOfRelations,
            relationLinks = links
        )

        assertEquals(listOf("k_status"), result.groupByItems.map { it.relationKey.key })
    }

    @Test
    fun `groupByItems includes the bundled done checkbox relation`() = runTest {
        // "done" is a system relation key, but it is a user-facing checkbox that desktop
        // allows grouping by, so it must NOT be filtered out like other system keys.
        val done = relation(Relations.DONE, RelationFormat.CHECKBOX, "Done")
        val storeOfRelations = store(done)
        val links = listOf(RelationLink(Relations.DONE, RelationFormat.CHECKBOX))

        val result = ViewerLayoutWidgetUi.init().updateState(
            viewer = viewer(),
            storeOfRelations = storeOfRelations,
            relationLinks = links
        )

        assertEquals(listOf(Relations.DONE), result.groupByItems.map { it.relationKey.key })
    }

    @Test
    fun `groupByItems excludes non-done system relations`() = runTest {
        // Other system relations of groupable format (e.g. the internal "isReadonly" flag)
        // stay excluded even when not hidden.
        val internal = relation("isReadonly", RelationFormat.CHECKBOX, "Read only")
        val storeOfRelations = store(internal)
        val links = listOf(RelationLink("isReadonly", RelationFormat.CHECKBOX))

        val result = ViewerLayoutWidgetUi.init().updateState(
            viewer = viewer(),
            storeOfRelations = storeOfRelations,
            relationLinks = links
        )

        assertTrue(result.groupByItems.isEmpty())
    }

    @Test
    fun `groupBackgroundColors reflects viewer`() = runTest {
        val storeOfRelations = store()
        val result = ViewerLayoutWidgetUi.init().updateState(
            viewer = viewer(groupBackgroundColors = true),
            storeOfRelations = storeOfRelations,
            relationLinks = emptyList()
        )
        assertTrue(result.groupBackgroundColors.toggled)
    }
}
