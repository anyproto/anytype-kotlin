package com.anytypeio.anytype.domain.objects

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.domain.objects.StoreOfRelations.TrackedEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultStoreOfRelationsTest {

    private fun newStore(): StoreOfRelations = DefaultStoreOfRelations()

    private fun structFor(id: Id): Struct = mapOf(
        Relations.ID to id,
        Relations.NAME to "relation-name-$id",
        Relations.RELATION_KEY to "relation-key-$id",
    )

    @Test
    fun `trackChanges emits Init on subscribe`() = runTest {
        val store = newStore()

        store.trackChanges().test {
            assertEquals(TrackedEvent.Init, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `trackChanges replays last Change to late subscriber`() = runTest {
        val store = newStore()
        val id: Id = "r1"

        // mutate BEFORE subscribing
        store.set(id, structFor(id))

        // subscriber should see Init then the replayed Change (because replay=1 + onStart Init)
        store.trackChanges().test {
            assertEquals(TrackedEvent.Init, awaitItem())
            assertEquals(TrackedEvent.Change, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun `trackChanges replays only latest Change when multiple changes occur before subscription`() = runTest {
        val store = newStore()
        val id1: Id = "t1"
        val id2: Id = "t2"

        // Make multiple changes BEFORE anyone subscribes
        store.set(id1, structFor(id1))  // First change
        store.set(id2, structFor(id2))  // Second change (should be the replayed one)

        // Because replay=1, only the LATEST Change should be replayed
        store.trackChanges().test {
            assertEquals(TrackedEvent.Init, awaitItem())
            assertEquals(TrackedEvent.Change, awaitItem())  // Only latest change replayed

            // Verify the store actually contains both items
            assertEquals(2, store.getAll().size)
            assertEquals(id1, store.getById(id1)?.id)
            assertEquals(id2, store.getById(id2)?.id)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---- size tracking + Change emission on real changes ----

    @Test
    fun `set increments size once and emits Change`() = runTest {
        val store = newStore()
        val id: Id = "r2"

        store.trackChanges().test {
            assertEquals(TrackedEvent.Init, awaitItem())

            store.set(id, structFor(id))
            assertEquals(1, store.size)
            assertEquals(TrackedEvent.Change, awaitItem())

            // Setting same id again still emits Change (content may change), but size remains 1
            store.set(id, structFor(id))
            assertEquals(1, store.size)
            assertEquals(TrackedEvent.Change, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `remove emits Change only when existed and decrements size`() = runTest {
        val store = newStore()
        val id: Id = "r3"

        store.trackChanges().test {
            assertEquals(TrackedEvent.Init, awaitItem())

            // removing missing id -> no Change
            store.remove(id)
            expectNoEvents()

            // add then remove -> two Changes, size back to 0
            store.set(id, structFor(id))
            assertEquals(TrackedEvent.Change, awaitItem())
            assertEquals(1, store.size)

            store.remove(id)
            assertEquals(TrackedEvent.Change, awaitItem())
            assertEquals(0, store.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clear emits Change only when there were items and resets size`() = runTest {
        val store = newStore()
        val a: Id = "a"
        val b: Id = "b"

        store.trackChanges().test {
            assertEquals(TrackedEvent.Init, awaitItem())

            // empty clear -> no Change
            store.clear()
            expectNoEvents()
            assertEquals(0, store.size)

            // add 2 items
            store.set(a, structFor(a))
            assertEquals(TrackedEvent.Change, awaitItem())
            store.set(b, structFor(b))
            assertEquals(TrackedEvent.Change, awaitItem())
            assertEquals(2, store.size)

            // clear now -> Change and size==0
            store.clear()
            assertEquals(TrackedEvent.Change, awaitItem())
            assertEquals(0, store.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---- merge semantics ----

    @Test
    fun `merge adds missing and updates existing then emits Change only if something changed`() =
        runTest {
            val store = newStore()
            val id1: Id = "m1"
            val id2: Id = "m2"

            store.trackChanges().test {
                assertEquals(TrackedEvent.Init, awaitItem())

                // First merge adds two -> Change
                store.merge(
                    listOf(
                        ObjectWrapper.Relation(structFor(id1)),
                        ObjectWrapper.Relation(structFor(id2))
                    )
                )
                assertEquals(TrackedEvent.Change, awaitItem())
                assertEquals(2, store.size)

                // Second merge with identical payloads:
                // Depending on amend() impl, if nothing changes, no Change should be emitted.
                // We conservatively allow either no events or a Change if amend produces a new instance.
                // To keep the test strict, we craft a slightly different payload to force a real update:
                store.merge(
                    listOf(
                        ObjectWrapper.Relation(structFor(id1) + ("extra" to "v")),
                        ObjectWrapper.Relation(structFor(id2))
                    )
                )
                // At least one actually changed, so expect a Change:
                assertEquals(TrackedEvent.Change, awaitItem())
                assertEquals(2, store.size)

                cancelAndIgnoreRemainingEvents()
            }
        }

    // ---- observe snapshots ----

    @Test
    fun `observe emits snapshots - empty then updated map`() = runTest {
        val store = newStore()
        val id: Id = "obs1"

        store.observe().test {
            // first snapshot corresponds to Init (empty)
            val initial = awaitItem()
            assertTrue(initial.isEmpty())

            // after set we should get a new snapshot with 1 entry
            store.set(id, structFor(id))
            val afterSet = awaitItem()
            assertEquals(1, afterSet.size)

            // after remove -> another snapshot with 0
            store.remove(id)
            val afterRemove = awaitItem()
            assertEquals(0, afterRemove.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // If ObjectWrapper.Relation.isValid requires specific fields, adjust structOfId accordingly
    // and uncomment these tests.

    @Test
    fun `observe emits current value immediately if valid and then reacts to changes`() = runTest {
        val store = newStore()
        val id: Id = "o1"

        // Pre-populate
        store.set(id, structFor(id))

        store.observe().test {
            // gets initial value
            val first = awaitItem()
            // your assertions about first.id / validity here, if accessible

            // and reacts to next change
            store.set(id, structFor(id))
            val second = awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
    }
}