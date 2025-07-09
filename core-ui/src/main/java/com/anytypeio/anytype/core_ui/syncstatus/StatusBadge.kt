package com.anytypeio.anytype.core_ui.syncstatus

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncError
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncUpdate
import com.anytypeio.anytype.core_ui.R
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import com.anytypeio.anytype.core_models.multiplayer.P2PStatusUpdate
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncNetwork
import com.anytypeio.anytype.core_ui.common.DefaultPreviews

@Composable
fun StatusBadge(
    status: SpaceSyncAndP2PStatusState?,
    modifier: Modifier = Modifier.size(20.dp)
) {
    when (status) {
        is SpaceSyncAndP2PStatusState.Error -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_sync_error_8),
                    contentDescription = "Sync Error",
                    modifier = Modifier.size(8.dp)
                )
            }
        }

        SpaceSyncAndP2PStatusState.Init -> {}
        is SpaceSyncAndP2PStatusState.Success -> {
            when (val spaceSyncUpdate = status.spaceSyncUpdate) {
                SpaceSyncUpdate.Initial -> {}
                is SpaceSyncUpdate.Update -> {
                    Box(
                        modifier = modifier,
                        contentAlignment = Alignment.Center
                    ) {
                        if (spaceSyncUpdate.error != SpaceSyncError.NULL) {
                            Image(
                                painter = painterResource(R.drawable.ic_sync_error_8),
                                contentDescription = null,
                                modifier = Modifier.size(8.dp)
                            )
                        } else {
                            when (spaceSyncUpdate.status) {
                                SpaceSyncStatus.SYNCED -> {
                                    Image(
                                        painter = painterResource(R.drawable.ic_synced_8),
                                        contentDescription = "Synced",
                                        modifier = Modifier.size(8.dp),
                                        contentScale = ContentScale.Inside
                                    )
                                }

                                SpaceSyncStatus.SYNCING -> {
                                    PulsatingCircle(
                                        color = colorResource(R.color.palette_system_green), // Replace with your color resource
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                SpaceSyncStatus.ERROR -> {
                                    Image(
                                        painter = painterResource(R.drawable.ic_sync_error_8),
                                        contentDescription = "Sync Error",
                                        modifier = Modifier.size(8.dp)
                                    )
                                }

                                SpaceSyncStatus.OFFLINE -> {
                                    Image(
                                        painter = painterResource(R.drawable.ic_sync_grey_8),
                                        contentDescription = "Offline",
                                        modifier = Modifier.size(8.dp)
                                    )
                                }

                                SpaceSyncStatus.NETWORK_UPDATE_NEEDED -> {
                                    Image(
                                        painter = painterResource(R.drawable.ic_sync_slow_8),
                                        contentDescription = "Network Update Needed",
                                        modifier = Modifier.size(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        null -> {}
    }
}

@Composable
fun PulsatingCircle(
    color: Color,
    modifier: Modifier = Modifier.size(20.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sync dot animation")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "sync dot animation"
    )

    val density = LocalDensity.current
    val centerRadius = with(density) { 4.dp.toPx() }

    // Outer circles' base radii
    val middleRadius = centerRadius + with(density) { 3.dp.toPx() }  // 7 dp radius
    val outerRadius = centerRadius + with(density) { 6.dp.toPx() }   // 10 dp radius

    Canvas(modifier = modifier) {

        // Apply scaling to the outer circles
        drawCircle(
            color = color.copy(alpha = 0.2f),
            radius = outerRadius * scale,
            center = center
        )
        drawCircle(
            color = color.copy(alpha = 0.4f),
            radius = middleRadius * scale,
            center = center
        )

        // Static center circle
        drawCircle(
            color = color,
            radius = centerRadius,
            center = center
        )
    }
}

@DefaultPreviews
@Composable
fun StatusBadgePreview() {
    StatusBadge(
        status = SpaceSyncAndP2PStatusState.Success(
            spaceSyncUpdate = SpaceSyncUpdate.Update(
                id = "1",
                status = SpaceSyncStatus.SYNCED,
                network = SpaceSyncNetwork.ANYTYPE,
                error = SpaceSyncError.NULL,
                syncingObjectsCounter = 2
            ),
            p2PStatusUpdate = P2PStatusUpdate.Initial
        )
    )
}