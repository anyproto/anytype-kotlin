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
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.feature_os_widgets.R
import com.anytypeio.anytype.feature_os_widgets.deeplink.OsWidgetDeepLinks
import com.anytypeio.anytype.feature_os_widgets.mapper.toDomain
import com.anytypeio.anytype.feature_os_widgets.model.OsWidgetSpaceIcon
import com.anytypeio.anytype.feature_os_widgets.model.OsWidgetSpaceItem
import com.anytypeio.anytype.persistence.oswidgets.OsWidgetsDataStore
import kotlinx.coroutines.flow.first
import java.io.File

/**
 * Glance App Widget that displays a list of user's spaces.
 * Tapping a space opens the app and navigates to that space.
 */
class OsSpacesListWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataStore = OsWidgetsDataStore(context)
        val spaces = dataStore.observeSpaces().first().toDomain()

        provideContent {
            GlanceTheme {
                WidgetContent(spaces = spaces)
            }
        }
    }
}

@Composable
private fun WidgetContent(spaces: List<OsWidgetSpaceItem>) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(OsWidgetBackgroundColor)
            .padding(8.dp)
    ) {
        if (spaces.isEmpty()) {
            EmptyState()
        } else {
            SpacesList(spaces = spaces)
        }
    }
}

@Composable
private fun EmptyState() {
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
                text = "Open Anytype to see spaces",
                style = TextStyle(
                color = ColorProvider(OsWidgetTextSecondary),
                    fontSize = 14.sp
                )
            )
        }
    }
}

@Composable
private fun SpacesList(spaces: List<OsWidgetSpaceItem>) {
    LazyColumn(
        modifier = GlanceModifier.fillMaxSize()
    ) {
        items(spaces, itemId = { it.spaceId.hashCode().toLong() }) { space ->
            SpaceCard(space = space)
        }
    }
}

@Composable
private fun SpaceCard(space: OsWidgetSpaceItem) {
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
                spaceUxType = space.spaceUxType
            )
            Spacer(modifier = GlanceModifier.width(12.dp))
            Text(
                text = space.name.ifEmpty { "Untitled" },
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

@Composable
private fun SpaceIcon(
    icon: OsWidgetSpaceIcon,
    name: String,
    spaceUxType: SpaceUxType
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

    // Determine corner radius based on space type (chat spaces use circle, data spaces use rounded rect)
    val cornerRadius = when (spaceUxType) {
        SpaceUxType.CHAT, SpaceUxType.ONE_TO_ONE -> 20  // Circle for 40dp (half of size)
        else -> SPACE_ICON_CORNER_RADIUS  // 6dp for data spaces
    }

    // Try to load cached image, fallback to placeholder
    val bitmap = (icon as? OsWidgetSpaceIcon.Image)?.let { loadCachedBitmap(it.url) }

    if (bitmap != null) {
        // Show the cached image
        Image(
            provider = ImageProvider(bitmap),
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
