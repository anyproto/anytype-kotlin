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
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
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
import com.anytypeio.anytype.feature_os_widgets.mapper.toDomain
import com.anytypeio.anytype.feature_os_widgets.model.OsWidgetSpaceIcon
import com.anytypeio.anytype.feature_os_widgets.model.OsWidgetSpaceItem
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetsDataStore
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * Glance App Widget that displays a list of user's spaces.
 * Tapping a space opens the app and navigates to that space.
 */
class OsSpacesListWidget : GlanceAppWidget() {

    companion object {
        private const val TAG = "OsSpacesListWidget"
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Timber.tag(TAG).d("provideGlance called, glanceId=$id")
        val appContext = context.applicationContext
        val dataStore = OsWidgetsDataStore(appContext)
        val entities = dataStore.observeSpaces().first()
        val spaces = entities.toDomain()
        Timber.tag(TAG).d("Read ${entities.size} space entities from DataStore, mapped to ${spaces.size} domain items")
        entities.forEachIndexed { i, e ->
            Timber.tag(TAG).d("  entity[$i]: spaceId=${e.spaceId}, name=${e.name}, icon=${e.iconImageUrl != null}")
        }
        val strings = SpacesListWidgetStrings(
            emptyState = appContext.getString(R.string.os_widget_open_app_to_see_spaces),
            untitled = appContext.getString(R.string.untitled)
        )

        if (spaces.isEmpty()) {
            Timber.tag(TAG).w("No spaces found in DataStore — will show empty state")
        }

        provideContent {
            GlanceTheme {
                WidgetContent(spaces = spaces, strings = strings)
            }
        }
    }
}

@Composable
private fun WidgetContent(spaces: List<OsWidgetSpaceItem>, strings: SpacesListWidgetStrings) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(OsWidgetBackgroundColor)
            .padding(8.dp)
    ) {
        if (spaces.isEmpty()) {
            EmptyState(emptyStateText = strings.emptyState)
        } else {
            SpacesList(spaces = spaces, untitledFallback = strings.untitled)
        }
    }
}

@Composable
private fun EmptyState(emptyStateText: String) {
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
                text = emptyStateText,
                style = TextStyle(
                color = ColorProvider(OsWidgetTextSecondary),
                    fontSize = 14.sp
                )
            )
        }
    }
}

@Composable
private fun SpacesList(spaces: List<OsWidgetSpaceItem>, untitledFallback: String) {
    LazyColumn(
        modifier = GlanceModifier.fillMaxSize()
    ) {
        items(spaces, itemId = { stableItemId(it.spaceId) }) { space ->
            SpaceCard(space = space, untitledFallback = untitledFallback)
        }
    }
}

@Composable
private fun SpaceCard(space: OsWidgetSpaceItem, untitledFallback: String) {
    val intent = OsWidgetDeepLinks.buildSpaceIntent(space.spaceId)

    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(OsWidgetSurfaceColor)
                .cornerRadius(12.dp)
                .padding(12.dp)
                .clickable(actionStartActivity(intent)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SpaceIcon(
                icon = space.icon,
                name = space.name,
                isOneToOneSpace = space.isOneToOneSpace
            )
            Spacer(modifier = GlanceModifier.width(12.dp))
            Text(
                text = space.name.ifEmpty { untitledFallback },
                style = TextStyle(
                    color = ColorProvider(OsWidgetTextPrimary),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1,
                modifier = GlanceModifier.defaultWeight()
            )
        }
    }
}

private data class SpacesListWidgetStrings(
    val emptyState: String,
    val untitled: String
)

@Composable
private fun SpaceIcon(
    icon: OsWidgetSpaceIcon,
    name: String,
    isOneToOneSpace: Boolean
) {
    val backgroundColor = when (icon) {
        is OsWidgetSpaceIcon.Image -> icon.color.toWidgetColor()
        is OsWidgetSpaceIcon.Placeholder -> icon.color.toWidgetColor()
    }

    val textColor = when (icon) {
        is OsWidgetSpaceIcon.Image -> icon.color.toWidgetLightTextColor()
        is OsWidgetSpaceIcon.Placeholder -> icon.color.toWidgetLightTextColor()
    }

    val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    // Determine corner radius based on space kind (1-1 spaces use circle,
    // regular data spaces use a rounded rect).
    val cornerRadius = if (isOneToOneSpace) {
        20  // Circle for 40dp (half of size)
    } else {
        SPACE_ICON_CORNER_RADIUS  // 6dp for data spaces
    }

    // Try to load cached image, fallback to placeholder
    val imageProvider = (icon as? OsWidgetSpaceIcon.Image)?.let { loadCachedImageProvider(it.url) }

    if (imageProvider != null) {
        // Show the cached image
        Image(
            provider = imageProvider,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = GlanceModifier
                .size(40.dp)
                .cornerRadius(cornerRadius.dp)
        )
    } else {
        // Show placeholder with initial
        Box(
            modifier = GlanceModifier
                .size(40.dp)
                .cornerRadius(cornerRadius.dp)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                style = TextStyle(
                    color = ColorProvider(textColor),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
