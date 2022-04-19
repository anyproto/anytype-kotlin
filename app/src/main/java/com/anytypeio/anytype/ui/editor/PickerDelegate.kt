package com.anytypeio.anytype.ui.editor

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.viewbinding.ViewBinding
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.const.FileConstants
import com.anytypeio.anytype.core_utils.ext.isPermissionGranted
import com.anytypeio.anytype.core_utils.ext.shouldShowRequestPermissionRationaleCompat
import com.anytypeio.anytype.core_utils.ext.showSnackbar
import com.anytypeio.anytype.core_utils.ext.startFilePicker
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseFragment
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.util.CopyFileStatus
import com.google.android.material.snackbar.Snackbar
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import timber.log.Timber

interface PickerDelegate : PickiTCallbacks {

    fun initPicker(fragment: BaseFragment<ViewBinding>, vm: EditorViewModel, ctx: Id)

    fun resolveActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

    fun openFilePicker(mimeType: String, requestCode: Int? = null)

    fun clearPickit()

    fun deleteTemporaryFile()

    fun onCopyFileCommand(command: CopyFileStatus)

    fun clearOnCopyFile()

    class Impl : PickerDelegate {

        private lateinit var fragment: BaseFragment<ViewBinding>
        private lateinit var vm: EditorViewModel
        private lateinit var ctx: Id
        private lateinit var pickiT: PickiT

        private var pickitProgressDialog: ProgressDialog? = null
        private var pickitProgressBar: ProgressBar? = null
        private var pickitAlertDialog: AlertDialog? = null
        private var snackbar: Snackbar? = null

        private var mimeType = ""
        private var requestCode: Int? = null

        override fun resolveActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            this.requestCode = requestCode
            when (requestCode) {
                FileConstants.REQUEST_MEDIA_CODE -> {
                    data?.data?.let { uri ->
                        pickiT.getPath(uri, Build.VERSION.SDK_INT)
                    }
                }
                FileConstants.REQUEST_FILE_SAF_CODE -> {
                    data?.data?.let { uri ->
                        vm.onStartCopyFileToCacheDir(uri)
                    } ?: run {
                        Timber.e("onActivityResult error, data is null")
                        fragment.toast("Error while getting file")
                    }
                }
                FileConstants.REQUEST_PROFILE_IMAGE_CODE -> {
                    data?.data?.let { uri ->
                        pickiT.getPath(uri, Build.VERSION.SDK_INT)
                    }
                }
                else -> {
                    Timber.e("onActivityResult error, Unknown Request Code:$requestCode")
                    fragment.toast("Unknown Request Code:$requestCode")
                }
            }
        }

        override fun openFilePicker(mimeType: String, requestCode: Int?) {
            this.mimeType = mimeType
            this.requestCode = requestCode
            if (fragment.requireContext().isPermissionGranted(mimeType)) {
                fragment.startFilePicker(mimeType, requestCode)
            } else {
                takeReadStoragePermission()
            }
        }

        override fun initPicker(fragment: BaseFragment<ViewBinding>, vm: EditorViewModel, ctx: Id) {
            this.fragment = fragment
            this.vm = vm
            this.ctx = ctx
            pickiT = PickiT(fragment.requireContext(), this, fragment.requireActivity())
        }

        override fun clearPickit() {
            pickiT.cancelTask()
            pickitAlertDialog?.dismiss()
            pickitProgressDialog?.dismiss()
            pickitAlertDialog = null
            pickitProgressBar = null
            pickitProgressDialog = null
            snackbar = null
        }

        override fun deleteTemporaryFile() {
            pickiT.deleteTemporaryFile(fragment.requireContext())
        }

        override fun onCopyFileCommand(command: CopyFileStatus) {

            when (command) {
                is CopyFileStatus.Error -> {
                    snackbar?.dismiss()
                    fragment.activity?.toast("Error while loading file:${command.msg}")
                }
                is CopyFileStatus.Completed -> {
                    snackbar?.dismiss()
                    onFilePathReady(command.result)
                }
                CopyFileStatus.Started -> {
                    snackbar = fragment.binding.root.showSnackbar(
                        R.string.loading_file,
                        Snackbar.LENGTH_INDEFINITE,
                        R.string.cancel
                    ) {
                        vm.onCancelCopyFileToCacheDir()
                    }
                }
            }
        }

