package com.anytypeio.anytype.ui.onboarding.screens.signin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.NetworkMode
import com.anytypeio.anytype.core_models.NetworkModeConfig
import com.anytypeio.anytype.core_ui.NetworkSettingCardColor
import com.anytypeio.anytype.core_ui.NetworkSettingDescriptionColor
import com.anytypeio.anytype.core_ui.NetworkSettingTitleColor
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Toolbar
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.ui.spaces.Section

@Preview
@Composable
fun DefaultNetworkSetupScreenPreview() {
    NetworkSetupScreen(
        config = NetworkModeConfig(
            networkMode = NetworkMode.DEFAULT,
            userFilePath = null,
            storedFilePath = null
        ),
        onAnytypeNetworkClicked = {},
        onLocalOnlyClicked = {},
        onSelfHostNetworkClicked = {},
        onSetSelfHostConfigConfigClicked = {},
        onExportLogsClick = {}
    )
}

@Preview
@Composable
fun SelfHostNetworkSetupScreenPreview() {
    NetworkSetupScreen(
        config = NetworkModeConfig(
            networkMode = NetworkMode.CUSTOM,
            userFilePath = null,
            storedFilePath = null
        ),
        onAnytypeNetworkClicked = {},
        onLocalOnlyClicked = {},
        onSelfHostNetworkClicked = {},
        onSetSelfHostConfigConfigClicked = {},
        onExportLogsClick = {}
    )
}

@Preview
@Composable
fun SelfHostNetworkWithPathSetupScreenPreview() {
    NetworkSetupScreen(
        config = NetworkModeConfig(
            networkMode = NetworkMode.CUSTOM,
            userFilePath = null,
            storedFilePath = null
        ),
        onAnytypeNetworkClicked = {},
        onLocalOnlyClicked = {},
        onSelfHostNetworkClicked = {},
        onSetSelfHostConfigConfigClicked = {},
        onExportLogsClick = {}
    )
}

@Preview
@Composable
fun LocalNetworkWithPathSetupScreenPreview() {
    NetworkSetupScreen(
        config = NetworkModeConfig(
            networkMode = NetworkMode.LOCAL,
            userFilePath = null,
            storedFilePath = null
        ),
        onAnytypeNetworkClicked = {},
        onLocalOnlyClicked = {},
        onSelfHostNetworkClicked = {},
        onSetSelfHostConfigConfigClicked = {},
        onExportLogsClick = {}
    )
}

@Composable
fun NetworkSetupScreen(
    config: NetworkModeConfig,
    onLocalOnlyClicked: () -> Unit,
    onAnytypeNetworkClicked: () -> Unit,
    onSelfHostNetworkClicked: () -> Unit,
    onSetSelfHostConfigConfigClicked: () -> Unit,
    onExportLogsClick: () -> Unit,
    onUseYamuxToggled: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Dragger(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.CenterHorizontally),
            color = Color(0xffE3E3E3)
        )
        Toolbar(
            title = stringResource(id = R.string.network),
            color = colorResource(id = R.color.text_white)
        )
        AnytypeNetworkCard(onAnytypeNetworkClicked, config)
        Section(
            title = stringResource(id = R.string.network_settings_networks_section),
            color = NetworkSettingDescriptionColor,
            textPaddingStart = 0.dp
        )
        SelfHostCard(
            config = config,
            onSelfHostNetworkClicked = onSelfHostNetworkClicked,
            onProvideConfigClicked = onSetSelfHostConfigConfigClicked
        )
        LocalOnlyCard(onLocalOnlyClicked, config)
        Section(
            title = stringResource(id = R.string.network_settings_troubleshooting_section),
            color = NetworkSettingDescriptionColor,
            textPaddingStart = 0.dp
        )
        ExportLogs(onExportLogsClick = onExportLogsClick)
        Spacer(modifier = Modifier.height(24.dp))
        UseYamuxCard(config = config, onUseYamuxToggled = onUseYamuxToggled)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ExportLogs(onExportLogsClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .background(color = NetworkSettingCardColor)
            .noRippleClickable { onExportLogsClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                horizontal = 20.dp,
                vertical = 20.dp
            )
        ) {
            Text(
                text = stringResource(id = R.string.settings_share_local_logs),
                style = BodyCalloutRegular,
                color = NetworkSettingTitleColor,
                modifier = Modifier.weight(1.0f)
            )
        }
    }
}

