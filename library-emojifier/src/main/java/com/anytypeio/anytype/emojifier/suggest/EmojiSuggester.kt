package com.anytypeio.anytype.emojifier.suggest

import com.anytypeio.anytype.emojifier.suggest.model.EmojiSuggest

interface EmojiSuggester {
    /**
     * @return a complete list of emoji suggests.
     */
    suspend fun fetch(): List<EmojiSuggest>

    /**
     * @param query user-generated query (for searching emojis)
     * @return a list of emoji suggests corresponding to this [query]
     */
    suspend fun search(query: String): List<EmojiSuggest>
}