package com.agileburo.anytype.di.app

import android.content.Context
import com.agileburo.anytype.AndroidApplication
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface ApplicationComponent {

    fun inject(app: AndroidApplication)
    fun mainScreenComponent(): MainScreenComponent
    fun context(): Context

}