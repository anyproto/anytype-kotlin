package com.agileburo.anytype.analytics.event

import com.agileburo.anytype.analytics.props.Props

interface Event {

    val name: String
    val prettified: String
    val props: Props

    data class Duration(
        val total: Long?,
        val middleware: Long?
    )

    data class Anytype(
        override val name: String,
        override val prettified: String,
        override val props: Props,
        val duration: Duration?
    ) : Event
}