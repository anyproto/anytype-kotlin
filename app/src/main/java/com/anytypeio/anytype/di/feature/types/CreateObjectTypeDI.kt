package com.anytypeio.anytype.di.feature.types

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.types.CreateObjectType
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.emojifier.data.Emoji
import com.anytypeio.anytype.emojifier.data.EmojiProvider
import com.anytypeio.anytype.presentation.types.CreateObjectTypeViewModel
import com.anytypeio.anytype.ui.types.create.CreateObjectTypeFragment
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [CreateObjectTypeDependencies::class],
    modules = [
        CreateObjectTypeModule::class,
        CreateObjectTypeModule.Declarations::class
    ]
)
@PerScreen
interface CreateObjectTypeComponent {

    @Component.Factory
    interface Factory {
        fun create(dependencies: CreateObjectTypeDependencies): CreateObjectTypeComponent
    }

    fun inject(fragment: CreateObjectTypeFragment)
}

@Module
object CreateObjectTypeModule {

    @Provides
    @PerScreen
    @JvmStatic
    fun provideEmojiProvider(): EmojiProvider = Emoji

    @JvmStatic
    @PerScreen
    @Provides
    fun provideCreateTypeInteractor(
        blockRepository: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): CreateObjectType = CreateObjectType(blockRepository, dispatchers)

    @Module
    interface Declarations {

        @PerScreen
        @Binds
        fun bindViewModelFactory(factory: CreateObjectTypeViewModel.Factory): ViewModelProvider.Factory

    }

}

interface CreateObjectTypeDependencies : ComponentDependencies {
    fun blockRepository(): BlockRepository
    fun dispatchers(): AppCoroutineDispatchers
    fun urlBuilder(): UrlBuilder
    fun analytics(): Analytics
    fun spaceManager(): SpaceManager
}