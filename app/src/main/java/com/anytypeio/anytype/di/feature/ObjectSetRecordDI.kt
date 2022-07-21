package com.anytypeio.anytype.di.feature;

import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewRecord
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetRecordCache
import com.anytypeio.anytype.presentation.sets.ObjectSetRecordViewModel
import com.anytypeio.anytype.ui.sets.modals.SetObjectSetRecordNameFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.StateFlow
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
        updateDataViewRecord: UpdateDataViewRecord,
        objectSetState: StateFlow<ObjectSet>,
        objectSetRecordCache: ObjectSetRecordCache
    ): ObjectSetRecordViewModel.Factory = ObjectSetRecordViewModel.Factory(
        objectSetState = objectSetState,
        objectSetRecordCache = objectSetRecordCache,
        updateDataViewRecord = updateDataViewRecord
    )
}

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class ObjectSetRecordScope