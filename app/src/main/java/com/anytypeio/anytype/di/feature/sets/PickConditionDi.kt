package com.anytypeio.anytype.di.feature.sets

import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.presentation.sets.filter.PickFilterConditionViewModel
import com.anytypeio.anytype.ui.sets.modals.PickFilterConditionFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [PickConditionModule::class])
@PerDialog
interface PickFilterConditionSubComponent {

    @Subcomponent.Builder
    interface Builder {

        fun module(module: PickConditionModule): Builder
        fun build(): PickFilterConditionSubComponent
    }

    fun inject(fragment: PickFilterConditionFragment)

}

@Module
object PickConditionModule {

    @JvmStatic
    @Provides
    @PerDialog
    fun provideFactory(): PickFilterConditionViewModel.Factory =
        PickFilterConditionViewModel.Factory()
}