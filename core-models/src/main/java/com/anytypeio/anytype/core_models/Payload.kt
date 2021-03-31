package com.anytypeio.anytype.core_models

/**
 * Represent events, as response to some command.
 * @param context id of the context
 * @param events new events, contained in response.
 */
data class Payload(
    val context: Id,
    val events: List<Event>
)