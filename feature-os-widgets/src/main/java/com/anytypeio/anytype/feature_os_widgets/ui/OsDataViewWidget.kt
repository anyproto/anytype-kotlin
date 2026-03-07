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
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.anytypeio.anytype.feature_os_widgets.R
import com.anytypeio.anytype.feature_os_widgets.deeplink.OsWidgetDeepLinks
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetDataViewEntity
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetDataViewItemEntity
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetsDataStore
import kotlinx.coroutines.CancellationException
import timber.log.Timber

private const val TAG = "OsDataViewWidget"

/**
 * Glance App Widget that displays items from a Set or Collection's data view.
 */
class OsDataViewWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Timber.tag(TAG).d("provideGlance called, glanceId=$id")
        try {
            val appContext = context.applicationContext
            val dataStore = OsWidgetsDataStore(appContext)
            val appWidgetId = GlanceAppWidgetManager(appContext).getAppWidgetId(id)
            Timber.tag(TAG).d("provideGlance: appWidgetId=$appWidgetId")
            val config = loadWidgetConfigWithRetry {
                dataStore.getDataViewConfig(appWidgetId)
            }
            Timber.tag(TAG).d("provideGlance: config loaded, objectId=${config?.objectId}, objectName=${config?.objectName}, items=${config?.items?.size}")
            val strings = DataViewWidgetStrings(
                notConfigured = appContext.getString(R.string.os_widget_not_configured),
                noItems = appContext.getString(R.string.os_widget_no_items),
                untitled = appContext.getString(R.string.untitled)
            )

            provideContent {
                GlanceTheme {
                    WidgetContent(config = config, strings = strings)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "provideGlance failed")
        }
    }

}

private data class DataViewWidgetStrings(
    val notConfigured: String,
    val noItems: String,
    val untitled: String
)

@Composable
private fun WidgetContent(config: OsWidgetDataViewEntity?, strings: DataViewWidgetStrings) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(OsWidgetBackgroundColor)
            .padding(8.dp)
    ) {
        if (config == null) {
            NotConfiguredState(notConfiguredText = strings.notConfigured)
        } else {
            DataViewContent(config = config, strings = strings)
        }
    }
}

@Composable
private fun NotConfiguredState(notConfiguredText: String) {
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
                text = notConfiguredText,
                style = TextStyle(
                    color = ColorProvider(OsWidgetTextSecondary),
                    fontSize = 14.sp
                )
            )
        }
    }
}

@Composable
private fun DataViewContent(config: OsWidgetDataViewEntity, strings: DataViewWidgetStrings) {
    Column(
        modifier = GlanceModifier.fillMaxSize()
    ) {
        // Header: "ObjectName · ViewerName"
        HeaderRow(config = config, untitledFallback = strings.untitled)
        Spacer(modifier = GlanceModifier.height(4.dp))

        if (config.items.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = strings.noItems,
                    style = TextStyle(
                        color = ColorProvider(OsWidgetTextSecondary),
                        fontSize = 13.sp
                    )
                )
            }
        } else {
            LazyColumn(
                modifier = GlanceModifier.fillMaxSize()
            ) {
                items(config.items, itemId = { stableItemId(it.id) }) { item ->
                    Column(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    ) {
                        ItemRow(
                            item = item,
                            spaceId = config.spaceId,
                            untitledFallback = strings.untitled
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderRow(config: OsWidgetDataViewEntity, untitledFallback: String) {
    val headerText = buildString {
        append(config.objectName.ifEmpty { untitledFallback })
        if (config.viewerName.isNotEmpty()) {
            append(" \u00B7 ")
            append(config.viewerName)
        }
    }
    val intent = OsWidgetDeepLinks.buildDataViewHeaderIntent(
        objectId = config.objectId,
        spaceId = config.spaceId
    )
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .clickable(actionStartActivity(intent)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = headerText,
            style = TextStyle(
                color = ColorProvider(OsWidgetTextSecondary),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight()
        )
    }
}

@Composable
private fun ItemRow(item: OsWidgetDataViewItemEntity, spaceId: String, untitledFallback: String) {
    val intent = OsWidgetDeepLinks.buildDataViewItemIntent(
        objectId = item.id,
        spaceId = spaceId
    )
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(OsWidgetSurfaceColor)
            .cornerRadius(8.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable(actionStartActivity(intent)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon initial circle
        val initial = item.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        Box(
            modifier = GlanceModifier
                .size(28.dp)
                .cornerRadius(14.dp)
                .background(OsWidgetIconPlaceholderColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                style = TextStyle(
                    color = ColorProvider(OsWidgetTextPrimary),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Spacer(modifier = GlanceModifier.width(10.dp))
        Column(
            modifier = GlanceModifier.defaultWeight()
        ) {
            Text(
                text = item.name.ifEmpty { untitledFallback },
                style = TextStyle(
                    color = ColorProvider(OsWidgetTextPrimary),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                ),
                maxLines = 1
            )
            if (item.typeName.isNotEmpty()) {
                Text(
                    text = item.typeName,
                    style = TextStyle(
                        color = ColorProvider(OsWidgetTextSecondary),
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}
