package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.event.model.Payload
import com.anytypeio.anytype.presentation.page.cover.UploadDocCoverImageViewModel
import com.anytypeio.anytype.presentation.util.Bridge
import com.anytypeio.anytype.ui.page.cover.UploadCoverImageFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [UploadDocCoverImageModule::class])
@PerModal
interface UploadDocCoverImageSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: UploadDocCoverImageModule): Builder
        fun build(): UploadDocCoverImageSubComponent
    }

    fun inject(fragment: UploadCoverImageFragment)
}

@Module
object UploadDocCoverImageModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideViewModelFactory(
        setDocCoverImage: SetDocCoverImage,
        payloadDispatcher: Bridge<Payload>,
    ): UploadDocCoverImageViewModel.Factory = UploadDocCoverImageViewModel.Factory(
        setDocCoverImage = setDocCoverImage,
        payloadDispatcher = payloadDispatcher
    )

    @JvmStatic
    @Provides
    @PerModal
    fun provideSetDocCoverImageUseCase(repo: BlockRepository): SetDocCoverImage = SetDocCoverImage(repo)
}