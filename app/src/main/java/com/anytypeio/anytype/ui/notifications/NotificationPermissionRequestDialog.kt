package com.anytypeio.anytype.ui.notifications

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.Prompt
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.domain.notifications.NotificationPermissionManager
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
                val bottomSheetState = rememberModalBottomSheetState()
                val scope = rememberCoroutineScope()

                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->
                    Timber.d("Permission granted: $isGranted")
                    activity?.onRequestPermissionsResult(
                        REQUEST_CODE,
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
                            permissionManager.onPermissionGranted()
                        } else {
                            permissionManager.onPermissionDenied()
                        }
                    }
                }

                MaterialTheme(typography = typography) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            scope.launch {
                                bottomSheetState.hide()
                            }.invokeOnCompletion {
                                permissionManager.onPermissionDismissed()
                            }
                        },
                        dragHandle = {},
                        containerColor = colorResource(id = R.color.background_secondary),
                        sheetState = bottomSheetState
                    ) {
                        Prompt(
                            title = stringResource(R.string.notifications_prompt_get_notified),
                            description = stringResource(R.string.notifications_prompt_description),
                            primaryButtonText = stringResource(R.string.notifications_prompt_primary_button_text),
                            secondaryButtonText = stringResource(R.string.notifications_prompt_secondary_button_text),
                            onPrimaryButtonClicked = {
                                permissionManager.onPermissionRequested()
                                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            },
                            onSecondaryButtonClicked = {
                                scope.launch {
                                    bottomSheetState.hide()
                                }.invokeOnCompletion {
                                    permissionManager.onPermissionDismissed()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun injectDependencies() {
        // Do nothing
    }

    override fun releaseDependencies() {
        // Do nothing
    }

    companion object {
        const val REQUEST_CODE = 0
    }
} 