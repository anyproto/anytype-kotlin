package com.anytypeio.anytype.ui.publishtoweb

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Header
import com.anytypeio.anytype.core_ui.foundation.Section
import com.anytypeio.anytype.core_ui.views.BodyRegular

@Composable
fun PublishToWebScreen(
    domain: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Dragger()
        Header(
            text = stringResource(R.string.publish_to_web)
        )
        Section(
            title = stringResource(R.string.web_publishing_customize_url)
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    color = colorResource(R.color.transparent_tertiary),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth(),
                text = domain,
                style = BodyRegular,
                color = colorResource(R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(48.dp)
                .border(
                    width = 0.5.dp,
                    color = colorResource(R.color.shape_primary),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            BasicTextField(
                state = TextFieldState(),
                textStyle = BodyRegular
            )
        }
        Section(
            title = stringResource(R.string.preferences)
        )
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .height(52.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_plus_rounded_24),
                contentDescription = stringResource(R.string.content_description_plus_button)
            )
            Text(
                text = stringResource(R.string.web_publishing_join_space_button),
                style = BodyRegular,
                color = colorResource(R.color.text_primary),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            )
            Switch(
                checked = true,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colorResource(id = R.color.white),
                    checkedTrackColor = colorResource(id = R.color.color_accent),
                    checkedTrackAlpha = 1f,
                    uncheckedThumbColor = colorResource(id = R.color.white),
                    uncheckedTrackColor = colorResource(id = R.color.shape_secondary)
                ),
                onCheckedChange = {

                }
            )
        }
    }
}

@DefaultPreviews
@Composable
fun PublishToWebScreenPreview() {
    PublishToWebScreen(
        domain = "claude.any.org"
    )
}