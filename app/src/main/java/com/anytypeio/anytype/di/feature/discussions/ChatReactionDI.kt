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
    dependencies = [ChatReactionPickerDependencies::class],
    modules = [
        ChatReactionPickerModule::class,
        ChatReactionPickerModule.Declarations::class
    ]
)
@PerScreen
interface ChatReactionPickerComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun withParams(params: SelectChatReactionViewModel.Params): Builder
        fun withDependencies(dependencies: ChatReactionPickerDependencies): Builder
        fun build(): ChatReactionPickerComponent
    }

    fun getViewModel(): SelectChatReactionViewModel
    fun inject(fragment: ChatReactionFragment)
}

@Module
object ChatReactionPickerModule {
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

interface ChatReactionPickerDependencies : ComponentDependencies {
    fun dispatchers(): AppCoroutineDispatchers
    fun suggester(): EmojiSuggester
    fun repo(): BlockRepository
    fun auth(): AuthRepository
    fun prefs(): UserSettingsRepository
}