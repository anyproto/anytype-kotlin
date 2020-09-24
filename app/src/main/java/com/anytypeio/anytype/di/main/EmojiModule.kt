package com.anytypeio.anytype.di.main


import android.content.Context
import com.anytypeio.anytype.domain.icon.DocumentEmojiIconProvider
import com.anytypeio.anytype.emojifier.data.DefaultDocumentEmojiIconProvider
import com.anytypeio.anytype.emojifier.suggest.EmojiSuggester
import com.anytypeio.anytype.emojifier.suggest.data.DefaultEmojiSuggestStorage
import com.anytypeio.anytype.emojifier.suggest.data.DefaultEmojiSuggester
import com.anytypeio.anytype.emojifier.suggest.data.EmojiSuggestStorage
import com.anytypeio.anytype.emojifier.suggest.data.EmojiSuggesterCache
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object EmojiModule {

    @JvmStatic
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

    @JvmStatic
    @Provides
    @Singleton
    fun provideEmojiSuggesterCache(): EmojiSuggesterCache {
        return EmojiSuggesterCache.DefaultCache()
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideEmojiSuggestStorage(context: Context): EmojiSuggestStorage {
        return DefaultEmojiSuggestStorage(context, Gson())
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideDocumentEmojiIconProvider(): DocumentEmojiIconProvider {
        return DefaultDocumentEmojiIconProvider()
    }
}