package com.agileburo.anytype.di.app

import android.content.Context
import com.agileburo.anytype.AndroidApplication
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private val app: AndroidApplication) {

    @Singleton
    @Provides
    fun provideContext(): Context = app.applicationContext
}