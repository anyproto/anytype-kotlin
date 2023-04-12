package com.anytypeio.anytype.presentation.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Url
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class MigrationErrorViewModel(
    val analytics: Analytics
) : ViewModel() {

    val commands = MutableSharedFlow<Command>()
    private val viewActions = MutableSharedFlow<ViewAction>()

    init {
        viewModelScope.launch {
            viewActions.collect { action ->
                when (action) {
                    ViewAction.CloseScreen -> {
                        sendAnalyticsEvent(ANALYTICS_TYPE_EXIT)
                        proceedWithCloseScreen()
                    }
                    ViewAction.VisitForum -> {
                        sendAnalyticsEvent(ANALYTICS_TYPE_CHECK_INSTRUCTIONS)
                        proceedWithVisitingForum()
                    }
                    ViewAction.DownloadDesktop -> {
                        sendAnalyticsEvent(ANALYTICS_TYPE_DESKTOP_DOWNLOAD)
                        proceedWithDesktopDownload()
                    }
                    ViewAction.ToggleMigrationNotReady -> {
                        // Do nothing
                    }
                    ViewAction.ToggleMigrationReady -> {
                        sendAnalyticsEvent(ANALYTICS_TYPE_MIGRATION_COMPLETED)
                    }
                }
            }
        }
    }

    fun onAction(action: ViewAction) {
        viewModelScope.launch {
            viewActions.emit(action)
        }
    }

    private fun proceedWithCloseScreen() {
        viewModelScope.launch {
            commands.emit(Command.Exit)
        }
    }

    private fun proceedWithVisitingForum() {
        viewModelScope.launch {
            commands.emit(Command.Browse(VISIT_FORUM_URL))
        }
    }

    private fun proceedWithDesktopDownload() {
        viewModelScope.launch {
            commands.emit(Command.Browse(DOWNLOAD_DESKTOP_URL))
        }
    }

    private fun sendAnalyticsEvent(type: String) {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = ANALYTICS_EVENT_SCREEN,
            props = Props(mapOf("type" to type))
        )
    }

    sealed interface Command {
        object Exit: Command
        data class Browse(val url: Url): Command
    }

    sealed interface ViewAction {
        object CloseScreen: ViewAction
        object ToggleMigrationNotReady: ViewAction
        object ToggleMigrationReady: ViewAction
        object VisitForum: ViewAction
        object DownloadDesktop: ViewAction
    }

    class Factory @Inject constructor(
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MigrationErrorViewModel(
                analytics = analytics
            ) as T
        }
    }

    companion object {
        const val DOWNLOAD_DESKTOP_URL = "https://download.anytype.io/"
        const val VISIT_FORUM_URL = "https://community.anytype.io/migration"
    }
}

private const val ANALYTICS_EVENT_SCREEN = "MigrationGoneWrong"
private const val ANALYTICS_TYPE_MIGRATION_COMPLETED = "complete"
private const val ANALYTICS_TYPE_CHECK_INSTRUCTIONS = "instructions"
private const val ANALYTICS_TYPE_DESKTOP_DOWNLOAD = "download"
private const val ANALYTICS_TYPE_EXIT = "exit"