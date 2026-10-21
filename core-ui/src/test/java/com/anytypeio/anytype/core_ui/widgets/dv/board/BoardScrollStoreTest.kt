package com.anytypeio.anytype.core_ui.widgets.dv.board

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.anytypeio.anytype.presentation.sets.model.Viewer
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class BoardScrollStoreTest {
    private val anchor = BoardScrollAnchor("card", "next", "previous", 12, -5)

    @Test
    fun `capture before clear and process recreation retains independent viewer column anchors`() {
        val store = BoardScrollStore()
        store.register("viewer-a", "column") { anchor }
        store.register("viewer-b", "column") { anchor.copy(id = "other", offset = 23) }
        val recreated = BoardScrollStore().apply { restore(store.save()) }
        assertEquals(anchor, recreated.anchor("viewer-a", "column"))
        assertEquals("other", recreated.anchor("viewer-b", "column")?.id)
        assertEquals(23, recreated.anchor("viewer-b", "column")?.offset)
        store.unregister("viewer-a", "column")
        assertEquals(anchor, store.anchor("viewer-a", "column"))
    }

    @Test
    fun `late disposal cannot resurrect removed column anchor`() {
        val store = BoardScrollStore()
        store.register("viewer", "removed") { anchor }
        store.capture()
        store.retainColumns("viewer", setOf("remaining"))
        store.unregister("viewer", "removed")
        assertNull(store.anchor("viewer", "removed"))
    }

    @Test
    fun `hidden group disposal retains anchor until backend removes group`() {
        val store = BoardScrollStore()
        store.retainColumns("viewer", setOf("hidden", "visible"))
        store.register("viewer", "hidden") { anchor }
        // Visibility changes do not change the canonical membership passed to the store.
        store.retainColumns("viewer", setOf("hidden", "visible"))
        store.unregister("viewer", "hidden")
        val recreated = BoardScrollStore().apply { restore(store.save()) }
        assertEquals(anchor, recreated.anchor("viewer", "hidden"))
        recreated.retainColumns("viewer", setOf("visible"))
        assertNull(recreated.anchor("viewer", "hidden"))
    }

    @Test
    fun `same viewer and empty group retain distinct anchors for each grouping relation`() {
        val first = Viewer.Board("viewer", "Board", groupingKey = "status")
        val second = first.copy(groupingKey = "assignee")
        val store = BoardScrollStore()
        store.register(first.scrollKey(), "empty") { anchor }
        store.unregister(first.scrollKey(), "empty")
        assertNull(store.anchor(second.scrollKey(), "empty"))
        store.register(second.scrollKey(), "empty") { anchor.copy(id = "other", offset = 45) }
        val recreated = BoardScrollStore().apply { restore(store.save()) }
        assertEquals(anchor, recreated.anchor(first.scrollKey(), "empty"))
        assertEquals("other", recreated.anchor(second.scrollKey(), "empty")?.id)
    }

    @Test
    fun `saved payload bounds viewer and column counts`() {
        val store = BoardScrollStore()
        repeat(20) { viewer ->
            repeat(70) { column ->
                store.register("viewer-$viewer", "column-$column") { anchor }
                store.unregister("viewer-$viewer", "column-$column")
            }
        }
        val bundle = store.save()
        assertEquals(16, bundle.getStringArrayList("viewers")?.size)
        assertEquals(64, bundle.getBundle("viewer-19")?.getStringArrayList("columns")?.size)
        assertNull(store.anchor("viewer-0", "column-69"))
    }

    @Test
    fun `rapid viewer replacement before composition also bounds metadata`() {
        val store = BoardScrollStore()
        repeat(30) { viewer -> store.retainColumns("viewer-$viewer", setOf("column-$viewer")) }
        assertEquals(16, store.save().getStringArrayList("viewers")?.size)
        // Evicted metadata must not reject a later fresh reader with a different group.
        store.register("viewer-0", "new-column") { anchor }
        store.unregister("viewer-0", "new-column")
        assertEquals(anchor, store.anchor("viewer-0", "new-column"))
    }
}
