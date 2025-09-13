package com.anytypeio.anytype.domain.objects

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultObjectStoreTest {

    private fun newStore(): ObjectStore = DefaultObjectStore()

    private fun objectWrapperBasic(
        id: Id,
        extra: Map<String, Any?> = emptyMap()
    ): ObjectWrapper.Basic =
        ObjectWrapper.Basic(mapOf(Relations.ID to id, Relations.NAME to "obj-$id") + extra)

    // ---------- size / set / get ----------

    @Test
    fun `set inserts new then overwrites existing, size increments only once`() = runTest {
        val s = newStore()
        val id: Id = "o1"

        assertEquals(0, s.size)

        s.set(id, objectWrapperBasic(id).map, subscriptions = listOf("sub1"))
        assertEquals(1, s.size)

        // overwrite same id → size should remain 1
        s.set(
            target = id,
            data = objectWrapperBasic(id, mapOf(Relations.NAME to "v2")).map,
            subscriptions = listOf("sub2")
        )
        assertEquals(1, s.size)

        val got = s.get(id)
        assertNotNull(got)
        assertEquals(id, got!!.id)
        assertEquals("v2", got.map[Relations.NAME])
    }

    // ---------- merge (objects + dependencies) ----------

    @Test
    fun `merge adds objects and dependencies, dedups subscriptions, updates size`() = runTest {
        val s = newStore()

        val o1 = objectWrapperBasic("o1")
        val o2 = objectWrapperBasic("o2")
        val d1 = objectWrapperBasic("d1")
        val d2 = objectWrapperBasic("d2")

        // first merge inserts 4 items → size = 4
        s.merge(
            objects = listOf(o1, o2),
            dependencies = listOf(d1, d2),
            subscriptions = listOf("A", "A", "B") // duplicates on purpose
        )
        assertEquals(4, s.size)

        // second merge with same ids but different payload + extra sub C
        s.merge(
            objects = listOf(objectWrapperBasic("o1", mapOf("x" to 4520)), objectWrapperBasic("o2")),
            dependencies = listOf(
                objectWrapperBasic("d1", mapOf("y" to 3635)),
                objectWrapperBasic("d2")
            ),
            subscriptions = listOf("B", "C")
        )
        // size should stay 4 (no new ids)
        assertEquals(4, s.size)

        // API sanity: getAll & getAllAsRelations reflect 4 items
        assertEquals(4, s.getAll().size)
        assertEquals(4, s.getAllAsRelations().size)

        val result1 = s.get("o1")
        assertNotNull(result1)
        assertEquals(4520, result1!!.map["x"])
        assertEquals("obj-o1", result1.map[Relations.NAME])
        val resultD1 = s.get("d1")
        assertNotNull(resultD1)
        assertEquals(3635, resultD1!!.map["y"])
        assertEquals("obj-d1", resultD1.map[Relations.NAME])
        val result2 = s.get("o2")
        assertNotNull(result2)
        val resultD2 = s.get("d2")
        assertNotNull(resultD2)
        assertEquals("obj-d2", resultD2!!.map[Relations.NAME])
    }

    // ---------- amend ----------

    @Test
    fun `amend inserts when absent and updates when present`() = runTest {
        val s = newStore()
        val id: Id = "am1"

        // absent → insert
        s.amend(id, diff = mapOf("id" to id, "k" to "v1"), subscriptions = listOf("S1"))
        assertEquals(1, s.size)
        assertEquals("v1", s.get(id)!!.map["k"])

        // present → update, size not changed
        s.amend(id, diff = mapOf("k" to "v2"), subscriptions = listOf("S2"))
        assertEquals(1, s.size)
        assertEquals("v2", s.get(id)!!.map["k"])
    }

    // ---------- unset ----------

    @Test
    fun `unset updates fields but does not change size`() = runTest {
        val s = newStore()
        val id: Id = "un1"

        s.set(
            id,
            objectWrapperBasic(id, mapOf("a" to 1, "b" to 2)).map,
            subscriptions = listOf("S")
        )
        assertEquals(1, s.size)
        assertEquals(1, s.get(id)!!.map["a"])
        assertEquals(2, s.get(id)!!.map["b"])

        // remove key "a"
        s.unset(id, keys = listOf("a"), subscriptions = emptyList())
        assertEquals(1, s.size)
        assertNull(s.get(id)!!.map["a"])
        assertEquals(2, s.get(id)!!.map["b"])
    }

    // ---------- getAllAsRelations & getRelationById ----------

    @Test
    fun `getAllAsRelations & getRelationById reflect current state`() = runTest {
        val s = newStore()
        val a = objectWrapperBasic("ra")
        val b = objectWrapperBasic("rb", mapOf("flag" to true))

        s.merge(objects = listOf(a, b), dependencies = emptyList(), subscriptions = emptyList())
        assertEquals(2, s.size)

        val rels = s.getAllAsRelations()
        assertEquals(2, rels.size)

        val relA = s.getRelationById("ra")
        assertNotNull(relA)
        assertEquals("ra", relA!!.id)
        assertEquals("obj-ra", relA.map[Relations.NAME])
    }

    // ---------- subscribe & unsubscribe (list) ----------

    @Test
    fun `unsubscribe removes entries that have no remaining subscribers`() = runTest {
        val s = newStore()
        // objects + dependencies
        s.merge(
            objects = listOf(objectWrapperBasic("o1"), objectWrapperBasic("o2")),
            dependencies = listOf(objectWrapperBasic("d1")),
            subscriptions = listOf("main")
        )
        assertEquals(3, s.size)

        // add one more subscription to o2 so it survives after unsub main
        s.subscribe(subscription = "sec", target = "o2")

        // unsubscribe list: main and its dependent "/dep"
        s.unsubscribe(subscriptions = listOf("main"))
        // expected: o1 removed (только main), d1 removed (зависимость "/dep"),
        // o2 остаётся (есть 'sec')
        assertEquals(1, s.size)
        assertNotNull(s.get("o2"))
        assertNull(s.get("o1"))
        assertNull(s.get("d1"))
    }

    // ---------- unsubscribe (single,target) ----------

    @Test
    fun `unsubscribe single-target removes only when it is the first subscriber`() = runTest {
        val s = newStore()
        s.set("tgt", objectWrapperBasic("tgt").map, subscriptions = listOf("S1"))
        assertEquals(1, s.size)

        // add S2 second
        s.subscribe(subscription = "S2", target = "tgt")

        // Removing S2 should NOT remove the object (first subscriber is S1)
        s.unsubscribe(subscription = "S2", target = "tgt")
        assertEquals(1, s.size)
        assertNotNull(s.get("tgt"))

        // Removing S1 (the first) should remove object
        s.unsubscribe(subscription = "S1", target = "tgt")
        assertEquals(0, s.size)
        assertNull(s.get("tgt"))
    }

    // ---------- concurrent-safety smoke (sequential in test env) ----------

    @Test
    fun `getAll returns snapshots independent of internal map`() = runTest {
        val s = newStore()
        s.set("x1", objectWrapperBasic("x1").map, subscriptions = emptyList())
        s.set("x2", objectWrapperBasic("x2").map, subscriptions = emptyList())

        val snapshot = s.getAll()
        s.set("x3", objectWrapperBasic("x3").map, subscriptions = emptyList())

        assertEquals(2, snapshot.size)
        assertEquals(3, s.size)
    }
}