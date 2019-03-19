package com.agileburo.anytype.feature_editor

import android.content.Context
import com.agileburo.anytype.core_utils.di.PerFeature
import com.agileburo.anytype.feature_editor.data.EditorRepo
import com.agileburo.anytype.feature_editor.data.EditorRepoImpl
import com.agileburo.anytype.feature_editor.domain.EditorInteractor
import com.agileburo.anytype.feature_editor.domain.EditorInteractorImpl
import com.agileburo.anytype.feature_editor.presentation.EditorViewModelFactory
import com.agileburo.anytype.feature_editor.ui.EditorFragment
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@PerFeature
@Subcomponent(modules = [EditorModule::class])
interface EditorComponent {

    fun inject(fragment: EditorFragment)
}

@Module
@PerFeature
class EditorModule {

    @Provides
    @PerFeature
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @PerFeature
    fun provideRepo(context: Context, gson: Gson): EditorRepo = EditorRepoImpl(context = context, gson = gson)

    @Provides
    @PerFeature
    fun provideInteractor(repo: EditorRepo): EditorInteractor = EditorInteractorImpl(repo = repo)


    @Provides
    @PerFeature
    fun provideFactory(interactor: EditorInteractor): EditorViewModelFactory =
        EditorViewModelFactory(interactor)
}