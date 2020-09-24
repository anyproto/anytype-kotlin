package com.anytypeio.anytype.emojifier.suggest.data

import com.anytypeio.anytype.emojifier.suggest.model.EmojiSuggest

interface EmojiSuggesterCache {
    suspend fun getIfPresent(): List<EmojiSuggest>?
    suspend fun put(suggests: List<EmojiSuggest>)

    class DefaultCache : EmojiSuggesterCache {

        var list: List<EmojiSuggest>? = null

        override suspend fun getIfPresent(): List<EmojiSuggest>? {
            return list
        }

        override suspend fun put(suggests: List<EmojiSuggest>) {
            list = suggests
        }
    }
}