package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.icon.SetDocumentEmojiIcon
import com.agileburo.anytype.presentation.page.picker.DocumentEmojiIconPickerViewModelFactory
import com.agileburo.anytype.ui.page.modals.DocumentEmojiIconPickerFragment
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
        setEmojiIcon: SetDocumentEmojiIcon
    ): DocumentEmojiIconPickerViewModelFactory = DocumentEmojiIconPickerViewModelFactory(
        setEmojiIcon = setEmojiIcon
    )

    @Provides
    @PerScreen
    fun provideSetDocumentEmojiIconUseCase(
        repo: BlockRepository
    ): SetDocumentEmojiIcon = SetDocumentEmojiIcon(
        repo = repo
    )
}