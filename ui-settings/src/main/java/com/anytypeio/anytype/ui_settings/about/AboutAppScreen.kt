package com.anytypeio.anytype.ui_settings.about

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.ui_settings.R

@Composable
fun AboutAppScreen(
    vm: AboutAppViewModel,
    version: String
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
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
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
                    fontSize = 17.sp,
                    color = colorResource(R.color.text_secondary)
                )
            }
            Box(
                modifier = Modifier.weight(2.0f, true),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = version,
                    fontSize = 17.sp,
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
                    fontSize = 17.sp,
                    color = colorResource(R.color.text_secondary)
                )
            }
            Box(
                modifier = Modifier.weight(2.0f, true),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = vm.libraryVersion.collectAsState().value,
                    fontSize = 17.sp,
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
            )
        ) {
            Box(modifier = Modifier.weight(1.0f, true)) {
                Text(
                    text = stringResource(R.string.user_id),
                    fontSize = 17.sp,
                    color = colorResource(R.color.text_secondary)
                )
            }
            Box(
                modifier = Modifier.weight(2.0f, true),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = vm.userId.collectAsState().value,
                    fontSize = 17.sp,
                    color = colorResource(R.color.text_primary)
                )
            }
        }
    }
}