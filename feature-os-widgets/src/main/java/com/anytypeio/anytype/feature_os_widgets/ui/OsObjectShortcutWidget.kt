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
import com.anytypeio.anytype.persistence.oswidgets.OsWidgetObjectShortcutEntity
import com.anytypeio.anytype.persistence.oswidgets.OsWidgetsDataStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import timber.log.Timber

private const val TAG = "OsObjectShortcutWidget"

/**
 * Glance App Widget that provides a shortcut to open a specific object.
 * Tapping the widget opens the configured object within its space.
 */
class OsObjectShortcutWidget : GlanceAppWidget() {

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
    ): OsWidgetObjectShortcutEntity? {
        repeat(MAX_RETRIES) { attempt ->
            dataStore.getObjectShortcutConfig(appWidgetId)?.let { return it }
            if (attempt < MAX_RETRIES - 1) {
                delay(RETRY_DELAY_MS)
            }
        }
        Timber.tag(TAG).d("Config not found for widget $appWidgetId after $MAX_RETRIES attempts")
        return null
    }
}

@Composable
private fun WidgetContent(config: OsWidgetObjectShortcutEntity?) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(OsWidgetBackgroundColor)
            .padding(8.dp)
    ) {
        if (config == null) {
            NotConfiguredState()
        } else {
            ObjectShortcutCard(config = config)
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
private fun ObjectShortcutCard(config: OsWidgetObjectShortcutEntity) {
    val intent = OsWidgetDeepLinks.buildObjectShortcutIntent(config.objectId, config.spaceId)

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
        // Object icon
        ObjectIcon(config = config)
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        // Object name
        Text(
            text = config.objectName.ifEmpty { "Object" },
            style = TextStyle(
                color = ColorProvider(OsWidgetTextPrimary),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )
        
        Spacer(modifier = GlanceModifier.height(4.dp))
        
        // Space name (secondary)
        if (config.spaceName.isNotEmpty()) {
            Text(
                text = config.spaceName,
                style = TextStyle(
                    color = ColorProvider(OsWidgetTextSecondary),
                    fontSize = 12.sp
                ),
                maxLines = 1
            )
        }
        
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
private fun ObjectIcon(config: OsWidgetObjectShortcutEntity) {
    val emoji = config.objectIconEmoji
    val hasCustomIcon = !config.objectIconName.isNullOrEmpty()
    
    if (!emoji.isNullOrEmpty()) {
        // Show emoji
        Text(
            text = emoji,
            style = TextStyle(
                fontSize = 32.sp
            )
        )
    } else if (hasCustomIcon) {
        // Custom icon type - show colored placeholder
        val iconColor = getIconColor(config.objectIconOption)
        val initial = config.objectName.firstOrNull()?.uppercaseChar()?.toString() ?: "O"
        Box(
            modifier = GlanceModifier
                .size(48.dp)
                .cornerRadius(8.dp)
                .background(OsWidgetIconPlaceholderColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                style = TextStyle(
                    color = ColorProvider(iconColor),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    } else {
        // Fallback placeholder
        val initial = config.objectName.firstOrNull()?.uppercaseChar()?.toString() ?: "O"
        Box(
            modifier = GlanceModifier
                .size(48.dp)
                .cornerRadius(8.dp)
                .background(OsWidgetIconPlaceholderColor),
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
}

/**
 * Maps icon option to a color for the widget.
 */
private fun getIconColor(iconOption: Int?): androidx.compose.ui.graphics.Color {
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
        else -> OsWidgetIconGray
    }
}
