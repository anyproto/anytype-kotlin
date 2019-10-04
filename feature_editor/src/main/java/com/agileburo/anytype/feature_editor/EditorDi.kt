package com.agileburo.anytype.feature_editor

import android.content.Context
import com.agileburo.anytype.core_utils.di.scope.PerFeature
import com.agileburo.anytype.core_utils.ext.BaseSchedulerProvider
import com.agileburo.anytype.feature_editor.data.*
import com.agileburo.anytype.feature_editor.data.datasource.BlockDataSource
import com.agileburo.anytype.feature_editor.data.datasource.IPFSDataSourceImpl
import com.agileburo.anytype.feature_editor.data.parser.ContentModelParser
import com.agileburo.anytype.feature_editor.domain.EditorInteractor
import com.agileburo.anytype.feature_editor.domain.EditorInteractorImpl
import com.agileburo.anytype.feature_editor.presentation.converter.BlockContentTypeConverter
import com.agileburo.anytype.feature_editor.presentation.converter.BlockContentTypeConverterImpl
import com.agileburo.anytype.feature_editor.presentation.mvvm.EditorViewModelFactory
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
    fun provideBlockConverter(
        contentConverter: ContentConverter,
        contentModelParser : ContentModelParser
    ): BlockConverter =
        BlockConverterImpl(contentConverter = contentConverter, contentModelParser = contentModelParser)

    @Provides
    @PerFeature
    fun provideRepo(blockConverter: BlockConverter, dataSource: BlockDataSource): EditorRepo =
        EditorRepoImpl(dataSource = dataSource, blockConverter = blockConverter)

    @Provides
    @PerFeature
    fun provideInteractor(repo: EditorRepo): EditorInteractor = EditorInteractorImpl(repo = repo)

    @Provides
    @PerFeature
    fun provideDataSource(context: Context, gson: Gson): BlockDataSource =
        IPFSDataSourceImpl(context = context, gson = gson)

    @Provides
    @PerFeature
    fun provideFactory(
        interactor: EditorInteractor,
        contentTypeConverter: BlockContentTypeConverter,
        baseSchedulerProvider: BaseSchedulerProvider
    ): EditorViewModelFactory =
        EditorViewModelFactory(interactor, contentTypeConverter, baseSchedulerProvider)

    @Provides
    @PerFeature
    fun provideBlockContentConverter(markConverter: MarkConverter): ContentConverter =
        ContentConverterImpl(markConverter = markConverter)

    @Provides
    @PerFeature
    fun provideMarkConverter(): MarkConverter = MarkConverterImpl()

    @Provides
    @PerFeature
    fun provideContentModelParser(gson : Gson) : ContentModelParser {
        return ContentModelParser(gson)
    }

    @Provides
    @PerFeature
    fun provideContentTypeConverter(): BlockContentTypeConverter =
        BlockContentTypeConverterImpl()


}