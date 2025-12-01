package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper

/**
 * Resolves a participant's display name by identity from the participant map.
 * Uses the standard fallback chain: name → globalName → fallback.
 *
 * @param identity The identity ID to look up
 * @param fallback The fallback string if participant not found, has no name, or identity is null/empty
 * @return The resolved name or fallback - always returns a non-null value
 */
fun Map<Id, ObjectWrapper.SpaceMember>.resolveParticipantName(
    identity: Id?,
    fallback: String
): String {
    if (identity.isNullOrEmpty()) return fallback
    val participant = this[identity]
    return participant?.name
        ?: participant?.globalName
        ?: fallback
}
