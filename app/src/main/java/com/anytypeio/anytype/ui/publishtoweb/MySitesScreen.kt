package com.anytypeio.anytype.ui.publishtoweb

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Header
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineSubheading
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.presentation.publishtoweb.MySitesViewState

@Composable
fun MySitesScreen(
    viewState: MySitesViewState,
    onViewObjectClicked: (MySitesViewState.Item) -> Unit = {},
    onOpenInBrowserClicked: (MySitesViewState.Item) -> Unit = {},
    onCopyWebLinkClicked: (MySitesViewState.Item) -> Unit = {},
    onUnpublishClicked: (MySitesViewState.Item) -> Unit = {},
    onBackClicked: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.background_primary))
            .systemBarsPadding()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Header(
                text = stringResource(R.string.my_sites)
            )
            Image(
                painter = painterResource(R.drawable.ic_default_top_back),
                contentDescription = null,
                modifier = Modifier
                    .noRippleClickable { onBackClicked() }
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
            )
        }
        if (viewState is MySitesViewState.Loading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(alignment = Alignment.Center)
                        .size(32.dp)
                    ,
                    color = colorResource(R.color.glyph_active),
                    trackColor = colorResource(R.color.glyph_active).copy(alpha = 0.5f),
                    strokeWidth = 2.dp
                )
            }
        }
        if (viewState is MySitesViewState.Content) {
            if (viewState.items.isEmpty()) {
                MySitesEmptyState(
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(
                        items = viewState.items,
                        key = { _, item -> item.obj },
                    ) { index, item ->

                        var showDropdownMenu by remember { mutableStateOf(false) }

                        Row(
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        onOpenInBrowserClicked(item)
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showDropdownMenu = true
                                    }
                                )
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth()
                                .height(72.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ListWidgetObjectIcon(
                                modifier = Modifier,
                                icon = item.icon
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = item.name,
                                    style = PreviewTitle2Medium,
                                    color = colorResource(R.color.text_primary),
                                    maxLines = 1
                                )
                                Text(
                                    text = "${item.timestamp} • ${item.size}",
                                    style = Caption1Regular,
                                    color = colorResource(R.color.text_secondary),
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Box {
                                Image(
                                    painter = painterResource(R.drawable.ic_action_more),
                                    contentDescription = "Three-dots button",
                                    modifier = Modifier.clickable {
                                        showDropdownMenu = !showDropdownMenu
                                    }
                                )

                                MaterialTheme(
                                    shapes = MaterialTheme.shapes.copy(
                                        medium = RoundedCornerShape(16.dp)
                                    ),
                                    colors = MaterialTheme.colors.copy(
                                        surface = colorResource(id = R.color.background_secondary)
                                    )
                                ) {
                                    DropdownMenu(
                                        offset = DpOffset(0.dp, 8.dp),
                                        expanded = showDropdownMenu,
                                        onDismissRequest = {
                                            showDropdownMenu = false
                                        },
                                        properties = PopupProperties(focusable = false)
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = stringResource(R.string.web_publishing_view_object),
                                                    color = colorResource(id = R.color.text_primary),
                                                    modifier = Modifier.padding(end = 64.dp)
                                                )
                                            },
                                            onClick = {
                                                onViewObjectClicked(item)
                                                showDropdownMenu = false
                                            }
                                        )
                                        Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = stringResource(R.string.web_publishing_open_in_browser),
                                                    color = colorResource(id = R.color.text_primary),
                                                    modifier = Modifier.padding(end = 64.dp)
                                                )
                                            },
                                            onClick = {
                                                onOpenInBrowserClicked(item)
                                                showDropdownMenu = false
                                            }
                                        )
                                        Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = stringResource(R.string.web_publishing_copy_web_link),
                                                    color = colorResource(id = R.color.text_primary),
                                                    modifier = Modifier.padding(end = 64.dp)
                                                )
                                            },
                                            onClick = {
                                                onCopyWebLinkClicked(item)
                                                showDropdownMenu = false
                                            }
                                        )
                                        Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = stringResource(R.string.web_publishing_unpublish),
                                                    color = colorResource(id = R.color.palette_system_red),
                                                    modifier = Modifier.padding(end = 64.dp)
                                                )
                                            },
                                            onClick = {
                                                onUnpublishClicked(item)
                                                showDropdownMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        if (index < viewState.items.size - 1) {
                            Divider(
                                paddingStart = 16.dp,
                                paddingEnd = 16.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@DefaultPreviews
@Composable
fun MySitesScreenPreview() {
    MaterialTheme {
        MySitesScreen(
            viewState = MySitesViewState.Content(
                items = listOf(
                    MySitesViewState.Item(
                        name = "Name",
                        size = "Size",
                        timestamp = "Timestamp",
                        icon = ObjectIcon.None,
                        space = SpaceId(""),
                        obj = "",
                        uri = "some uri"
                    )
                )
            )
        )
    }
}

@Composable
fun MySitesEmptyState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.ic_my_sites_empty_state),
                contentDescription = null
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.my_sites_empty_state_text),
                color = colorResource(R.color.text_primary),
                textAlign = TextAlign.Center,
                style = BodyRegular
            )
        }
    }
}

@DefaultPreviews
@Composable
fun MySitesViewStatePreview() {
    MySitesEmptyState()
}
