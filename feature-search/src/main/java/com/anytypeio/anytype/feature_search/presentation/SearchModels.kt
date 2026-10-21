package com.anytypeio.anytype.feature_search.presentation

import androidx.compose.runtime.Immutable
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.misc.OpenObjectNavigation
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.core_models.ui.SpaceIconView

/** Where the search runs — derived from the scope token, never stored separately. */
sealed class SearchScope {
    data object Global : SearchScope()
    data class Space(val space: Id, val isCurrent: Boolean) : SearchScope()
}

/** A pill rendered inside the input; [token] is the removable filter it represents. */
@Immutable
data class TokenView(
    val token: SearchToken,
    /** Literal label (names); when null the UI resolves [labelRes] (+ [formatArg]). */
    val label: String? = null,
    val labelRes: Int? = null,
    val formatArg: String? = null,
    val spaceIcon: SpaceIconView? = null,
    val objectIcon: ObjectIcon? = null,
    /** Static glyph for kind buckets (Messages, Types, …). */
    val iconRes: Int? = null
)

/** The empty-query browse orders (iOS §8.5). */
enum class BrowseSort { EDITED, CREATED, NAME }

/**
 * What the surface is for. [ATTACH_TO_CHAT]: the scope token is seeded but
 * hidden and non-removable (a picker must never return cross-space objects),
 * the Messages chip is suppressed, and picking a result returns the object
 * instead of navigating (iOS §1.4).
 */
enum class SearchPurpose { NAVIGATION, ATTACH_TO_CHAT }

/** The three long-tail picker sheets (iOS §6, B2). */
enum class PickerType { CHANNELS, PEOPLE, TYPES }

/**
 * A suggestion chip: tap = add [token], or open [picker] when set. Chips have
 * no selected state and no toggle-off — removal happens only on the token
 * itself.
 */
@Immutable
data class ChipView(
    val id: String,
    val token: SearchToken? = null,
    val picker: PickerType? = null,
    val label: String? = null,
    val labelRes: Int? = null,
    val formatArg: String? = null,
    val objectIcon: ObjectIcon? = null,
    val spaceIcon: SpaceIconView? = null,
    /** Static glyph for kind buckets and picker chips. */
    val iconRes: Int? = null
)

/** One row of a picker sheet. */
@Immutable
data class PickerRowView(
    val id: String,
    val token: SearchToken,
    val label: String,
    /** Caption template + one arg (count or name), resolved by the UI. */
    val captionRes: Int? = null,
    val captionCountArg: Int? = null,
    val captionStringArg: String? = null,
    val objectIcon: ObjectIcon? = null,
    val spaceIcon: SpaceIconView? = null
)

@Immutable
sealed class SearchResultView {

    abstract val key: String

    data class SectionHeader(
        val titleRes: Int? = null,
        val title: String? = null,
        val formatArg: String? = null,
        /** Non-null = this header carries the 3-option browse sort menu. */
        val sortMenu: BrowseSort? = null
    ) : SearchResultView() {
        override val key: String = "section:$titleRes:$title:$formatArg"
    }

    /**
     * A person lead row in global text results — one row per identity,
     * aggregated across spaces (iOS §8.3, B13).
     */
    data class PersonRow(
        val identity: Id,
        val name: String,
        val icon: ObjectIcon,
        /** Membership count excluding the person's own 1:1 space. */
        val sharedChannelCount: Int,
        /** A concrete participant object for opening the profile. */
        val participantId: Id,
        val participantSpace: Id,
        /** Fallback caption for a person with no shared Channels (iOS §8.3). */
        val globalName: String? = null
    ) : SearchResultView() {
        override val key: String = "person:$identity"
    }

    /**
     * Up to 3 Channel-name matches lead the global text results: primary tap
     * opens the Channel, the drill affordance scopes the search to it.
     */
    data class ChannelRow(
        val space: Id,
        val name: String,
        val icon: SpaceIconView
    ) : SearchResultView() {
        override val key: String = "channel:$space"
    }

