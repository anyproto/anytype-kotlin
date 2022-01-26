package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.middleware.interactor.LocalNetworkAddressHandler
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class LocalNetworkAddressModule {

    @Singleton
    @Provides
    fun provideHandler(): LocalNetworkAddressHandler = LocalNetworkAddressHandler()
}