package com.anytypeio.anytype.ui.alert

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_ui.extensions.toast
import com.anytypeio.anytype.core_ui.foundation.AlertConfig
import com.anytypeio.anytype.core_ui.foundation.BUTTON_PRIMARY
import com.anytypeio.anytype.core_ui.foundation.BUTTON_SECONDARY
import com.anytypeio.anytype.core_ui.foundation.GRADIENT_TYPE_GREEN
import com.anytypeio.anytype.core_ui.foundation.GenericAlert
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.ui.settings.typography

class AlertUpdateAppFragment : BaseBottomSheetComposeFragment() {

    lateinit var onCancel: () -> Unit

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    GenericAlert(
                        onFirstButtonClicked = throttledClick(
                            onClick = { dismiss() }
                        ),
                        onSecondButtonClicked = throttledClick(
                            onClick = { proceedWithUpdateIntent() }
                        ),
                        config = AlertConfig.WithTwoButtons(
                            title = stringResource(id = R.string.anytype_update_alert_title),
                            description = stringResource(R.string.anytype_update_alert_description),
                            firstButtonText = stringResource(R.string.close),
                            secondButtonText = stringResource(R.string.update),
                            secondButtonType = BUTTON_PRIMARY,
                            firstButtonType = BUTTON_SECONDARY,
                            icon = AlertConfig.Icon(
                                icon = R.drawable.ic_alert_update,
                                gradient = GRADIENT_TYPE_GREEN
                            )
                        )
                    )
                }
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        onCancel()
        super.onDismiss(dialog)
    }

    private fun proceedWithUpdateIntent() {
        try {
            val downloadUrl = getString(R.string.download_anytype_url)
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(downloadUrl)
            )
            startActivity(intent)
        } catch (e: Exception) {
            context?.toast(getString(R.string.generic_error))
        }
    }

    override fun injectDependencies() {
        // Do nothing
    }

    override fun releaseDependencies() {
        // Do nothing
    }
}