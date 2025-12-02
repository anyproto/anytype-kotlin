package com.anytypeio.anytype.di.feature

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.download.DownloadFile
import com.anytypeio.anytype.domain.download.Downloader
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.media.MediaViewModel
import com.anytypeio.anytype.ui.media.MediaActivity
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers

@Component(
    dependencies = [MediaDependencies::class],
    modules = [
        MediaModule::class,
        MediaModule.Declarations::class
    ]
)
@PerScreen
interface MediaComponent {

    @Component.Factory
    interface Factory {
        fun create(dependency: MediaDependencies): MediaComponent
    }

    fun inject(activity: MediaActivity)
}

@Module
object MediaModule {

    @Module
    interface Declarations {
        @PerScreen
        @Binds
        fun factory(factory: MediaViewModel.Factory): ViewModelProvider.Factory
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDownloadFileUseCase(
        downloader: Downloader
    ): DownloadFile = DownloadFile(
        downloader = downloader,
        context = Dispatchers.IO
    )
}

interface MediaDependencies : ComponentDependencies {
    fun urlBuilder(): UrlBuilder
    fun repo(): BlockRepository
    fun dispatchers(): AppCoroutineDispatchers
    fun downloader(): Downloader
    fun userSettings(): UserSettingsRepository
}