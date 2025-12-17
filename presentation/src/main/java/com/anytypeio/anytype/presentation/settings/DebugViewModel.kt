package com.anytypeio.anytype.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.base.onFailure
import com.anytypeio.anytype.domain.base.onSuccess
import com.anytypeio.anytype.domain.chats.ReadAllChatMessages
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.debugging.DebugExportLogs
import com.anytypeio.anytype.domain.debugging.DebugGoroutines
import com.anytypeio.anytype.domain.debugging.DebugRunProfiler
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.util.downloader.DebugStats
import com.anytypeio.anytype.presentation.util.downloader.DebugSpace
import com.anytypeio.anytype.presentation.util.downloader.UriFileProvider
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class DebugViewModel @Inject constructor(
    private val getAccount: GetAccount,
    private val readAllChatMessages: ReadAllChatMessages,
    private val debugGoroutines: DebugGoroutines,
    private val debugStat: DebugStats,
    private val debugSpace: DebugSpace,
    private val debugExportLogs: DebugExportLogs,
    private val debugRunProfiler: DebugRunProfiler,
    private val pathProvider: PathProvider,
    private val uriFileProvider: UriFileProvider,
    private val userSettingsRepository: UserSettingsRepository
) : BaseViewModel() {

    val commands = MutableSharedFlow<Command>(replay = 0)
    private val jobs = mutableListOf<Job>()

    val messages = MutableStateFlow<String?>(null)
    val isProfilerOnStartupEnabled = MutableStateFlow(false)
    val profilerState = MutableStateFlow<ProfilerState>(ProfilerState.Idle)

    init {
        viewModelScope.launch {
            isProfilerOnStartupEnabled.value = userSettingsRepository.getRunProfilerOnStartup()
        }
    }

    fun onExportWorkingDirectory() {
        viewModelScope.launch {
            getAccount
                .async(Unit)
                .onSuccess { account ->
                    commands.emit(
                        Command.ExportWorkingDirectory(
                            folderName = account.id,
                            exportFileName = "anytype-${account.id}.zip"
                        )
                    )
                }
                .onFailure { error ->
                    Timber.e(error, "Failed to get account for export working directory")
                    messages.value =
                        "Failed to get account for export working directory: ${error.message}"
                }
        }
    }

    fun onReadAllChats() {
        viewModelScope.launch {
            readAllChatMessages
                .async(Unit)
                .onSuccess {
                    Timber.d("readAllMessages success")
                    commands.emit(Command.Toast("readAllMessages success"))
                }
                .onFailure {
                    Timber.w(it, "readAllMessages failure")
                    //messages.value = "readAllMessages failure: ${it.message}"
                }
        }
    }

    fun onDiagnosticsGoroutinesClicked() {
        proceedWithGoroutinesDebug()
    }

    private fun proceedWithGoroutinesDebug() {
        Timber.d("proceedWithGoroutinesDebug")
        jobs += viewModelScope.launch {
            debugGoroutines.async(DebugGoroutines.Params()).fold(
                onSuccess = { path ->
                    Timber.d("Debug goroutines success: $path")
                    commands.emit(Command.ShareDebugGoroutines(path, uriFileProvider))
                },
                onFailure = {
                    Timber.e(it, "Error while collecting goroutines diagnostics")
                    messages.value = "Error while collecting goroutines diagnostics: ${it.message}"
                }
            )
        }
    }

    fun onDiagnosticsStatClicked() {
        jobs += viewModelScope.launch {
            debugStat.async(Unit).fold(
                onSuccess = { path ->
                    Timber.d("Debug stat success: $path")
                    commands.emit(Command.ShareDebugStat(path, uriFileProvider))
                },
                onFailure = {
                    Timber.e(it, "Error while collecting stat diagnostics")
                    messages.value = "Error while collecting stat diagnostics: ${it.message}"
                }
            )
        }
    }

    fun onDiagnosticsSpaceSummaryClicked(spaceId: String?) {
        if (spaceId.isNullOrBlank()) {
            messages.value = "No Space ID provided for diagnostics"
            return
        }
        jobs += viewModelScope.launch {
            val params = DebugSpace.Params(spaceId = SpaceId(spaceId))
            debugSpace.async(params).fold(
                onSuccess = { path ->
                    Timber.d("Debug space success: $path")
                    commands.emit(Command.ShareDebugSpaceSummary(path, uriFileProvider))
                },
                onFailure = {
                    messages.value = "Error while collecting space summary diagnostics: ${it.message}"
                    Timber.e(it, "Error while collecting space summary diagnostics")
                }
            )
        }
    }

    fun onDiagnosticsExportLogClicked() {
        Timber.d("onExportLogsClick: ")
        jobs += viewModelScope.launch {
            val dir = pathProvider.cachePath()
            val params = DebugExportLogs.Params(dir = dir)
            debugExportLogs.async(params).fold(
                onSuccess = { zipFilePath ->
                    Timber.d("Debug logs success: $zipFilePath")
                    commands.emit(Command.ShareDebugLogs(zipFilePath, uriFileProvider))
                },
                onFailure = {
                    Timber.e(it, "Error while collecting debug logs")
                    messages.value = "Error while collecting debug logs: ${it.message}"
                }
            )
        }
    }

    fun onShowMessage(msg: String) {
        Timber.d("onShowMessage: $msg")
        messages.value = msg
    }

    fun clearMessages() {
        messages.value = null
    }

    fun onProfilerOnStartupToggled(enabled: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.setRunProfilerOnStartup(enabled)
            isProfilerOnStartupEnabled.value = enabled
        }
    }

    fun onRunProfilerNowClicked() {
        if (profilerState.value is ProfilerState.Running) return
        viewModelScope.launch {
            profilerState.value = ProfilerState.Running
            debugRunProfiler.async(DebugRunProfiler.Params(durationInSeconds = PROFILER_DURATION_SECONDS))
                .onSuccess { path ->
                    Timber.d("Debug profiler success: $path")
                    profilerState.value = ProfilerState.Completed(path)
                }
                .onFailure { error ->
                    Timber.e(error, "Error while running profiler")
                    profilerState.value = ProfilerState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun onShareProfilerResultClicked() {
        viewModelScope.launch {
            val state = profilerState.value
            if (state is ProfilerState.Completed) {
                commands.emit(Command.ShareProfilerResult(state.filePath, uriFileProvider))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        jobs.cancel()
    }

    class Factory @Inject constructor(
        private val getAccount: GetAccount,
        private val readAllChatMessages: ReadAllChatMessages,
        private val debugGoroutines: DebugGoroutines,
        private val debugStats: DebugStats,
        private val debugSpace: DebugSpace,
        private val debugExportLogs: DebugExportLogs,
        private val debugRunProfiler: DebugRunProfiler,
        private val pathProvider: PathProvider,
        private val uriFileProvider: UriFileProvider,
        private val userSettingsRepository: UserSettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DebugViewModel(
                getAccount = getAccount,
                readAllChatMessages = readAllChatMessages,
                debugGoroutines = debugGoroutines,
                debugStat = debugStats,
                debugSpace = debugSpace,
                debugExportLogs = debugExportLogs,
                debugRunProfiler = debugRunProfiler,
                pathProvider = pathProvider,
                uriFileProvider = uriFileProvider,
                userSettingsRepository = userSettingsRepository
            ) as T
        }
    }

    sealed class Command {
        data class Toast(val msg: String) : Command()
        data class ExportWorkingDirectory(
            val folderName: String,
            val exportFileName: String
        ) : Command()

        data class ShareDebugGoroutines(val path: String, val uriFileProvider: UriFileProvider) :
            Command()
        
        data class ShareDebugStat(val path: String, val uriFileProvider: UriFileProvider) :
            Command()
            
        data class ShareDebugLogs(val path: String, val uriFileProvider: UriFileProvider) :
            Command()
            
        data class ShareDebugSpaceSummary(val path: String, val uriFileProvider: UriFileProvider) :
            Command()

        data class ShareProfilerResult(val path: String, val uriFileProvider: UriFileProvider) :
            Command()
    }

    sealed class ProfilerState {
        data object Idle : ProfilerState()
        data object Running : ProfilerState()
        data class Completed(val filePath: String) : ProfilerState()
        data class Error(val message: String) : ProfilerState()
    }

    companion object {
        const val EXPORT_WORK_DIRECTORY_TEMP_FOLDER = "anytype-work-directory.zip"
        const val PROFILER_DURATION_SECONDS = 60
    }
}