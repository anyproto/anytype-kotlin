package com.anytypeio.anytype.ui.multiplayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Header
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.lightspark.composeqr.QrCodeView

@DefaultPreviews
@Composable
fun ShareQrCodeScreenPreview() {
    ShareQrCodeScreen(link ="https://github.com/anyproto/anytype-kotlin")
}

@Composable
fun ShareQrCodeScreen(link: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
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
        ) {
            Image(
                modifier = Modifier.align(Alignment.CenterStart),
                painter = painterResource(R.drawable.ic_copy_24),
                contentDescription = "Copy icon"
            )
            Text(
                modifier = Modifier
                    .padding(start = 36.dp, end = 20.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterStart)
                ,
                text = link,
                color = colorResource(R.color.text_primary),
                style = BodyRegular,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}