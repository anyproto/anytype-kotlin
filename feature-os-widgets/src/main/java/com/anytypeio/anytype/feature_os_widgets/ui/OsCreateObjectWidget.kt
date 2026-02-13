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

/**
 * Glance App Widget that provides a quick-create button for a specific object type.
 * Tapping the widget creates a new object and opens it in the app.
 */
class OsCreateObjectWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataStore = OsWidgetsDataStore(context)
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val config = dataStore.getCreateObjectConfig(appWidgetId)

        provideContent {
            GlanceTheme {
                WidgetContent(config = config, appWidgetId = appWidgetId)
            }
        }
    }
}

@Composable
private fun WidgetContent(config: OsWidgetCreateObjectEntity?, appWidgetId: Int) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(OsWidgetBackgroundColor)
            .padding(8.dp)
    ) {
        if (config == null) {
            NotConfiguredState()
        } else {
            CreateObjectCard(config = config, appWidgetId = appWidgetId)
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
private fun CreateObjectCard(config: OsWidgetCreateObjectEntity, appWidgetId: Int) {
    val intent = OsWidgetDeepLinks.buildCreateObjectIntent(config.appWidgetId)

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
