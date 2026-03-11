package com.anytypeio.anytype.feature_os_widgets.ui

import android.content.Context
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
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetSpaceShortcutEntity
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetsDataStore
import kotlinx.coroutines.CancellationException
import timber.log.Timber

private const val TAG = "OsSpaceShortcutWidget"

/**
 * Glance App Widget that provides a shortcut to open a specific space.
 * Tapping the widget opens the configured space.
 */
class OsSpaceShortcutWidget : GlanceAppWidget() {

    companion object {
        private val SMALL_SIZE = DpSize(57.dp, 57.dp)
        private val MEDIUM_SIZE = DpSize(110.dp, 110.dp)
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(SMALL_SIZE, MEDIUM_SIZE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Timber.tag(TAG).d("provideGlance called, glanceId=$id")
        try {
            val appContext = context.applicationContext
            val dataStore = OsWidgetsDataStore(appContext)
            val appWidgetId = GlanceAppWidgetManager(appContext).getAppWidgetId(id)
            Timber.tag(TAG).d("provideGlance: appWidgetId=$appWidgetId")

            val config = loadWidgetConfigWithRetry {
                dataStore.getSpaceShortcutConfig(appWidgetId)
            }
            Timber.tag(TAG).d("provideGlance: config loaded, spaceId=${config?.spaceId}, spaceName=${config?.spaceName}, iconOption=${config?.spaceIconOption}, cachedIconPath=${config?.cachedIconPath}")

            val strings = SpaceShortcutWidgetStrings(
                spaceFallback = appContext.getString(R.string.space)
            )

            provideContent {
                GlanceTheme {
                    WidgetContent(
                        config = config,
                        size = androidx.glance.LocalSize.current,
                        strings = strings
                    )
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "provideGlance failed")
        }
    }

}

private data class SpaceShortcutWidgetStrings(
    val spaceFallback: String
)

@Composable
private fun WidgetContent(
    config: OsWidgetSpaceShortcutEntity?,
    size: DpSize,
    strings: SpaceShortcutWidgetStrings
) {
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
            SpaceShortcutCard(config = config, size = size, strings = strings)
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
private fun SpaceShortcutCard(
    config: OsWidgetSpaceShortcutEntity,
    size: DpSize,
    strings: SpaceShortcutWidgetStrings
) {
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
                    text = config.spaceName.ifEmpty { strings.spaceFallback },
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
    val iconColor = getWidgetIconColor(
        iconOption = config.spaceIconOption,
        defaultColor = OsWidgetIconSky
    )
    val initial = config.spaceName.firstOrNull()?.uppercaseChar()?.toString() ?: "S"
    val imageProvider = config.cachedIconPath?.let(::loadCachedImageProvider)
    val fontSize = (size.value * 0.5f).coerceIn(16f, 28f).sp

    if (imageProvider != null) {
        // Show cached image
        Image(
            provider = imageProvider,
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
