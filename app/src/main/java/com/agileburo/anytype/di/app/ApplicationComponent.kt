package com.agileburo.anytype.di.app

import android.content.Context
import com.agileburo.anytype.AndroidApplication
import com.agileburo.anytype.core_utils.ext.BaseSchedulerProvider
import com.agileburo.anytype.feature_editor.EditorComponent
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, RxJavaModule::class])
interface ApplicationComponent {

    fun inject(app: AndroidApplication)
    fun mainScreenComponent(): MainScreenComponent
    fun editorComponent(): EditorComponent

    fun context(): Context
    fun schedulerProvider(): BaseSchedulerProvider

}