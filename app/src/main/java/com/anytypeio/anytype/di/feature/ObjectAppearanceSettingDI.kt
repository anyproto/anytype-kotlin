package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.block.interactor.SetLinkAppearance
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.objects.ObjectAppearanceSettingViewModel
import com.anytypeio.anytype.presentation.objects.appearance.ObjectAppearanceCoverViewModel
import com.anytypeio.anytype.presentation.objects.appearance.ObjectAppearanceIconViewModel
import com.anytypeio.anytype.presentation.objects.appearance.ObjectAppearancePreviewLayoutViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
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
        storage: Editor.Storage,
        setLinkAppearance: SetLinkAppearance,
        dispatcher: Dispatcher<Payload>
    ): ObjectAppearanceSettingViewModel.Factory {
        return ObjectAppearanceSettingViewModel.Factory(
            storage = storage,
            setLinkAppearance = setLinkAppearance,
            dispatcher = dispatcher
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
        storage: Editor.Storage,
        setLinkAppearance: SetLinkAppearance,
        dispatcher: Dispatcher<Payload>
    ): ObjectAppearanceIconViewModel.Factory {
        return ObjectAppearanceIconViewModel.Factory(
            storage = storage,
            setLinkAppearance = setLinkAppearance,
            dispatcher = dispatcher
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
        storage: Editor.Storage,
        setLinkAppearance: SetLinkAppearance,
        dispatcher: Dispatcher<Payload>
    ): ObjectAppearancePreviewLayoutViewModel.Factory {
        return ObjectAppearancePreviewLayoutViewModel.Factory(
            storage = storage,
            setLinkAppearance = setLinkAppearance,
            dispatcher = dispatcher
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
        storage: Editor.Storage,
        setLinkAppearance: SetLinkAppearance,
        dispatcher: Dispatcher<Payload>
    ): ObjectAppearanceCoverViewModel.Factory {
        return ObjectAppearanceCoverViewModel.Factory(
            storage = storage,
            setLinkAppearance = setLinkAppearance,
            dispatcher = dispatcher
        )
    }
}
//endregion