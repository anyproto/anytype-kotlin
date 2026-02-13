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
import com.anytypeio.anytype.persistence.oswidgets.OsWidgetCreateObjectEntity
import com.anytypeio.anytype.persistence.oswidgets.OsWidgetsDataStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import timber.log.Timber

private const val TAG = "OsCreateObjectWidget"

/**
 * Glance App Widget that provides a quick-create button for a specific object type.
 * Tapping the widget creates a new object and opens it in the app.
 */
class OsCreateObjectWidget : GlanceAppWidget() {

    companion object {
        // Retry configuration for handling race condition with config activity.
        // User needs time to: select space (~1-2s) + select type (~1-2s).
        // Total wait time: 10 retries * 500ms = 5 seconds.
        private const val MAX_RETRIES = 10
        private const val RETRY_DELAY_MS = 500L
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        try {
            val appContext = context.applicationContext
            val dataStore = OsWidgetsDataStore(appContext)
            val appWidgetId = GlanceAppWidgetManager(appContext).getAppWidgetId(id)

            // Retry logic to handle race condition where config might not be persisted yet
            val config = getConfigWithRetry(dataStore, appWidgetId)

            provideContent {
                GlanceTheme {
                    WidgetContent(config = config)
                }
            }
        } catch (e: CancellationException) {
            // Rethrow cancellation exceptions to allow proper coroutine cancellation
            throw e
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "provideGlance failed")
        }
    }

    /**
     * Attempts to get the widget configuration with retries.
     * 
     * This handles the race condition where provideGlance might be called
     * before the config activity has finished persisting the configuration.
     */
    private suspend fun getConfigWithRetry(
        dataStore: OsWidgetsDataStore,
        appWidgetId: Int
    ): OsWidgetCreateObjectEntity? {
        repeat(MAX_RETRIES) { attempt ->
            dataStore.getCreateObjectConfig(appWidgetId)?.let { return it }
            if (attempt < MAX_RETRIES - 1) {
                delay(RETRY_DELAY_MS)
            }
        }
        Timber.tag(TAG).d("Config not found for widget $appWidgetId after $MAX_RETRIES attempts")
        return null
    }
}

@Composable
private fun WidgetContent(config: OsWidgetCreateObjectEntity?) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(OsWidgetBackgroundColor)
            .padding(8.dp)
    ) {
        if (config == null) {
            NotConfiguredState()
        } else {
            CreateObjectCard(config = config)
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
private fun CreateObjectCard(config: OsWidgetCreateObjectEntity) {
    val deepLink = OsWidgetDeepLinks.buildCreateObjectDeepLink(config.appWidgetId)
    val intent = OsWidgetDeepLinks.buildCreateObjectIntent(config.appWidgetId)
    Timber.tag(TAG).d("CreateObjectCard: appWidgetId=${config.appWidgetId}, spaceId=${config.spaceId}, typeKey=${config.typeKey}, deepLink=$deepLink")

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
        // Type emoji or placeholder
        TypeIcon(emoji = config.typeIconEmoji, typeName = config.typeName)
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        // Type name
        Text(
            text = config.typeName.ifEmpty { "Object" },
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
        
        // Create action hint
        Text(
            text = "Tap to create",
            style = TextStyle(
                color = ColorProvider(OsWidgetTextTertiary),
                fontSize = 11.sp
            )
        )
    }
}

@Composable
private fun TypeIcon(emoji: String?, typeName: String) {
    if (!emoji.isNullOrEmpty()) {
        // Show emoji
        Text(
            text = emoji,
            style = TextStyle(
                fontSize = 32.sp
            )
        )
    } else {
        // Show placeholder with initial
        val initial = typeName.firstOrNull()?.uppercaseChar()?.toString() ?: "+"
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
