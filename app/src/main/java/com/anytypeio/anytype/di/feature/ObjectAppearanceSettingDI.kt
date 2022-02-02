package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.presentation.editor.editor.Orchestrator
import com.anytypeio.anytype.presentation.objects.ObjectAppearanceSettingViewModel
import com.anytypeio.anytype.presentation.objects.appearance.ObjectAppearanceCoverViewModel
import com.anytypeio.anytype.presentation.objects.appearance.ObjectAppearanceIconViewModel
import com.anytypeio.anytype.presentation.objects.appearance.ObjectAppearancePreviewLayoutViewModel
import com.anytypeio.anytype.ui.objects.ObjectAppearanceCoverFragment
import com.anytypeio.anytype.ui.objects.ObjectAppearanceIconFragment
import com.anytypeio.anytype.ui.objects.ObjectAppearancePreviewLayoutFragment
import com.anytypeio.anytype.ui.objects.ObjectAppearanceSettingFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

//region SETTINGS
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
//endregion

//region ICON
@Subcomponent(modules = [ObjectAppearanceIconModule::class])
@PerModal
interface ObjectAppearanceIconSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: ObjectAppearanceIconModule): Builder
        fun build(): ObjectAppearanceIconSubComponent
    }

    fun inject(fragment: ObjectAppearanceIconFragment)
}

@Module
object ObjectAppearanceIconModule {
    @JvmStatic
    @Provides
    @PerModal
    fun provideObjectAppearanceIconViewModelFactory(
        orchestrator: Orchestrator
    ): ObjectAppearanceIconViewModel.Factory {
        return ObjectAppearanceIconViewModel.Factory(
            orchestrator = orchestrator
        )
    }
}
//endregion

//region PREVIEW LAYOUT
@Subcomponent(modules = [ObjectAppearancePreviewLayoutModule::class])
@PerModal
interface ObjectAppearancePreviewLayoutSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: ObjectAppearancePreviewLayoutModule): Builder
        fun build(): ObjectAppearancePreviewLayoutSubComponent
    }

    fun inject(fragment: ObjectAppearancePreviewLayoutFragment)
}

@Module
object ObjectAppearancePreviewLayoutModule {
    @JvmStatic
    @Provides
    @PerModal
    fun provideObjectAppearancePreviewLayoutViewModelFactory(
        orchestrator: Orchestrator
    ): ObjectAppearancePreviewLayoutViewModel.Factory {
        return ObjectAppearancePreviewLayoutViewModel.Factory(
            orchestrator = orchestrator
        )
    }
}
//endregion

//region COVER
@Subcomponent(modules = [ObjectAppearanceCoverModule::class])
@PerModal
interface ObjectAppearanceCoverSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: ObjectAppearanceCoverModule): Builder
        fun build(): ObjectAppearanceCoverSubComponent
    }

    fun inject(fragment: ObjectAppearanceCoverFragment)
}

@Module
object ObjectAppearanceCoverModule {
    @JvmStatic
    @Provides
    @PerModal
    fun provideObjectAppearanceCoverViewModelFactory(
        orchestrator: Orchestrator
    ): ObjectAppearanceCoverViewModel.Factory {
        return ObjectAppearanceCoverViewModel.Factory(
            orchestrator = orchestrator
        )
    }
}
//endregion