package com.anytypeio.anytype.feature_os_widgets.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
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
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetCreateObjectEntity
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetsDataStore
import kotlinx.coroutines.CancellationException
import timber.log.Timber

private const val TAG = "OsCreateObjectWidget"

/**
 * Glance App Widget that provides a quick-create button for a specific object type.
 * Tapping the widget creates a new object and opens it in the app.
 */
class OsCreateObjectWidget : GlanceAppWidget() {

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
                dataStore.getCreateObjectConfig(appWidgetId)
            }
            Timber.tag(TAG).d("provideGlance: config loaded, spaceId=${config?.spaceId}, typeKey=${config?.typeKey}, typeName=${config?.typeName}")
            val strings = CreateObjectWidgetStrings(
                notConfigured = appContext.getString(R.string.os_widget_not_configured),
                objectFallback = appContext.getString(R.string.object_1),
                tapToCreate = appContext.getString(R.string.os_widget_tap_to_create)
            )

            provideContent {
                GlanceTheme {
                    WidgetContent(config = config, size = LocalSize.current, strings = strings)
                }
            }
        } catch (e: CancellationException) {
            // Rethrow cancellation exceptions to allow proper coroutine cancellation
            throw e
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "provideGlance failed")
        }
    }

}

@Composable
private fun WidgetContent(
    config: OsWidgetCreateObjectEntity?,
    size: DpSize,
    strings: CreateObjectWidgetStrings
) {
    val isSmall = size.width < 100.dp

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(OsWidgetBackgroundColor)
            .padding(if (isSmall) 4.dp else 8.dp)
    ) {
        if (config == null) {
            NotConfiguredState(isSmall = isSmall, notConfiguredText = strings.notConfigured)
        } else {
            CreateObjectCard(config = config, size = size, strings = strings)
        }
    }
}

@Composable
private fun NotConfiguredState(isSmall: Boolean, notConfiguredText: String) {
    Box(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isSmall) {
            Image(
                provider = ImageProvider(R.drawable.ic_anytype_logo_widget),
                contentDescription = null,
                modifier = GlanceModifier.size(32.dp)
            )
        } else {
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
}

@Composable
private fun CreateObjectCard(
    config: OsWidgetCreateObjectEntity,
    size: DpSize,
    strings: CreateObjectWidgetStrings
) {
    val deepLink = OsWidgetDeepLinks.buildCreateObjectDeepLink(config.appWidgetId, config.deepLinkToken)
    val intent = OsWidgetDeepLinks.buildCreateObjectIntent(config.appWidgetId, config.deepLinkToken)
    Timber.tag(TAG).d("CreateObjectCard: appWidgetId=${config.appWidgetId}, spaceId=${config.spaceId}, typeKey=${config.typeKey}, deepLink=$deepLink")

    val isSmall = size.width < 100.dp
    // Adaptive sizes based on widget dimensions
    val iconSize = (size.width.value * 0.35f).coerceIn(24f, 48f).dp
    val emojiFontSize = (iconSize.value * 0.65f).coerceIn(16f, 32f).sp
    val initialFontSize = (iconSize.value * 0.5f).coerceIn(12f, 24f).sp
    val nameFontSize = (size.width.value * 0.12f).coerceIn(11f, 14f).sp
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
            // Compact layout - icon + type name
            Column(
                modifier = GlanceModifier.padding(padding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TypeIcon(
                    config = config,
                    iconSize = iconSize,
                    emojiFontSize = emojiFontSize,
                    initialFontSize = initialFontSize
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = config.typeName.ifEmpty { strings.objectFallback },
                    style = TextStyle(
                        color = ColorProvider(OsWidgetTextPrimary),
                        fontSize = nameFontSize,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
            }
        } else {
            // Full layout
            Column(
                modifier = GlanceModifier.padding(padding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TypeIcon(
                    config = config,
                    iconSize = iconSize,
                    emojiFontSize = emojiFontSize,
                    initialFontSize = initialFontSize
                )

                Spacer(modifier = GlanceModifier.height(6.dp))

                // Type name
                Text(
                    text = config.typeName.ifEmpty { strings.objectFallback },
                    style = TextStyle(
                        color = ColorProvider(OsWidgetTextPrimary),
                        fontSize = nameFontSize,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )

                // Space name (secondary)
                if (config.spaceName.isNotEmpty()) {
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = config.spaceName,
                        style = TextStyle(
                            color = ColorProvider(OsWidgetTextSecondary),
                            fontSize = 12.sp
                        ),
                        maxLines = 1
                    )
                }

                Spacer(modifier = GlanceModifier.height(6.dp))

                // Create action hint
                Text(
                    text = strings.tapToCreate,
                    style = TextStyle(
                        color = ColorProvider(OsWidgetTextTertiary),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

private data class CreateObjectWidgetStrings(
    val notConfigured: String,
    val objectFallback: String,
    val tapToCreate: String
)

@Composable
private fun TypeIcon(
    config: OsWidgetCreateObjectEntity,
    iconSize: androidx.compose.ui.unit.Dp,
    emojiFontSize: androidx.compose.ui.unit.TextUnit,
    initialFontSize: androidx.compose.ui.unit.TextUnit
) {
    val emoji = config.typeIconEmoji
    val hasCustomIcon = !config.typeIconName.isNullOrEmpty()
    val iconCornerRadius = (iconSize.value * 0.16f).coerceIn(4f, 8f).dp

    if (!emoji.isNullOrEmpty()) {
        // Show emoji
        Text(
            text = emoji,
            style = TextStyle(
                fontSize = emojiFontSize
            )
        )
    } else if (hasCustomIcon) {
        // Custom icon type - show colored placeholder
        val iconColor = getWidgetIconColor(
            iconOption = config.typeIconOption,
            defaultColor = OsWidgetIconGray
        )
        val initial = config.typeName.firstOrNull()?.uppercaseChar()?.toString() ?: "+"
        Box(
            modifier = GlanceModifier
                .size(iconSize)
                .cornerRadius(iconCornerRadius)
                .background(OsWidgetIconPlaceholderColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                style = TextStyle(
                    color = ColorProvider(iconColor),
                    fontSize = initialFontSize,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    } else {
        // Fallback placeholder
        val initial = config.typeName.firstOrNull()?.uppercaseChar()?.toString() ?: "+"
        Box(
            modifier = GlanceModifier
                .size(iconSize)
                .cornerRadius(iconCornerRadius)
                .background(OsWidgetIconPlaceholderColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                style = TextStyle(
                    color = ColorProvider(OsWidgetTextPrimary),
                    fontSize = initialFontSize,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
