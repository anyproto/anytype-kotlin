package com.anytypeio.anytype.ui.editor

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.const.FileConstants
import com.anytypeio.anytype.core_utils.ext.Mimetype
import com.anytypeio.anytype.core_utils.ext.showSnackbar
import com.anytypeio.anytype.core_utils.ext.startFilePicker
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.other.MediaPermissionHelper
import com.anytypeio.anytype.presentation.util.CopyFileStatus
import com.google.android.material.snackbar.Snackbar
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import timber.log.Timber

interface PickerDelegate : PickiTCallbacks {

    fun initPicker(ctx: Id)

    fun resolveActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

    fun openFilePicker(mimeType: Mimetype, requestCode: Int? = null)

    fun clearPickit()

    fun deleteTemporaryFile()

    fun onCopyFileCommand(command: CopyFileStatus)

    fun clearOnCopyFile()

    sealed class Actions {
        data class OnStartCopyFileToCacheDir(val uri: Uri, val path: String? = null) : Actions()
        object OnCancelCopyFileToCacheDir : Actions()
        data class OnProceedWithFilePath(val filePath: String) : Actions()
        data class OnPickedDocImageFromDevice(val ctx: String, val filePath: String) : Actions()
    }

    class Impl(
        private val fragment: Fragment,
        private val actions: (Actions) -> Unit
    ) : PickerDelegate {

        private lateinit var ctx: Id
        private lateinit var pickiT: PickiT
        private lateinit var permissionHelper: MediaPermissionHelper

        private var pickitProgressDialog: ProgressDialog? = null
        private var pickitProgressBar: ProgressBar? = null
        private var pickitAlertDialog: AlertDialog? = null
        private var snackbar: Snackbar? = null

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
                        actions(Actions.OnStartCopyFileToCacheDir(uri))
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
                FileConstants.REQUEST_NETWORK_MODE_CODE -> {
                    data?.data?.let { uri ->
                        actions(Actions.OnStartCopyFileToCacheDir(uri))
                    } ?: run {
                        Timber.e("onActivityResult error, data is null")
                        fragment.toast("Error while getting file")
                    }
                }
                else -> {
                    Timber.e("onActivityResult error, Unknown Request Code:$requestCode")
                    fragment.toast("Unknown Request Code:$requestCode")
                }
            }
        }

        override fun openFilePicker(mimeType: Mimetype, requestCode: Int?) {
            try {
                permissionHelper.openFilePicker(mimeType, requestCode)
            } catch (e: Throwable) {
                Timber.e(e, "Error while opening file picker")
                fragment.toast("Error while opening file picker")
            }
        }

        override fun initPicker(ctx: Id) {
            this.ctx = ctx
            pickiT = PickiT(fragment.requireContext(), this, fragment.requireActivity())
            permissionHelper = MediaPermissionHelper(
                fragment = fragment,
                onPermissionDenied = { fragment.toast(R.string.permission_read_denied) },
                onPermissionSuccess = { mimetype, code -> fragment.startFilePicker(mimetype, code) }
            )
        }

        override fun clearPickit() {
            pickiT.cancelTask()
            pickitAlertDialog?.dismiss()
            pickitProgressDialog?.dismiss()
            pickitAlertDialog = null
            pickitProgressBar = null
            pickitProgressDialog = null
            snackbar?.dismiss()
            snackbar = null
        }

        fun onStop() {
            pickiT.cancelTask()
            pickitAlertDialog?.dismiss()
            pickitProgressDialog?.dismiss()
            snackbar?.dismiss()
        }

        override fun deleteTemporaryFile() {
            pickiT.deleteTemporaryFile(fragment.requireContext())
        }

        override fun onCopyFileCommand(command: CopyFileStatus) {

            when (command) {
                is CopyFileStatus.Error -> {
                    snackbar?.dismiss()
                    fragment.activity?.toast("Cancel loading file")
                }
                is CopyFileStatus.Completed -> {
                    snackbar?.dismiss()
                    onFilePathReady(command.result)
                }
                CopyFileStatus.Started -> {
                    fragment.view?.rootView?.let {
                        snackbar = it.showSnackbar(
                            R.string.loading_file,
                            Snackbar.LENGTH_INDEFINITE,
                            R.string.cancel
                        ) {
                            actions(Actions.OnCancelCopyFileToCacheDir)
                        }
                    }
                }
            }
        }

        override fun clearOnCopyFile() {
            actions(Actions.OnCancelCopyFileToCacheDir)
            snackbar?.dismiss()
            snackbar = null
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
                    view.findViewById<View>(R.id.btnCancel).setOnClickListener {
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
                    actions(Actions.OnPickedDocImageFromDevice(ctx, filePath))
                } else {
                    actions(Actions.OnProceedWithFilePath(filePath))
                }
            } else {
                Timber.e("onFilePathReady, filePath is null")
            }
        }
    }
}

