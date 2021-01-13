package com.anytypeio.anytype.di.feature;

import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.event.model.Payload
import com.anytypeio.anytype.domain.page.SetDocCoverColor
import com.anytypeio.anytype.presentation.page.cover.SelectDocCoverViewModel
import com.anytypeio.anytype.presentation.util.Bridge
import com.anytypeio.anytype.ui.page.cover.DocCoverGalleryFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [SelectDocCoverModule::class])
@PerModal
interface SelectDocCoverSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: SelectDocCoverModule): Builder
        fun build(): SelectDocCoverSubComponent
    }

    fun inject(fragment: DocCoverGalleryFragment)
}

@Module
object SelectDocCoverModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideSelectDocCoverViewModelFactory(
        setDocCoverColor: SetDocCoverColor,
        payloadDispatcher: Bridge<Payload>
    ): SelectDocCoverViewModel.Factory = SelectDocCoverViewModel.Factory(
        setDocCoverColor = setDocCoverColor,
        payloadDispatcher = payloadDispatcher
    )

    @JvmStatic
    @Provides
    @PerModal
    fun provideSetDocCoverColorUseCase(
        repo: BlockRepository
    ): SetDocCoverColor = SetDocCoverColor(repo)
}