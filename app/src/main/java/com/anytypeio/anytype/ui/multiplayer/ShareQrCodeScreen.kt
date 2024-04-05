package com.anytypeio.anytype.ui.multiplayer

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.lightspark.composeqr.QrCodeView

@Preview
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
        Text(
            text = stringResource(id = R.string.multiplayer_share_space),
            style = HeadlineHeading,
            color = colorResource(id = R.color.text_primary)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.multiplayer_how_to_share_space_qr_code),
            color = colorResource(id = R.color.text_primary),
            style = BodyCalloutRegular
        )
        Spacer(modifier = Modifier.height(16.dp))
        QrCodeView(
            data = link,
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
    }
}