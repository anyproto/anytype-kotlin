package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.icon.SetDocumentEmojiIcon
import com.anytypeio.anytype.domain.icon.SetDocumentImageIcon
import com.anytypeio.anytype.presentation.page.picker.DocumentIconActionMenuViewModelFactory
import com.anytypeio.anytype.ui.page.modals.actions.DocumentIconActionMenuFragment
import com.anytypeio.anytype.ui.page.modals.actions.ProfileIconActionMenuFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [DocumentIconActionMenuModule::class])
@PerScreen
interface DocumentActionMenuSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun documentIconActionMenuModule(module: DocumentIconActionMenuModule): Builder
        fun build(): DocumentActionMenuSubComponent
    }

    fun inject(fragment: DocumentIconActionMenuFragment)
    fun inject(fragment: ProfileIconActionMenuFragment)
}

@Module
class DocumentIconActionMenuModule {

    @Provides
    @PerScreen
    fun provideDocumentIconActionMenuViewModelFactory(
        setEmojiIcon: SetDocumentEmojiIcon,
        setImageIcon: SetDocumentImageIcon
    ): DocumentIconActionMenuViewModelFactory = DocumentIconActionMenuViewModelFactory(
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