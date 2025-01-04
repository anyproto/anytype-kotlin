package com.anytypeio.anytype.di.feature.discussions

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.emojifier.data.Emoji
import com.anytypeio.anytype.emojifier.data.EmojiProvider
import com.anytypeio.anytype.emojifier.suggest.EmojiSuggester
import com.anytypeio.anytype.feature_discussions.presentation.SelectChatReactionViewModel
import com.anytypeio.anytype.ui.chats.ChatReactionFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [SelectChatReactionDependencies::class],
    modules = [
        SelectChatReactionModule::class,
        SelectChatReactionModule.Declarations::class
    ]
)
@PerScreen
interface SelectChatReactionPickerComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun withParams(params: SelectChatReactionViewModel.Params): Builder
        fun withDependencies(dependencies: SelectChatReactionDependencies): Builder
        fun build(): SelectChatReactionPickerComponent
    }

    fun getViewModel(): SelectChatReactionViewModel
    fun inject(fragment: ChatReactionFragment)
}

@Module
object SelectChatReactionModule {
    @Provides
    @PerScreen
    @JvmStatic
    fun provideEmojiProvider(): EmojiProvider = Emoji

    @Module
    interface Declarations {
        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: SelectChatReactionViewModel.Factory
        ): ViewModelProvider.Factory
    }
}

interface SelectChatReactionDependencies : ComponentDependencies {
    fun dispatchers(): AppCoroutineDispatchers
    fun suggester(): EmojiSuggester
    fun repo(): BlockRepository
    fun auth(): AuthRepository
    fun prefs(): UserSettingsRepository
}