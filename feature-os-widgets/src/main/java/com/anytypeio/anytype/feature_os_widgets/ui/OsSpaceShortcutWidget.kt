package com.anytypeio.anytype.feature_os_widgets.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.SizeMode
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.unit.DpSize
import com.anytypeio.anytype.feature_os_widgets.R
import com.anytypeio.anytype.feature_os_widgets.deeplink.OsWidgetDeepLinks
import com.anytypeio.anytype.persistence.oswidgets.OsWidgetSpaceShortcutEntity
import com.anytypeio.anytype.persistence.oswidgets.OsWidgetsDataStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import timber.log.Timber
import java.io.File

private const val TAG = "OsSpaceShortcutWidget"

/**
 * Glance App Widget that provides a shortcut to open a specific space.
 * Tapping the widget opens the configured space.
 */
class OsSpaceShortcutWidget : GlanceAppWidget() {

    companion object {
        // Give user up to 30 seconds to configure the widget
        private const val MAX_RETRIES = 60
        private const val RETRY_DELAY_MS = 500L
        private val SMALL_SIZE = DpSize(57.dp, 57.dp)
        private val MEDIUM_SIZE = DpSize(110.dp, 110.dp)
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(SMALL_SIZE, MEDIUM_SIZE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        try {
            val appContext = context.applicationContext
            val dataStore = OsWidgetsDataStore(appContext)
            val appWidgetId = GlanceAppWidgetManager(appContext).getAppWidgetId(id)

            val config = getConfigWithRetry(dataStore, appWidgetId)

            provideContent {
                GlanceTheme {
                    WidgetContent(config = config, size = androidx.glance.LocalSize.current)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "provideGlance failed")
        }
    }

    private suspend fun getConfigWithRetry(
        dataStore: OsWidgetsDataStore,
        appWidgetId: Int
    ): OsWidgetSpaceShortcutEntity? {
        repeat(MAX_RETRIES) { attempt ->
            dataStore.getSpaceShortcutConfig(appWidgetId)?.let { return it }
            if (attempt < MAX_RETRIES - 1) {
                delay(RETRY_DELAY_MS)
            }
        }
        Timber.tag(TAG).d("Config not found for widget $appWidgetId after $MAX_RETRIES attempts")
        return null
    }
}

@Composable
private fun WidgetContent(config: OsWidgetSpaceShortcutEntity?, size: DpSize) {
    val isSmall = size.width < 100.dp
    
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(OsWidgetBackgroundColor)
            .padding(if (isSmall) 4.dp else 8.dp)
    ) {
        if (config == null) {
            NotConfiguredState(isSmall = isSmall)
        } else {
            SpaceShortcutCard(config = config, size = size)
        }
    }
}

@Composable
private fun NotConfiguredState(isSmall: Boolean) {
    Box(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_anytype_logo_widget),
            contentDescription = null,
            modifier = GlanceModifier.size(if (isSmall) 32.dp else 48.dp)
        )
    }
}

@Composable
private fun SpaceShortcutCard(config: OsWidgetSpaceShortcutEntity, size: DpSize) {
    val intent = OsWidgetDeepLinks.buildSpaceShortcutIntent(config.spaceId)
    val isSmall = size.width < 100.dp
    
    // Adaptive sizes based on widget dimensions
    val iconSize = (size.width.value * 0.45f).coerceIn(32f, 56f).dp
    val nameFontSize = (size.width.value * 0.12f).coerceIn(11f, 16f).sp
    val padding = (size.width.value * 0.08f).coerceIn(4f, 12f).dp
    val cornerRadius = if (isSmall) 8.dp else 12.dp

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(OsWidgetSurfaceColor)
            .cornerRadius(cornerRadius)
            .clickable(actionStartActivity(intent)),
        contentAlignment = Alignment.Center
    ) {
        if (isSmall) {
            // Compact layout - just icon
            SpaceIcon(config = config, size = iconSize)
        } else {
            // Full layout
            Column(
                modifier = GlanceModifier.padding(padding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SpaceIcon(config = config, size = iconSize)
                Spacer(modifier = GlanceModifier.height(6.dp))
                Text(
                    text = config.spaceName.ifEmpty { "Space" },
                    style = TextStyle(
                        color = ColorProvider(OsWidgetTextPrimary),
                        fontSize = nameFontSize,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun SpaceIcon(config: OsWidgetSpaceShortcutEntity, size: androidx.compose.ui.unit.Dp = 48.dp) {
    val iconColor = getSpaceIconColor(config.spaceIconOption)
    val initial = config.spaceName.firstOrNull()?.uppercaseChar()?.toString() ?: "S"
    val bitmap = config.cachedIconPath?.let { loadCachedBitmap(it) }
    val fontSize = (size.value * 0.5f).coerceIn(16f, 28f).sp

    if (bitmap != null) {
        // Show cached image
        Image(
            provider = ImageProvider(bitmap),
            contentDescription = config.spaceName,
            contentScale = ContentScale.Crop,
            modifier = GlanceModifier
                .size(size)
                .cornerRadius(SPACE_ICON_CORNER_RADIUS.dp)
        )
    } else {
        // Show colored placeholder with initial
        Box(
            modifier = GlanceModifier
                .size(size)
                .cornerRadius(SPACE_ICON_CORNER_RADIUS.dp)
                .background(iconColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                style = TextStyle(
                    color = ColorProvider(OsWidgetTextPrimary),
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

/**
 * Loads a bitmap from a local file path.
 * Returns null if file doesn't exist or can't be decoded.
 */
private fun loadCachedBitmap(filePath: String): Bitmap? {
    return try {
        val file = File(filePath)
        if (file.exists()) {
            BitmapFactory.decodeFile(filePath)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Maps space icon option to a background color.
 */
private fun getSpaceIconColor(iconOption: Int?): androidx.compose.ui.graphics.Color {
    return when (iconOption) {
        1 -> OsWidgetIconGray
        2 -> OsWidgetIconYellow
        3 -> OsWidgetIconAmber
        4 -> OsWidgetIconRed
        5 -> OsWidgetIconPink
        6 -> OsWidgetIconPurple
        7 -> OsWidgetIconBlue
        8 -> OsWidgetIconSky
        9 -> OsWidgetIconTeal
        10 -> OsWidgetIconGreen
        else -> OsWidgetIconSky // Default space color
    }
}
