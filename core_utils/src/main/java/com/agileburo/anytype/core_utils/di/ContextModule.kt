package com.agileburo.anytype.core_utils.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ContextModule(val application: Application) {

    @Singleton
    @Provides
    fun provideContext(): Context = application
}