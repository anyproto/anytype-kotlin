package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.icon.SetDocumentEmojiIcon
import com.agileburo.anytype.domain.icon.SetDocumentImageIcon
import com.agileburo.anytype.presentation.page.picker.DocumentIconPickerViewModelFactory
import com.agileburo.anytype.ui.page.modals.DocumentEmojiIconPickerFragment
import com.agileburo.anytype.ui.page.modals.actions.DocumentIconActionMenu
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [DocumentIconPickerModule::class])
@PerScreen
interface DocumentIconPickerSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun documentIconPickerModule(module: DocumentIconPickerModule): Builder
        fun build(): DocumentIconPickerSubComponent
    }

    fun inject(fragment: DocumentEmojiIconPickerFragment)
    fun inject(fragment: DocumentIconActionMenu)
}

@Module
class DocumentIconPickerModule {

    @Provides
    @PerScreen
    fun provideDocumentIconPickerViewModelFactory(
        setEmojiIcon: SetDocumentEmojiIcon,
        setImageIcon: SetDocumentImageIcon
    ): DocumentIconPickerViewModelFactory = DocumentIconPickerViewModelFactory(
        setEmojiIcon = setEmojiIcon,
        setImageIcon = setImageIcon
    )

    @Provides
    @PerScreen
    fun provideSetDocumentEmojiIconUseCase(
        repo: BlockRepository
    ): SetDocumentEmojiIcon = SetDocumentEmojiIcon(
        repo = repo
    )

    @Provides
    @PerScreen
    fun provideSetDocumentImageIconUseCase(
        repo: BlockRepository
    ): SetDocumentImageIcon = SetDocumentImageIcon(
        repo = repo
    )
}