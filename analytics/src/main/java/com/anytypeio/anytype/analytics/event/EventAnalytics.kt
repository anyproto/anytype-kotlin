package com.anytypeio.anytype.analytics.event

import com.anytypeio.anytype.analytics.props.Props

interface EventAnalytics {

    val name: String
    val prettified: String?
    val props: Props

    data class Duration(
        val start: Long?,
        val middleware: Long?,
        val render: Long? = null
    )

    data class Anytype(
        override val name: String,
        override val prettified: String? = null,
        override val props: Props,
        val duration: Duration?
    ) : EventAnalytics
}