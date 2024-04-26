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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.Prompt
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.ui.settings.typography
import timber.log.Timber

class NotificationPermissionPromptDialog : BaseBottomSheetComposeFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
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
                    dismiss()
                }
                MaterialTheme(typography = typography) {
                    Prompt(
                        title = stringResource(R.string.notifications_prompt_get_notified),
                        description = stringResource(R.string.notifications_prompt_description),
                        primaryButtonText = stringResource(R.string.notifications_prompt_primary_button_text),
                        secondaryButtonText = stringResource(R.string.notifications_prompt_secondary_button_text),
                        onPrimaryButtonClicked = {
                            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        },
                        onSecondaryButtonClicked = {
                            dismiss()
                        }
                    )
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