package com.anytypeio.anytype.ui.alert

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
import com.anytypeio.anytype.core_ui.foundation.AlertConfig
import com.anytypeio.anytype.core_ui.foundation.BUTTON_SECONDARY
import com.anytypeio.anytype.core_ui.foundation.GRADIENT_TYPE_RED
import com.anytypeio.anytype.core_ui.foundation.GenericAlert
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.ui.settings.typography

class AlertImportExperienceUnsupported : BaseBottomSheetComposeFragment() {

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
                        config = AlertConfig.WithOneButton(
                            title = stringResource(id = R.string.alert_gallery_unsupported_title),
                            description = stringResource(R.string.alert_gallery_unsupported_description),
                            firstButtonText = stringResource(R.string.button_close),
                            firstButtonType = BUTTON_SECONDARY,
                            icon = AlertConfig.Icon(
                                icon = R.drawable.ic_alert_error,
                                gradient = GRADIENT_TYPE_RED
                            )
                        )
                    )
                }
            }
        }
    }
}