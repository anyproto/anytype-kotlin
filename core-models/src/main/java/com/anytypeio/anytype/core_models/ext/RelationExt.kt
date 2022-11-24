package com.anytypeio.anytype.core_models.ext

import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.RelationLink

fun List<RelationLink>.process(
    event: Event.Command.ObjectRelationLinks
): List<RelationLink> = when (event) {
    is Event.Command.ObjectRelationLinks.Amend -> amend(event.relationLinks)
    is Event.Command.ObjectRelationLinks.Remove -> remove(event.keys)
}

fun List<RelationLink>.amend(relationLinks: List<RelationLink>): List<RelationLink> {
    val map = this.associateBy { it.key }.toMutableMap()
    relationLinks.forEach { relation ->
        map[relation.key] = relation
    }
    return map.values.toList()
}

fun List<RelationLink>.remove(keys: List<Key>): List<RelationLink> {
    return filter { !keys.contains(it.key) }
}

fun Any?.addIds(ids: List<Id>): List<Id> {
    val new = mutableListOf<Id>()
    when (this) {
        is List<*> -> new.addAll(typeOf())
        is Id -> new.add(this)
    }
    new.addAll(ids)
    return new
}

inline fun <reified T> List<*>.typeOf(): List<T> {
    val retlist = mutableListOf<T>()
    this.forEach {
        if (it is T) {
            retlist.add(it)
        }
    }
    return retlist
}