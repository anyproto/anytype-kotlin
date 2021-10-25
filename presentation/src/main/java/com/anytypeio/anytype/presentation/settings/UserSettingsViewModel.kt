package com.anytypeio.anytype.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.launch.SetDefaultEditorType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class UserSettingsViewModel(
    private val getDefaultEditorType: GetDefaultEditorType,
    private val setDefaultEditorType: SetDefaultEditorType,
    private val analytics: Analytics
) : ViewModel() {

    val commands = MutableSharedFlow<Command>(replay = 0)

    init {
        viewModelScope.launch {
            getDefaultEditorType.invoke(Unit).proceed(
                failure = { Timber.e(it, "Error while getting user settings") },
                success = { response ->
                    if (response.type == ObjectType.NOTE_URL) {
                        commands.emit(Command.NoteSelected)
                    } else {
                        commands.emit(Command.PageSelected)
                    }
                }
            )
        }
    }

    fun onNoteClicked() {
        proceedWithUpdateType(ObjectType.NOTE_URL)
    }

    fun onPageClicked() {
        proceedWithUpdateType(ObjectType.PAGE_URL)
    }

    private fun proceedWithUpdateType(type: String) {
        viewModelScope.launch {
            setDefaultEditorType.invoke(SetDefaultEditorType.Params(type)).process(
                failure = {
                    Timber.e(it, "Error while setting default object type")
                    commands.emit(Command.Exit)
                },
                success = { commands.emit(Command.Exit) }
            )
        }
    }

    sealed class Command {

        object NoteSelected : Command()
        object PageSelected : Command()
        object Exit : Command()
    }

    class Factory(
        private val getDefaultEditorType: GetDefaultEditorType,
        private val setDefaultEditorType: SetDefaultEditorType,
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(
            modelClass: Class<T>
        ): T = UserSettingsViewModel(
            getDefaultEditorType = getDefaultEditorType,
            setDefaultEditorType = setDefaultEditorType,
            analytics = analytics
        ) as T
    }
}