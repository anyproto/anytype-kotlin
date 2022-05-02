package com.anytypeio.anytype.ui_settings.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.core_ui.foundation.Arrow
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Option
import com.anytypeio.anytype.core_ui.foundation.Toolbar
import com.anytypeio.anytype.ui_settings.R

@Composable
fun AccountAndDataScreen(
    onKeychainPhraseClicked: () -> Unit,
    onClearFileCachedClicked: () -> Unit,
    onDeleteAccountClicked: () -> Unit,
    onLogoutClicked: () -> Unit,
    isLogoutInProgress: Boolean,
    isClearCacheInProgress: Boolean
) {
    Column {
        Box(Modifier.padding(vertical = 6.dp).align(Alignment.CenterHorizontally)) {
            Dragger()
        }
        Toolbar(stringResource(R.string.account_and_data))
        Section(stringResource(R.string.access))
        Option(
            image = R.drawable.ic_keychain_phrase,
            text = stringResource(R.string.recovery_phrase),
            onClick = onKeychainPhraseClicked
        )
        Divider(paddingStart = 60.dp)
        Section(stringResource(R.string.data))
        ActionWithProgressBar(
            name = stringResource(R.string.clear_file_cache),
            color = colorResource(R.color.text_primary),
            onClick = onClearFileCachedClicked,
            isInProgress = isClearCacheInProgress
        )
        Divider()
        Section(stringResource(R.string.account))
        Action(
                name = stringResource(R.string.delete_account),
                color = colorResource(R.color.text_primary),
                onClick = onDeleteAccountClicked
        )
        Divider()
        ActionWithProgressBar(
                name = stringResource(R.string.log_out),
                color = colorResource(R.color.anytype_text_red),
                onClick = onLogoutClicked,
                isInProgress = isLogoutInProgress
        )
        Divider()
        Box(Modifier.height(54.dp))
    }
}

@Composable
fun Section(name: String) {
    Box(
        modifier = Modifier.height(52.dp).fillMaxWidth(),
        contentAlignment = Alignment.BottomStart
    ) {
        Text(
            text = name,
            fontSize = 13.sp,
            modifier = Modifier.padding(
                start = 20.dp,
                bottom = 8.dp
            ),
            color = colorResource(R.color.text_secondary)
        )
    }
}

@Composable
fun Pincode(
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(52.dp).clickable(onClick = onClick)
    ) {
        Image(
            painterResource(R.drawable.ic_pin_code),
            contentDescription = "Pincode icon",
            modifier = Modifier.padding(
                start = 20.dp
            )
        )
        Text(
            text = stringResource(R.string.pin_code),
            color = colorResource(R.color.text_primary),
            modifier = Modifier.padding(
                start = 12.dp
            )
        )
        Box(
            modifier = Modifier.weight(1.0f, true),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row {
                Text(
                    text = stringResource(R.string.off),
                    fontSize = 17.sp,
                    color = colorResource(R.color.text_secondary),
                    modifier = Modifier.padding(end = 10.dp)
                )
                Arrow()
            }
        }
    }
}

@Composable
fun Action(
    name: String,
    color: Color = Color.Unspecified,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier.height(52.dp).fillMaxWidth().clickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = name,
            color = color,
            fontSize = 17.sp,
            modifier = Modifier.padding(
                start = 20.dp
            )
        )
    }
}

@Composable
fun ActionWithProgressBar(
    name: String,
    color: Color = Color.Unspecified,
    onClick: () -> Unit = {},
    isInProgress: Boolean
) {
    Box(
        modifier = Modifier.height(52.dp).fillMaxWidth().clickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = name,
            color = color,
            fontSize = 17.sp,
            modifier = Modifier.padding(
                start = 20.dp
            )
        )
        if (isInProgress) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 20.dp).size(24.dp),
                color = colorResource(R.color.shape_secondary)
            )
        }
    }
}