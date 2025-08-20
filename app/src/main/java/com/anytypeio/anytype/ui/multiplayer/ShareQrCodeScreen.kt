package com.anytypeio.anytype.ui.multiplayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.features.SpaceIconView
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Header
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_utils.clipboard.copyPlainTextToClipboard
import com.anytypeio.anytype.presentation.spaces.SpaceSettingsViewModel
import com.anytypeio.anytype.presentation.spaces.UiEvent
import com.anytypeio.anytype.presentation.spaces.UiSpaceQrCodeState
import com.lightspark.composeqr.QrCodeView

@DefaultPreviews
@Composable
fun ShareQrCodeScreenPreview() {
    ShareQrCodeScreen(link = "https://github.com/anyproto/anytype-kotlin")
}

@Composable
fun ShareQrCodeScreen(link: String) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Dragger(Modifier.padding(vertical = 6.dp))
        Header(
            text = stringResource(id = R.string.space_settings_qrcode)
        )
        Spacer(modifier = Modifier.height(20.dp))
        QrCodeView(
            data = link,
            modifier = Modifier.size(200.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_anytype_qr_code_logo),
                contentDescription = "Anytype QR code logo"
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable {
                    context.copyPlainTextToClipboard(
                        plainText = link,
                        label = "Space invite link",
                        successToast = context.getString(R.string.space_invite_link_copied)
                    )
                }
                .padding(horizontal = 20.dp)
        ) {
            Image(
                modifier = Modifier.align(Alignment.CenterStart),
                painter = painterResource(R.drawable.ic_copy_24),
                contentDescription = "Copy icon"
            )
            Text(
                modifier = Modifier
                    .padding(start = 36.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterStart),
                text = link,
                color = colorResource(R.color.text_primary),
                style = BodyRegular,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareSpaceQrCodeScreen(viewModel: SpaceSettingsViewModel) {
    val qrCodeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    when (val state = viewModel.uiQrCodeState.collectAsStateWithLifecycle().value) {
        UiSpaceQrCodeState.Hidden -> return
        is UiSpaceQrCodeState.SpaceInvite -> {
            ModalBottomSheet(
                onDismissRequest = {
                    viewModel.onHideQrCodeScreen()
                },
                sheetState = qrCodeSheetState,
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
                            text = if (state.spaceName.isNotEmpty()) {
                                "${stringResource(R.string.space_settings_join_space_by_qr_title)} ${state.spaceName}"
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
                        data = state.link,
                        modifier = Modifier.size(200.dp)
                    ) {
                        if (state.icon != null) {
                            SpaceIconView(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                mainSize = 60.dp,
                                icon = state.icon!!
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
                        onClick = {
                            viewModel.onUiEvent(
                                UiEvent.OnShareLinkClicked(
                                    link = state.link
                                )
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}