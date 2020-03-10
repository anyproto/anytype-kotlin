package com.agileburo.anytype.di.main

import com.agileburo.anytype.domain.emoji.Emojifier
import com.agileburo.anytype.emojifier.DefaultEmojifier
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class EmojiModule {

    @Provides
    @Singleton
    fun provideEmojifier(): Emojifier = DefaultEmojifier()
}