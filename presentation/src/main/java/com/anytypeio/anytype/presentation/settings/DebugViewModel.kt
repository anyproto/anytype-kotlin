package com.anytypeio.anytype.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.onSuccess
import com.anytypeio.anytype.domain.chats.ReadAllMessages
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class DebugViewModel @Inject constructor(
    private val getAccount: GetAccount,
    private val readAllMessages: ReadAllMessages
) : BaseViewModel() {

    val commands = MutableSharedFlow<Command>(replay = 0)

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
        }
    }

    fun onReadAllChats() {
        viewModelScope.launch {
            readAllMessages
                .async(Unit)
                .onSuccess {
                    // do nothing
                }
        }
    }

    class Factory @Inject constructor(
        private val getAccount: GetAccount,
        private val readAllMessages: ReadAllMessages
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DebugViewModel(
                getAccount = getAccount,
                readAllMessages = readAllMessages
            ) as T
        }
    }

    sealed class Command {
        data class ExportWorkingDirectory(
            val folderName: String,
            val exportFileName: String
        ): Command()
    }

    companion object {
        const val EXPORT_WORK_DIRECTORY_TEMP_FOLDER = "anytype-work-directory.zip"
    }
}