package com.anytypeio.anytype.feature_object_type.fields.ui

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.simpleIcon
import com.anytypeio.anytype.core_ui.extensions.swapList
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Medium
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.presentation.relations.UiSpacePropertiesScreenState
import com.anytypeio.anytype.presentation.relations.UiSpacePropertyItem

@Composable
fun SpacePropertiesListScreen(
    uiState: UiSpacePropertiesScreenState,
    onBackPressed: () -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = colorResource(id = R.color.background_primary),
        contentColor = colorResource(id = R.color.background_primary),
        topBar = {
            Topbar(
                modifier = Modifier
                    .height(48.dp),
                onBackPressed = onBackPressed

            )
        },
        content = { paddingValues ->
            val contentModifier =
                if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK)
                    Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding())
                else
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)

            val lazyListState = rememberLazyListState()

            val items = remember {
                mutableStateListOf<UiSpacePropertyItem>()
            }
            items.swapList(uiState.items)

            LazyColumn(
                modifier = contentModifier,
                state = lazyListState,
            ) {
                items(
                    count = items.size,
                    key = { index -> items[index].id },
                    itemContent = {
                        val item = items[it]
                        Relation(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp)
                                .noRippleThrottledClickable {
                                    // Handle click
                                },
                            item = item
                        )
                    }
                )
            }
        }
    )
}

@Composable
private fun Relation(
    modifier: Modifier,
    item: UiSpacePropertyItem
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(start = 0.dp, top = 14.dp, end = 12.dp, bottom = 14.dp)
                .wrapContentSize()
        ) {
            item.format.simpleIcon()?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = "Relation format icon",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        val name = item.name.trim().ifBlank { stringResource(R.string.untitled) }

        Text(
            text = name,
            style = PreviewTitle1Medium,
            color = colorResource(id = R.color.text_primary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Topbar(
    modifier: Modifier,
    onBackPressed: () -> Unit,
) {
    Box(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .fillMaxHeight()
                .noRippleThrottledClickable {
                    onBackPressed()
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Image(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .wrapContentSize(),
                painter = painterResource(R.drawable.ic_default_top_back),
                contentDescription = stringResource(R.string.content_desc_back_button)
            )
        }
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(R.string.space_properties_screen_title),
            style = Title1,
            color = colorResource(R.color.text_primary),
            textAlign = TextAlign.Center
        )
    }
}