package com.anytypeio.anytype.ui.settings

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.views.BaseAlertDialog
import com.anytypeio.anytype.core_utils.ext.shareFirstFileFromPath
import com.anytypeio.anytype.core_utils.ext.shareFileFromPath
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.tools.ZIP_MIME_TYPE
import com.anytypeio.anytype.core_utils.tools.zipDirectory
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.settings.DebugViewModel
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import javax.inject.Inject
import kotlin.getValue
import timber.log.Timber

class DebugFragment : BaseBottomSheetComposeFragment() {

    private val createFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument(ZIP_MIME_TYPE)) { uri ->
        uri?.let { saveZipToUri(it) }
    }

    @Inject
    lateinit var factory: DebugViewModel.Factory

    private val vm by viewModels<DebugViewModel> { factory }
    
    private val spaceId: String? by lazy {
        arguments?.getString(ARG_SPACE_ID_KEY)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View  = content {
        DebugScreen(
            onExportAllClicked = vm::onExportWorkingDirectory,
            onReadAllChats = vm::onReadAllChats,
            onDebugStackGoroutines = vm::onDiagnosticsGoroutinesClicked,
            onDebugStat = vm::onDiagnosticsStatClicked,
            onDebugSpaceSummary = { vm.onDiagnosticsSpaceSummaryClicked(spaceId) },
            onDebugExportLog = vm::onDiagnosticsExportLogClicked,
            isProfilerOnStartupEnabled = vm.isProfilerOnStartupEnabled.collectAsStateWithLifecycle().value,
            onProfilerOnStartupToggled = vm::onProfilerOnStartupToggled,
            profilerState = vm.profilerState.collectAsStateWithLifecycle().value,
            profilerDurationSeconds = DebugViewModel.PROFILER_DURATION_SECONDS,
            onRunProfilerNowClicked = vm::onRunProfilerNowClicked,
            onShareProfilerResultClicked = vm::onShareProfilerResultClicked
        )
        val messages = vm.messages.collectAsStateWithLifecycle().value
        if (messages != null) {
            BaseAlertDialog(
                dialogText = messages,
                buttonText = getString(R.string.button_ok),
                onButtonClick = { vm.clearMessages() },
                onDismissRequest = { vm.clearMessages() }
            )
        }
        LaunchedEffect(Unit) {
            vm.commands.collect { cmd ->
                when(cmd) {
                    is DebugViewModel.Command.ExportWorkingDirectory -> {
                        proceedWithZippingAndSharingWorkDirectory(
                            folderName = cmd.folderName,
                            exportFileName = cmd.exportFileName
                        )
                    }
                    is DebugViewModel.Command.Toast -> {
                        toast(cmd.msg)
                    }

                    is DebugViewModel.Command.ShareDebugGoroutines -> {
                        runCatching {
                            shareFirstFileFromPath(
                                path = cmd.path,
                                uriFileProvider = cmd.uriFileProvider
                            )
                        }.onFailure { error ->
                            Timber.e(error, "Failed to share debug goroutines")
                            vm.onShowMessage(msg = "Failed to share debug goroutines: ${error.message}")
                        }
                    }
                    
                    is DebugViewModel.Command.ShareDebugStat -> {
                        runCatching {
                            shareFirstFileFromPath(
                                path = cmd.path,
                                uriFileProvider = cmd.uriFileProvider
                            )
                        }.onFailure { error ->
                            Timber.e(error, "Failed to share debug stat")
                            vm.onShowMessage(msg = "Failed to share debug stat: ${error.message}")
                        }
                    }
                    
                    is DebugViewModel.Command.ShareDebugLogs -> {
                        runCatching {
                            shareFileFromPath(
                                path = cmd.path,
                                uriFileProvider = cmd.uriFileProvider
                            )
                        }.onFailure { error ->
                            Timber.e(error, "Failed to share debug logs")
                            vm.onShowMessage(msg = "Failed to share debug logs: ${error.message}")
                        }
                    }
                    
                    is DebugViewModel.Command.ShareDebugSpaceSummary -> {
                        runCatching {
                            shareFirstFileFromPath(
                                path = cmd.path,
                                uriFileProvider = cmd.uriFileProvider
                            )
                        }.onFailure { error ->
                            Timber.e(error, "Failed to share space summary")
                            vm.onShowMessage(msg = "Failed to share space summary: ${error.message}")
                        }
                    }

                    is DebugViewModel.Command.ShareProfilerResult -> {
                        runCatching {
                            shareFileFromPath(
                                path = cmd.path,
                                uriFileProvider = cmd.uriFileProvider
                            )
                        }.onFailure { error ->
                            Timber.e(error, "Failed to share profiler result")
                            vm.onShowMessage(msg = "Failed to share profiler result: ${error.message}")
                        }
                    }
                }
            }
        }
    }

    private fun proceedWithZippingAndSharingWorkDirectory(
        folderName: String,
        exportFileName: String
    ) {
        val folder = File(
            requireContext().filesDir,
            folderName
        )
        val zipped = File(
            requireContext().cacheDir,
            DebugViewModel.EXPORT_WORK_DIRECTORY_TEMP_FOLDER
        )
        zipDirectory(
            sourceDir = folder,
            zipFile = zipped
        )
        createFileLauncher.launch(exportFileName)
    }

    private fun saveZipToUri(uri: Uri) {
        try {
            requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                val zipFile = File(requireContext().cacheDir, DebugViewModel.EXPORT_WORK_DIRECTORY_TEMP_FOLDER)
                FileInputStream(zipFile).use { it.copyTo(outputStream) }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun injectDependencies() {
        componentManager().debugComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().debugComponent.release()
    }
    
    companion object {
        private const val ARG_SPACE_ID_KEY = "arg.debug.space-id"
        
        fun args(spaceId: String?) = bundleOf(ARG_SPACE_ID_KEY to spaceId)
        
        fun newInstance(spaceId: String? = null) = DebugFragment().apply {
            arguments = args(spaceId)
        }
    }
}