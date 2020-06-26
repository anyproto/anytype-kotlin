package com.agileburo.anytype.di.main


import android.content.Context
import com.agileburo.anytype.emojifier.suggest.EmojiSuggester
import com.agileburo.anytype.emojifier.suggest.data.DefaultEmojiSuggestStorage
import com.agileburo.anytype.emojifier.suggest.data.DefaultEmojiSuggester
import com.agileburo.anytype.emojifier.suggest.data.EmojiSuggestStorage
import com.agileburo.anytype.emojifier.suggest.data.EmojiSuggesterCache
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class EmojiModule {

    @Provides
    @Singleton
    fun provideEmojiSuggester(
        cache: EmojiSuggesterCache,
        storage: EmojiSuggestStorage
    ): EmojiSuggester {
        return DefaultEmojiSuggester(
            cache = cache,
            storage = storage
        )
    }

    @Provides
    @Singleton
    fun provideEmojiSuggesterCache(): EmojiSuggesterCache {
        return EmojiSuggesterCache.DefaultCache()
    }

    @Provides
    @Singleton
    fun provideEmojiSuggestStorage(context: Context): EmojiSuggestStorage {
        return DefaultEmojiSuggestStorage(context, Gson())
    }
}