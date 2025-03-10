package com.anytypeio.anytype.core_ui.features.fields

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.light
import com.anytypeio.anytype.core_ui.views.Relations1
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.presentation.sets.model.TagView

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FieldTypeMultiSelect(
    modifier: Modifier = Modifier,
    title: String,
    tags: List<TagView>,
    isLocal: Boolean,
    onFieldClick: () -> Unit,
    onAddToCurrentTypeClick: () -> Unit,
    onRemoveFromObjectClick: () -> Unit,
) {
    val isMenuExpanded = remember { mutableStateOf(false) }
    val defaultModifier = modifier
        .combinedClickable(
            onClick = onFieldClick,
            onLongClick = {
                if (isLocal) isMenuExpanded.value = true
            }
        )
        .fillMaxWidth()
        .border(
            width = 1.dp,
            color = colorResource(id = R.color.shape_secondary),
            shape = RoundedCornerShape(12.dp)
        )
        .padding(vertical = 16.dp)
        .padding(horizontal = 16.dp)

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val halfScreenWidth = screenWidth / 2 - 32.dp

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
                text = title,
                style = Relations1,
                color = colorResource(id = R.color.text_secondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        TagRow(
            tags = tags,
            modifier = Modifier.fillMaxWidth(),
            textStyle = Relations1
        )
        FieldItemDropDownMenu(
            showMenu = isMenuExpanded.value,
            onDismissRequest = {
                isMenuExpanded.value = false
            },
            onAddToCurrentTypeClick = {
                isMenuExpanded.value = false
                onAddToCurrentTypeClick()
            },
            onRemoveFromObjectClick = {
                isMenuExpanded.value = false
                onRemoveFromObjectClick()
            }
        )
    }
}

/**
 * A composable that displays a single tag “chip” with text and a background.
 *
 * @param text The tag text.
 * @param backgroundColor The chip’s background color.
 * @param textStyle The [TextStyle] used for the tag text.
 * @param isSingle If true, the chip is rendered in single-mode – meaning that if the chip does not fit
 *        in the available width, its text will be truncated (TextOverflow.Ellipsis).
 * @param isOverflow If true, this chip is an overflow indicator (e.g. “+3”).
 * @param modifier Modifier to be applied to the chip.
 */
@Composable
fun TagChip(
    text: String,
    tagColor: String,
    textStyle: TextStyle,
    isSingle: Boolean = false,
    isOverflow: Boolean = false,
    modifier: Modifier = Modifier
) {
    // In single mode, we allow truncation.
    Box(
        modifier = modifier
            .wrapContentWidth()
            .background(light(tagColor), shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp)
    ) {
        Text(
            text = text,
            style = textStyle,
            color = dark(tagColor),
            maxLines = 1,
            overflow = if (isSingle) TextOverflow.Ellipsis else TextOverflow.Clip
        )
    }
}

/**
 * A composable that lays out a row of tags in a single horizontal line.
 *
 * The behavior is as follows:
 * 1. **Single tag case:** If there is only one tag, it is displayed in a row. If its intrinsic width
 *    exceeds the available width, the text is truncated (TextOverflow.Ellipsis).
 *
 * 2. **Multiple tags case:** The layout tries to display as many tags as possible in full (i.e. without truncation).
 *    - If a tag would be rendered with truncation, it is omitted and all remaining tags are replaced by
 *      an overflow chip (e.g. “+n”).
 *    - For example, if the first tag is short and fits but the second tag’s full width would exceed the available space,
 *      then only the first tag is displayed and an overflow chip shows the remaining count.
 *
 * @param tags The list of [Tag] objects to display.
 * @param modifier Modifier to be applied to the overall layout.
 * @param textStyle The [TextStyle] used for the tag text.
 * @param spacing The spacing (in dp) between adjacent tags.
 * @param overflowChipColor The background color for the overflow chip.
 */
