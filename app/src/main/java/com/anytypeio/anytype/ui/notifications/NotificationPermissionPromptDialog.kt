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
import androidx.lifecycle.lifecycleScope
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import com.anytypeio.anytype.R
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary.allowPush
import com.anytypeio.anytype.analytics.base.EventsDictionary.AllowPushRoute
import com.anytypeio.anytype.analytics.base.EventsDictionary.clickAllowPush
import com.anytypeio.anytype.analytics.base.EventsDictionary.ClickAllowPushType
import com.anytypeio.anytype.analytics.base.EventsDictionary.screenAllowPush
import com.anytypeio.anytype.analytics.base.EventsDictionary.ScreenAllowPushType
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_ui.foundation.Prompt
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject
import kotlinx.coroutines.launch
import timber.log.Timber

class NotificationPermissionPromptDialog : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var analytics: Analytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            analytics.sendEvent(
                eventName = screenAllowPush,
                props = Props(
                    mapOf(EventsPropertiesKey.type to ScreenAllowPushType.INITIAL.value)
                )
            )
        }
    }

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
                    if (isGranted) {
                        lifecycleScope.launch {
                            analytics.sendEvent(
                                eventName = allowPush,
                                props = Props(
                                    mapOf(EventsPropertiesKey.route to AllowPushRoute.POPUP.value)
                                )
                            )
                        }
                    }
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
                            lifecycleScope.launch {
                                analytics.sendEvent(
                                    eventName = clickAllowPush,
                                    props = Props(
                                        mapOf(EventsPropertiesKey.type to ClickAllowPushType.ENABLE_NOTIFICATIONS.value)
                                    )
                                )
                            }
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
        componentManager().appPreferencesComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().appPreferencesComponent.release()
    }

    companion object {
        const val REQUEST_CODE = 0
    }
}