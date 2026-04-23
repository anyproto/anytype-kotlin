package com.anytypeio.anytype.ui_settings.fstorage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.FileDownloadLimit
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Regular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.ui_settings.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineDownloadsSelectorSheet(
    current: FileDownloadLimit,
    onValueSelected: (FileDownloadLimit) -> Unit,
    onDismiss: () -> Unit
) {
    val contentModifier = Modifier
        .statusBarsPadding()
        .fillMaxSize()
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = contentModifier,
        containerColor = colorResource(id = R.color.background_secondary),
        onDismissRequest = onDismiss,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = colorResource(id = R.color.background_primary),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Dragger(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 6.dp)
            )
            Text(
                text = stringResource(id = R.string.offline_downloads_title),
                style = Title1,
                color = colorResource(id = R.color.text_primary),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )
            FileDownloadLimit.entries.forEachIndexed { index, limit ->
                LimitOptionRow(
                    label = limit.displayLabel(),
                    isSelected = limit == current,
                    onClick = {
                        onValueSelected(limit)
                        onDismiss()
                    }
                )
                if (index < FileDownloadLimit.entries.lastIndex) {
                    Divider(
                        paddingStart = 16.dp,
                        paddingEnd = 16.dp,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun LimitOptionRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .noRippleThrottledClickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = label,
            style = PreviewTitle1Regular,
            color = colorResource(id = R.color.text_primary)
        )
        if (isSelected) {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = R.drawable.ic_check_black_14),
                contentDescription = null,
                contentScale = ContentScale.Inside
            )
        } else {
            Box(modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun FileDownloadLimit.displayLabel(): String = stringResource(
    when (this) {
        FileDownloadLimit.OFF -> R.string.offline_downloads_value_off
        FileDownloadLimit.MB_20 -> R.string.offline_downloads_value_20mb
        FileDownloadLimit.MB_100 -> R.string.offline_downloads_value_100mb
        FileDownloadLimit.MB_250 -> R.string.offline_downloads_value_250mb
        FileDownloadLimit.GB_1 -> R.string.offline_downloads_value_1gb
        FileDownloadLimit.UNLIMITED -> R.string.offline_downloads_value_unlimited
    }
)

@DefaultPreviews
@Composable
fun OfflineDownloadsSelectorSheetPreview() {
    OfflineDownloadsSelectorSheet(
        current = FileDownloadLimit.UNLIMITED,
        onValueSelected = {},
        onDismiss = {}
    )
}
