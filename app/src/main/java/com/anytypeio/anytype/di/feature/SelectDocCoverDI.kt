package com.anytypeio.anytype.di.feature;

import android.content.Context
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.device.DefaultGradientCollectionProvider
import com.anytypeio.anytype.device.DeviceCoverCollectionProvider
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.cover.*
import com.anytypeio.anytype.domain.event.model.Payload
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.page.cover.SelectDocCoverViewModel
import com.anytypeio.anytype.presentation.util.Bridge
import com.anytypeio.anytype.ui.page.cover.DocCoverGalleryFragment
import com.google.gson.Gson
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
        setDocCoverGradient: SetDocCoverGradient,
        payloadDispatcher: Bridge<Payload>,
        getCoverCollection: GetCoverImageCollection,
        getCoverGradientCollection: GetCoverGradientCollection,
        urlBuilder: UrlBuilder
    ): SelectDocCoverViewModel.Factory = SelectDocCoverViewModel.Factory(
        setDocCoverColor = setDocCoverColor,
        setDocCoverGradient = setDocCoverGradient,
        payloadDispatcher = payloadDispatcher,
        getCoverCollection = getCoverCollection,
        getCoverGradientCollection = getCoverGradientCollection,
        urlBuilder = urlBuilder
    )

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
    fun provideGetCoverCollection(
        provider: CoverCollectionProvider
    ): GetCoverImageCollection = GetCoverImageCollection(provider)

    @JvmStatic
    @Provides
    @PerModal
    fun provideGetCoverCollectionProvider(
        context: Context
    ): CoverCollectionProvider = DeviceCoverCollectionProvider(
        context = context,
        gson = Gson()
    )

    @JvmStatic
    @Provides
    @PerModal
    fun provideGetCoverGradientCollectionUseCase(
    ): GetCoverGradientCollection = GetCoverGradientCollection(DefaultGradientCollectionProvider())
}