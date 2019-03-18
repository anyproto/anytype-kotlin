package com.agileburo.anytype.di.app

import android.content.Context
import com.agileburo.anytype.AndroidApplication
import com.agileburo.anytype.feature_editor.EditorComponent
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface ApplicationComponent {

    fun inject(app: AndroidApplication)
    fun mainScreenComponent(): MainScreenComponent
    fun editorComponent(): EditorComponent

    fun context(): Context

}