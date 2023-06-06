package com.anytypeio.anytype.ui_settings.fstorage

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.presentation.settings.FilesStorageViewModel.ScreenState
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.ui_settings.R
import com.anytypeio.anytype.ui_settings.fstorage.MockFileStorage.mockData
import com.anytypeio.anytype.ui_settings.main.SpaceImageBlock

@Composable
fun FilesStorageScreen(
    data: ScreenState,
    onOffloadFilesClicked: () -> Unit,
    onManageFilesClicked: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = colorResource(id = R.color.background_primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 20.dp),
        ) {
            Box(
                modifier = Modifier
                    .padding(vertical = 6.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Dragger()
            }
            Header()
            Spacer(
                modifier = Modifier.height(36.dp)
            )
            Text(
                text = stringResource(id = R.string.remote_storage),
                style = Title1,
                color = colorResource(id = R.color.text_primary),
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(id = R.string.you_can_store, data.spaceLimit),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                color = colorResource(R.color.text_primary),
                style = BodyCalloutRegular
            )
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier
                    .height(48.dp)
            ) {
                SpaceImageBlock(
                    icon = data.spaceIcon,
                    onSpaceIconClick = {},
                    mainSize = 48.dp,
                    emojiSize = 24.dp,
                    gradientSize = 36.dp
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(start = 12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = data.spaceName.orEmpty(),
                        style = PreviewTitle2Medium,
                        color = colorResource(id = R.color.text_primary),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = stringResource(
                            id = R.string.space_usage,
                            data.spaceUsage,
                            data.spaceLimit
                        ),
                        style = Relations3,
                        color = if (data.percentUsage != null && data.percentUsage!! >= 0.9F) {
                            colorResource(id = R.color.palette_system_red)
                        } else {
                            colorResource(id = R.color.text_secondary)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            data.percentUsage?.let {
                DefaultLinearProgressIndicator(progress = it)
            }
            Spacer(modifier = Modifier.height(20.dp))
            ButtonSecondary(
                text = stringResource(id = R.string.manage_files),
                onClick = onManageFilesClicked,
                size = ButtonSize.XSmall.apply {
                    contentPadding = PaddingValues(12.dp, 7.dp, 12.dp, 7.dp)
                }
            )
            Spacer(modifier = Modifier.height(44.dp))
            Text(
                text = stringResource(id = R.string.local_storage),
                style = Title1,
                color = colorResource(id = R.color.text_primary),
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(id = R.string.in_order_to_save),
                style = BodyCalloutRegular,
                color = colorResource(id = R.color.text_primary),
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier
                    .wrapContentHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = colorResource(id = R.color.shape_tertiary),
                            shape = RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\uD83D\uDCF1",
                        style = TextStyle(fontSize = 28.sp)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = data.device.orEmpty(),
                        style = PreviewTitle2Medium,
                        color = colorResource(id = R.color.text_primary),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = stringResource(
                            id = R.string.local_storage_used,
                            data.localUsage
                        ),
                        style = Relations3,
                        color = colorResource(id = R.color.text_secondary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            ButtonSecondary(
                text = stringResource(id = R.string.offload_files),
                onClick = onOffloadFilesClicked,
                size = ButtonSize.XSmall.apply {
                    contentPadding = PaddingValues(12.dp, 7.dp, 12.dp, 7.dp)
                }
            )
        }
    }
}

@Composable
fun DefaultLinearProgressIndicator(progress: Float) {
    val animatedProgress = animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    ).value
    LinearProgressIndicator(
        progress = animatedProgress,
        color = colorResource(id = R.color.text_primary),
        modifier = Modifier
            .height(6.dp)
            .fillMaxWidth(),
        backgroundColor = colorResource(id = R.color.shape_tertiary),
        strokeCap = StrokeCap.Round
    )
}

@Composable
private fun Header(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(top = 12.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.file_storage),
            style = Title1,
            color = colorResource(id = R.color.text_primary)
        )
    }
}

object MockFileStorage {
    val mockSpaceIcon = SpaceIconView.Gradient(from = "#EEDEAE", to = "#B93252")
    val mockSpaceName = "Anton’s space"
    val mockSpaceInfraUsage = "212 MB of 1 GB used"
    val mockSpaceInfraPercent = 0.9F
    val mockDevice = "iPhone 13 Pro"
    val mockSpaceLocalUsage = "518 MB used"
    val mockInfraMax = "14 GB"
    val mockData = ScreenState(
        spaceIcon = mockSpaceIcon,
        spaceName = mockSpaceName,
        spaceUsage = mockSpaceInfraUsage,
        percentUsage = mockSpaceInfraPercent,
        device = mockDevice,
        localUsage = mockSpaceLocalUsage,
        spaceLimit = mockInfraMax
    )
}

@Composable
@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
fun PreviewFilesStorageScreen() {
    FilesStorageScreen(
        data = mockData,
        onOffloadFilesClicked = { },
        onManageFilesClicked = { }
    )
}


