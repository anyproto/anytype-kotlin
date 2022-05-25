package com.anytypeio.anytype.emojifier.suggest.data

import com.anytypeio.anytype.emojifier.suggest.EmojiSuggester
import com.anytypeio.anytype.emojifier.suggest.model.EmojiSuggest

class DefaultEmojiSuggester(
    private val cache: EmojiSuggesterCache,
    private val storage: EmojiSuggestStorage
) : EmojiSuggester {

    override suspend fun fetch(): List<EmojiSuggest> {
        return cache.getIfPresent() ?: storage.fetch().also { cache.put(it) }
    }

    override suspend fun search(query: String): List<EmojiSuggest> {
        return fetch().filter { value ->
            value.name.contains(query, true) || value.category.contains(query, true)
        }
    }
}