        override fun clearOnCopyFile() {
            vm.onCancelCopyFileToCacheDir()
            snackbar?.dismiss()
            snackbar = null
        }

        private fun takeReadStoragePermission() {
            if (fragment.requireActivity()
                    .shouldShowRequestPermissionRationaleCompat(READ_EXTERNAL_STORAGE)
            ) {
                fragment.binding.root.showSnackbar(
                    R.string.permission_read_rationale,
                    Snackbar.LENGTH_INDEFINITE,
                    R.string.button_ok
                ) {
                    permissionReadStorage.launch(arrayOf(READ_EXTERNAL_STORAGE))
                }
            } else {
                permissionReadStorage.launch(arrayOf(READ_EXTERNAL_STORAGE))
            }
        }

        private val permissionReadStorage by lazy {
            fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantResults ->
                val readResult = grantResults[READ_EXTERNAL_STORAGE]
                if (readResult == true) {
                    fragment.startFilePicker(mimeType, requestCode)
                } else {
                    fragment.binding.root.showSnackbar(
                        R.string.permission_read_denied,
                        Snackbar.LENGTH_SHORT
                    )
                }
            }
        }

        override fun PickiTonUriReturned() {
            Timber.d("PickiTonUriReturned")
            if (pickitProgressDialog == null || pickitProgressDialog?.isShowing == false) {
                pickitProgressDialog = ProgressDialog(fragment.requireContext()).apply {
                    setMessage(fragment.getString(R.string.pickit_waiting))
                    setCancelable(false)
                }
                pickitProgressDialog?.show()
            }
        }

        override fun PickiTonStartListener() {
            Timber.d("PickiTonStartListener")
            if (pickitProgressDialog?.isShowing == true) {
                pickitProgressDialog?.cancel()
            }
            pickitAlertDialog =
                AlertDialog.Builder(fragment.requireContext(), R.style.SyncFromCloudDialog).apply {
                    val view =
                        LayoutInflater.from(fragment.requireContext())
                            .inflate(R.layout.dialog_layout, null)
                    setView(view)
                    view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                        pickiT.cancelTask()
                        if (pickitAlertDialog?.isShowing == true) {
                            pickitAlertDialog?.cancel()
                        }
                    }
                    pickitProgressBar = view.findViewById(R.id.mProgressBar)
                }.create()
            pickitAlertDialog?.show()
            Timber.d("PickiTonStartListener")
        }

        override fun PickiTonProgressUpdate(progress: Int) {
            Timber.d("PickiTonProgressUpdate progress:$progress")
            pickitProgressBar?.progress = progress
        }

        override fun PickiTonCompleteListener(
            path: String?,
            wasDriveFile: Boolean,
            wasUnknownProvider: Boolean,
            wasSuccessful: Boolean,
            Reason: String?
        ) {
            Timber.d("PickiTonCompleteListener path:$path, wasDriveFile:$wasDriveFile, wasUnknownProvider:$wasUnknownProvider, wasSuccessful:$wasSuccessful, reason:$Reason")
            if (pickitAlertDialog?.isShowing == true) {
                pickitAlertDialog?.cancel()
            }
            if (pickitProgressDialog?.isShowing == true) {
                pickitProgressDialog?.dismiss()
            }
            if (BuildConfig.DEBUG) {
                when {
                    wasDriveFile -> fragment.toast(fragment.getString(R.string.pickit_drive))
                    wasUnknownProvider -> fragment.toast(fragment.getString(R.string.pickit_file_selected))
                    else -> fragment.toast(fragment.getString(R.string.pickit_local_file))
                }
            }
            when {
                wasSuccessful -> onFilePathReady(path)
                else -> fragment.toast("Error: $Reason")
            }
        }

        override fun PickiTonMultipleCompleteListener(
            paths: ArrayList<String>?,
            wasSuccessful: Boolean,
            Reason: String?
        ) {
            fragment.toast("Not implemented yet")
        }

        private fun onFilePathReady(filePath: String?) {
            if (filePath != null) {
                if (requestCode == FileConstants.REQUEST_PROFILE_IMAGE_CODE) {
                    vm.onPickedDocImageFromDevice(ctx, filePath)
                } else {
                    vm.onProceedWithFilePath(filePath = filePath)
                }
            } else {
                Timber.e("onFilePathReady, filePath is null")
            }
        }
    }
}

