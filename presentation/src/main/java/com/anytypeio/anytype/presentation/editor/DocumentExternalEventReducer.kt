package com.anytypeio.anytype.presentation.editor

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.core_utils.ext.replace
import com.anytypeio.anytype.core_utils.tools.toPrettyString
import com.anytypeio.anytype.presentation.common.StateReducer
import timber.log.Timber

/**
 * Reduces external events (coming not from user, but from outside) to state.
 */
class DocumentExternalEventReducer : StateReducer<List<Block>, Event> {

    override val function: suspend (List<Block>, Event) -> List<Block>
        get() = { state, event ->
            reduce(
                state,
                event
            )
        }

    override suspend fun reduce(state: List<Block>, event: Event): List<Block> = when (event) {
        is Event.Command.ShowObject -> event.blocks
        is Event.Command.AddBlock -> state + event.blocks
        is Event.Command.UpdateStructure -> state.replace(
            replacement = { target ->
                target.copy(children = event.children)
            },
            target = { block -> block.id == event.id }
        )
        is Event.Command.DeleteBlock -> state.filter { !event.targets.contains(it.id) }
        is Event.Command.GranularChange -> state.replace(
            replacement = { block ->
                when (val content = block.content) {
                    is Block.Content.RelationBlock -> {
                        block.copy(
                            content = content.copy(),
                            backgroundColor = event.backgroundColor ?: block.backgroundColor,
                        )
                    }
                    is Block.Content.Text -> {
                        block.copy(
                            content = content.copy(
                                style = event.style ?: content.style,
                                color = event.color ?: content.color,
                                text = event.text ?: content.text,
                                marks = event.marks ?: content.marks,
                                isChecked = event.checked ?: content.isChecked,
                                align = event.alignment ?: content.align,
                                iconEmoji = event.emojiIcon ?: content.iconEmoji,
                                iconImage = event.imageIcon ?: content.iconImage
                            ),
                            backgroundColor = event.backgroundColor ?: block.backgroundColor,
                        )
                    }
                    else -> block.copy(
                        backgroundColor = event.backgroundColor ?: block.backgroundColor,
                    )
                }
            },
            target = { block -> block.id == event.id }
        )
        is Event.Command.UpdateFields -> state.replace(
            replacement = { block -> block.copy(fields = event.fields) },
            target = { block -> block.id == event.target }
        )

        is Event.Command.UpdateFileBlock -> state.replace(
            replacement = { block ->
                val content = block.content<Block.Content.File>()
                block.copy(
                    content = content.copy(
                        targetObjectId = event.targetObjectId ?: content.targetObjectId,
                        name = event.name ?: content.name,
                        mime = event.mime ?: content.mime,
                        size = event.size ?: content.size,
                        type = event.type ?: content.type,
                        state = event.state ?: content.state
                    )
                )
            },
            target = { block -> block.id == event.blockId }
        )
        is Event.Command.BookmarkGranularChange -> state.replace(
            replacement = { block ->
                val content = block.content<Block.Content.Bookmark>()
                block.copy(
                    content = content.copy(
                        url = event.url ?: content.url,
                        title = event.title ?: content.title,
                        description = event.description ?: content.description,
                        image = event.image ?: content.image,
                        favicon = event.favicon ?: content.favicon,
                        state = event.state ?: content.state,
                        targetObjectId = event.targetObjectId ?: content.targetObjectId
                    )
                )
            },
            target = { block -> block.id == event.target }
        )
        is Event.Command.LinkGranularChange -> state.replace(
            replacement = { block ->
                val content = block.content<Block.Content.Link>()
                val iconSize = event.iconSize ?: content.iconSize
                val cardStyle = event.cardStyle ?: content.cardStyle
                val description = event.description ?: content.description
                val relations = event.relations ?: content.relations
                block.copy(
                    content = content.copy(
                        iconSize = iconSize,
                        cardStyle = cardStyle,
                        description = description,
                        relations = relations
                    )
                )
            },
            target = { block -> block.id == event.id }
        )
        is Event.Command.UpdateDividerBlock -> state.replace(
            replacement = { block ->
                val content = block.content<Block.Content.Divider>()
                block.copy(
                    content = content.copy(
                        style = event.style
                    )
                )
            },
            target = { block -> block.id == event.id }
        )
        is Event.Command.BlockEvent.SetRelation -> state.replace(
            replacement = { block ->
                val content = block.content<Block.Content.RelationBlock>()
                block.copy(
                    content = content.copy(
                        key = event.key
                    )
                )
            },
            target = { block -> block.id == event.id }
        )
        is Event.Command.DataView.SetTargetObjectId -> {
            state.replace(
                replacement = { block ->
                    val content = block.content<Block.Content.DataView>()
                    block.copy(
                        content = content.copy(
                            targetObjectId = event.targetObjectId
                        )
                    )
                },
                target = { block -> block.id == event.dv }
            )
        }
        else -> state.also {
            Timber.d("Ignoring event: ${event::class.java.canonicalName}:\n${event.toPrettyString()}")
        }
    }
}

typealias Flag = Int

object Flags {
    const val FLAG_REFRESH: Flag = 0
    val skipRefreshKeys = listOf(
        Relations.NAME,
        Relations.LAST_MODIFIED_DATE,
        Relations.SNIPPET,
        Relations.SYNC_DATE,
        Relations.SYNC_STATUS,
        Relations.INTERNAL_FLAGS
    )
}

fun List<Event>.flags(ctx: Id): List<Flag> {
    forEach { event ->
        when (event) {
            is Event.Command.Details.Amend -> {
                if (event.target == ctx) {
                    if (event.details.keys.any { key -> !Flags.skipRefreshKeys.contains(key) }) {
                        return listOf(Flags.FLAG_REFRESH)
                    }
                } else {
                    return listOf(Flags.FLAG_REFRESH)
                }
            }
            is Event.Command.Details.Unset -> {
                if (event.target == ctx) {
                    if (event.keys.any { key -> !Flags.skipRefreshKeys.contains(key) }) {
                        return listOf(Flags.FLAG_REFRESH)
                    }
                } else {
                    return listOf(Flags.FLAG_REFRESH)
                }
            }
            else -> {
                return listOf(Flags.FLAG_REFRESH)
            }
        }
    }
    return emptyList()
}