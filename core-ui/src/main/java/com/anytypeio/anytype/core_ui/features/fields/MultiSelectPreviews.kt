package com.anytypeio.anytype.core_ui.features.fields

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.presentation.relations.value.tagstatus.RelationsListItem

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
                    RelationsListItem.Item.Tag(
                        name = "Urgent",
                        color = ThemeColor.RED,
                        number = 1,
                        isSelected = true,
                        optionId = "1"
                    ),
                    RelationsListItem.Item.Tag(
                        name = "Personal",
                        color = ThemeColor.ORANGE,
                        number = 1,
                        isSelected = true,
                        optionId = "2"
                    ),
                    RelationsListItem.Item.Tag(
                        name = "Done",
                        color = ThemeColor.LIME,
                        number = 1,
                        isSelected = true,
                        optionId = "3"
                    ),
                    RelationsListItem.Item.Tag(
                        name = "In Progress",
                        color = ThemeColor.BLUE,
                        number = 1,
                        isSelected = true,
                        optionId = "4"
                    ),
                    RelationsListItem.Item.Tag(
                        name = "Waiting",
                        color = ThemeColor.YELLOW,
                        number = 1,
                        isSelected = true,
                        optionId = "5"
                    ),
                    RelationsListItem.Item.Tag(
                        name = "Blocked",
                        color = ThemeColor.PURPLE,
                        number = 1,
                        isSelected = true,
                        optionId = "6"
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
                    RelationsListItem.Item.Tag(
                        name = "This is an extremely long tag that should be truncated if it doesn't fit in the available space",
                        color = ThemeColor.BLUE,
                        number = 1,
                        isSelected = true,
                        optionId = "1"
                    )
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
                    RelationsListItem.Item.Tag(
                        name = "Short",
                        color = ThemeColor.TEAL,
                        number = 1,
                        isSelected = true,
                        optionId = "1"
                    )
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
                    RelationsListItem.Item.Tag(
                        name = "Urgent",
                        color = ThemeColor.RED,
                        number = 1,
                        isSelected = true,
                        optionId = "1"
                    ),
                    RelationsListItem.Item.Tag(
                        name = "This is a very long tag that might not fit entirely",
                        color = ThemeColor.ORANGE,
                        number = 1,
                        isSelected = true,
                        optionId = "2"
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
                    RelationsListItem.Item.Tag(
                        name = "Urgent",
                        color = ThemeColor.RED,
                        number = 1,
                        isSelected = true,
                        optionId = "1"
                    ),
                    RelationsListItem.Item.Tag(
                        name = "Personal",
                        color = ThemeColor.ORANGE,
                        number = 1,
                        isSelected = true,
                        optionId = "2"
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
                    RelationsListItem.Item.Tag(
                        name = "Urgent",
                        color = ThemeColor.RED,
                        number = 1,
                        isSelected = true,
                        optionId = "1"
                    ),
                    RelationsListItem.Item.Tag(
                        name = "Personal",
                        color = ThemeColor.ORANGE,
                        number = 1,
                        isSelected = true,
                        optionId = "2"
                    ),
                    RelationsListItem.Item.Tag(
                        name = "Done",
                        color = ThemeColor.LIME,
                        number = 1,
                        isSelected = true,
                        optionId = "3"
                    )
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
                    RelationsListItem.Item.Tag(
                        name = "Urgent",
                        color = ThemeColor.RED,
                        number = 1,
                        isSelected = true,
                        optionId = "1"
                    ),
                    RelationsListItem.Item.Tag(
                        name = "Personal",
                        color = ThemeColor.ORANGE,
                        number = 1,
                        isSelected = true,
                        optionId = "2"
                    ),
                    RelationsListItem.Item.Tag(
                        name = "Done",
                        color = ThemeColor.LIME,
                        number = 1,
                        isSelected = true,
                        optionId = "3"
                    ),
                    RelationsListItem.Item.Tag(
                        name = "In Progress",
                        color = ThemeColor.BLUE,
                        number = 1,
                        isSelected = true,
                        optionId = "4"
                    )
                ),
                title = "Tag"
            )
        }
    }
}