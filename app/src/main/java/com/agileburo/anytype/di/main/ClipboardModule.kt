package com.agileburo.anytype.di.main

import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import com.agileburo.anytype.clipboard.AnytypeClipboard
import com.agileburo.anytype.clipboard.AnytypeClipboardStorage
import com.agileburo.anytype.clipboard.AnytypeUriMatcher
import com.agileburo.anytype.data.auth.mapper.Serializer
import com.agileburo.anytype.data.auth.other.ClipboardDataUriMatcher
import com.agileburo.anytype.data.auth.repo.clipboard.ClipboardDataRepository
import com.agileburo.anytype.data.auth.repo.clipboard.ClipboardDataStore
import com.agileburo.anytype.domain.clipboard.Clipboard
import com.agileburo.anytype.middleware.converters.ClipboardSerializer
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ClipboardModule {

    @Provides
    @Singleton
    fun provideClipboardRepository(
        factory: ClipboardDataStore.Factory
    ) : Clipboard = ClipboardDataRepository(factory)

    @Provides
    @Singleton
    fun provideClipboardDataStoreFactory(
        storage: ClipboardDataStore.Storage,
        system: ClipboardDataStore.System
    ) : ClipboardDataStore.Factory = ClipboardDataStore.Factory(storage, system)

    @Provides
    @Singleton
    fun provideClipboardStorage(
        context: Context,
        serializer: Serializer
    ) : ClipboardDataStore.Storage = AnytypeClipboardStorage(context, serializer)

    @Provides
    @Singleton
    fun provideClipboardSystem(
        cm: ClipboardManager
    ) : ClipboardDataStore.System = AnytypeClipboard(cm)

    @Provides
    @Singleton
    fun provideClipboardManager(
        context: Context
    ) : ClipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

    @Provides
    @Singleton
    fun provideUriMatcher() : Clipboard.UriMatcher = ClipboardDataUriMatcher(
        matcher = AnytypeUriMatcher()
    )

    @Provides
    @Singleton
    fun provideSerializer() : Serializer = ClipboardSerializer()
}