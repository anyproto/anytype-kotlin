package com.anytypeio.anytype.di.feature

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.media.MediaViewModel
import com.anytypeio.anytype.ui.media.MediaActivity
import dagger.Binds
import dagger.Component
import dagger.Module

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
}

interface MediaDependencies : ComponentDependencies {
    fun urlBuilder(): UrlBuilder
}