package com.anytypeio.anytype.presentation.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.presentation.auth.account.MigrationHelperDelegate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class MigrationErrorViewModel(
    private val analytics: Analytics,
    private val delegate: MigrationHelperDelegate
) : ViewModel(), MigrationHelperDelegate by delegate {

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
        viewModelScope.launch {
            onStartMigrationRequested()
        }
    }


    private fun proceedWithCloseScreen() {
        viewModelScope.launch {
            commands.emit(Command.Exit)
        }
    }

    @Deprecated("To be deleted")
    fun onAction(action: ViewAction) {
        viewModelScope.launch {
            viewActions.emit(action)
        }
    }

    @Deprecated("To be deleted")
    private fun proceedWithVisitingForum() {
        viewModelScope.launch {
            commands.emit(Command.Browse(VISIT_FORUM_URL))
        }
    }

    @Deprecated("To be deleted")
    private fun proceedWithDesktopDownload() {
        viewModelScope.launch {
            commands.emit(Command.Browse(DOWNLOAD_DESKTOP_URL))
        }
    }

    @Deprecated("To be deleted")
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

    @Deprecated("To be deleted")
    sealed interface ViewAction {
        object CloseScreen: ViewAction
        object ToggleMigrationNotReady: ViewAction
        object ToggleMigrationReady: ViewAction
        object VisitForum: ViewAction
        object DownloadDesktop: ViewAction
    }

    class Factory @Inject constructor(
        private val analytics: Analytics,
        private val delegate: MigrationHelperDelegate
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MigrationErrorViewModel(
                analytics = analytics,
                delegate = delegate
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