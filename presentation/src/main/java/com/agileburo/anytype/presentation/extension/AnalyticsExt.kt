package com.agileburo.anytype.presentation.extension

import com.agileburo.anytype.analytics.base.EventsDictionary.PROP_STYLE
import com.agileburo.anytype.analytics.base.EventsDictionary.PROP_TYPE
import com.agileburo.anytype.analytics.event.EventAnalytics
import com.agileburo.anytype.analytics.props.Props
import com.agileburo.anytype.domain.block.model.Block

fun Block.Prototype.getAnalyticsEvent(
    eventName: String,
    startTime: Long,
    middlewareTime: Long?
): EventAnalytics.Anytype {
    val props = when (this) {
        is Block.Prototype.Text -> {
            Props(
                mapOf(
                    PROP_TYPE to "text",
                    PROP_STYLE to getStyleName()
                )
            )
        }
        is Block.Prototype.Page -> {
            Props(
                mapOf(
                    PROP_STYLE to style.name
                )
            )
        }
        is Block.Prototype.File -> {
            Props(
                mapOf(
                    PROP_TYPE to "file",
                    PROP_STYLE to getStyleName()
                )
            )
        }
        is Block.Prototype.Link -> {
            Props(mapOf(PROP_TYPE to "link"))
        }
        Block.Prototype.Divider -> {
            Props(
                mapOf(
                    PROP_TYPE to "div",
                    PROP_STYLE to "line"
                )
            )
        }
        Block.Prototype.Bookmark -> {
            Props(mapOf(PROP_TYPE to "bookmark"))
        }
    }

    return EventAnalytics.Anytype(
        name = eventName,
        props = props,
        duration = EventAnalytics.Duration(
            start = startTime,
            middleware = middlewareTime
        )
    )
}

fun Block.Content.Text.Style.getProps(): Props {
    return Props(
        mapOf(
            PROP_TYPE to "text",
            PROP_STYLE to getStyleName()
        )
    )
}

fun Block.Content.Text.Style.getStyleName(): String = when (this) {
    Block.Content.Text.Style.P -> "Paragraph"
    Block.Content.Text.Style.H1 -> "Header1"
    Block.Content.Text.Style.H2 -> "Header2"
    Block.Content.Text.Style.H3 -> "Header3"
    Block.Content.Text.Style.H4 -> "Header4"
    Block.Content.Text.Style.TITLE -> "Title"
    Block.Content.Text.Style.QUOTE -> "Quote"
    Block.Content.Text.Style.CODE_SNIPPET -> "Code"
    Block.Content.Text.Style.BULLET -> "Bulleted"
    Block.Content.Text.Style.NUMBERED -> "Numbered"
    Block.Content.Text.Style.TOGGLE -> "Toggle"
    Block.Content.Text.Style.CHECKBOX -> "Checkbox"
}

fun Block.Prototype.Text.getStyleName() = this.style.getStyleName()

fun Block.Prototype.File.getStyleName() = when (this.type) {
    Block.Content.File.Type.NONE -> "None"
    Block.Content.File.Type.FILE -> "File"
    Block.Content.File.Type.IMAGE -> "Image"
    Block.Content.File.Type.VIDEO -> "Video"
}