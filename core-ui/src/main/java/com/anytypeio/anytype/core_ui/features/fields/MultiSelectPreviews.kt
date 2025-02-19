package com.anytypeio.anytype.core_ui.features.fields

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.presentation.relations.value.tagstatus.RelationsListItem
import com.anytypeio.anytype.presentation.sets.model.TagView

// --------------------
// Test Case 1: Multiple Tags (Your First Test Case)
// --------------------
@DefaultPreviews
@Composable
fun TagsPreview() {
    LazyColumn {
        item {
            FieldTypeMultiSelect(
                tags = listOf(
                    TagView(
                        id = "1",
                        tag = "Urgent",
                        color = ThemeColor.RED.code,
                    ),
                    TagView(
                        id = "2",
                        tag = "Personal",
                        color = ThemeColor.ORANGE.code,
                    ),
                    TagView(
                        id = "3",
                        tag = "Done",
                        color = ThemeColor.LIME.code,
                    ),
                    TagView(
                        id = "4",
                        tag = "In Progress",
                        color = ThemeColor.BLUE.code,
                    ),
                    TagView(
                        id = "5",
                        tag = "Waiting",
                        color = ThemeColor.YELLOW.code,
                    ),
                    TagView(
                        id = "6",
                        tag = "Blocked",
                        color = ThemeColor.PURPLE.code,
                    ),
                    TagView(
                        id = "7",
                        tag = "Spam",
                        color = ThemeColor.PINK.code,
                    )
                ),
                title = "Tag"
            )
        }
    }
}

// --------------------
// Test Case 2: Single Tag with a Very Long Name (truncated)
// --------------------
@Preview(showBackground = true)
@Composable
fun SingleLongTagPreview() {
    LazyColumn {
        item {
            FieldTypeMultiSelect(
                tags = listOf(
                    TagView(
                        id = "1",
                        tag = "This is an extremely long tag that should be truncated if it doesn't fit in the available space",
                        color = ThemeColor.RED.code,
                    ),
                ),
                title = "Tag"
            )
        }
    }
}

// --------------------
// Test Case 3: Single Tag with a Short Name
// --------------------
@Preview(showBackground = true)
@Composable
fun SingleShortTagPreview() {
    LazyColumn {
        item {
            FieldTypeMultiSelect(
                tags = listOf(
                    TagView(
                        id = "1",
                        tag = "Urgent",
                        color = ThemeColor.RED.code,
                    ),
                ),
                title = "Tag"
            )
        }
    }
}

// --------------------
// Test Case 4: Two Tags – First Tag Short, Second Tag Very Long (second omitted → overflow)
// --------------------
@Preview(showBackground = true)
@Composable
fun TwoTagsFirstShortSecondLongPreview() {
    LazyColumn {
        item {
            FieldTypeMultiSelect(
                tags = listOf(
                    TagView(
                        id = "1",
                        tag = "Urgent",
                        color = ThemeColor.RED.code,
                    ),
                    TagView(
                        id = "2",
                        tag = "This is a very long tag that might not fit entirely",
                        color = ThemeColor.ORANGE.code,
                    )
                ),
                title = "Tag"
            )
        }
    }
}

// --------------------
// Test Case 5: Two Short Tags (both displayed)
// --------------------
@Preview(showBackground = true)
@Composable
fun TwoShortTagsPreview() {
    LazyColumn {
        item {
            FieldTypeMultiSelect(
                tags = listOf(
                    TagView(
                        id = "1",
                        tag = "Urgent",
                        color = ThemeColor.RED.code,
                    ),
                    TagView(
                        id = "2",
                        tag = "Personal",
                        color = ThemeColor.ORANGE.code,
                    )
                ),
                title = "Tag"
            )
        }
    }
}

// --------------------
// Test Case 6: Three Short Tags (all displayed)
// --------------------
@Preview(showBackground = true)
@Composable
fun ThreeShortTagsPreview() {
    LazyColumn {
        item {
            FieldTypeMultiSelect(
                tags = listOf(
                    TagView(
                        id = "1",
                        tag = "Urgent",
                        color = ThemeColor.RED.code,
                    ),
                    TagView(
                        id = "2",
                        tag = "Personal",
                        color = ThemeColor.ORANGE.code,
                    ),
                    TagView(
                        id = "3",
                        tag = "Done",
                        color = ThemeColor.LIME.code,
                    ),
                ),
                title = "Tag"
            )
        }
    }
}

// --------------------
// Test Case 7: Four Tags with Overflow (only some tags displayed, remainder shown as +n)
// --------------------
@Preview(showBackground = true)
@Composable
fun FourTagsWithOverflowPreview() {
    LazyColumn {
        item {
            FieldTypeMultiSelect(
                tags = listOf(
                    TagView(
                        id = "1",
                        tag = "Urgent",
                        color = ThemeColor.RED.code,
                    ),
                    TagView(
                        id = "2",
                        tag = "Personal",
                        color = ThemeColor.ORANGE.code,
                    ),
                    TagView(
                        id = "3",
                        tag = "Done",
                        color = ThemeColor.LIME.code,
                    ),
                    TagView(
                        id = "4",
                        tag = "In Progress",
                        color = ThemeColor.BLUE.code,
                    )
                ),
                title = "Tag"
            )
        }
    }
}