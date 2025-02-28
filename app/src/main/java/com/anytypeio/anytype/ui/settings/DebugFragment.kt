package com.anytypeio.anytype.ui.settings

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
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

class DebugFragment : BaseBottomSheetComposeFragment() {

    private val createFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument(ZIP_MIME_TYPE)) { uri ->
        uri?.let { saveZipToUri(it) }
    }

    @Inject
    lateinit var factory: DebugViewModel.Factory

    private val vm by viewModels<DebugViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View  = content {
        DebugScreen(
            onExportAllClicked = vm::onExportWorkingDirectory
        )
        LaunchedEffect(Unit) {
            vm.commands.collect { cmd ->
                when(cmd) {
                    is DebugViewModel.Command.ExportWorkingDirectory -> {
                        proceedWithZippingAndSharingWorkDirectory(
                            folderName = cmd.folderName,
                            exportFileName = cmd.exportFileName
                        )
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
}