    data class ObjectRow(
        val id: Id,
        val space: Id,
        val icon: ObjectIcon,
        val name: String,
        val nameHighlights: List<IntRange> = emptyList(),
        val snippet: String? = null,
        val snippetHighlights: List<IntRange> = emptyList(),
        val typeName: String? = null,
        /** Set on cross-space rows — display attribution only. */
        val spaceName: String? = null,
        val layout: ObjectType.Layout?,
        val navigation: OpenObjectNavigation,
        /** Drill target when the row can narrow the search (type rows, linked rows). */
        val drill: SearchToken? = null
    ) : SearchResultView() {
        override val key: String = "obj:$id"
    }

    data class MessageRow(
        val messageId: Id,
        val chatId: Id,
        val space: Id,
        val authorName: String,
        val authorIcon: ObjectIcon?,
        val authorIdentity: Id,
        val createdAt: Long,
        val text: String,
        val highlights: List<IntRange> = emptyList(),
        val containerName: String?,
        /**
         * A discussion message's container resolves to the PARENT object (its
         * id differs from [chatId]); opening lands on the parent editor with
         * the thread at the message (iOS §11.2).
         */
        val discussionParentId: Id? = null,
        val spaceName: String? = null
    ) : SearchResultView() {
        override val key: String = "msg:$messageId"
    }

    /**
     * A suggestion row above a focused listing — the way back out wide
     * ("Search <Type> in all Channels"): applies [token] and clears the query.
     */
    data class SuggestionRow(
        val id: String,
        val labelRes: Int,
        val formatArg: String? = null,
        val token: SearchToken
    ) : SearchResultView() {
        override val key: String = "suggest:$id"
    }

    /**
     * A focused person's membership in ONE space — picking it lands the
     * creator token AND that space's scope in a single mutation.
     */
    data class FocusPersonSpaceRow(
        val identity: Id,
        val space: Id,
        val spaceName: String,
        val icon: ObjectIcon,
        val personName: String,
        val isOneToOne: Boolean
    ) : SearchResultView() {
        override val key: String = "focusPerson:$identity:$space"
    }

    /** Empty object-search state teaching the Messages filter (JS-9865 rev 5). */
    data object MessagesTutorialRow : SearchResultView() {
        override val key: String = "messages-tutorial"
    }

    /** The Channels bucket's one action (desktop #2356 / iOS §8.7). */
    data object CreateChannelRow : SearchResultView() {
        override val key: String = "create-channel"
    }

    /**
     * One row per distinct type across all spaces (grouped by uniqueKey),
     * served from the in-memory cross-space types store.
     */
    data class TypeAggRow(
        val uniqueKey: Key,
        val name: String,
        val icon: ObjectIcon,
        val spaceCount: Int,
        /** Representative instance for opening (current space's if it exists). */
        val typeId: Id,
        val typeSpace: Id,
        /** Name of the representative's space, for the single-space caption. */
        val spaceName: String? = null
    ) : SearchResultView() {
        override val key: String = "type:$uniqueKey"
    }
}

@Immutable
data class SearchUiState(
    val results: List<SearchResultView> = emptyList(),
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    /** Mode the on-screen list was loaded for — rows render by this, not live tokens. */
    val loadedScope: SearchScope = SearchScope.Global,
    val hasMore: Boolean = false,
    /** Bumps on every fresh (non-append) load — the UI resets scroll on it. */
    val loadEpoch: Long = 0L
)

sealed class SearchNavigation {
    /** ATTACH_TO_CHAT purpose only: the picked object, returned to the host. */
    data class ObjectSelected(val id: Id, val space: Id) : SearchNavigation()
    data class OpenObject(val navigation: OpenObjectNavigation) : SearchNavigation()
    data class OpenChatAtMessage(
        val space: Id,
        val chat: Id,
        val message: Id,
        val switchSpace: Boolean
    ) : SearchNavigation()
    /** Open the parent object's editor with the discussion at the message. */
    data class OpenDiscussionAtMessage(
        val space: Id,
        val parentObject: Id,
        val discussion: Id,
        val message: Id
    ) : SearchNavigation()
    data class OpenSpace(val space: Id) : SearchNavigation()
    data object OpenCreateChannel : SearchNavigation()
    data class Toast(val messageRes: Int) : SearchNavigation()
}
