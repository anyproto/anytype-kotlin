package com.anytypeio.anytype.ui.notifications

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManager
import com.anytypeio.anytype.ui.settings.typography
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class NotificationPermissionRequestDialog : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var permissionManager: NotificationPermissionManager

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                NotificationPermissionRequestContent(
                    onPermissionRequested = { permissionManager.onPermissionRequested() },
                    onPermissionGranted = { permissionManager.onPermissionGranted() },
                    onPermissionDenied = { permissionManager.onPermissionDenied() },
                    onPermissionDismissed = { permissionManager.onPermissionDismissed() },
                    onRequestPermissionsResult = { requestCode, permissions, grantResults ->
                        activity?.onRequestPermissionsResult(requestCode, permissions, grantResults)
                    }
                )
            }
        }
    }

    override fun injectDependencies() {
        componentManager().notificationPermissionComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().notificationPermissionComponent.release()
    }

    companion object {
        const val REQUEST_CODE = 0
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationPermissionRequestContent(
    onPermissionRequested: () -> Unit,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    onPermissionDismissed: () -> Unit,
    onRequestPermissionsResult: (Int, Array<String>, IntArray) -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        Timber.d("Permission granted: $isGranted")
        onRequestPermissionsResult(
            NotificationPermissionRequestDialog.REQUEST_CODE,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            if (isGranted)
                intArrayOf(PackageManager.PERMISSION_GRANTED)
            else
                intArrayOf(PackageManager.PERMISSION_DENIED)
        )
        scope.launch {
            bottomSheetState.hide()
        }.invokeOnCompletion {
            if (isGranted) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }
    }

    MaterialTheme(typography = typography) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    bottomSheetState.hide()
                }.invokeOnCompletion {
                    onPermissionDismissed()
                }
            },
            dragHandle = {},
            containerColor = colorResource(id = R.color.widget_background),
            sheetState = bottomSheetState
        ) {
            NotificationPermissionContent(
                onEnableNotifications = {
                    onPermissionRequested()
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                },
                onDismiss = {
                    scope.launch {
                        bottomSheetState.hide()
                    }.invokeOnCompletion {
                        onPermissionDismissed()
                    }
                }
            )
        }
    }
}

@Composable
private fun NotificationPermissionContent(
    onEnableNotifications: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(color = colorResource(id = R.color.widget_background)),
    ) {
        Image(
            modifier = Modifier.fillMaxWidth().height(232.dp),
            painter = painterResource(id = R.drawable.push_modal_illustration),
            contentDescription = "Push notifications illustration",
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.notifications_modal_title),
            style = HeadlineHeading,
            textAlign = TextAlign.Center,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.notifications_modal_description),
            style = Title2,
            textAlign = TextAlign.Center,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        ButtonPrimary(
            text = stringResource(R.string.notifications_modal_success_button),
            onClick = onEnableNotifications,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            size = ButtonSize.Large,
        )
        Spacer(modifier = Modifier.height(10.dp))
        ButtonSecondary(
            text = stringResource(R.string.notifications_modal_cancel_button),
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            size = ButtonSize.Large
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@DefaultPreviews
@Composable
fun NotificationPermissionRequestDialogPreview() {
    NotificationPermissionRequestContent(
        onPermissionRequested = {},
        onPermissionGranted = {},
        onPermissionDenied = {},
        onPermissionDismissed = {},
        onRequestPermissionsResult = { _, _, _ -> }
    )
}