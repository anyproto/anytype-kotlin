package com.agileburo.anytype.emojifier.suggest.data

import com.agileburo.anytype.emojifier.suggest.model.EmojiSuggest

interface EmojiSuggestStorage {
    suspend fun fetch(): List<EmojiSuggest>
}