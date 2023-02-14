package com.anytypeio.anytype.presentation.editor.picker

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.icon.RemoveIcon
import com.anytypeio.anytype.domain.icon.SetEmojiIcon
import com.anytypeio.anytype.domain.icon.SetImageIcon
import com.anytypeio.anytype.emojifier.data.EmojiProvider
import com.anytypeio.anytype.emojifier.suggest.EmojiSuggester
import com.anytypeio.anytype.presentation.picker.IconPickerViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectIconPickerViewModel<Iconable>(
    private val setEmojiIcon: SetEmojiIcon<Iconable>,
    private val setImageIcon: SetImageIcon<Iconable>,
    private val removeDocumentIcon: RemoveIcon<Iconable>,
    private val dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics,
    provider: EmojiProvider,
    suggester: EmojiSuggester
) : IconPickerViewModel<Iconable>(provider, suggester) {

    override fun setEmoji(iconable: Iconable, emojiUnicode: String) {
        viewModelScope.launch {
            setEmojiIcon(
                params = SetEmojiIcon.Params(
                    emoji = emojiUnicode,
                    target = iconable
                )
            ).process(
                failure = { Timber.e(it, "Error while setting emoji") },
                success = { payload ->
                    sendEvent(
                        analytics = analytics,
                        eventName = EventsDictionary.objectSetIcon
                    )
                    if (payload.events.isNotEmpty()) dispatcher.send(payload)
                    state.value = ViewState.Exit
                }
            )
        }
    }

    override fun onRemoveClicked(iconable: Iconable) {
        viewModelScope.launch {
            removeDocumentIcon(iconable).process(
                failure = { Timber.e(it, "Error while removing icon") },
                success = { payload ->
                    sendEvent(
                        analytics = analytics,
                        eventName = EventsDictionary.objectRemoveIcon
                    )
                    if (payload.events.isNotEmpty()) dispatcher.send(payload)
                    state.value = ViewState.Exit
                }
            )
        }
    }

    override fun onPickedFromDevice(iconable: Iconable, path: String) {
        viewModelScope.launch {
            state.value = ViewState.Loading
            setImageIcon(
                SetImageIcon.Params(
                    target = iconable,
                    path = path
                )
            ).process(
                failure = {
                    Timber.e("Error while setting image icon")
                    state.value = ViewState.Init
                },
                success = { (payload, _) ->
                    dispatcher.send(payload)
                    state.value = ViewState.Exit
                }
            )
        }
    }

}