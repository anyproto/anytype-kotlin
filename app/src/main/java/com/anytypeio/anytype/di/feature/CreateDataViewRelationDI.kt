package com.anytypeio.anytype.di.feature;

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.presentation.relations.CreateDataViewRelationViewModelFactory
import com.anytypeio.anytype.ui.relations.CreateDataViewRelationFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [CreateDataViewRelationModule::class])
@PerScreen
interface CreateDataViewRelationSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: CreateDataViewRelationModule): Builder
        fun build(): CreateDataViewRelationSubComponent
    }

    fun inject(fragment: CreateDataViewRelationFragment)

}

@Module
object CreateDataViewRelationModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCreateDVRelationViewModelFactory() = CreateDataViewRelationViewModelFactory()
}