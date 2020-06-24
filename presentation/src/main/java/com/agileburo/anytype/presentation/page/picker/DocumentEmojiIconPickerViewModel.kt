package com.agileburo.anytype.presentation.page.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.icon.SetDocumentEmojiIcon
import com.agileburo.anytype.emojifier.Emoji
import com.agileburo.anytype.library_page_icon_picker_widget.model.EmojiPickerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class DocumentEmojiIconPickerViewModel(
    private val setEmojiIcon: SetDocumentEmojiIcon
) : ViewModel() {

    private val state: MutableStateFlow<ViewState> = MutableStateFlow(ViewState.Init)

    init {
        viewModelScope.launch {
            state.value = ViewState.Loading
            state.value = ViewState.Success(views = load())
        }
    }

    fun state(): StateFlow<ViewState> = state

    private suspend fun load(): List<EmojiPickerView> = withContext(Dispatchers.IO) {

        val views = mutableListOf<EmojiPickerView>()

        views.add(EmojiPickerView.EmojiFilter)

        Emoji.data.forEachIndexed { category, emojis ->

            views.add(
                EmojiPickerView.GroupHeader(
                    category = category
                )
            )

            emojis.forEachIndexed { index, unicode ->
                val skin = Emoji.colors.any { color -> unicode.contains(color) }
                if (!skin) {
                    views.add(
                        EmojiPickerView.Emoji(
                            unicode = unicode,
                            page = category,
                            index = index
                        )
                    )
                }
            }
        }

        views
    }

    fun onEmojiClicked(unicode: String, target: Id, context: Id) {
        viewModelScope.launch {
            setEmojiIcon(
                params = SetDocumentEmojiIcon.Params(
                    emoji = unicode,
                    target = target,
                    context = context
                )
            ).proceed(
                failure = { Timber.e(it, "Error while setting emoji") },
                success = { state.apply { value = ViewState.Exit } }
            )
        }
    }

    sealed class ViewState {
        object Init : ViewState()
        object Loading : ViewState()
        data class Success(val views: List<EmojiPickerView>) : ViewState()
        object Exit : ViewState()
    }
}