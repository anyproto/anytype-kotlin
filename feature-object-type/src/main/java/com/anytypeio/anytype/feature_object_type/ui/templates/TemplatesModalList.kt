package com.anytypeio.anytype.feature_object_type.ui.templates

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.widgets.TemplateItemContent
import com.anytypeio.anytype.feature_object_type.R
import com.anytypeio.anytype.feature_object_type.ui.TypeEvent
import com.anytypeio.anytype.feature_object_type.ui.UiTemplatesModalListState
import com.anytypeio.anytype.presentation.editor.cover.CoverColor
import com.anytypeio.anytype.presentation.templates.TemplateView

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TemplatesModalList(
    modifier: Modifier,
    uiState: UiTemplatesModalListState.Visible,
    onTypeEvent: (TypeEvent) -> Unit
) {

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        modifier = modifier,
        dragHandle = {
            Column {
                Spacer(modifier = Modifier.height(6.dp))
                Dragger()
                Spacer(modifier = Modifier.height(6.dp))
            }
        },
        scrimColor = colorResource(id = R.color.modal_screen_outside_background),
        containerColor = colorResource(id = R.color.background_secondary),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetState = bottomSheetState,
        onDismissRequest = {
            onTypeEvent(TypeEvent.OnTemplatesModalListDismiss)
        }
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.Center),
                textAlign = TextAlign.Center,
                text = stringResource(id = R.string.templates),
                style = Title1,
                color = colorResource(id = R.color.text_primary)
            )
            if (uiState.showAddIcon) {
                IconAdd(onTypeEvent = onTypeEvent)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (uiState.items.isEmpty()) {
            EmptyState(
                modifier = Modifier
                    .height(224.dp)
                    .fillMaxWidth()
            )
        }
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(
                space = 12.dp,
                alignment = Alignment.Start
            ),
        ) {
            items(
                count = uiState.items.size,
                key = { index ->
                    val item = uiState.items[index]
                    when (item) {
                        is TemplateView.Blank -> item.id
                        is TemplateView.New -> "new"
                        is TemplateView.Template -> item.id
                    }
                },
                itemContent = { index ->
                    ItemContent(
                        modifier = Modifier,
                        item = uiState.items[index],
                        onTypeEvent = onTypeEvent
                    )
                }
            )
        }
        Spacer(modifier = Modifier.height(11.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EmptyState(modifier: Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.object_type_templates_empty),
            style = BodyCalloutRegular,
            color = colorResource(id = R.color.text_secondary),
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.ItemContent(
    modifier: Modifier,
    item: TemplateView,
    onTypeEvent: (TypeEvent) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isMenuExpanded by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.shape_secondary),
                shape = RoundedCornerShape(size = 16.dp)
            )
            .height(224.dp)
            .width(120.dp)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = 24.dp),
                onClick = {
                    onTypeEvent(TypeEvent.OnTemplateItemClick(item))
                },
                onLongClick = {
                    if (item is TemplateView.Template) {
                        isMenuExpanded = true
                    }
                },
                enabled = true,
            )
    ) {
        TemplateItemContent(
            item = item,
            showDefaultIcon = true
        )
        DropdownMenu(
            modifier = Modifier.width(244.dp),
            expanded = isMenuExpanded,
            onDismissRequest = { isMenuExpanded = false },
            shape = RoundedCornerShape(size = 10.dp),
            containerColor = colorResource(id = R.color.background_primary),
            shadowElevation = 5.dp,
        ) {
            if (!item.isDefault) {
                DropdownMenuItem(
                    modifier = Modifier.height(44.dp),
                    onClick = {
                        onTypeEvent(TypeEvent.OnTemplateMenuClick.SetAsDefault(item))
                        isMenuExpanded = false
                    }
                ) {
                    androidx.compose.material.Text(
                        text = stringResource(R.string.object_type_templates_menu_set_default),
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_primary),
                        modifier = Modifier
                    )
                }
                Divider(
                    height = 0.5.dp,
                    paddingStart = 0.dp,
                    paddingEnd = 0.dp,
                    color = colorResource(R.color.shape_primary)
                )
            }
            DropdownMenuItem(
                modifier = Modifier.height(44.dp),
                onClick = {
                    onTypeEvent(TypeEvent.OnTemplateMenuClick.Edit(item))
                    isMenuExpanded = false
                }
            ) {
                Text(
                    text = stringResource(R.string.object_type_templates_menu_edit),
                    style = BodyRegular,
                    color = colorResource(id = R.color.text_primary),
                    modifier = Modifier
                )
            }
            Divider(
                height = 0.5.dp,
                paddingStart = 0.dp,
                paddingEnd = 0.dp,
                color = colorResource(R.color.shape_primary)
            )
            DropdownMenuItem(
                modifier = Modifier.height(44.dp),
                onClick = {
                    onTypeEvent(TypeEvent.OnTemplateMenuClick.Duplicate(item))
                    isMenuExpanded = false
                }
            ) {
                Text(
                    text = stringResource(R.string.object_type_templates_menu_duplicate),
                    style = BodyRegular,
                    color = colorResource(id = R.color.text_primary),
                    modifier = Modifier
                )
            }
            Divider(
                height = 0.5.dp,
                paddingStart = 0.dp,
                paddingEnd = 0.dp,
                color = colorResource(R.color.shape_primary)
            )
            DropdownMenuItem(
                modifier = Modifier.height(44.dp),
                onClick = {
                    onTypeEvent(TypeEvent.OnTemplateMenuClick.Delete(item))
                    isMenuExpanded = false
                }
            ) {
                Text(
                    text = stringResource(R.string.object_type_templates_menu_delete),
                    style = BodyRegular,
                    color = colorResource(id = R.color.palette_system_red),
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
private fun BoxScope.IconAdd(onTypeEvent: (TypeEvent) -> Unit) {
    Box(
        modifier = Modifier
            .wrapContentSize()
            .align(Alignment.CenterEnd)
            .noRippleThrottledClickable {
                onTypeEvent(TypeEvent.OnTemplatesAddIconClick)
            }
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_default_plus),
            contentDescription = "Add new Template icon",
            modifier = Modifier
                .height(48.dp)
                .width(56.dp)
                .align(Alignment.Center),
            contentScale = ContentScale.None
        )
    }
}

@DefaultPreviews
@Composable
fun TemplatesModalListPreview() {
    TemplatesModalList(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        uiState = UiTemplatesModalListState.Visible(
            items = listOf(
                TemplateView.Template(
                    id = "1",
                    name = "Template 1",
                    targetTypeId = TypeId("page"),
                    targetTypeKey = TypeKey("ot-page"),
                    layout = ObjectType.Layout.BASIC,
                    image = null,
                    emoji = ":)",
                    coverColor = CoverColor.RED,
                    coverGradient = null,
                    coverImage = null,
                ),
                TemplateView.Template(
                    id = "2",
                    name = "Template 2",
                    targetTypeId = TypeId("note"),
                    targetTypeKey = TypeKey("ot-note"),
                    layout = ObjectType.Layout.NOTE,
                    image = null,
                    emoji = null,
                    coverColor = null,
                    coverGradient = null,
                    coverImage = null,
                )
            ),
            showAddIcon = true
        ),
        onTypeEvent = {}
    )
}

@DefaultPreviews
@Composable
fun TemplatesModalListEmptyPreview() {
    TemplatesModalList(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        uiState = UiTemplatesModalListState.Visible(
            items = listOf(),
            showAddIcon = true
        ),
        onTypeEvent = {}
    )
}