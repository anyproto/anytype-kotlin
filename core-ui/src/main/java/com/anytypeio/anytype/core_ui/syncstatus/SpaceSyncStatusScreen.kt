package com.anytypeio.anytype.core_ui.syncstatus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Text
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.multiplayer.P2PStatus
import com.anytypeio.anytype.core_models.multiplayer.P2PStatusUpdate
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncError
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncNetwork
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncUpdate
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_ui.widgets.DragStates
import com.anytypeio.anytype.presentation.sync.SyncStatusWidgetState
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SpaceSyncStatusScreen(
    modifier: Modifier = Modifier,
    uiState: SyncStatusWidgetState,
    onDismiss: () -> Unit,
    scope: CoroutineScope,
    onUpdateAppClick: () -> Unit
) {
    val isVisible = uiState is SyncStatusWidgetState.Success || uiState is SyncStatusWidgetState.Error

    val swappableState = rememberSwipeableState(DragStates.VISIBLE)
    if (swappableState.isAnimationRunning && swappableState.targetValue == DragStates.DISMISSED) {
        DisposableEffect(Unit) {
            onDispose {
                onDismiss()
            }
        }
    }

    if (!isVisible) {
        DisposableEffect(Unit) {
            onDispose {
                scope.launch { swappableState.snapTo(DragStates.VISIBLE) }
            }
        }
    }

    val sizePx = with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = Modifier
            .swipeable(
                state = swappableState,
                orientation = Orientation.Vertical,
                anchors = mapOf(0f to DragStates.VISIBLE, sizePx to DragStates.DISMISSED),
                thresholds = { _, _ -> FractionalThreshold(0.3f) })
            .offset { IntOffset(0, swappableState.offset.value.roundToInt()) }
    ) {
        ElevatedCard(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = colorResource(id = R.color.background_secondary)
            ),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 126.dp
            )
        ) {
            Spacer(modifier = Modifier.height(6.dp))
            Dragger(modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(6.dp))
            when (uiState) {
                is SyncStatusWidgetState.Error -> ErrorState()
                SyncStatusWidgetState.Hidden -> LoadingState()
                is SyncStatusWidgetState.Success -> SuccessState(
                    spaceSyncUpdate = uiState.spaceSyncUpdate,
                    p2pStatus = uiState.p2PStatusUpdate,
                    onUpdateAppClick = onUpdateAppClick
                )
            }
        }
    }
}

enum class MenuVisibilityState(val fraction: Float) { Hidden(0f), Visible(1f) }

@Composable
private fun ColumnScope.LoadingState() {
    CircularProgressIndicator(
        modifier = Modifier
            .size(24.dp)
            .padding(bottom = 16.dp)
            .align(Alignment.CenterHorizontally),
        color = colorResource(R.color.shape_secondary)
    )
}

@Composable
private fun ColumnScope.ErrorState() {
    Text(
        text = stringResource(id = R.string.sync_status_get_error),
        style = BodyRegular,
        color = colorResource(R.color.palette_dark_red),
        modifier = Modifier
            .padding(8.dp)
            .align(Alignment.CenterHorizontally)
    )
}

@Composable
private fun SuccessState(
    spaceSyncUpdate: SpaceSyncUpdate,
    p2pStatus: P2PStatusUpdate,
    onUpdateAppClick: () -> Unit
) {
    if (spaceSyncUpdate is SpaceSyncUpdate.Update) {
        SpaceSyncStatusItem(spaceSyncUpdate, onUpdateAppClick)
        Divider()
    }
    if (p2pStatus is P2PStatusUpdate.Update) {
        P2PStatusItem(p2pStatus)
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun P2PStatusItem(
    p2pStatus: P2PStatusUpdate.Update
) {
    val networkCardSettings = getP2PCardSettings(p2pStatus = p2pStatus)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier.wrapContentSize(),
            painter = networkCardSettings.icon,
            contentDescription = "p2p status icon",
            alpha = networkCardSettings.alpha
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp)
        ) {
            Text(
                text = networkCardSettings.mainText,
                style = Title2,
                color = colorResource(R.color.text_primary)
            )
            if (networkCardSettings.secondaryText != null) {
                Text(
                    text = networkCardSettings.secondaryText,
                    style = Relations3,
                    color = colorResource(R.color.text_secondary)
                )
            }
        }
    }
}

