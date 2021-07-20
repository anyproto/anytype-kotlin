package com.anytypeio.anytype.presentation.navigation

data class PageNavigationView(
    val title: String,
    val subtitle: String,
    val emoji: String?,
    val image: String?,
    val inbound: List<ObjectView>,
    val outbound: List<ObjectView>
)