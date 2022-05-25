package com.anytypeio.anytype.emojifier.suggest.data

import com.anytypeio.anytype.emojifier.suggest.model.EmojiSuggest

interface EmojiSuggestStorage {
    suspend fun fetch(): List<EmojiSuggest>
}