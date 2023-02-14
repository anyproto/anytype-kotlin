package com.anytypeio.anytype.di.feature.types

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.types.CreateType
import com.anytypeio.anytype.emojifier.data.Emoji
import com.anytypeio.anytype.emojifier.data.EmojiProvider
import com.anytypeio.anytype.presentation.types.TypeCreationViewModel
import com.anytypeio.anytype.ui.types.create.TypeCreationFragment
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [TypeCreationDependencies::class],
    modules = [
        TypeCreationModule::class,
        TypeCreationModule.Declarations::class
    ]
)
@PerScreen
interface TypeCreationComponent {

    @Component.Factory
    interface Factory {
        fun create(dependencies: TypeCreationDependencies): TypeCreationComponent
    }

    fun inject(fragment: TypeCreationFragment)
}

@Module
object TypeCreationModule {

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
    ): CreateType = CreateType(blockRepository, dispatchers)

    @Module
    interface Declarations {

        @PerScreen
        @Binds
        fun bindViewModelFactory(factory: TypeCreationViewModel.Factory): ViewModelProvider.Factory

    }

}

interface TypeCreationDependencies : ComponentDependencies {
    fun blockRepository(): BlockRepository
    fun dispatchers(): AppCoroutineDispatchers
    fun urlBuilder(): UrlBuilder
}