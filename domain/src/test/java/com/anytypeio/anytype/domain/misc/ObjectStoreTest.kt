package com.anytypeio.anytype.domain.misc

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.objects.DefaultObjectStore
import org.junit.Test
import kotlin.test.assertEquals

class ObjectStoreTest {

    @Test
    fun `should subscribe earch object to each subscription and clear store after all subscriptions have been unregistered`() {

        val store = DefaultObjectStore()

        val count = 30

        val subscriptions = mutableListOf<Id>()
        repeat(count) { idx ->
            subscriptions.add("sub${idx.inc()}")
        }

        val objects = mutableListOf<ObjectWrapper.Basic>()

        repeat(count) { idx ->
            objects.add(
                ObjectWrapper.Basic(
                    map = mapOf(
                        Relations.ID to "obj${idx.inc()}"
                    )
                )
            )
        }

        store.merge(
            objects = objects,
            subscriptions = subscriptions,
            dependencies = emptyList()
        )

        // checking that store contains all the objecs

        assertEquals(
            actual = store.size,
            expected = objects.size
        )

        // checking that earch objects has all subscriptions

        store.map.forEach { (_, holder) ->
            assertEquals(
                expected = subscriptions,
                actual = holder.subscriptions
            )
        }

        // unregister all existing subscriptions

        store.unsubscribe(subscriptions = subscriptions)

        // checking that store is empty now.

        assertEquals(
            expected = 0,
            actual = store.size
        )
    }

    @Test
    fun `should subscribe each object to each subscription and unregister half of subscriptions`() {

        val store = DefaultObjectStore()

        val count = 30

        val subscriptions = mutableListOf<Id>()
        repeat(count) { idx ->
            subscriptions.add("sub${idx.inc()}")
        }

        val objects = mutableListOf<ObjectWrapper.Basic>()

        repeat(count) { idx ->
            objects.add(
                ObjectWrapper.Basic(
                    map = mapOf(
                        Relations.ID to "obj${idx.inc()}"
                    )
                )
            )
        }

        store.merge(
            objects = objects,
            subscriptions = subscriptions,
            dependencies = emptyList()
        )

        // checking that store contains all the objecs

        assertEquals(
            actual = store.size,
            expected = objects.size
        )

        // checking that earch objects has all subscriptions

        store.map.forEach { (_, holder) ->
            assertEquals(
                expected = subscriptions,
                actual = holder.subscriptions
            )
        }

        // unregister all existing subscriptions

        store.unsubscribe(subscriptions = subscriptions.take(count/2))

        // checking that store is still full.

        assertEquals(
            expected = count,
            actual = store.size
        )

        // checking that each object is subscribed only to the half of subscriptions

        store.map.forEach { (_, holder) ->
            assertEquals(
                expected = count/2,
                actual = holder.subscriptions.size
            )
        }
    }
}