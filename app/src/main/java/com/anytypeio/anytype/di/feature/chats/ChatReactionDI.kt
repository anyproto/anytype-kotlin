package com.anytypeio.anytype.di.feature.chats

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.feature_chats.presentation.ChatReactionViewModel
import com.anytypeio.anytype.ui.chats.ChatReactionFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module

@Component(
    dependencies = [ChatReactionDependencies::class],
    modules = [
        ChatReactionModule::class,
        ChatReactionModule.Declarations::class
    ]
)
@PerScreen
interface ChatReactionComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun withParams(params: ChatReactionViewModel.Params): Builder
        fun withDependencies(dependencies: ChatReactionDependencies): Builder
        fun build(): ChatReactionComponent
    }

    fun inject(fragment: ChatReactionFragment)
}

@Module
object ChatReactionModule {

    @Module
    interface Declarations {
        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: ChatReactionViewModel.Factory
        ): ViewModelProvider.Factory
    }
}

interface ChatReactionDependencies : ComponentDependencies {
    fun dispatchers(): AppCoroutineDispatchers
    fun repo(): BlockRepository
    fun auth(): AuthRepository
    fun urlBuilder(): UrlBuilder
    fun members(): ActiveSpaceMemberSubscriptionContainer
}