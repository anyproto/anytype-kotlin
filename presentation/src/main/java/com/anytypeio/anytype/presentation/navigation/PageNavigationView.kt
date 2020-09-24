package com.anytypeio.anytype.presentation.navigation

import com.anytypeio.anytype.core_ui.features.navigation.PageLinkView

data class PageNavigationView(
    val title: String,
    val subtitle: String,
    val emoji: String?,
    val image: String?,
    val inbound: List<PageLinkView>,
    val outbound: List<PageLinkView>
)