package com.anytypeio.anytype.feature_object_type.ui

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.extensions.simpleIcon
import com.anytypeio.anytype.core_ui.extensions.swapList
import com.anytypeio.anytype.core_ui.views.BodyCalloutMedium
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.feature_object_type.R
import com.anytypeio.anytype.feature_object_type.models.UiFieldsListItem
import com.anytypeio.anytype.feature_object_type.models.UiFieldsListState
import com.anytypeio.anytype.feature_object_type.models.UiIconState
import com.anytypeio.anytype.feature_object_type.models.UiTitleState
import com.anytypeio.anytype.presentation.objects.ObjectIcon

@Composable
fun TypeFieldsMainScreen(
    uiFieldsListState: UiFieldsListState,
    uiTitleState: UiTitleState,
    uiIconState: UiIconState
) {
    val items = remember { mutableStateListOf<UiFieldsListItem>() }
    items.swapList(uiFieldsListState.items)

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = colorResource(id = R.color.background_primary),
        contentColor = colorResource(id = R.color.background_primary),
        topBar = {
            val modifier = if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK) {
                Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .fillMaxWidth()
            } else {
                Modifier.fillMaxWidth()
            }
            Column(modifier = modifier) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        modifier = Modifier.wrapContentSize().align(Alignment.Center),
                        text = stringResource(R.string.object_type_fields_title),
                        style = Title1,
                        color = colorResource(R.color.text_primary)
                    )
                }
                InfoBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .background(color = colorResource(R.color.shape_tertiary)),
                    uiTitleState = uiTitleState,
                    uiIconState = uiIconState
                )
            }
        },
        content = { paddingValues ->
            val contentModifier = if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK) {
                Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
            } else {
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            }
            LazyColumn(modifier = contentModifier) {
                items(
                    count = items.size,
                    key = { items[it].id },
                    contentType = { index ->
                        when (items[index]) {
                            is UiFieldsListItem.FieldItem -> "field"
                            is UiFieldsListItem.Section.FieldsMenu -> "section"
                            is UiFieldsListItem.Section.Header -> "section"
                        }
                    },
                    itemContent = { index ->
                        val item = items[index]
                        when (item) {
                            is UiFieldsListItem.FieldItem -> {
                                FieldItem(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .bottomBorder()
                                        .animateItem(),
                                    item = item
                                )
                            }

                            is UiFieldsListItem.Section.FieldsMenu -> Section(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                item = item
                            )

                            is UiFieldsListItem.Section.Header -> Section(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                item = item
                            )
                        }
                    }
                )
            }
        }
    )
}

@Composable
private fun InfoBar(modifier: Modifier, uiTitleState: UiTitleState, uiIconState: UiIconState) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.object_type_fields_info_text),
            style = Caption1Medium,
            color = colorResource(id = R.color.text_primary),
        )
        ListWidgetObjectIcon(
            modifier = Modifier.size(18.dp).padding(start = 4.dp),
            icon = uiIconState.icon,
            backgroundColor = R.color.transparent_black
        )
        Text(
            modifier = Modifier.padding(start = 4.dp),
            text = uiTitleState.title,
            style = Caption1Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = colorResource(id = R.color.text_primary),
        )
    }
}

@Composable
private fun Section(modifier: Modifier, item: UiFieldsListItem.Section) {
    val title = when (item) {
        is UiFieldsListItem.Section.Header -> stringResource(R.string.object_type_fields_section_header)
        is UiFieldsListItem.Section.FieldsMenu -> stringResource(R.string.object_type_fields_section_fields_menu)
    }
    Box(modifier = modifier) {
        Text(
            modifier = Modifier
                .padding(start = 20.dp, bottom = 7.dp)
                .align(Alignment.BottomStart),
            text = title,
            style = BodyCalloutMedium,
            color = colorResource(id = R.color.text_secondary),
        )
        Image(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 6.dp),
            painter = painterResource(R.drawable.ic_default_plus),
            contentDescription = "$title plus button"
        )
    }
}

@Composable
private fun FieldItem(
    modifier: Modifier,
    item: UiFieldsListItem.FieldItem
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

        Text(
            text = item.fieldTitle,
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun Modifier.bottomBorder(
    strokeWidth: Dp = 0.5.dp,
    color: Color = colorResource(R.color.shape_primary)
) = composed(
    factory = {
        val density = LocalDensity.current
        val strokeWidthPx = density.run { strokeWidth.toPx() }

        Modifier.drawBehind {
            val width = size.width
            val height = size.height - strokeWidthPx / 2

            drawLine(
                color = color,
                start = Offset(x = 0f, y = height),
                end = Offset(x = width, y = height),
                strokeWidth = strokeWidthPx
            )
        }
    }
)

@DefaultPreviews
@Composable
fun PreviewTypeFieldsMainScreen() {
    TypeFieldsMainScreen(
        uiTitleState = UiTitleState(title = "Page", isEditable = false),
        uiIconState = UiIconState(icon = ObjectIcon.Empty.ObjectType, isEditable = false),
        uiFieldsListState = UiFieldsListState(
            items = listOf(
                UiFieldsListItem.Section.Header(),
                UiFieldsListItem.FieldItem(
                    id = "id1",
                    fieldKey = "key1",
                    fieldTitle = "Status",
                    format = RelationFormat.STATUS,
                    canDrag = true,
                    canDelete = true
                ),
                UiFieldsListItem.FieldItem(
                    id = "id2",
                    fieldKey = "key2",
                    fieldTitle = "Name",
                    format = RelationFormat.LONG_TEXT,
                    canDrag = true,
                    canDelete = true
                ),
                UiFieldsListItem.Section.FieldsMenu(),
                UiFieldsListItem.FieldItem(
                    id = "id3",
                    fieldKey = "key3",
                    fieldTitle = "Links",
                    format = RelationFormat.URL,
                ),
                UiFieldsListItem.FieldItem(
                    id = "id4",
                    fieldKey = "key4",
                    fieldTitle = "Date",
                    format = RelationFormat.DATE,
                )
            )
        ),
    )
}