@Composable
private fun SpaceSyncStatusItem(
    spaceSyncUpdate: SpaceSyncUpdate.Update,
    onUpdateAppClick: () -> Unit = {}
) {
    val networkCardSettings = getNetworkCardSettings(
        syncStatus = spaceSyncUpdate.status,
        network = spaceSyncUpdate.network,
        error = spaceSyncUpdate.error,
        syncingObjectsCounter = spaceSyncUpdate.syncingObjectsCounter
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .noRippleClickable {
                if (spaceSyncUpdate.status == SpaceSyncStatus.NETWORK_UPDATE_NEEDED) {
                    onUpdateAppClick()
                }
            }
    ) {
        Image(
            modifier = Modifier
                .wrapContentSize()
                .padding(start = 16.dp)
                .align(Alignment.CenterStart),
            painter = networkCardSettings.icon,
            contentDescription = "sync status icon",
            alpha = networkCardSettings.alpha
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 76.dp)
                .align(Alignment.CenterStart)
        ) {
            Text(
                text = networkCardSettings.mainText,
                style = Title2,
                color = colorResource(R.color.text_primary)
            )
            if (networkCardSettings.secondaryText != null) {
                Text(
                    text = networkCardSettings.secondaryText,
                    style = Relations3,
                    color = colorResource(R.color.text_secondary)
                )
            }
        }
        if (spaceSyncUpdate.status == SpaceSyncStatus.NETWORK_UPDATE_NEEDED
            && spaceSyncUpdate.error == SpaceSyncError.NULL) {
            Image(
                modifier = Modifier
                    .padding(end = 20.dp)
                    .wrapContentSize()
                    .align(Alignment.CenterEnd),
                painter = painterResource(id = R.drawable.ic_arrow_top_end),
                contentDescription = "update app icon"
            )
        }
    }
}

@Composable
private fun getP2PCardSettings(
    p2pStatus: P2PStatusUpdate.Update
): CardSettings {
    return when (p2pStatus.status) {
        P2PStatus.NOT_CONNECTED -> {
            CardSettings(
                icon = painterResource(R.drawable.ic_sync_p2p_default),
                mainText = stringResource(id = R.string.sync_status_p2p),
                secondaryText = stringResource(id = R.string.sync_status_p2p_not_connected)
            )
        }

        P2PStatus.NOT_POSSIBLE, P2PStatus.RESTRICTED -> {
            CardSettings(
                icon = painterResource(R.drawable.ic_sync_p2p_default),
                mainText = stringResource(id = R.string.sync_status_p2p),
                secondaryText = stringResource(id = R.string.sync_status_p2p_disabled)
            )
        }

        P2PStatus.CONNECTED -> {
            CardSettings(
                icon = painterResource(R.drawable.ic_sync_p2p_connected),
                mainText = stringResource(id = R.string.sync_status_p2p),
                secondaryText = pluralStringResource(
                    id = R.plurals.sync_status_p2p_devices,
                    count = p2pStatus.devicesCounter.toInt(),
                    formatArgs = arrayOf(p2pStatus.devicesCounter.toInt())
                )
            )
        }
        P2PStatus.RESTRICTED -> {
            CardSettings(
                icon = painterResource(R.drawable.ic_sync_p2p_error),
                mainText = stringResource(id = R.string.sync_status_p2p),
                secondaryText = stringResource(id = R.string.sync_status_p2p_disabled)
            )
        }
    }
}

