package com.anytypeio.anytype.feature_chats.presentation

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Hash
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.LinkPreview
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.domain.chats.ChatContainer
import com.anytypeio.anytype.presentation.confgs.ChatConfig
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.search.GlobalSearchItemView

sealed interface ChatView {

    data class DateSection(
        val formattedDate: String,
        val timeInMillis: Long
    ) : ChatView

    data class Message(
        val id: String,
        val order: Id = "",
        val content: Content,
        val author: String,
        val creator: Id?,
        val timestamp: Long,
        val formattedDate: String = "",
        val attachments: List<Attachment> = emptyList(),
        val reactions: List<Reaction> = emptyList(),
        val isUserAuthor: Boolean = false,
        val shouldHideUsername: Boolean = false,
        val isEdited: Boolean = false,
        val avatar: Avatar = Avatar.Initials(),
        val reply: Reply? = null,
        val startOfUnreadMessageSection: Boolean = false
    ) : ChatView {

        val isMaxReactionCountReached: Boolean =
            reactions.size >= ChatConfig.MAX_REACTION_COUNT ||
            reactions.count { it.isSelected } >= ChatConfig.MAX_USER_REACTION_COUNT

        data class Content(val msg: String, val parts: List<Part>) {
            data class Part(
                val part: String,
                val styles: List<Block.Content.Text.Mark> = emptyList()
            ) {
                val isBold: Boolean = styles.any { it.type == Block.Content.Text.Mark.Type.BOLD }
                val isItalic: Boolean = styles.any { it.type == Block.Content.Text.Mark.Type.ITALIC }
                val isStrike = styles.any { it.type == Block.Content.Text.Mark.Type.STRIKETHROUGH }
                val underline = styles.any { it.type == Block.Content.Text.Mark.Type.UNDERLINE }
                val isCode = styles.any { it.type == Block.Content.Text.Mark.Type.KEYBOARD }

                val link = styles.find { it.type == Block.Content.Text.Mark.Type.LINK }
                val emoji = styles.find { it.type == Block.Content.Text.Mark.Type.EMOJI }
                val mention = styles.find { it.type == Block.Content.Text.Mark.Type.MENTION }
            }
        }

        data class Reply(
            val msg: Id,
            val text: String,
            val author: String
        )

        sealed class Attachment {

            data class Gallery(val images: List<Image>): Attachment() {

                val rowConfig = getRowConfiguration(images.size)

                private fun getRowConfiguration(imageCount: Int): List<Int> {
                    return when (imageCount) {
                        2 -> listOf(2)
                        3 -> listOf(1, 2)
                        4 -> listOf(2, 2)
                        5 -> listOf(2, 3)
                        6 -> listOf(3, 3)
                        7 -> listOf(2, 2, 3)
                        8 -> listOf(2, 3, 3)
                        9 -> listOf(3, 3, 3)
                        10 -> listOf(2, 2, 3, 3)
                        else -> listOf()
                    }
                }
            }

            data class Image(
                val target: Id,
                val url: String,
                val name: String,
                val ext: String
            ): Attachment()

            data class Video(
                val target: Id,
                val url: String,
                val name: String,
                val ext: String
            ): Attachment()

            data class Link(
                val target: Id,
                val wrapper: ObjectWrapper.Basic?,
                val icon: ObjectIcon = ObjectIcon.None,
                val typeName: String
            ): Attachment()

            data class Bookmark(
                val id: Id,
                val url: String,
                val title: String,
                val description: String,
                val imageUrl: String?
            ) : Attachment()
        }

        sealed class ChatBoxAttachment {

            data class Media(
                val uri: String,
                val state: State = State.Idle,
                val isVideo: Boolean = false,
                val capturedByCamera: Boolean = false
            ): ChatBoxAttachment()

            data class File(
                val uri: String,
                val name: String,
                val size: Int,
                val state: State = State.Idle
            ): ChatBoxAttachment()

            sealed class Existing : ChatBoxAttachment() {
                data class Image(
                    val target: Id,
                    val url: Url
                ) : Existing()

                data class Video(
                    val target: Id,
                    val url: Url
                ) : Existing()

                data class Link(
                    val target: Id,
                    val name: String,
                    val typeName: String,
                    val icon: ObjectIcon
                ) : Existing()
            }

            data class Link(
                val target: Id,
                val wrapper: GlobalSearchItemView
            ): ChatBoxAttachment()

            data class Bookmark(
                val preview: LinkPreview,
                val isLoadingPreview: Boolean = false,
                val isUploading: Boolean = false
            ) : ChatBoxAttachment()

            sealed class State {
                data object Idle : State()
                data object Uploading : State()
                data object Uploaded : State()
                data object Failed : State()
            }
        }

        data class Reaction(
            val emoji: String,
            val count: Int,
            val isSelected: Boolean = false
        )
        sealed class Avatar {
            data class Initials(val initial: String = ""): Avatar()
            data class Image(val hash: Hash): Avatar()
        }
    }
}

data class ChatViewState(
    val messages: List<ChatView> = emptyList(),
    val intent: ChatContainer.Intent = ChatContainer.Intent.None,
    val counter: Counter = Counter()
) {
    data class Counter(
        val messages: Int = 0,
        val mentions: Int = 0
    )
}