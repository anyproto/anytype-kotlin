package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.icon.RemoveDocumentIcon
import com.anytypeio.anytype.domain.icon.SetDocumentEmojiIcon
import com.anytypeio.anytype.domain.icon.SetDocumentImageIcon
import com.anytypeio.anytype.emojifier.data.Emoji
import com.anytypeio.anytype.emojifier.suggest.EmojiSuggester
import com.anytypeio.anytype.presentation.editor.picker.ObjectIconPickerViewModelFactory
import com.anytypeio.anytype.presentation.editor.picker.ObjectSetIconPickerViewModelFactory
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.editor.modals.ObjectIconPickerFragment
import com.anytypeio.anytype.ui.sets.ObjectSetIconPickerFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [ObjectIconPickerBaseModule::class, ObjectIconPickerModule::class])
@PerModal
interface ObjectIconPickerComponent {
    @Subcomponent.Builder
    interface Builder {
        fun base(module: ObjectIconPickerBaseModule): Builder
        fun module(module: ObjectIconPickerModule): Builder
        fun build(): ObjectIconPickerComponent
    }

    fun inject(fragment: ObjectIconPickerFragment)
}

@Subcomponent(modules = [ObjectIconPickerBaseModule::class, ObjectSetIconPickerModule::class])
@PerModal
interface ObjectSetIconPickerComponent {
    @Subcomponent.Builder
    interface Builder {
        fun base(module: ObjectIconPickerBaseModule): Builder
        fun module(module: ObjectSetIconPickerModule): Builder
        fun build(): ObjectSetIconPickerComponent
    }

    fun inject(fragment: ObjectSetIconPickerFragment)
}

@Module
object ObjectIconPickerBaseModule {
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

    @Provides
    @PerModal
    fun provideRemoveDocumentIconUseCase(
        repo: BlockRepository
    ): RemoveDocumentIcon = RemoveDocumentIcon(
        repo = repo
    )
}

@Module
object ObjectIconPickerModule {
    @Provides
    @PerModal
    fun provideViewModelFactory(
        setEmojiIcon: SetDocumentEmojiIcon,
        setImageIcon: SetDocumentImageIcon,
        removeDocumentIcon: RemoveDocumentIcon,
        emojiSuggester: EmojiSuggester,
        dispatcher: Dispatcher<Payload>
    ): ObjectIconPickerViewModelFactory = ObjectIconPickerViewModelFactory(
        setEmojiIcon = setEmojiIcon,
        setImageIcon = setImageIcon,
        removeDocumentIcon = removeDocumentIcon,
        emojiSuggester = emojiSuggester,
        emojiProvider = Emoji,
        dispatcher = dispatcher,
    )
}

@Module
object ObjectSetIconPickerModule {
    @Provides
    @PerModal
    fun provideViewModelFactory(
        setEmojiIcon: SetDocumentEmojiIcon,
        setImageIcon: SetDocumentImageIcon,
        removeDocumentIcon: RemoveDocumentIcon,
        emojiSuggester: EmojiSuggester,
        dispatcher: Dispatcher<Payload>
    ): ObjectSetIconPickerViewModelFactory = ObjectSetIconPickerViewModelFactory(
        setEmojiIcon = setEmojiIcon,
        setImageIcon = setImageIcon,
        removeDocumentIcon = removeDocumentIcon,
        emojiSuggester = emojiSuggester,
        emojiProvider = Emoji,
        dispatcher = dispatcher,
    )
}