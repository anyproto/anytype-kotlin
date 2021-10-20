package com.anytypeio.anytype.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.domain.launch.GetDefaultPageType
import com.anytypeio.anytype.domain.launch.SetDefaultPageType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class UserSettingsViewModel(
    private val getDefaultPageType: GetDefaultPageType,
    private val setDefaultPageType: SetDefaultPageType,
    private val analytics: Analytics
) : ViewModel() {

    val commands = MutableSharedFlow<Command>(replay = 0)

    init {
        viewModelScope.launch {
            getDefaultPageType.invoke(Unit).proceed(
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
            setDefaultPageType.invoke(SetDefaultPageType.Params(type)).process(
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
        private val getDefaultPageType: GetDefaultPageType,
        private val setDefaultPageType: SetDefaultPageType,
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(
            modelClass: Class<T>
        ): T = UserSettingsViewModel(
            getDefaultPageType = getDefaultPageType,
            setDefaultPageType = setDefaultPageType,
            analytics = analytics
        ) as T
    }
}