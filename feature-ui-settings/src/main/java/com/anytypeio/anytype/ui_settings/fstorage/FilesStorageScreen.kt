package com.anytypeio.anytype.ui_settings.fstorage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.core_models.FileDownloadLimit
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingPrimaryLarge
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.ButtonWarning
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineTitleSemibold
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Regular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.presentation.settings.FilesStorageViewModel.ScreenState
import com.anytypeio.anytype.ui_settings.R
import com.anytypeio.anytype.ui_settings.fstorage.MockFileStorage.mockData

@Composable
fun LocalStorageScreen(
    data: ScreenState,
    downloadLimit: FileDownloadLimit,
    useCellular: Boolean,
    onOffloadFilesClicked: () -> Unit,
    onDeleteAccountClicked: () -> Unit,
    onOfflineDownloadsClicked: () -> Unit,
    onUseCellularToggled: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(rememberNestedScrollInteropConnection()),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = colorResource(id = R.color.background_primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .padding(vertical = 6.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Dragger()
            }
            Header(stringResource(id = R.string.local_storage))
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(
                            color = colorResource(id = R.color.shape_tertiary),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\uD83D\uDCF1",
                        style = TextStyle(fontSize = 56.sp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.device_storage_used),
                style = Caption1Medium,
                color = colorResource(id = R.color.text_secondary),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = data.localUsage,
                    style = HeadlineTitleSemibold,
                    color = colorResource(id = R.color.text_primary),
                    modifier = Modifier
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.in_order_to_save),
                style = BodyCalloutRegular,
                color = colorResource(id = R.color.text_primary),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ButtonOnboardingPrimaryLarge(
                    text = stringResource(id = R.string.offload_files),
                    onClick = onOffloadFilesClicked,
                    size = ButtonSize.Medium,
                    modifierBox = Modifier,
                )
            }
            Spacer(modifier = Modifier.height(50.dp))
            Text(
                text = stringResource(id = R.string.offline_downloads_section_title),
                style = Caption1Medium,
                color = colorResource(id = R.color.text_secondary)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OfflineDownloadsRow(
                currentLimit = downloadLimit,
                onClick = onOfflineDownloadsClicked
            )
            if (downloadLimit != FileDownloadLimit.OFF) {
                Spacer(modifier = Modifier.height(8.dp))
                UseCellularRow(
                    checked = useCellular,
                    onCheckedChange = onUseCellularToggled
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(id = R.string.danger_zone),
                style = Title1,
                color = colorResource(id = R.color.text_primary)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.deleted_account_danger_zone_msg),
                style = BodyCalloutRegular,
                color = colorResource(id = R.color.text_primary),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            ButtonWarning(
                text = stringResource(id = R.string.delete_account),
                onClick = onDeleteAccountClicked,
                size = ButtonSize.SmallSecondary.apply {
                    contentPadding = PaddingValues(12.dp, 7.dp, 12.dp, 7.dp)
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Header(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(top = 12.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = Title1,
            color = colorResource(id = R.color.text_primary)
        )
    }
}

@Composable
private fun OfflineDownloadsRow(
    currentLimit: FileDownloadLimit,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                shape = shape,
                width = 0.5.dp,
                color = colorResource(id = R.color.shape_primary)
            )
            .clip(shape)
            .clickable(onClick = onClick, role = Role.Button)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.offline_downloads_title),
                style = PreviewTitle1Regular,
                color = colorResource(id = R.color.text_primary)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.offline_downloads_subtitle),
                style = Caption1Regular,
                color = colorResource(id = R.color.text_secondary)
            )
        }
        Text(
            text = currentLimit.displayLabel(),
            style = PreviewTitle1Regular,
            color = colorResource(id = R.color.text_secondary)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Image(
            painter = painterResource(id = R.drawable.ic_disclosure_8_24),
            contentDescription = null,
            modifier = Modifier.wrapContentSize()
        )
    }
}

@Composable
private fun UseCellularRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                shape = shape,
                width = 0.5.dp,
                color = colorResource(id = R.color.shape_primary)
            )
            .clip(shape)
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.offline_downloads_use_cellular),
            style = PreviewTitle1Regular,
            color = colorResource(id = R.color.text_primary)
        )
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colorResource(id = R.color.white),
                uncheckedThumbColor = colorResource(id = R.color.white),
                checkedTrackColor = colorResource(id = R.color.palette_system_amber_50),
                uncheckedTrackColor = colorResource(id = R.color.shape_secondary),
                uncheckedBorderColor = Color.Transparent
            )
        )
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

object MockFileStorage {
    val mockSpaceInfraUsage = "212 MB of 1 GB used"
    val mockSpaceInfraPercent = 0.9F
    val mockDevice = "iPhone 13 Pro"
    val mockSpaceLocalUsage = "518 MB"
    val mockInfraMax = "14 GB"
    val mockData = ScreenState(
        spaceUsage = mockSpaceInfraUsage,
        percentUsage = mockSpaceInfraPercent,
        device = mockDevice,
        localUsage = mockSpaceLocalUsage,
        spaceLimit = mockInfraMax,
        isShowGetMoreSpace = true,
        isShowSpaceUsedWarning = true
    )
}

@Composable
@DefaultPreviews
fun PreviewLocalStorageScreen() {
    LocalStorageScreen(
        data = mockData,
        downloadLimit = FileDownloadLimit.MB_100,
        useCellular = false,
        onOffloadFilesClicked = {},
        onDeleteAccountClicked = {},
        onOfflineDownloadsClicked = {},
        onUseCellularToggled = {}
    )
}

@Composable
@DefaultPreviews
fun PreviewLocalStorageScreen_OffState() {
    LocalStorageScreen(
        data = mockData,
        downloadLimit = FileDownloadLimit.OFF,
        useCellular = false,
        onOffloadFilesClicked = {},
        onDeleteAccountClicked = {},
        onOfflineDownloadsClicked = {},
        onUseCellularToggled = {}
    )
}



