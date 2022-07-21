package com.anytypeio.anytype.di.feature;

import com.anytypeio.anytype.presentation.sets.ObjectSetCreateBookmarkRecordViewModel
import com.anytypeio.anytype.ui.sets.modals.SetObjectCreateBookmarkRecordFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import javax.inject.Scope

@Subcomponent(modules = [ObjectSetCreateBookmarkRecordModule::class])
@ObjectSetCreateBookmarkRecordScope
interface ObjectSetCreateBookmarkRecordSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: ObjectSetCreateBookmarkRecordModule): Builder
        fun build(): ObjectSetCreateBookmarkRecordSubComponent
    }

    fun inject(fragment: SetObjectCreateBookmarkRecordFragment)
}

@Module
object ObjectSetCreateBookmarkRecordModule {

    @JvmStatic
    @Provides
    @ObjectSetCreateBookmarkRecordScope
    fun provideObjectSetRecordViewModelFactory(
    ): ObjectSetCreateBookmarkRecordViewModel.Factory = ObjectSetCreateBookmarkRecordViewModel.Factory()
}

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class ObjectSetCreateBookmarkRecordScope