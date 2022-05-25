package com.anytypeio.anytype.analytics.event

import com.anytypeio.anytype.analytics.props.Props

interface EventAnalytics {

    val name: String
    val props: Props

    data class Duration(
        var start: Long?,
        var middleware: Long?,
        var render: Long? = null
    )

    data class Anytype(
        override val name: String,
        override val props: Props = Props.empty(),
        val duration: Duration? = null
    ) : EventAnalytics
}