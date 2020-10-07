package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.domain.event.model.Payload
import com.anytypeio.anytype.presentation.util.Bridge
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
object BridgeModule {

    @JvmStatic
    @Provides
    @Singleton
    fun providePayloadPridge(): Bridge<Payload> = Bridge()
}