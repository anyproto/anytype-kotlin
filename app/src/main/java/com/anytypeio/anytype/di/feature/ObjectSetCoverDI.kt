package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.cover.RemoveDocCover
import com.anytypeio.anytype.domain.cover.SetDocCoverColor
import com.anytypeio.anytype.domain.cover.SetDocCoverGradient
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.presentation.objects.CoverSliderObjectSetViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.editor.cover.CoverSliderObjectSetFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [ObjectSetCoverSliderModule::class])
@PerModal
interface ObjectSetCoverSliderComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: ObjectSetCoverSliderModule): Builder
        fun build(): ObjectSetCoverSliderComponent
    }

    fun inject(fragment: CoverSliderObjectSetFragment)
}

@Module
object ObjectSetCoverSliderModule {

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
    fun provideSetDocCoverImageUseCase(
        repo: BlockRepository
    ): SetDocCoverImage = SetDocCoverImage(repo)

    @JvmStatic
    @Provides
    @PerModal
    fun provideRemoveDocCoverUseCase(
        repo: BlockRepository
    ): RemoveDocCover = RemoveDocCover(repo)

    @JvmStatic
    @Provides
    @PerModal
    fun provideViewModelFactory(
        setCoverImage: SetDocCoverImage,
        setCoverColor: SetDocCoverColor,
        setCoverGradient: SetDocCoverGradient,
        removeCover: RemoveDocCover,
        dispatcher: Dispatcher<Payload>
    ): CoverSliderObjectSetViewModel.Factory = CoverSliderObjectSetViewModel.Factory(
        setCoverImage = setCoverImage,
        setCoverColor = setCoverColor,
        setCoverGradient = setCoverGradient,
        removeCover = removeCover,
        dispatcher = dispatcher
    )
}