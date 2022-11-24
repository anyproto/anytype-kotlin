package com.anytypeio.anytype.di.feature;

import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.presentation.sets.ObjectSetRecordViewModel
import com.anytypeio.anytype.ui.sets.modals.SetObjectSetRecordNameFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import javax.inject.Named
import javax.inject.Scope

@Subcomponent(modules = [ObjectSetRecordModule::class])
@ObjectSetRecordScope
interface ObjectSetRecordSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: ObjectSetRecordModule): Builder
        fun build(): ObjectSetRecordSubComponent
    }

    fun inject(fragment: SetObjectSetRecordNameFragment)
}

@Module
object ObjectSetRecordModule {
    @JvmStatic
    @Provides
    @ObjectSetRecordScope
    fun provideObjectSetRecordViewModelFactory(
        setObjectDetails: UpdateDetail,
        @Named("object-set-store") objectStore: ObjectStore
    ): ObjectSetRecordViewModel.Factory = ObjectSetRecordViewModel.Factory(
        setObjectDetails = setObjectDetails,
        objectStore = objectStore
    )
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ObjectSetRecordScope