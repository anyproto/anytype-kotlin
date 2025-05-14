package com.anytypeio.anytype.di.main

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.anytypeio.anytype.device.network_type.NetworkConnectionStatusImpl
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.device.NetworkConnectionStatus
import com.anytypeio.anytype.domain.network.NetworkModeProvider
import com.anytypeio.anytype.persistence.networkmode.DefaultNetworkModeProvider
import com.anytypeio.anytype.persistence.networkmode.DefaultNetworkModeProvider.NetworkModeConstants.NAMED_NETWORK_MODE_PREFS
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope

@Module
object NetworkModeModule {

    @JvmStatic
    @Provides
    @Singleton
    @Named(NAMED_NETWORK_MODE_PREFS)
    fun provideNetworkModeSharedPreferences(
        context: Context
    ): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    @JvmStatic
    @Provides
    @Singleton
    fun provider(
        @Named(NAMED_NETWORK_MODE_PREFS) sharedPreferences: SharedPreferences
    ): NetworkModeProvider = DefaultNetworkModeProvider(sharedPreferences)

    @JvmStatic
    @Provides
    @Singleton
    fun provideNetworkConnectionStatus(
        context: Context,
        dispatcher: AppCoroutineDispatchers,
        blockRepository: BlockRepository,
        @Named(ConfigModule.DEFAULT_APP_COROUTINE_SCOPE) scope: CoroutineScope
    ): NetworkConnectionStatus = NetworkConnectionStatusImpl(
        context = context,
        dispatchers = dispatcher,
        coroutineScope = scope,
        blockRepository = blockRepository
    )
}