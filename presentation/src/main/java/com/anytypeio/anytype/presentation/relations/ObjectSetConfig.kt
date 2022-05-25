package com.anytypeio.anytype.presentation.relations

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.typeOf

object ObjectSetConfig {
    /**
     * Object name key
     */
    const val NAME_KEY = "name"

    /**
     * Object id key
     */
    const val ID_KEY = "id"

    /**
     * Emoji unicode key
     */
    const val EMOJI_KEY = "iconEmoji"

    /**
     * Object type key.
     */
    const val TYPE_KEY = "type"

    const val DEFAULT_LIMIT = 50
}

val Map<String, Any?>.type: String?
    get() = when (val value = get(ObjectSetConfig.TYPE_KEY)) {
        is String -> value
        is List<*> -> value.typeOf<Id>().first()
        else -> null
    }