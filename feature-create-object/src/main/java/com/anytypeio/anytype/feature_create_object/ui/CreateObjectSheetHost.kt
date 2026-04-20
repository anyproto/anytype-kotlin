package com.anytypeio.anytype.feature_create_object.ui

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_utils.ext.isVideo
import com.anytypeio.anytype.feature_create_object.presentation.CreateObjectAction
import com.anytypeio.anytype.feature_create_object.presentation.CreateObjectUploadTarget
import com.anytypeio.anytype.feature_create_object.presentation.NewCreateObjectViewModel
import java.io.File
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Reusable host for the global Create-object popup.
 *
 * This composable wires up everything a screen needs to surface the full
 * [CreateObjectPopup]: the visibility/state plumbing, the four
 * `ActivityResult` launchers for Photos / Camera / Files uploads, the
 * permission launcher for camera capture, and the action dispatch that
 * routes media picks into [vm]'s upload and type picks into
 * [onCreateObjectOfType]. Hosts that don't need uploads can simply pass a
 * [NewCreateObjectViewModel] whose `VmParams.showMediaSection` is false —
 * the media rows will stay hidden and the launchers remain inert.
 */
@Composable
fun CreateObjectSheetHost(
    vm: NewCreateObjectViewModel,
    visible: Boolean,
    onDismiss: () -> Unit,
    onCreateObjectOfType: (ObjectWrapper.Type) -> Unit,
    onAttachExistingObject: () -> Unit = {}
) {
    val state = vm.state.collectAsStateWithLifecycle().value

    LaunchedEffect(visible) {
        if (visible) vm.onOpen()
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val uploadMediaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        vm.uploadFiles(
            uris.map { uri ->
                val type = if (isVideo(uri, context)) {
                    Block.Content.File.Type.VIDEO
                } else {
                    Block.Content.File.Type.IMAGE
                }
                CreateObjectUploadTarget(uri.toString(), type)
            }
        )
        onDismiss()
    }

    val uploadFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        vm.uploadFiles(
            uris.map { uri ->
                CreateObjectUploadTarget(
                    uri = uri.toString(),
                    type = Block.Content.File.Type.NONE
                )
            }
        )
        onDismiss()
    }

    var capturedPhotoUri by rememberSaveable { mutableStateOf<String?>(null) }
    var capturedPhotoPath by rememberSaveable { mutableStateOf<String?>(null) }

    val takePhotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        val uri = capturedPhotoUri
        val sourcePath = capturedPhotoPath
        if (isSuccess && uri != null) {
            vm.uploadFiles(
                listOf(
                    CreateObjectUploadTarget(
                        uri = uri,
                        type = Block.Content.File.Type.IMAGE,
                        sourceFilePath = sourcePath
                    )
                )
            )
            onDismiss()
        } else if (sourcePath != null) {
            // Capture cancelled/failed — still clean up the empty temp file.
            runCatching { File(sourcePath).delete() }
        }
        capturedPhotoUri = null
        capturedPhotoPath = null
    }

    val takePhotoPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCameraForCreateObjectUpload(
                context = context,
                launcher = takePhotoLauncher,
                onPhotoReady = { uri, file ->
                    capturedPhotoUri = uri.toString()
                    capturedPhotoPath = file.absolutePath
                }
            )
        } else {
            Timber.w("Camera permission denied for create-object upload")
        }
    }

    CreateObjectPopup(
        expanded = visible,
        onDismissRequest = onDismiss,
        state = state,
        onAction = { action ->
            when (action) {
                CreateObjectAction.SelectPhotos -> {
                    uploadMediaLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageAndVideo
                        )
                    )
                }
                CreateObjectAction.TakePhoto -> {
                    takePhotoPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
                CreateObjectAction.SelectFiles -> {
                    uploadFileLauncher.launch(arrayOf("*/*"))
                }
                CreateObjectAction.AttachExistingObject -> {
                    onAttachExistingObject()
                }
                is CreateObjectAction.CreateObjectOfType -> {
                    scope.launch {
                        val type = vm.resolveType(TypeKey(action.typeKey))
                        if (type != null) {
                            onCreateObjectOfType(type)
                        } else {
                            Timber.w("Create-object: type ${action.typeKey} not in store")
                        }
                        onDismiss()
                    }
                }
                is CreateObjectAction.UpdateSearch,
                is CreateObjectAction.Retry -> {
                    vm.onAction(action)
                }
            }
        }
    )
}

/**
 * Writes a temp JPEG into the app cache, gives the launcher the FileProvider URI,
 * and reports the URI back via [onPhotoReady] so callers can upload on capture.
 */
internal fun launchCameraForCreateObjectUpload(
    context: Context,
    launcher: ManagedActivityResultLauncher<Uri, Boolean>,
    onPhotoReady: (uri: Uri, file: File) -> Unit
) {
    val tempDir = File(context.cacheDir, CREATE_OBJECT_UPLOAD_TEMP_FOLDER)
    if (!tempDir.exists()) tempDir.mkdirs()
    val photoFile = File.createTempFile("IMG_", ".jpg", tempDir)
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        photoFile
    )
    onPhotoReady(uri, photoFile)
    launcher.launch(uri)
}

private const val CREATE_OBJECT_UPLOAD_TEMP_FOLDER = "create_object_upload_temp_folder"
