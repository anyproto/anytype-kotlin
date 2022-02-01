package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.presentation.editor.editor.Orchestrator
import com.anytypeio.anytype.presentation.objects.ObjectAppearanceSettingViewModel
import com.anytypeio.anytype.ui.objects.ObjectAppearanceSettingFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [ObjectAppearanceSettingModule::class])
@PerModal
interface ObjectAppearanceSettingSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: ObjectAppearanceSettingModule): Builder
        fun build(): ObjectAppearanceSettingSubComponent
    }

    fun inject(fragment: ObjectAppearanceSettingFragment)
}

@Module
object ObjectAppearanceSettingModule {
    @JvmStatic
    @Provides
    @PerModal
    fun provideObjectAppearanceSettingViewModelFactory(
        orchestrator: Orchestrator
    ): ObjectAppearanceSettingViewModel.Factory {
        return ObjectAppearanceSettingViewModel.Factory(
            orchestrator = orchestrator
        )
    }
}