@Composable
fun TagRow(
    tags: List<TagView>,
    modifier: Modifier = Modifier,
    textStyle: TextStyle,
    spacing: Dp = 4.dp,
    overflowChipColor: Color = Color.Red
) {
    val density = LocalDensity.current

    SubcomposeLayout(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopStart)
    ) { constraints ->
        val availableWidth = constraints.maxWidth
        val spacingPx = spacing.roundToPx()

        // If there are no tags, layout an empty box.
        if (tags.isEmpty()) {
            return@SubcomposeLayout layout(0, 0) {}
        }

        // --- Single tag case ---
        if (tags.size == 1) {
            // Render the single tag in "single" mode so that it truncates if needed.
            val tagPlaceable = subcompose("tag0") {
                TagChip(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    text = tags[0].tag,
                    tagColor = tags[0].color,
                    textStyle = textStyle,
                    isSingle = true
                )
            }.first().measure(constraints)
            return@SubcomposeLayout layout(
                width = availableWidth,
                height = tagPlaceable.height
            ) {
                tagPlaceable.placeRelative(0, 0)
            }
        }


        // --- Multiple tags case ---
        val measuredPlaceables = mutableListOf<Placeable>()
        var consumedWidth = 0
        var shownTagCount = 0

        // Iterate over tags and measure their full intrinsic width (i.e. no truncation).
        for ((index, tag) in tags.withIndex()) {
            // Measure the tag chip with an "unbounded" width to get its full intrinsic width.
            val tagPlaceable = subcompose("tag$index") {
                TagChip(
                    text = tags[index].tag,
                    tagColor = tags[index].color,
                    textStyle = textStyle,
                    isSingle = false
                )
            }.first().measure(constraints.copy(maxWidth = Constraints.Infinity))

            // Calculate additional spacing (if not the first tag).
            val additionalSpacing = if (shownTagCount > 0) spacingPx else 0

            // How many tags would remain if we add this tag?
            val remainingCount = tags.size - (shownTagCount + 1)
            // Pre-measure an overflow chip if needed, using a unique key.
            val overflowPlaceableCandidate = if (remainingCount > 0) {
                subcompose("overflow_$index") {
                    TagChip(
                        text = "+$remainingCount",
                        tagColor = tags[index].color,
                        textStyle = textStyle,
                        isOverflow = true
                    )
                }.first().measure(constraints.copy(maxWidth = Constraints.Infinity))
            } else {
                null
            }

            // Compute candidate width: current consumed width + spacing + tag width +
            // (if needed, spacing and overflow chip width)
            val candidateWidth = consumedWidth +
                    additionalSpacing +
                    tagPlaceable.width +
                    (if (overflowPlaceableCandidate != null) spacingPx + overflowPlaceableCandidate.width else 0)

            // If the candidate width fits into the available width, accept this tag.
            if (candidateWidth <= availableWidth) {
                measuredPlaceables.add(tagPlaceable)
                consumedWidth += additionalSpacing + tagPlaceable.width
                shownTagCount++
            } else {
                // Otherwise, do not include this tag; break out of the loop.
                break
            }
        }

        // Calculate the number of remaining tags.
        val remainingCount = tags.size - shownTagCount
        val overflowPlaceable = if (remainingCount > 0) {
            subcompose("overflow_final") {
                Box(
                    modifier = Modifier.background(
                        color = colorResource(R.color.shape_tertiary),
                        shape = RoundedCornerShape(4.dp)
                    )
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        text = "+$remainingCount",
                        style = Relations2.copy(
                            color = colorResource(id = R.color.text_secondary)
                        ),
                        maxLines = 1,
                    )
                }
            }.first().measure(constraints.copy(maxWidth = Constraints.Infinity))
        } else {
            null
        }

        // Final width is the sum of consumed width plus spacing and overflow chip (if present)
        val totalWidth = if (overflowPlaceable != null) {
            consumedWidth + spacingPx + overflowPlaceable.width
        } else {
            consumedWidth
        }
        val maxHeight =
            (measuredPlaceables.map { it.height } + listOf(overflowPlaceable?.height ?: 0))
                .maxOrNull() ?: 0

        layout(totalWidth, maxHeight) {
            var xPosition = 0
            measuredPlaceables.forEach { placeable ->
                placeable.placeRelative(xPosition, 0)
                xPosition += placeable.width + spacingPx
            }
            val offsetYPx = with(density) { 1.dp.roundToPx() }
            overflowPlaceable?.placeRelative(xPosition, offsetYPx)
        }
    }
}