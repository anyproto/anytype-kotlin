package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.widgets.WidgetDispatchEvent
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DispatcherModule {

    @JvmStatic
    @Provides
    @Singleton
    fun providePayloadDispatcher(): Dispatcher<Payload> = Dispatcher.Default()

    @JvmStatic
    @Provides
    @Singleton
    fun provideWidgetEventDispatcher(): Dispatcher<WidgetDispatchEvent> = Dispatcher.Default()
}