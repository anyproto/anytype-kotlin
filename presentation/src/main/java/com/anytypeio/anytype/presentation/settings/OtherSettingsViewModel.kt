package com.anytypeio.anytype.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsDictionary.PROP_TYPE
import com.anytypeio.anytype.analytics.event.EventAnalytics
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.launch.SetDefaultEditorType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class OtherSettingsViewModel(
    private val getDefaultEditorType: GetDefaultEditorType,
    private val setDefaultEditorType: SetDefaultEditorType,
    private val analytics: Analytics
) : ViewModel() {

    val commands = MutableSharedFlow<Command>(replay = 0)

    init {
        viewModelScope.launch {
            getDefaultEditorType.invoke(Unit).proceed(
                failure = { Timber.e(it, "Error while getting user settings") },
                success = { commands.emit(Command.SetObjectType(name = it.name)) }
            )
        }
    }

    fun onObjectTypeClicked() {
        viewModelScope.launch {
            commands.emit(Command.NavigateToObjectTypesScreen(DEFAULT_SETTINGS_SMART_BLOCK_TYPE))
        }
    }

    fun proceedWithUpdateType(type: Id?, name: String?) {
        if (type == null || name == null) {
            viewModelScope.launch {
                commands.emit(Command.Toast("Cannot change default object type"))
            }
            return
        }
        viewModelScope.launch {
            setDefaultEditorType.invoke(SetDefaultEditorType.Params(type, name)).process(
                failure = {
                    Timber.e(it, "Error while setting default object type")
                    commands.emit(Command.Toast(msg = "Error while setting default object type"))
                },
                success = {
                    commands.emit(Command.SetObjectType(name = name))
                    analytics.registerEvent(
                        EventAnalytics.Anytype(
                            name = EventsDictionary.DEFAULT_TYPE_CHANGED,
                            props = Props(mapOf(PROP_TYPE to type)),
                            duration = null
                        )
                    )
                }
            )
        }
    }

    sealed class Command {
        data class SetObjectType(val name: String?) : Command()
        data class Toast(val msg: String) : Command()
        data class NavigateToObjectTypesScreen(val smartBlockType: SmartBlockType) : Command()
        object Exit : Command()
    }

    companion object {
        private val DEFAULT_SETTINGS_SMART_BLOCK_TYPE = SmartBlockType.PAGE
    }

    class Factory(
        private val getDefaultEditorType: GetDefaultEditorType,
        private val setDefaultEditorType: SetDefaultEditorType,
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(
            modelClass: Class<T>
        ): T = OtherSettingsViewModel(
            getDefaultEditorType = getDefaultEditorType,
            setDefaultEditorType = setDefaultEditorType,
            analytics = analytics
        ) as T
    }
}