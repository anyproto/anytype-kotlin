package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.icon.SetDocumentEmojiIcon
import com.anytypeio.anytype.emojifier.data.Emoji
import com.anytypeio.anytype.emojifier.suggest.EmojiSuggester
import com.anytypeio.anytype.presentation.page.picker.DocumentEmojiIconPickerViewModelFactory
import com.anytypeio.anytype.ui.page.modals.DocumentEmojiIconPickerFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [DocumentEmojiIconPickerModule::class])
@PerScreen
interface DocumentEmojiIconPickerSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun documentIconActionMenuModule(module: DocumentEmojiIconPickerModule): Builder
        fun build(): DocumentEmojiIconPickerSubComponent
    }

    fun inject(fragment: DocumentEmojiIconPickerFragment)
}

@Module
class DocumentEmojiIconPickerModule {

    @Provides
    @PerScreen
    fun provideDocumentEmojiIconPickerViewModel(
        setEmojiIcon: SetDocumentEmojiIcon,
        emojiSuggester: EmojiSuggester
    ): DocumentEmojiIconPickerViewModelFactory = DocumentEmojiIconPickerViewModelFactory(
        setEmojiIcon = setEmojiIcon,
        emojiSuggester = emojiSuggester,
        emojiProvider = Emoji
    )

    @Provides
    @PerScreen
    fun provideSetDocumentEmojiIconUseCase(
        repo: BlockRepository
    ): SetDocumentEmojiIcon = SetDocumentEmojiIcon(
        repo = repo
    )
}