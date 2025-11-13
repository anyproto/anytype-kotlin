package com.anytypeio.anytype.di.feature.chats

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.emojifier.data.Emoji
import com.anytypeio.anytype.emojifier.data.EmojiProvider
import com.anytypeio.anytype.emojifier.suggest.EmojiSuggester
import com.anytypeio.anytype.feature_chats.presentation.SelectChatIconViewModel
import com.anytypeio.anytype.ui.chats.SelectChatIconFragment
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [SelectChatIconDependencies::class],
    modules = [
        SelectChatIconModule::class,
        SelectChatIconModule.Declarations::class
    ]
)
@PerScreen
interface SelectChatIconComponent {
    @Component.Builder
    interface Builder {
        fun withDependencies(dependencies: SelectChatIconDependencies): Builder
        fun build(): SelectChatIconComponent
    }

    fun getViewModel(): SelectChatIconViewModel
    fun inject(fragment: SelectChatIconFragment)
}

@Module
object SelectChatIconModule {
    @Provides
    @PerScreen
    @JvmStatic
    fun provideEmojiProvider(): EmojiProvider = Emoji

    @Module
    interface Declarations {
        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: SelectChatIconViewModel.Factory
        ): ViewModelProvider.Factory
    }
}

interface SelectChatIconDependencies : ComponentDependencies {
    fun dispatchers(): AppCoroutineDispatchers
    fun suggester(): EmojiSuggester
}
