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
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.anytypeio.anytype.feature_os_widgets.R
import com.anytypeio.anytype.feature_os_widgets.deeplink.OsWidgetDeepLinks
import com.anytypeio.anytype.persistence.oswidgets.OsWidgetSpaceShortcutEntity
import com.anytypeio.anytype.persistence.oswidgets.OsWidgetsDataStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import timber.log.Timber

private const val TAG = "OsSpaceShortcutWidget"

/**
 * Glance App Widget that provides a shortcut to open a specific space.
 * Tapping the widget opens the configured space.
 */
class OsSpaceShortcutWidget : GlanceAppWidget() {

    companion object {
        private const val MAX_RETRIES = 10
        private const val RETRY_DELAY_MS = 500L
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        try {
            val appContext = context.applicationContext
            val dataStore = OsWidgetsDataStore(appContext)
            val appWidgetId = GlanceAppWidgetManager(appContext).getAppWidgetId(id)

            val config = getConfigWithRetry(dataStore, appWidgetId)

            provideContent {
                GlanceTheme {
                    WidgetContent(config = config)
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
private fun WidgetContent(config: OsWidgetSpaceShortcutEntity?) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(OsWidgetBackgroundColor)
            .padding(8.dp)
    ) {
        if (config == null) {
            NotConfiguredState()
        } else {
            SpaceShortcutCard(config = config)
        }
    }
}

@Composable
private fun NotConfiguredState() {
    Box(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_anytype_logo_widget),
                contentDescription = null,
                modifier = GlanceModifier.size(48.dp)
            )
            Spacer(modifier = GlanceModifier.height(12.dp))
            Text(
                text = "Widget not configured",
                style = TextStyle(
                    color = ColorProvider(OsWidgetTextSecondary),
                    fontSize = 14.sp
                )
            )
        }
    }
}

@Composable
private fun SpaceShortcutCard(config: OsWidgetSpaceShortcutEntity) {
    val intent = OsWidgetDeepLinks.buildSpaceShortcutIntent(config.spaceId)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(OsWidgetSurfaceColor)
            .cornerRadius(12.dp)
            .padding(12.dp)
            .clickable(actionStartActivity(intent)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Space icon placeholder
        SpaceIcon(config = config)
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        // Space name
        Text(
            text = config.spaceName.ifEmpty { "Space" },
            style = TextStyle(
                color = ColorProvider(OsWidgetTextPrimary),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 2
        )
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        // Action hint
        Text(
            text = "Tap to open",
            style = TextStyle(
                color = ColorProvider(OsWidgetTextTertiary),
                fontSize = 11.sp
            )
        )
    }
}

@Composable
private fun SpaceIcon(config: OsWidgetSpaceShortcutEntity) {
    // Show colored placeholder with initial
    val iconColor = getSpaceIconColor(config.spaceIconOption)
    val initial = config.spaceName.firstOrNull()?.uppercaseChar()?.toString() ?: "S"
    
    Box(
        modifier = GlanceModifier
            .size(48.dp)
            .cornerRadius(SPACE_ICON_CORNER_RADIUS.dp)
            .background(iconColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = TextStyle(
                color = ColorProvider(OsWidgetTextPrimary),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )
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
