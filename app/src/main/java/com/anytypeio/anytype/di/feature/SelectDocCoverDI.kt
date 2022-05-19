package com.anytypeio.anytype.di.feature;

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.device.DefaultGradientCollectionProvider
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.cover.GetCoverGradientCollection
import com.anytypeio.anytype.domain.cover.RemoveDocCover
import com.anytypeio.anytype.domain.cover.SetDocCoverColor
import com.anytypeio.anytype.domain.cover.SetDocCoverGradient
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.presentation.editor.cover.SelectCoverObjectSetViewModel
import com.anytypeio.anytype.presentation.editor.cover.SelectCoverObjectViewModel
import com.anytypeio.anytype.presentation.editor.editor.DetailModificationManager
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.editor.cover.SelectCoverObjectFragment
import com.anytypeio.anytype.ui.editor.cover.SelectCoverObjectSetFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [SelectCoverObjectModule::class])
@PerModal
interface SelectCoverObjectSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: SelectCoverObjectModule): Builder
        fun build(): SelectCoverObjectSubComponent
    }

    fun inject(fragment: SelectCoverObjectFragment)
}

@Module
object SelectCoverObjectModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideSetDocCoverColorUseCase(
        repo: BlockRepository
    ): SetDocCoverColor = SetDocCoverColor(repo)

    @JvmStatic
    @Provides
    @PerModal
    fun provideSetDocCoverGradientUseCase(
        repo: BlockRepository
    ): SetDocCoverGradient = SetDocCoverGradient(repo)

    @JvmStatic
    @Provides
    @PerModal
    fun provideRemoveDocCoverUseCase(
        repo: BlockRepository
    ): RemoveDocCover = RemoveDocCover(repo)

    @JvmStatic
    @Provides
    @PerModal
    fun provideSelectDocCoverViewModelFactory(
        setCoverImage: SetDocCoverImage,
        setCoverColor: SetDocCoverColor,
        setCoverGradient: SetDocCoverGradient,
        removeCover: RemoveDocCover,
        dispatcher: Dispatcher<Payload>,
        details: DetailModificationManager,
        getCoverGradientCollection: GetCoverGradientCollection,
        analytics: Analytics
    ): SelectCoverObjectViewModel.Factory = SelectCoverObjectViewModel.Factory(
        setCoverImage = setCoverImage,
        setCoverColor = setCoverColor,
        setCoverGradient = setCoverGradient,
        removeCover = removeCover,
        dispatcher = dispatcher,
        details = details,
        getCoverGradientCollection = getCoverGradientCollection,
        analytics = analytics
    )

    @JvmStatic
    @Provides
    @PerModal
    fun provideGetCoverGradientCollectionUseCase(
    ): GetCoverGradientCollection = GetCoverGradientCollection(DefaultGradientCollectionProvider)
}

@Subcomponent(modules = [SelectCoverObjectSetModule::class])
@PerModal
interface SelectCoverObjectSetSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: SelectCoverObjectSetModule): Builder
        fun build(): SelectCoverObjectSetSubComponent
    }

    fun inject(fragment: SelectCoverObjectSetFragment)
}

@Module
object SelectCoverObjectSetModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideSetDocCoverColorUseCase(
        repo: BlockRepository
    ): SetDocCoverColor = SetDocCoverColor(repo)

    @JvmStatic
    @Provides
    @PerModal
    fun provideSetDocCoverGradientUseCase(
        repo: BlockRepository
    ): SetDocCoverGradient = SetDocCoverGradient(repo)

    @JvmStatic
    @Provides
    @PerModal
    fun provideRemoveDocCoverUseCase(
        repo: BlockRepository
    ): RemoveDocCover = RemoveDocCover(repo)

    @JvmStatic
    @Provides
    @PerModal
    fun provideSelectDocCoverViewModelFactory(
        setCoverImage: SetDocCoverImage,
        setCoverColor: SetDocCoverColor,
        setCoverGradient: SetDocCoverGradient,
        removeCover: RemoveDocCover,
        dispatcher: Dispatcher<Payload>,
        getCoverGradientCollection: GetCoverGradientCollection,
        analytics: Analytics
    ): SelectCoverObjectSetViewModel.Factory = SelectCoverObjectSetViewModel.Factory(
        setCoverImage = setCoverImage,
        setCoverColor = setCoverColor,
        setCoverGradient = setCoverGradient,
        removeCover = removeCover,
        dispatcher = dispatcher,
        getCoverGradientCollection = getCoverGradientCollection,
        analytics = analytics
    )

    @JvmStatic
    @Provides
    @PerModal
    fun provideGetCoverGradientCollectionUseCase(
    ): GetCoverGradientCollection = GetCoverGradientCollection(DefaultGradientCollectionProvider)
}