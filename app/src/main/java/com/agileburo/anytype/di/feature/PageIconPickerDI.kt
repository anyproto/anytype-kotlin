package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.icon.SetIconName
import com.agileburo.anytype.presentation.page.picker.PageIconPickerViewModelFactory
import com.agileburo.anytype.ui.page.modals.PageIconPickerFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [PageIconPickerModule::class])
@PerScreen
interface PageIconPickerSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun pageIconPickerModule(module: PageIconPickerModule): Builder
        fun build(): PageIconPickerSubComponent
    }

    fun inject(fragment: PageIconPickerFragment)
}

@Module
class PageIconPickerModule {

    @Provides
    @PerScreen
    fun providePageIconPickerViewModelFactory(
        setIconName: SetIconName
    ): PageIconPickerViewModelFactory = PageIconPickerViewModelFactory(
        setIconName = setIconName
    )

    @Provides
    @PerScreen
    fun provideSetIconNameUseCase(
        repo: BlockRepository
    ): SetIconName = SetIconName(
        repo = repo
    )
}