@Composable
private fun UseYamuxCard(config: NetworkModeConfig, onUseYamuxToggled: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .background(color = NetworkSettingCardColor)
            .noRippleClickable { onUseYamuxToggled() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                horizontal = 20.dp,
                vertical = 20.dp
            )
        ) {
            Text(
                text = stringResource(id = R.string.settings_use_yamux),
                style = BodyCalloutRegular,
                color = NetworkSettingTitleColor,
                modifier = Modifier.weight(1.0f)
            )
            if (!config.useReserveMultiplexLib) {
                Image(
                    painter = painterResource(id = R.drawable.ic_network_settings_checked),
                    contentDescription = "enabled or disabled QUIC"
                )
            } else {
                Spacer(modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun LocalOnlyCard(
    onLocalOnlyClicked: () -> Unit,
    config: NetworkModeConfig
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .fillMaxWidth()
            .background(color = NetworkSettingCardColor)
            .clickable { onLocalOnlyClicked() }
    ) {
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.network_settings_local_only),
                style = BodyCalloutRegular,
                color = NetworkSettingTitleColor,
                modifier = Modifier.weight(1.0f)
            )
            if (config.networkMode == NetworkMode.LOCAL) {
                Image(
                    painter = painterResource(id = R.drawable.ic_network_settings_checked),
                    contentDescription = "Check icon"
                )
            } else {
                Spacer(modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.network_settings_local_only_description),
            style = BodyCalloutRegular,
            color = NetworkSettingDescriptionColor,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
private fun SelfHostCard(
    onSelfHostNetworkClicked: () -> Unit,
    onProvideConfigClicked: () -> Unit,
    config: NetworkModeConfig
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .fillMaxWidth()
            .background(color = NetworkSettingCardColor)
            .clickable { onSelfHostNetworkClicked() }
    ) {
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.network_settings_self_host),
                style = BodyCalloutRegular,
                color = NetworkSettingTitleColor,
                modifier = Modifier.weight(1.0f)
            )
            if (config.networkMode == NetworkMode.CUSTOM) {
                Image(
                    painter = painterResource(id = R.drawable.ic_network_settings_checked),
                    contentDescription = "Check icon"
                )
            } else {
                Spacer(modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.network_settings_self_host_description),
            style = BodyCalloutRegular,
            color = NetworkSettingDescriptionColor,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        if (config.networkMode == NetworkMode.CUSTOM) {
            Spacer(modifier = Modifier.height(8.dp))
            val configName = config.userFilePath
            if (configName.isNullOrEmpty()) {
                Text(
                    text = stringResource(R.string.network_settings_provide_config_hint),
                    style = Caption1Medium,
                    color = colorResource(id = R.color.palette_system_red),
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .noRippleClickable { onProvideConfigClicked() }
                )
            } else {
                Text(
                    text = stringResource(
                        id = R.string.network_settings_self_host_config,
                        configName
                    ),
                    style = Caption1Medium,
                    color = colorResource(id = R.color.palette_system_green),
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .noRippleClickable { onProvideConfigClicked() },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Divider(
            paddingStart = 20.dp,
            paddingEnd = 0.dp
        )
    }
}

@Composable
private fun AnytypeNetworkCard(
    onAnytypeNetworkClicked: () -> Unit,
    config: NetworkModeConfig
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onAnytypeNetworkClicked() }
            .background(color = NetworkSettingCardColor)
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.padding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.network_settings_anytype_network),
                style = BodyCalloutRegular,
                color = NetworkSettingTitleColor,
                modifier = Modifier.weight(1.0f)
            )
            if (config.networkMode == NetworkMode.DEFAULT) {
                Image(
                    painter = painterResource(id = R.drawable.ic_network_settings_checked),
                    contentDescription = "Check icon"
                )
            } else {
                Spacer(modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.network_settings_anytype_network_description),
            style = BodyCalloutRegular,
            color = NetworkSettingDescriptionColor
        )
        Spacer(modifier = Modifier.height(14.dp))
    }
}