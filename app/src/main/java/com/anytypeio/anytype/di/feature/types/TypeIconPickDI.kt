package com.anytypeio.anytype.di.feature.types

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.emojifier.data.Emoji
import com.anytypeio.anytype.emojifier.data.EmojiProvider
import com.anytypeio.anytype.emojifier.suggest.EmojiSuggester
import com.anytypeio.anytype.presentation.types.icon_picker.TypeIconPickerViewModel
import com.anytypeio.anytype.ui.types.picker.TypeIconPickFragment
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [TypeIconPickDependencies::class],
    modules = [
        TypeIconPickModule::class,
        TypeIconPickModule.Declarations::class
    ]
)
@PerScreen
interface TypeIconPickComponent {

    @Component.Factory
    interface Factory {
        fun create(dependencies: TypeIconPickDependencies): TypeIconPickComponent
    }

    fun inject(fragment: TypeIconPickFragment)
}

@Module
object TypeIconPickModule {

    @Provides
    @PerScreen
    @JvmStatic
    fun provideEmojiProvider(): EmojiProvider = Emoji

    @Module
    interface Declarations {

        @PerScreen
        @Binds
        fun bindViewModelFactory(factory: TypeIconPickerViewModel.Factory): ViewModelProvider.Factory

    }

}

interface TypeIconPickDependencies : ComponentDependencies {
    fun emojiSuggester(): EmojiSuggester
    fun blockRepository(): BlockRepository
}