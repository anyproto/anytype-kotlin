package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.presentation.sets.CreateObjectTypeViewModel
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.ui.sets.CreateObjectTypeFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [CreateObjectTypeModule::class])
@PerScreen
interface CreateObjectTypeSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: CreateObjectTypeModule): Builder
        fun build(): CreateObjectTypeSubComponent
    }

    fun inject(fragment: CreateObjectTypeFragment)
}

@Module
object CreateObjectTypeModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCreateObjectTypeViewModelFactory(
    ): CreateObjectTypeViewModel.Factory {
        return CreateObjectTypeViewModel.Factory()
    }
}