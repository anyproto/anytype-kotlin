package com.anytypeio.anytype.domain.misc

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle

class DefaultRelationsStoreTest {

    @Test
    fun `should add all relations then clear it`() = runTest {

        val store = DefaultStoreOfRelations()

        val count = 30

        val relations = mutableListOf<ObjectWrapper.Relation>()

        repeat(count) { idx ->
            relations.add(
                StubRelationObject(
                    id = "obj${idx.inc()}"
                )
            )
        }

        store.merge(relations = relations)

        // checking that store contains all the objects

        assertEquals(
            actual = store.size,
            expected = relations.size
        )

        // clear all stored relations

        store.clear()

        // checking that store is empty now.

        assertEquals(
            expected = 0,
            actual = store.size
        )
    }

    @Test
    fun `should be able to get all stored relations by keys`() = runTest {

        val store = DefaultStoreOfRelations()

        val count = 30

        val relations = mutableListOf<ObjectWrapper.Relation>()

        repeat(count) { idx ->
            relations.add(
                StubRelationObject(
                    id = "obj${idx.inc()}",
                    key = "key${idx.inc()}"
                )
            )
        }

        store.merge(relations = relations)

        // checking that store contains all the objects

        assertEquals(
            actual = store.size,
            expected = relations.size
        )

        // trying to retrieve all inserted relation by keys

        relations.forEach { r ->
            assertEquals(
                expected = r,
                actual = store.getByKey(r.key)
            )
        }
    }

    @Test
    fun `should be able to get all stored relations by ids`() = runTest {

        val store = DefaultStoreOfRelations()

        val count = 30

        val relations = mutableListOf<ObjectWrapper.Relation>()

        repeat(count) { idx ->
            relations.add(
                StubRelationObject(
                    id = "obj${idx.inc()}",
                    key = "key${idx.inc()}"
                )
            )
        }

        store.merge(relations = relations)

        // checking that store contains all the objects

        assertEquals(
            actual = store.size,
            expected = relations.size
        )

        // trying to retrieve all inserted relation by keys

        relations.forEach { r ->
            assertEquals(
                expected = r,
                actual = store.getById(r.id)
            )
        }
    }

    @Test
    fun `should be able to get all stored relations by get-all method`() = runTest {

        val store = DefaultStoreOfRelations()

        val count = 30

        val relations = mutableListOf<ObjectWrapper.Relation>()

        repeat(count) { idx ->
            relations.add(
                StubRelationObject(
                    id = "obj${idx.inc()}",
                    key = "key${idx.inc()}"
                )
            )
        }

        store.merge(relations = relations)

        // checking that store contains all the objects

        assertEquals(
            actual = store.size,
            expected = relations.size
        )

        // trying to retrieve all inserted relation by keys

        assertEquals(
            expected = relations,
            actual = store.getAll()
        )
    }

    @Test
    fun `should add all relations then remove half of it`() = runTest {

        val store = DefaultStoreOfRelations()

        val count = 30

        val relations = mutableListOf<ObjectWrapper.Relation>()

        repeat(count) { idx ->
            relations.add(
                StubRelationObject(
                    id = "obj${idx.inc()}"
                )
            )
        }

        val toBeRemoved = relations.toList().drop(relations.size / 2).map { it.id }

        store.merge(relations = relations)

        // checking that store contains all the objects

        assertEquals(
            actual = store.size,
            expected = relations.size
        )

        // removing half of relations

        toBeRemoved.forEach { store.remove(it) }

        // checking that half of objects removed

        assertEquals(
            expected = count / 2,
            actual = store.size
        )

        toBeRemoved.forEach { id ->
            assertEquals(
                expected = null,
                actual = store.getById(id)
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should observe changes in store`() = runTest {
        turbineScope{
            val store = DefaultStoreOfRelations()
            val relation = StubRelationObject()
            val relation2 = StubRelationObject()
            store.observe().test {
                val firstItem = awaitItem()
                assertTrue(firstItem.isEmpty(), "Initial store should be empty")

                store.merge(listOf(relation))
                advanceUntilIdle()
                val secondItem = awaitItem()
                assertEquals(1, secondItem.size, "Store should have one relation after merge")

                store.merge(listOf(relation2))
                advanceUntilIdle()
                val thirdItem = awaitItem()
                assertEquals(2, thirdItem.size, "Store should have two relations after second merge")
            }
        }
    }
}