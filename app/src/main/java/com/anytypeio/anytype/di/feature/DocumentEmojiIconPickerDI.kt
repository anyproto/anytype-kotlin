package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.icon.RemoveDocumentIcon
import com.anytypeio.anytype.domain.icon.SetDocumentEmojiIcon
import com.anytypeio.anytype.emojifier.data.Emoji
import com.anytypeio.anytype.emojifier.suggest.EmojiSuggester
import com.anytypeio.anytype.presentation.page.editor.DetailModificationManager
import com.anytypeio.anytype.presentation.page.picker.DocumentEmojiIconPickerViewModelFactory
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.page.modals.DocumentEmojiIconPickerFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [DocumentEmojiIconPickerModule::class])
@PerModal
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
    @PerModal
    fun provideDocumentEmojiIconPickerViewModel(
        setEmojiIcon: SetDocumentEmojiIcon,
        removeDocumentIcon: RemoveDocumentIcon,
        emojiSuggester: EmojiSuggester,
        dispatcher: Dispatcher<Payload>,
        details: DetailModificationManager
    ): DocumentEmojiIconPickerViewModelFactory = DocumentEmojiIconPickerViewModelFactory(
        setEmojiIcon = setEmojiIcon,
        removeDocumentIcon = removeDocumentIcon,
        emojiSuggester = emojiSuggester,
        emojiProvider = Emoji,
        dispatcher = dispatcher,
        details = details
    )

    @Provides
    @PerModal
    fun provideSetDocumentEmojiIconUseCase(
        repo: BlockRepository
    ): SetDocumentEmojiIcon = SetDocumentEmojiIcon(
        repo = repo
    )

    @Provides
    @PerModal
    fun provideRemoveDocumentIconUseCase(
        repo: BlockRepository
    ): RemoveDocumentIcon = RemoveDocumentIcon(
        repo = repo
    )
}