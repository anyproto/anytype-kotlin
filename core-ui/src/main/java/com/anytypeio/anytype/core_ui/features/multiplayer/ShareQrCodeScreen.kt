package com.anytypeio.anytype.core_ui.features.multiplayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.widgets.objectIcon.SpaceIconView
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.spaces.SpaceSettingsViewModel
import com.anytypeio.anytype.presentation.spaces.UiEvent
import com.anytypeio.anytype.presentation.spaces.UiSpaceQrCodeState
import com.lightspark.composeqr.QrCodeView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareSpaceQrCodeScreen(viewModel: SpaceSettingsViewModel) {
    when (val state = viewModel.uiQrCodeState.collectAsStateWithLifecycle().value) {
        UiSpaceQrCodeState.Hidden -> return
        is UiSpaceQrCodeState.SpaceInvite -> {
            QrCodeScreen(
                spaceName = state.spaceName,
                link = state.link,
                icon = state.icon,
                onShare = { viewModel.onUiEvent(UiEvent.OnShareLinkClicked(it)) },
                onDismiss = { viewModel.onHideQrCodeScreen() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareSpaceQrCodeScreen(
    qrCodeState: UiSpaceQrCodeState,
    onShareLinkClicked: (String) -> Unit,
    onHideQrCodeScreen: () -> Unit
) {
    when (qrCodeState) {
        UiSpaceQrCodeState.Hidden -> return
        is UiSpaceQrCodeState.SpaceInvite -> {
            QrCodeScreen(
                spaceName = qrCodeState.spaceName,
                link = qrCodeState.link,
                icon = qrCodeState.icon,
                onShare = onShareLinkClicked,
                onDismiss = onHideQrCodeScreen
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrCodeScreen(
    spaceName: String,
    link: String,
    icon: SpaceIconView?,
    onShare: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(bottom = 16.dp)
                .fillMaxWidth()
                .background(
                    color = colorResource(id = R.color.background_secondary),
                    shape = RoundedCornerShape(20.dp),
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Dragger(Modifier.padding(vertical = 6.dp))
            Box(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    text = if (spaceName.isNotEmpty()) {
                        "${stringResource(R.string.space_settings_join_space_by_qr_title)} $spaceName"
                    } else {
                        stringResource(id = R.string.space_settings_qrcode)
                    },
                    color = colorResource(id = R.color.text_primary),
                    style = Title1,
                    maxLines = 5,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            QrCodeView(
                data = link,
                modifier = Modifier.size(200.dp)
            ) {
                if (icon != null) {
                    SpaceIconView(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        mainSize = 60.dp,
                        icon = icon
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_anytype_qr_code_logo),
                        contentDescription = "Anytype QR code logo"
                    )
                }

            }
            Spacer(modifier = Modifier.height(20.dp))

            ButtonSecondary(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .align(
                        Alignment.CenterHorizontally
                    ),
                text = stringResource(R.string.multiplayer_share),
                size = ButtonSize.Large,
                onClick = { onShare(link) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}