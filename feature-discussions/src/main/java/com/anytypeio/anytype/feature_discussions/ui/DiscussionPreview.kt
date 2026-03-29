package com.anytypeio.anytype.feature_discussions.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionHeader
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionView
@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
private fun DiscussionScreenPreview() {
    val header = DiscussionHeader(
        title = "Update Desktop Menus",
        commentCount = 15
    )
    val comments = listOf(
        DiscussionView.Comment(
            id = "1",
            author = "John Doe",
            creator = "user1",
            timestamp = 1710000000000L,
            formattedDate = "Mar 9, 14:30",
            avatar = DiscussionView.Avatar.Initials(initial = "J"),
            content = DiscussionView.Content(
                msg = "I think we should update the file menu first. The current layout is confusing for new users.",
                parts = listOf(
                    DiscussionView.Content.Part(
                        part = "I think we should update the "
                    ),
                    DiscussionView.Content.Part(
                        part = "file menu",
                        styles = listOf(
                            Block.Content.Text.Mark(
                                range = IntRange(0, 8),
                                type = Block.Content.Text.Mark.Type.BOLD
                            )
                        )
                    ),
                    DiscussionView.Content.Part(
                        part = " first. The current layout is confusing for new users."
                    )
                )
            ),
            reactions = listOf(
                DiscussionView.Reaction(emoji = "\uD83D\uDC4D", count = 3),
                DiscussionView.Reaction(emoji = "\uD83D\uDE80", count = 1)
            ),
            replyCount = 2
        ),
        DiscussionView.Reply(
            id = "2",
            author = "Jane Smith",
            creator = "user2",
            timestamp = 1710003600000L,
            formattedDate = "Mar 9, 15:00",
            avatar = DiscussionView.Avatar.Initials(initial = "J"),
            content = DiscussionView.Content(
                msg = "Agreed! I can take care of the file menu redesign.",
                parts = listOf(
                    DiscussionView.Content.Part(
                        part = "Agreed! I can take care of the file menu redesign."
                    )
                )
            )
        ),
        DiscussionView.Comment(
            id = "3",
            author = "Alex Johnson",
            creator = "user3",
            timestamp = 1710090000000L,
            formattedDate = "Mar 10, 09:00",
            avatar = DiscussionView.Avatar.Initials(initial = "A"),
            content = DiscussionView.Content(
                msg = "We also need to add keyboard shortcuts for all new menu items.",
                parts = listOf(
                    DiscussionView.Content.Part(
                        part = "We also need to add keyboard shortcuts for all new menu items."
                    )
                )
            ),
            reactions = listOf(
                DiscussionView.Reaction(emoji = "✅", count = 5)
            )
        )
    )
    DiscussionScreen(
        header = header,
        comments = comments,
        onBackClicked = {}
    )
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
private fun DiscussionCommentItemPreview() {
    DiscussionCommentItem(
        comment = DiscussionView.Comment(
            id = "1",
            author = "John Doe",
            creator = "user1",
            timestamp = 1710000000000L,
            formattedDate = "Mar 9, 14:30",
            avatar = DiscussionView.Avatar.Initials(initial = "J"),
            content = DiscussionView.Content(
                msg = "This is a sample comment with bold and italic text.",
                parts = listOf(
                    DiscussionView.Content.Part(part = "This is a sample comment with "),
                    DiscussionView.Content.Part(
                        part = "bold",
                        styles = listOf(
                            Block.Content.Text.Mark(
                                range = IntRange(0, 3),
                                type = Block.Content.Text.Mark.Type.BOLD
                            )
                        )
                    ),
                    DiscussionView.Content.Part(part = " and "),
                    DiscussionView.Content.Part(
                        part = "italic",
                        styles = listOf(
                            Block.Content.Text.Mark(
                                range = IntRange(0, 5),
                                type = Block.Content.Text.Mark.Type.ITALIC
                            )
                        )
                    ),
                    DiscussionView.Content.Part(part = " text.")
                )
            ),
            reactions = listOf(
                DiscussionView.Reaction(emoji = "\uD83D\uDC4D", count = 3),
                DiscussionView.Reaction(emoji = "❤\uFE0F", count = 2),
                DiscussionView.Reaction(emoji = "\uD83D\uDE80", count = 1)
            ),
            replyCount = 4
        )
    )
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
private fun DiscussionReplyItemPreview() {
    DiscussionReplyItem(
        reply = DiscussionView.Reply(
            id = "2",
            author = "Jane Smith",
            creator = "user2",
            timestamp = 1710003600000L,
            formattedDate = "Mar 9, 15:00",
            avatar = DiscussionView.Avatar.Initials(initial = "J"),
            content = DiscussionView.Content(
                msg = "Great point! I'll look into this right away.",
                parts = listOf(
                    DiscussionView.Content.Part(
                        part = "Great point! I'll look into this right away."
                    )
                )
            ),
            reactions = listOf(
                DiscussionView.Reaction(emoji = "\uD83D\uDC4D", count = 1)
            )
        )
    )
}
