package com.anytypeio.anytype.ui_settings.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.ui_settings.R

@Composable
fun AboutAppScreen(
    libraryVersion: String,
    anytypeId: String,
    version: String,
    onAnytypeIdClicked: () -> Unit
) {
    Column {
        Box(
            modifier = Modifier.padding(top = 6.dp).align(Alignment.CenterHorizontally)
        ) {
            Dragger()
        }
        Box(
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(
                top = 75.dp,
                bottom = 16.dp
            )
        ) {
            Text(
                text = stringResource(R.string.about),
                style = Title1,
                color = colorResource(R.color.text_primary)
            )
        }
        Row(
            modifier = Modifier.padding(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 12.dp
            )
        ) {
            Box(modifier = Modifier.weight(1.0f, true)) {
                Text(
                    text = stringResource(R.string.app_version),
                    style = BodyRegular,
                    color = colorResource(R.color.text_secondary)
                )
            }
            Box(
                modifier = Modifier.weight(2.0f, true),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = version,
                    style = BodyRegular,
                    color = colorResource(R.color.text_primary)
                )
            }
        }
        Row(
            modifier = Modifier.padding(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 12.dp
            )
        ) {
            Box(modifier = Modifier.weight(1.0f, true)) {
                Text(
                    text = stringResource(R.string.library),
                    style = BodyRegular,
                    color = colorResource(R.color.text_secondary)
                )
            }
            Box(
                modifier = Modifier.weight(2.0f, true),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = libraryVersion,
                    style = BodyRegular,
                    color = colorResource(R.color.text_primary)
                )
            }
        }
        Row(
            modifier = Modifier.padding(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 32.dp
            ).clickable(onClick = onAnytypeIdClicked)
        ) {
            Box(modifier = Modifier.weight(1.0f, true)) {
                Text(
                    text = stringResource(R.string.user_id),
                    style = BodyRegular,
                    color = colorResource(R.color.text_secondary)
                )
            }
            Box(
                modifier = Modifier.weight(2.0f, true),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = anytypeId,
                    style = BodyRegular,
                    color = colorResource(R.color.text_primary)
                )
            }
        }
    }
}