@Composable
private fun getNetworkCardSettings(
    syncStatus: SpaceSyncStatus,
    network: SpaceSyncNetwork,
    error: SpaceSyncError,
    syncingObjectsCounter: Long
): CardSettings {
    when (network) {
        SpaceSyncNetwork.ANYTYPE -> {

            if (error != SpaceSyncError.NULL) {
                return CardSettings(
                    icon = painterResource(R.drawable.ic_sync_net_error),
                    mainText = stringResource(id = R.string.sync_status_anytype_network),
                    secondaryText = stringResource(id = getErrorText(error))
                )
            }

            return when (syncStatus) {
                SpaceSyncStatus.SYNCED -> {
                    CardSettings(
                        icon = painterResource(R.drawable.ic_sync_net_connected),
                        mainText = stringResource(id = R.string.sync_status_anytype_network),
                        secondaryText = stringResource(id = R.string.sync_status_anytype_end_to_end)
                    )
                }

                SpaceSyncStatus.SYNCING -> {
                    CardSettings(
                        icon = painterResource(R.drawable.ic_sync_net_connected),
                        alpha = 0.5f,
                        withAnimation = true,
                        mainText = stringResource(id = R.string.sync_status_anytype_network),
                        secondaryText = pluralStringResource(
                            id = R.plurals.sync_status_network_items,
                            count = syncingObjectsCounter.toInt(),
                            formatArgs = arrayOf(syncingObjectsCounter.toInt())
                        )
                    )
                }

                SpaceSyncStatus.ERROR -> {
                    val errorText = getErrorText(error)
                    CardSettings(
                        icon = painterResource(R.drawable.ic_sync_net_error),
                        mainText = stringResource(id = R.string.sync_status_anytype_network),
                        secondaryText = stringResource(id = errorText)
                    )
                }

                SpaceSyncStatus.OFFLINE -> {
                    CardSettings(
                        icon = painterResource(R.drawable.ic_sync_net_default),
                        mainText = stringResource(id = R.string.sync_status_anytype_network),
                        secondaryText = stringResource(id = R.string.sync_status_anytype_network_no_connecting)
                    )
                }

                SpaceSyncStatus.NETWORK_UPDATE_NEEDED -> {
                    CardSettings(
                        icon = painterResource(R.drawable.ic_sync_limitations),
                        mainText = stringResource(id = R.string.sync_status_anytype_network),
                        secondaryText = stringResource(id = R.string.sync_status_anytype_sync_slow)
                    )
                }
            }
        }

        SpaceSyncNetwork.SELF_HOST -> {

            if (error != SpaceSyncError.NULL) {
                return CardSettings(
                    icon = painterResource(R.drawable.ic_sync_self_error),
                    mainText = stringResource(id = R.string.sync_status_self_host),
                    secondaryText = stringResource(id = getErrorText(error))
                )
            }

            return when (syncStatus) {
                SpaceSyncStatus.SYNCED -> {
                    CardSettings(
                        icon = painterResource(R.drawable.ic_sync_self_connected),
                        mainText = stringResource(id = R.string.sync_status_self_host),
                        secondaryText = stringResource(id = R.string.sync_status_self_host_synced)
                    )
                }

                SpaceSyncStatus.SYNCING -> {
                    CardSettings(
                        icon = painterResource(R.drawable.ic_sync_self_connected),
                        alpha = 0.5f,
                        withAnimation = true,
                        mainText = stringResource(id = R.string.sync_status_self_host),
                        secondaryText = pluralStringResource(
                            id = R.plurals.sync_status_self_host_syncing,
                            count = syncingObjectsCounter.toInt(),
                            formatArgs = arrayOf(syncingObjectsCounter.toInt())
                        )
                    )
                }

                SpaceSyncStatus.ERROR -> {
                    val errorText = getErrorText(error)
                    CardSettings(
                        icon = painterResource(R.drawable.ic_sync_self_error),
                        mainText = stringResource(id = R.string.sync_status_self_host),
                        secondaryText = stringResource(id = errorText)
                    )
                }

                SpaceSyncStatus.OFFLINE -> {
                    CardSettings(
                        icon = painterResource(R.drawable.ic_sync_self_default),
                        mainText = stringResource(id = R.string.sync_status_self_host),
                        secondaryText = stringResource(id = R.string.sync_status_anytype_network_no_connecting)
                    )
                }
                SpaceSyncStatus.NETWORK_UPDATE_NEEDED -> {
                    CardSettings(
                        icon = painterResource(R.drawable.ic_sync_limitations),
                        mainText = stringResource(id = R.string.sync_status_self_host),
                        secondaryText = stringResource(id = R.string.sync_status_anytype_sync_slow)
                    )
                }
            }
        }

        SpaceSyncNetwork.LOCAL_ONLY -> {

            if (error != SpaceSyncError.NULL) {
                return CardSettings(
                    icon = painterResource(R.drawable.ic_sync_net_error),
                    mainText = stringResource(id = R.string.sync_status_local_only_title),
                    secondaryText = stringResource(id = getErrorText(error))
                )
            }

            return CardSettings(
                icon = painterResource(R.drawable.ic_sync_local_only),
                mainText = stringResource(id = R.string.sync_status_local_only_title),
                secondaryText = stringResource(id = R.string.sync_status_data_backup)
            )
        }
    }
}

