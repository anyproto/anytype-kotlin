package com.anytypeio.anytype.core_ui.features.fields

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.BodyCallout
import com.anytypeio.anytype.core_ui.views.Relations1
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import com.anytypeio.anytype.presentation.sets.model.FileView

@Composable
fun FieldTypeFile(
    modifier: Modifier = Modifier,
    fieldObject: ObjectRelationView.File
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val halfScreenWidth = screenWidth / 2 - 32.dp

    val defaultModifier = modifier
        .fillMaxWidth()
        .border(
            width = 1.dp,
            color = colorResource(id = R.color.shape_secondary),
            shape = RoundedCornerShape(12.dp)
        )
        .padding(vertical = 16.dp)
        .padding(horizontal = 16.dp)
    if (fieldObject.files.size == 1) {
        // If there is only one item, display the title and the item in one row.
        val singleItem = fieldObject.files.first()
        Row(
            modifier = defaultModifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = halfScreenWidth)
                    .wrapContentHeight()
                    .padding(vertical = 2.dp)
            ) {
                Text(
                    text = fieldObject.name,
                    style = Relations1,
                    color = colorResource(id = R.color.text_secondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier.widthIn(max = halfScreenWidth)
            ) {
                ItemView(
                    modifier = Modifier.wrapContentHeight(),
                    objView = singleItem
                )
            }
        }
    } else {
        Column(
            modifier = defaultModifier
        ) {
            Text(
                modifier = Modifier.wrapContentWidth(),
                text = fieldObject.name,
                style = Relations1,
                color = colorResource(id = R.color.text_secondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // The first item (if present)
                if (fieldObject.files.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = halfScreenWidth)
                    ) {
                        ItemView(
                            modifier = Modifier.wrapContentHeight(),
                            objView = fieldObject.files.first()
                        )
                    }
                }
                // The second item (if present)
                if (fieldObject.files.size > 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.widthIn(max = halfScreenWidth)) {
                        if (fieldObject.files.size == 2) {
                            ItemView(
                                modifier = Modifier.wrapContentHeight(),
                                objView = fieldObject.files[1]
                            )
                        } else {
                            // If there are more than two items, display the second item with a "+n" suffix.
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ListWidgetObjectIcon(
                                    icon = fieldObject.files[1].icon,
                                    iconSize = 18.dp,
                                    modifier = Modifier,
                                    onTaskIconClicked = {
                                        // Do nothing
                                    }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                // The main text with an integrated suffix that occupies the remaining space.
                                FileNameWithSuffix(
                                    text = fieldObject.files[1].name,
                                    suffix = "+${fieldObject.files.size - 2}",
                                    textStyle = BodyCallout.copy(
                                        color = colorResource(id = R.color.text_primary)
                                    ),
                                    countStyle = Relations2.copy(
                                        color = colorResource(id = R.color.text_secondary)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .align(Alignment.CenterVertically)
                                        .padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper function to display a single item: icon (if available) + text.
@Composable
internal fun ItemView(modifier: Modifier, objView: FileView) {
    Row(
        modifier = modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ListWidgetObjectIcon(
            icon = objView.icon,
            iconSize = 18.dp,
            modifier = Modifier,
            onTaskIconClicked = {
                // Do nothing
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = objView.name,
            style = BodyCallout.copy(
                color = colorResource(id = R.color.text_primary)
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * A composable that displays a row consisting of the main text and a suffix.
 * If the main text is short enough, the suffix (for example, "+n")
 * will appear immediately after it; if the text is long, it will be truncated (with an ellipsis)
 * to leave space for the suffix.
 */
@Composable
internal fun FileNameWithSuffix(
    text: String,
    suffix: String,
    textStyle: TextStyle,
    countStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    SubcomposeLayout(modifier = modifier) { constraints ->
        val suffixConstraints = constraints.copy(minWidth = 0, maxWidth = Constraints.Infinity)
        val suffixPlaceable = subcompose("suffix") {
            Box(
                modifier = Modifier.background(
                    color = colorResource(R.color.shape_tertiary),
                    shape = RoundedCornerShape(4.dp)
                )
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    text = suffix,
                    style = countStyle,
                    maxLines = 1,
                )
            }
        }.first().measure(suffixConstraints)

        // The available space for the main text is the total width minus the width of the suffix.
        val availableWidthForText = (constraints.maxWidth - suffixPlaceable.width).coerceAtLeast(0)
        val textConstraints = constraints.copy(minWidth = 0, maxWidth = availableWidthForText)
        val textPlaceable = subcompose("text") {
            Text(
                text = text,
                style = textStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }.first().measure(textConstraints)

        // If width constraints are specified (e.g., when using weight), use the full available width.
        val finalWidth =
            if (constraints.hasBoundedWidth) constraints.maxWidth else (textPlaceable.width + suffixPlaceable.width)
        val height = maxOf(textPlaceable.height, suffixPlaceable.height)
        layout(finalWidth, height) {
            // Align content to the left.
            textPlaceable.placeRelative(0, 0)
            val offsetYPx = with(density) { 0.5.dp.roundToPx() }
            val offsetXPx = with(density) { 8.dp.roundToPx() }
            suffixPlaceable.placeRelative(textPlaceable.width + offsetXPx, offsetYPx)
        }
    }
}