package com.anytypeio.anytype.feature_object_type.properties.add.ui

import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.extensions.simpleIcon
import com.anytypeio.anytype.core_ui.foundation.DefaultSearchBar
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Regular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.widgets.dv.DragHandle
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.feature_object_type.fields.ui.commonItemModifier
import com.anytypeio.anytype.feature_object_type.properties.add.AddPropertyEvent
import com.anytypeio.anytype.feature_object_type.properties.add.UiAddPropertyItem
import com.anytypeio.anytype.feature_object_type.properties.add.UiAddPropertyScreenState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFieldScreen(
    state: UiAddPropertyScreenState,
    event: (AddPropertyEvent) -> Unit
) {
    var isSearchEmpty by remember { mutableStateOf(true) }

    val lazyListState = rememberLazyListState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(rememberNestedScrollInteropConnection()),
        containerColor = Color.Transparent,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = colorResource(id = R.color.background_primary),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DragHandle()
                TopToolbar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )
                DefaultSearchBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    hint = R.string.object_type_add_property_screen_search_hint
                ) { newQuery ->
                    isSearchEmpty = newQuery.isEmpty()
                    event(
                        AddPropertyEvent.OnSearchQueryChanged(newQuery)
                    )
                }
                Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
            }
        },
        content = { paddingValues ->
            val modifier = if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK)
                Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
            else
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            LazyColumn(
                modifier = modifier
                    .background(color = colorResource(id = R.color.background_primary),),
                state = lazyListState
            ) {
                items(
                    count = state.items.size,
                    key = { index -> state.items[index].id },
                    itemContent = { index ->
                        val item = state.items[index]
                        when (item) {
                            is UiAddPropertyItem.Format -> {
                                PropertyTypeItem(
                                    modifier = commonItemModifier()
                                        .noRippleThrottledClickable {
                                            event(AddPropertyEvent.OnTypeClicked(item))
                                        },
                                    item = item
                                )
                            }

                            is UiAddPropertyItem.Default -> {
                                FieldItem(
                                    modifier = commonItemModifier()
                                        .noRippleThrottledClickable {
                                            event(AddPropertyEvent.OnExistingClicked(item))
                                        },
                                    item = item
                                )
                            }

                            is UiAddPropertyItem.Create -> {
                                PropertyCreateItem(
                                    modifier = commonItemModifier()
                                        .noRippleThrottledClickable {
                                            event(AddPropertyEvent.OnCreate(item))
                                        },
                                    item = item
                                )
                            }

                            is UiAddPropertyItem.Section -> Section(item = item)
                        }
                    }
                )
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    )
}

@Composable
private fun Section(
    item: UiAddPropertyItem.Section
) {
    val title = when (item) {
        is UiAddPropertyItem.Section.Existing -> stringResource(R.string.object_type_add_property_screen_section_existing)
        is UiAddPropertyItem.Section.Types -> stringResource(R.string.object_type_add_property_screen_section_types)
    }
    Text(
        modifier = Modifier
            .padding(top = 26.dp, start = 20.dp, end = 20.dp, bottom = 8.dp)
            .fillMaxWidth(),
        text = title,
        style = Caption1Medium,
        color = colorResource(id = R.color.text_secondary),
    )
}

@Composable
private fun PropertyTypeItem(
    modifier: Modifier,
    item: UiAddPropertyItem.Format
) {
    Row(
        modifier = modifier,
        verticalAlignment = CenterVertically
    ) {
        val formatIcon = item.format.simpleIcon()
        if (formatIcon != null) {
            Image(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(24.dp),
                painter = painterResource(id = formatIcon),
                contentDescription = "Relation format icon",
            )
        }
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .padding(end = 16.dp),
            text = item.prettyName,
            style = PreviewTitle1Regular,
            color = colorResource(id = R.color.text_primary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PropertyCreateItem(
    modifier: Modifier,
    item: UiAddPropertyItem.Create
) {
    Row(
        modifier = modifier,
        verticalAlignment = CenterVertically
    ) {
        val formatIcon = item.format.simpleIcon()
        if (formatIcon != null) {
            Image(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(24.dp),
                painter = painterResource(id = formatIcon),
                contentDescription = "Relation format icon",
            )
        }
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .padding(end = 16.dp),
            text = stringResource(R.string.object_type_add_property_screen_create, item.title),
            style = PreviewTitle1Regular,
            color = colorResource(id = R.color.text_primary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FieldItem(
    modifier: Modifier,
    item: UiAddPropertyItem.Default
) {
    Row(
        modifier = modifier,
        verticalAlignment = CenterVertically
    ) {
        val formatIcon = item.format.simpleIcon()
        if (formatIcon != null) {
            Image(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(24.dp),
                painter = painterResource(id = formatIcon),
                contentDescription = "Relation format icon",
            )
        }
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .padding(end = 16.dp),
            text = item.title,
            style = PreviewTitle1Regular,
            color = colorResource(id = R.color.text_primary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopToolbar(
    modifier: Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.object_type_add_property_screen_title),
            style = Title1,
            color = colorResource(R.color.text_primary),
            textAlign = TextAlign.Center
        )
    }
}

@DefaultPreviews
@Composable
fun PreviewAddFieldScreen() {
    AddFieldScreen(
        state = UiAddPropertyScreenState(
            items = listOf(
                UiAddPropertyItem.Create(
                    id = "111",
                    format = RelationFormat.LONG_TEXT,
                    title = "This is very very long title, which is very very long, but not very very long"
                ),
                UiAddPropertyItem.Section.Types(),
                UiAddPropertyItem.Format(
                    id = "11",
                    format = RelationFormat.STATUS,
                    prettyName = "Status"
                ),
                UiAddPropertyItem.Format(
                    id = "12",
                    format = RelationFormat.OBJECT,
                    prettyName = "Object"
                ),
                UiAddPropertyItem.Format(
                    id = "13",
                    format = RelationFormat.LONG_TEXT,
                    prettyName = "Long Text"
                ),
                UiAddPropertyItem.Format(
                    id = "14",
                    format = RelationFormat.PHONE,
                    prettyName = "Phone"
                ),
                UiAddPropertyItem.Section.Existing(),
                UiAddPropertyItem.Default(
                    id = "1",
                    propertyKey = "key",
                    title = "Title",
                    format = RelationFormat.LONG_TEXT
                ),
                UiAddPropertyItem.Default(
                    id = "2",
                    propertyKey = "key",
                    title = "Some Object",
                    format = RelationFormat.OBJECT
                )
            )
        ),
        event = {}
    )
}