package com.anytypeio.anytype.di.main

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.anytypeio.anytype.persistence.networkmode.DefaultNetworkModeProvider
import com.anytypeio.anytype.persistence.networkmode.DefaultNetworkModeProvider.NetworkModeConstants.NAMED_NETWORK_MODE_PREFS
import com.anytypeio.anytype.persistence.networkmode.NetworkModeProvider
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

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
}