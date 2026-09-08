package com.anytypeio.anytype.feature_search.presentation

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType

/**
 * The token model of the unified search surface — the model the desktop and
 * iOS clients converged on. One token per group; groups AND together. Applied
 * filters exist ONLY as tokens — chips are pure one-tap adders with no
 * selected state. There is no backlink/"related to" group: it was unreachable
 * in global mode and iOS cut it (iOS B5).
 */
sealed class SearchToken {

    /** Stable identity — what pill selection and chip identity key on. */
    abstract val id: String

    /** Scope: where the search runs. Absent scope token = global (vault-wide). */
    data class SpaceScope(val space: Id) : SearchToken() {
        override val id: String get() = "space:$space"
    }

    /** What: a layout bucket. */
    data class Kind(val bucket: KindBucket) : SearchToken() {
        override val id: String get() = "kind:${bucket.name}"
    }

    /**
     * What: a specific object type, keyed by [uniqueKey]. The uniqueKey is
     * stable across spaces, so the token is kept verbatim across every scope
     * boundary — no re-pointing, no bucket mapping (iOS B17).
     */
    data class TypeFilter(
        val uniqueKey: Key,
        /** A concrete type object id, for opening/labeling; not used for filtering. */
        val typeId: Id? = null
    ) : SearchToken() {
        override val id: String get() = "type:$uniqueKey"
    }

    /** Who: created by this identity (label carries the operator: "By …"). */
    data class Creator(val identity: Id) : SearchToken() {
        override val id: String get() = "creator:$identity"
    }

    /**
     * What: messages of ONE chat — a narrowed Messages bucket. Carries its
     * own [space] so the filter keeps working after the scope token is gone
     * (iOS §3.1/§7.5). Removing it widens one step to the Messages bucket.
     */
    data class ChatFilter(val chat: Id, val space: Id) : SearchToken() {
        override val id: String get() = "chat:$chat"
    }

    /**
     * What: a FOCUSED type — the list becomes that uniqueKey's per-space
     * instances, served synchronously from the vault-wide types store
     * (desktop JS-9865 §3; iOS typeFocus). Adding a scope converts it to a
     * plain [TypeFilter].
     */
    data class TypeFocus(val uniqueKey: Key, val typeId: Id? = null) : SearchToken() {
        override val id: String get() = "typeFocus:$uniqueKey"
    }

    /**
     * What: a FOCUSED person — the list becomes their membership in every
     * shared space; a row pick lands creator + that space's scope in one
     * mutation (desktop JS-9865 §4; iOS personFocus). Adding a scope or a
     * creator drops it.
     */
    data class PersonFocus(val identity: Id) : SearchToken() {
        override val id: String get() = "personFocus:$identity"
    }
}

enum class TokenGroup { SCOPE, WHAT, WHO }

val SearchToken.group: TokenGroup
    get() = when (this) {
        is SearchToken.SpaceScope -> TokenGroup.SCOPE
        is SearchToken.Kind, is SearchToken.TypeFilter, is SearchToken.ChatFilter,
        is SearchToken.TypeFocus, is SearchToken.PersonFocus -> TokenGroup.WHAT
        is SearchToken.Creator -> TokenGroup.WHO
    }

/** Adding a token replaces its group's slot in place (same position). */
fun List<SearchToken>.plusToken(token: SearchToken): List<SearchToken> {
    val idx = indexOfFirst { it.group == token.group }
    return if (idx >= 0) {
        toMutableList().apply { set(idx, token) }
    } else {
        this + token
    }
}

enum class KindBucket {
    CHANNEL,
    MESSAGE,
    MEDIA,
    PAGE,
    BOOKMARK,
    COLLECTION,
    QUERY,
    CHAT,
    TYPE
}

/** Buckets that only exist in global mode (per-space types replace them in a space). */
val GLOBAL_ONLY_BUCKETS = setOf(
    KindBucket.CHANNEL,
    KindBucket.PAGE,
    KindBucket.BOOKMARK,
    KindBucket.COLLECTION,
    KindBucket.QUERY,
    KindBucket.CHAT,
    KindBucket.TYPE
)

/** Layout sets per bucket — the filter each bucket applies on object loaders. */
fun KindBucket.layouts(): List<ObjectType.Layout> = when (this) {
    KindBucket.PAGE -> listOf(
        ObjectType.Layout.BASIC,
        ObjectType.Layout.NOTE,
        ObjectType.Layout.TODO
    )
    KindBucket.MEDIA -> listOf(
        ObjectType.Layout.FILE,
        ObjectType.Layout.PDF,
        ObjectType.Layout.AUDIO,
        ObjectType.Layout.VIDEO,
        ObjectType.Layout.IMAGE
    )
    KindBucket.BOOKMARK -> listOf(ObjectType.Layout.BOOKMARK)
    KindBucket.COLLECTION -> listOf(ObjectType.Layout.COLLECTION)
    KindBucket.QUERY -> listOf(ObjectType.Layout.SET)
    KindBucket.CHAT -> listOf(
        ObjectType.Layout.CHAT,
        ObjectType.Layout.CHAT_DERIVED
    )
    KindBucket.TYPE -> listOf(ObjectType.Layout.OBJECT_TYPE)
    // Channel and Message buckets switch the loader; they carry no layout filter.
    KindBucket.CHANNEL, KindBucket.MESSAGE -> emptyList()
}

/**
 * Yield rules when crossing a scope boundary. [newScope] non-null = entering
 * that space; null = going global. Type and creator tokens are kept verbatim
 * in both directions (`type.uniqueKey Equal` matches vault-wide); a chat
 * filter pins its own space so it also carries; only global-only kind buckets
 * drop when entering a space. A chat filter whose chat lives in a DIFFERENT
 * space than a newly-added scope widens to the Messages bucket.
 */
fun List<SearchToken>.mapAcrossBoundary(newScope: Id?): List<SearchToken> =
    mapNotNull { token ->
        when (token) {
            is SearchToken.Kind ->
                if (newScope != null && token.bucket in GLOBAL_ONLY_BUCKETS) null else token
            is SearchToken.ChatFilter ->
                if (newScope != null && token.space != newScope) {
                    SearchToken.Kind(KindBucket.MESSAGE)
                } else {
                    token
                }
            // A scope means "search in this Channel": a type focus narrows to
            // the plain type filter; a person focus is meaningless there.
            is SearchToken.TypeFocus ->
                if (newScope != null) {
                    SearchToken.TypeFilter(uniqueKey = token.uniqueKey, typeId = token.typeId)
                } else {
                    token
                }
            is SearchToken.PersonFocus -> if (newScope != null) null else token
            else -> token
        }
    }