private fun getErrorText(error: SpaceSyncError): Int {
    return when (error) {
        SpaceSyncError.NULL -> R.string.sync_status_unrecognized
        SpaceSyncError.STORAGE_LIMIT_EXCEED -> R.string.sync_status_storage_limit_exceed
        SpaceSyncError.INCOMPATIBLE_VERSION -> R.string.sync_status_incompatible_version
        SpaceSyncError.NETWORK_ERROR -> R.string.sync_status_network_error
    }
}

private data class CardSettings(
    val icon: Painter,
    val alpha: Float = 1f,
    val withAnimation: Boolean = false,
    val mainText: String,
    val secondaryText: String? = null
)

@Preview(name = "AnytypeNetworkSynced", showBackground = true)
@Composable
fun SpaceSyncStatusPreview1() {
    val spaceSyncUpdate = SpaceSyncUpdate.Update(
        id = "1",
        status = SpaceSyncStatus.SYNCED,
        network = SpaceSyncNetwork.ANYTYPE,
        error = SpaceSyncError.NULL,
        syncingObjectsCounter = 0
    )
    SpaceSyncStatusItem(spaceSyncUpdate = spaceSyncUpdate)
}

@Preview(name = "AnytypeNetworkSyncing", showBackground = true)
@Composable
fun SpaceSyncStatusPreview2() {
    val spaceSyncUpdate = SpaceSyncUpdate.Update(
        id = "1",
        status = SpaceSyncStatus.SYNCING,
        network = SpaceSyncNetwork.ANYTYPE,
        error = SpaceSyncError.NULL,
        syncingObjectsCounter = 2
    )
    SpaceSyncStatusItem(spaceSyncUpdate = spaceSyncUpdate)
}

@Preview(name = "AnytypeNetworkError", showBackground = true)
@Composable
fun SpaceSyncStatusPreview3() {
    val spaceSyncUpdate = SpaceSyncUpdate.Update(
        id = "1",
        status = SpaceSyncStatus.ERROR,
        network = SpaceSyncNetwork.ANYTYPE,
        error = SpaceSyncError.NETWORK_ERROR,
        syncingObjectsCounter = 0
    )
    SpaceSyncStatusItem(spaceSyncUpdate = spaceSyncUpdate)
}

@Preview(name = "AnytypeNetworkOffline", showBackground = true)
@Composable
fun SpaceSyncStatusPreview4() {
    val spaceSyncUpdate = SpaceSyncUpdate.Update(
        id = "1",
        status = SpaceSyncStatus.OFFLINE,
        network = SpaceSyncNetwork.ANYTYPE,
        error = SpaceSyncError.NULL,
        syncingObjectsCounter = 0
    )
    SpaceSyncStatusItem(spaceSyncUpdate = spaceSyncUpdate)
}

@Preview(name = "SelfHostSynced", showBackground = true)
@Composable
fun SpaceSyncStatusPreview5() {
    val spaceSyncUpdate = SpaceSyncUpdate.Update(
        id = "1",
        status = SpaceSyncStatus.SYNCED,
        network = SpaceSyncNetwork.SELF_HOST,
        error = SpaceSyncError.NULL,
        syncingObjectsCounter = 0
    )
    SpaceSyncStatusItem(spaceSyncUpdate = spaceSyncUpdate)
}

