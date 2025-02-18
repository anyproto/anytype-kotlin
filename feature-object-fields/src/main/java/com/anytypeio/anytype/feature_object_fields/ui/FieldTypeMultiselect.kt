package com.anytypeio.anytype.feature_object_fields.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.Relations1
import com.anytypeio.anytype.feature_object_fields.R


/**
 * Data class representing a Tag.
 *
 * @param text The tag's text.
 * @param backgroundColor The background color of the tag.
 */
data class Tag(val text: String, val backgroundColor: Color)

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
    backgroundColor: Color,
    textStyle: TextStyle,
    isSingle: Boolean = false,
    isOverflow: Boolean = false,
    modifier: Modifier = Modifier
) {
    // In single mode, we allow truncation.
    Box(
        modifier = modifier
            .wrapContentWidth()
            .background(backgroundColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp)
    ) {
        Text(
            text = text,
            style = textStyle,
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
    tags: List<Tag>,
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
                    text = tags[0].text,
                    backgroundColor = tags[0].backgroundColor,
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
                    text = tag.text,
                    backgroundColor = tag.backgroundColor,
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
                        backgroundColor = overflowChipColor,
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
                TagChip(
                    text = "+$remainingCount",
                    backgroundColor = overflowChipColor,
                    textStyle = textStyle,
                    isOverflow = true
                )
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
        val maxHeight = (measuredPlaceables.map { it.height } + listOf(overflowPlaceable?.height ?: 0))
            .maxOrNull() ?: 0

        layout(totalWidth, maxHeight) {
            var xPosition = 0
            measuredPlaceables.forEach { placeable ->
                placeable.placeRelative(xPosition, 0)
                xPosition += placeable.width + spacingPx
            }
            overflowPlaceable?.placeRelative(xPosition, 0)
        }
    }
}

@DefaultPreviews
@Composable
fun TagsPreview() {
    LazyColumn {
        item {
            TagRow(
                tags = listOf(
                    Tag(
                        text = "Tag 1",
                        backgroundColor = colorResource(R.color.palette_system_sky)
                    ),
                    Tag(
                        text = "Tag 2",
                        backgroundColor = colorResource(R.color.palette_system_yellow)
                    ),
                    Tag(text = "Tag 3", backgroundColor = Color.Red),
                    Tag(text = "Tag 4", backgroundColor = Color.Yellow),
                    Tag(text = "Tag 5", backgroundColor = Color.Blue),
                    Tag(text = "Tag 6", backgroundColor = Color.Green),
                    Tag(text = "Tag 7", backgroundColor = Color.Magenta),
                    Tag(text = "Tag 8", backgroundColor = Color.Cyan),
                    Tag(text = "Tag 9", backgroundColor = Color.Gray),
                    Tag(text = "Tag 10", backgroundColor = Color.Black),
                    Tag(text = "Tag 11", backgroundColor = Color.White),
                ),
                textStyle = Relations1.copy(
                    color = colorResource(R.color.text_primary),
                )
            )
        }
    }
}