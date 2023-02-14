package com.anytypeio.anytype.presentation.types.icon_picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.emojifier.data.EmojiProvider
import com.anytypeio.anytype.emojifier.suggest.EmojiSuggester
import com.anytypeio.anytype.presentation.picker.IconPickerViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow

class TypeIconPickerViewModel(
    provider: EmojiProvider,
    suggester: EmojiSuggester,
) : IconPickerViewModel<Unit>(provider, suggester) {

    val actions = MutableStateFlow<EmojiPickerAction>(EmojiPickerAction.Idle)

    override fun setEmoji(iconable: Unit, emojiUnicode: String) {
        actions.value = EmojiPickerAction.SetEmoji(emojiUnicode)
    }

    override fun onRemoveClicked(iconable: Unit) {
        actions.value = EmojiPickerAction.RemoveEmoji
    }

    override fun onPickedFromDevice(iconable: Unit, path: String) {
        // do nothing
    }

    sealed class EmojiPickerAction {
        object Idle : EmojiPickerAction()
        class SetEmoji(val emojiUnicode: String) : EmojiPickerAction()
        object RemoveEmoji : EmojiPickerAction()
    }

    class Factory @Inject constructor(
        private val emojiSuggester: EmojiSuggester,
        private val emojiProvider: EmojiProvider
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TypeIconPickerViewModel(
                suggester = emojiSuggester,
                provider = emojiProvider
            ) as T
        }
    }

}
