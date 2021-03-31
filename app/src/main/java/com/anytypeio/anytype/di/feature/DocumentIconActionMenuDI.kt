package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.icon.SetDocumentEmojiIcon
import com.anytypeio.anytype.domain.icon.SetDocumentImageIcon
import com.anytypeio.anytype.presentation.page.editor.DetailModificationManager
import com.anytypeio.anytype.presentation.page.picker.DocumentIconActionMenuViewModelFactory
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.page.modals.actions.DocumentIconActionMenuFragment
import com.anytypeio.anytype.ui.page.modals.actions.ProfileIconActionMenuFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [DocumentIconActionMenuModule::class])
@PerModal
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
    @PerModal
    fun provideDocumentIconActionMenuViewModelFactory(
        setEmojiIcon: SetDocumentEmojiIcon,
        setImageIcon: SetDocumentImageIcon,
        dispatcher: Dispatcher<Payload>,
        details: DetailModificationManager
    ): DocumentIconActionMenuViewModelFactory = DocumentIconActionMenuViewModelFactory(
        setEmojiIcon = setEmojiIcon,
        setImageIcon = setImageIcon,
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
    fun provideSetDocumentImageIconUseCase(
        repo: BlockRepository
    ): SetDocumentImageIcon = SetDocumentImageIcon(
        repo = repo
    )
}