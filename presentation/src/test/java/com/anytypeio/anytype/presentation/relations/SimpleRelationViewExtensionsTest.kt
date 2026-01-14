package com.anytypeio.anytype.presentation.relations

import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.presentation.sets.filterVisible
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import org.junit.Assert.assertEquals
import org.junit.Test

class SimpleRelationViewExtensionsTest {

    @Test
    fun filterVisible_shouldReturnAllRelationsWhenNoneAreHidden() {
        val relations = listOf(
            SimpleRelationView(
                key = "key1",
                title = "Title 1",
                format = Relation.Format.LONG_TEXT,
                isHidden = false
            ),
            SimpleRelationView(
                key = "key2",
                title = "Title 2",
                format = Relation.Format.LONG_TEXT,
                isHidden = false
            )
        )
        val result = relations.filterVisible()
        assertEquals(relations, result)
    }

    @Test
    fun filterVisible_shouldExcludeHiddenRelations() {
        val relations = listOf(
            SimpleRelationView(
                key = "key1",
                title = "Title 1",
                format = Relation.Format.LONG_TEXT,
                isHidden = false
            ),
            SimpleRelationView(
                key = "key2",
                title = "Title 2",
                format = Relation.Format.LONG_TEXT,
                isHidden = true
            )
        )
        val result = relations.filterVisible()
        assertEquals(listOf(relations[0]), result)
    }

    @Test
    fun filterVisible_shouldIncludeEssentialKeysEvenIfHidden() {
        val relations = listOf(
            SimpleRelationView(
                key = Relations.NAME,
                title = "Name",
                format = Relation.Format.LONG_TEXT,
                isHidden = true
            ),
            SimpleRelationView(
                key = Relations.DONE,
                title = "Done",
                format = Relation.Format.LONG_TEXT,
                isHidden = true
            ),
            SimpleRelationView(
                key = "key3",
                title = "Title 3",
                format = Relation.Format.LONG_TEXT,
                isHidden = true
            )
        )
        val result = relations.filterVisible()
        assertEquals(listOf(relations[0], relations[1]), result)
    }

    @Test
    fun filterVisible_shouldReturnEmptyListWhenAllRelationsAreHiddenAndNonEssential() {
        val relations = listOf(
            SimpleRelationView(
                key = "key1",
                title = "Title 1",
                format = Relation.Format.LONG_TEXT,
                isHidden = true
            ),
            SimpleRelationView(
                key = "key2",
                title = "Title 2",
                format = Relation.Format.LONG_TEXT,
                isHidden = true
            )
        )
        val result = relations.filterVisible()
        assertEquals(emptyList<SimpleRelationView>(), result)
    }

    @Test
    fun filterVisible_shouldHandleEmptyList() {
        val relations = emptyList<SimpleRelationView>()
        val result = relations.filterVisible()
        assertEquals(emptyList<SimpleRelationView>(), result)
    }
}