@Preview(name = "SelfHostSyncing", showBackground = true)
@Composable
fun SpaceSyncStatusPreview6() {
    val spaceSyncUpdate = SpaceSyncUpdate.Update(
        id = "1",
        status = SpaceSyncStatus.SYNCING,
        network = SpaceSyncNetwork.SELF_HOST,
        error = SpaceSyncError.NULL,
        syncingObjectsCounter = 2
    )
    SpaceSyncStatusItem(spaceSyncUpdate = spaceSyncUpdate)
}

@Preview(name = "SelfHostError", showBackground = true)
@Composable
fun SpaceSyncStatusPreview7() {
    val spaceSyncUpdate = SpaceSyncUpdate.Update(
        id = "1",
        status = SpaceSyncStatus.ERROR,
        network = SpaceSyncNetwork.SELF_HOST,
        error = SpaceSyncError.NETWORK_ERROR,
        syncingObjectsCounter = 0
    )
    SpaceSyncStatusItem(spaceSyncUpdate = spaceSyncUpdate)
}

@Preview(name = "SelfHostOffline", showBackground = true)
@Composable
fun SpaceSyncStatusPreview8() {
    val spaceSyncUpdate = SpaceSyncUpdate.Update(
        id = "1",
        status = SpaceSyncStatus.OFFLINE,
        network = SpaceSyncNetwork.SELF_HOST,
        error = SpaceSyncError.NULL,
        syncingObjectsCounter = 0
    )
    SpaceSyncStatusItem(spaceSyncUpdate = spaceSyncUpdate)
}

@Preview(name = "LocalOnly", showBackground = true)
@Composable
fun SpaceSyncStatusPreview9() {
    val spaceSyncUpdate = SpaceSyncUpdate.Update(
        id = "1",
        status = SpaceSyncStatus.SYNCING,
        network = SpaceSyncNetwork.LOCAL_ONLY,
        error = SpaceSyncError.NULL,
        syncingObjectsCounter = 0
    )
    SpaceSyncStatusItem(spaceSyncUpdate = spaceSyncUpdate)
}

@Preview(name = "P2PNotConnected", showBackground = true)
@Composable
fun SpaceSyncStatusPreview10() {
    val p2pStatus = P2PStatusUpdate.Update(
        status = P2PStatus.NOT_CONNECTED,
        devicesCounter = 0,
        spaceId = "1"
    )
    P2PStatusItem(p2pStatus = p2pStatus)
}

@Preview(name = "P2PNotPossible and P2PRestricted", showBackground = true)
@Composable
fun SpaceSyncStatusPreview11() {
    val p2pStatus = P2PStatusUpdate.Update(
        status = P2PStatus.NOT_POSSIBLE,
        devicesCounter = 0,
        spaceId = "1"
    )
    P2PStatusItem(p2pStatus = p2pStatus)
}

@Preview(name = "P2PConnected", showBackground = true)
@Composable
fun SpaceSyncStatusPreview12() {
    val p2pStatus = P2PStatusUpdate.Update(
        status = P2PStatus.CONNECTED,
        devicesCounter = 3,
        spaceId = "1"
    )
    P2PStatusItem(p2pStatus = p2pStatus)
}

@Preview(name = "AnytypeNetworkNeedUpdate", showBackground = true)
@Composable
fun SpaceSyncStatusPreview13() {
    val spaceSyncUpdate = SpaceSyncUpdate.Update(
        id = "1",
        status = SpaceSyncStatus.NETWORK_UPDATE_NEEDED,
        network = SpaceSyncNetwork.ANYTYPE,
        error = SpaceSyncError.NULL,
        syncingObjectsCounter = 0
    )
    SpaceSyncStatusItem(spaceSyncUpdate = spaceSyncUpdate)
}

@Preview(name = "AnytypeNetworkNeedUpdate, Error", showBackground = true)
@Composable
fun SpaceSyncStatusPreview14() {
    val spaceSyncUpdate = SpaceSyncUpdate.Update(
        id = "1",
        status = SpaceSyncStatus.NETWORK_UPDATE_NEEDED,
        network = SpaceSyncNetwork.SELF_HOST,
        error = SpaceSyncError.STORAGE_LIMIT_EXCEED,
        syncingObjectsCounter = 0
    )
    SpaceSyncStatusItem(spaceSyncUpdate = spaceSyncUpdate)
}