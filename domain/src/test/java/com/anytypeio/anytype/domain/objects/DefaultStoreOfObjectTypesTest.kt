package com.anytypeio.anytype.domain.objects

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes.TrackedEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultStoreOfObjectTypesTest {

    private fun newStore(): StoreOfObjectTypes =
        DefaultStoreOfObjectTypes()

    private fun structOfId(id: Id): Struct = mapOf(
        Relations.ID to id,
        Relations.NAME to "type-name-$id",
        Relations.UNIQUE_KEY to "unique-key-$id",
    )

    // --- TRACK CHANGES BASICS ---

    @Test
    fun `trackChanges emits Init immediately on subscription`() = runTest {
        val store = newStore()

        store.trackChanges().test {
            // onStart emits Init before anything else
            assertEquals(TrackedEvent.Init, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `trackChanges replays last Change to late subscribers (Init then Change)`() = runTest {
        val store = newStore()
        val id: Id = "t1"

        // Emit a change BEFORE anyone subscribes
        store.set(id, structOfId(id))

        // Because updates has replay=1 and trackChanges onStart emits Init,
        // a new subscriber should see: Init, then the replayed Change
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
        store.set(id1, structOfId(id1))  // First change
        store.set(id2, structOfId(id2))  // Second change (should be the replayed one)

        // Because replay=1, only the LATEST Change should be replayed
        store.trackChanges().test {
            assertEquals(TrackedEvent.Init, awaitItem())
            assertEquals(TrackedEvent.Change, awaitItem())  // Only latest change replayed
            
            // Verify the store actually contains both items
            assertEquals(2, store.getAll().size)
            assertEquals(id1, store.get(id1)?.id)
            assertEquals(id2, store.get(id2)?.id)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- MUTATIONS EMIT CHANGE WHEN THEY ACTUALLY CHANGE STATE ---

    @Test
    fun `set then remove both emit Change`() = runTest {
        val store = newStore()
        val id: Id = "t42"

        store.trackChanges().test {
            // Subscribing gets Init
            assertEquals(TrackedEvent.Init, awaitItem())

            // set -> Change
            store.set(id, structOfId(id))
            assertEquals(TrackedEvent.Change, awaitItem())

            // remove -> Change (because the id exists)
            store.remove(id)
            assertEquals(TrackedEvent.Change, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clear emits Change only when there were items`() = runTest {
        val store = newStore()
        val id: Id = "to-clear"

        // Case 1: empty clear -> no Change after Init
        store.trackChanges().test {
            assertEquals(TrackedEvent.Init, awaitItem())
            store.clear()
            expectNoEvents() // nothing to clear
            cancelAndIgnoreRemainingEvents()
        }

        // Case 2: non-empty clear -> Change after Init
        store.set(id, structOfId(id))

        store.trackChanges().test {
            assertEquals(TrackedEvent.Init, awaitItem())
            store.clear()
            assertEquals(TrackedEvent.Change, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `unset emits Change only when target exists`() = runTest {
        val store = newStore()
        val id: Id = "has-keys"
        val k1: Key = "k1"
        val k2: Key = "k2"

        store.trackChanges().test {
            assertEquals(TrackedEvent.Init, awaitItem())

            // unset on missing target -> no Change
            store.unset("missing", listOf(k1, k2))
            expectNoEvents()

            // create target, then unset -> Change
            store.set(id, structOfId(id))
            assertEquals(TrackedEvent.Change, awaitItem())

            store.unset(id, listOf(k1, k2))
            assertEquals(TrackedEvent.Change, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    // If ObjectWrapper.Type(isValid) requires specific fields, adjust structOfId accordingly
    // and uncomment these tests.

     @Test
     fun `observe emits current value immediately if valid and then reacts to changes`() = runTest {
         val store = newStore()
         val id: Id = "o1"

         // Pre-populate
         store.set(id, structOfId(id))

         store.observe(id).test {
             // gets initial value
             val first = awaitItem()
             // your assertions about first.id / validity here, if accessible

             // and reacts to next change
             store.set(id, structOfId(id))
             val second = awaitItem()
             cancelAndIgnoreRemainingEvents()
         }
     }
}