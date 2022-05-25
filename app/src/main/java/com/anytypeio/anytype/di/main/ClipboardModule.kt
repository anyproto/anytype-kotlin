package com.anytypeio.anytype.di.main

import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import com.anytypeio.anytype.clipboard.AnytypeClipboard
import com.anytypeio.anytype.clipboard.AnytypeClipboardStorage
import com.anytypeio.anytype.clipboard.AnytypeUriMatcher
import com.anytypeio.anytype.data.auth.mapper.Serializer
import com.anytypeio.anytype.data.auth.other.ClipboardDataUriMatcher
import com.anytypeio.anytype.data.auth.repo.clipboard.ClipboardDataRepository
import com.anytypeio.anytype.data.auth.repo.clipboard.ClipboardDataStore
import com.anytypeio.anytype.domain.clipboard.Clipboard
import com.anytypeio.anytype.middleware.converters.ClipboardSerializer
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object ClipboardModule {

    @JvmStatic
    @Provides
    @Singleton
    fun provideClipboardRepository(
        factory: ClipboardDataStore.Factory
    ) : Clipboard = ClipboardDataRepository(factory)

    @JvmStatic
    @Provides
    @Singleton
    fun provideClipboardDataStoreFactory(
        storage: ClipboardDataStore.Storage,
        system: ClipboardDataStore.System
    ) : ClipboardDataStore.Factory = ClipboardDataStore.Factory(storage, system)

    @JvmStatic
    @Provides
    @Singleton
    fun provideClipboardStorage(
        context: Context,
        serializer: Serializer
    ) : ClipboardDataStore.Storage = AnytypeClipboardStorage(context, serializer)

    @JvmStatic
    @Provides
    @Singleton
    fun provideClipboardSystem(
        cm: ClipboardManager
    ) : ClipboardDataStore.System = AnytypeClipboard(cm)

    @JvmStatic
    @Provides
    @Singleton
    fun provideClipboardManager(
        context: Context
    ) : ClipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

    @JvmStatic
    @Provides
    @Singleton
    fun provideUriMatcher() : Clipboard.UriMatcher = ClipboardDataUriMatcher(
        matcher = AnytypeUriMatcher()
    )

    @JvmStatic
    @Provides
    @Singleton
    fun provideSerializer() : Serializer = ClipboardSerializer()
}