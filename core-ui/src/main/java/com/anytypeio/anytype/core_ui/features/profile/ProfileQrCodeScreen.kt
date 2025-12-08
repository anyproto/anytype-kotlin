package com.anytypeio.anytype.core_ui.features.profile

import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingPrimaryLarge
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingSecondaryLarge
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.presentation.profile.UiProfileQrCodeState
import com.lightspark.composeqr.QrCodeView

/**
 * Bottom sheet screen for displaying the user's profile QR code.
 * When scanned, this QR code allows others to initiate a 1-1 chat with the user.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileQrCodeScreen(
    state: UiProfileQrCodeState,
    onShare: (String) -> Unit,
    onCopyLink: (String) -> Unit,
    onScanQrCode: () -> Unit,
    onDismiss: () -> Unit
) {
    when (state) {
        UiProfileQrCodeState.Hidden -> return
        is UiProfileQrCodeState.ProfileLink -> {
            ProfileQrCodeContent(
                link = state.link,
                globalName = state.globalName,
                identity = state.identity,
                onShare = onShare,
                onCopyLink = onCopyLink,
                onScanQrCode = onScanQrCode,
                onDismiss = onDismiss
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileQrCodeContent(
    link: String,
    globalName: String?,
    identity: String?,
    onShare: (String) -> Unit,
    onCopyLink: (String) -> Unit,
    onScanQrCode: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .navigationBarsPadding()
                .fillMaxSize()
                .background(
                    color = colorResource(id = R.color.background_secondary),
                    shape = RoundedCornerShape(20.dp),
                )
        ) {
            Dragger(
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(vertical = 6.dp)
            )

            // Toolbar
            QrCodeToolbar(
                globalName = globalName,
                onScanQrCode = onScanQrCode,
                identity = identity
            )

            // QR Code with circular text
            QrCodeWithCircularText(
                modifier = Modifier.align(Alignment.Center),
                link = link,
                circularText = stringResource(R.string.profile_qr_circular_text)
            )

            Column(
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                // Copy button (secondary) - at top
                ButtonOnboardingPrimaryLarge(
                    modifierBox = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth(),
                    text = stringResource(R.string.profile_qr_copy_button),
                    size = ButtonSize.Large,
                    onClick = { onCopyLink(link) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Share button (primary) - at bottom
                ButtonOnboardingSecondaryLarge(
                    modifierBox = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth(),
                    text = stringResource(R.string.profile_qr_share_button),
                    size = ButtonSize.Large,
                    onClick = { onShare(link) }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun QrCodeToolbar(
    globalName: String?,
    identity: String?,
    onScanQrCode: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp)
            .height(44.dp)
            .padding(horizontal = 4.dp),
    ) {

        GlobalNameOrIdentity(
            modifier = Modifier.align(Alignment.Center),
            globalName = globalName,
            identity = identity,
            onIdentityClicked = {}
        )

        // Scanner button - opens QR scanner
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(44.dp)
                .noRippleThrottledClickable {
                    onScanQrCode()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_qr_code_32),
                contentDescription = "Scan QR",
                tint = colorResource(id = R.color.text_primary)
            )
        }
    }
}

@Composable
private fun QrCodeWithCircularText(
    modifier: Modifier,
    link: String,
    circularText: String,
    qrSize: Dp = 206.dp,
    circularTextRadius: Dp = 148.dp
) {
    val density = LocalDensity.current
    val textColor = colorResource(id = R.color.text_transparent_secondary)
    val textColorInt = android.graphics.Color.argb(
        (textColor.alpha * 255).toInt(),
        (textColor.red * 255).toInt(),
        (textColor.green * 255).toInt(),
        (textColor.blue * 255).toInt()
    )

    Box(
        modifier = modifier.size(circularTextRadius * 2 + 20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Rounded square text around QR code
        Box(
            modifier = Modifier
                .size(circularTextRadius * 2)
                .drawBehind {
                    val radiusPx = circularTextRadius.toPx()
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    val cornerRadius = radiusPx * 0.2f

                    val path = Path().apply {
                        addRoundRect(
                            RectF(
                                centerX - radiusPx,
                                centerY - radiusPx,
                                centerX + radiusPx,
                                centerY + radiusPx
                            ),
                            cornerRadius,
                            cornerRadius,
                            Path.Direction.CW
                        )
                    }

                    val paint = Paint().apply {
                        color = textColorInt
                        textSize = with(density) { 11.sp.toPx() }
                        isAntiAlias = true
                        letterSpacing = 0.1f
                    }

                    // Repeat text to fill the rounded square
                    val repeatedText = circularText.repeat(8)

                    drawContext.canvas.nativeCanvas.drawTextOnPath(
                        repeatedText,
                        path,
                        0f,
                        0f,
                        paint
                    )
                }
        )

        // QR Code in center (no icon in center per design)
        QrCodeView(
            data = link,
            modifier = Modifier.size(qrSize)
        )
    }
}

@DefaultPreviews
@Composable
private fun PreviewProfileQrCodeContent() {
    ProfileQrCodeContent(
        link = "https://anytype.io/profile/johndoe",
        globalName = "",
        onShare = {},
        onCopyLink = {},
        onScanQrCode = {},
        onDismiss = {},
        identity = "AAsfud9shfuhdsufhsd8f9sdh9fhsh9sd"
    )
}
