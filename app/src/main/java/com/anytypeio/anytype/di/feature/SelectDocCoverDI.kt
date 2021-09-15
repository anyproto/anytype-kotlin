package com.anytypeio.anytype.di.feature;

import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.device.DefaultGradientCollectionProvider
import com.anytypeio.anytype.domain.cover.GetCoverGradientCollection
import com.anytypeio.anytype.presentation.editor.cover.SelectCoverViewModel
import com.anytypeio.anytype.ui.editor.cover.SelectCoverGalleryFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [SelectCoverModule::class])
@PerModal
interface SelectCoverSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: SelectCoverModule): Builder
        fun build(): SelectCoverSubComponent
    }

    fun inject(fragment: SelectCoverGalleryFragment)
}

@Module
object SelectCoverModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideSelectDocCoverViewModelFactory(
        getCoverGradientCollection: GetCoverGradientCollection
    ): SelectCoverViewModel.Factory = SelectCoverViewModel.Factory(getCoverGradientCollection)

    @JvmStatic
    @Provides
    @PerModal
    fun provideGetCoverGradientCollectionUseCase(
    ): GetCoverGradientCollection = GetCoverGradientCollection(DefaultGradientCollectionProvider())
}