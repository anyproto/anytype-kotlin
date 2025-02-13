package com.anytypeio.anytype.feature_object_type.ui.templates

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.widgets.TemplateItemContent
import com.anytypeio.anytype.feature_object_type.ui.UiTemplatesListState
import com.anytypeio.anytype.feature_object_type.ui.TypeEvent
import com.anytypeio.anytype.presentation.templates.TemplateView
import timber.log.Timber

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TemplatesList(
    uiTemplatesListState: UiTemplatesListState,
    onTypeEvent: (TypeEvent) -> Unit
) {

    Timber.d("TemplatesList :$uiTemplatesListState")

    val scrollState = rememberLazyListState()
    val interactionSource = remember { MutableInteractionSource() }

    LazyRow(
        state = scrollState,
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        items(
            count = uiTemplatesListState.items.size,
            key = { index ->
                val item = uiTemplatesListState.items[index]
                when (item) {
                    is TemplateView.Blank -> item.id
                    is TemplateView.New -> "new"
                    is TemplateView.Template -> item.id
                }
            },
            itemContent = {
                var isMenuExpanded by remember { mutableStateOf(false) }
                val item = uiTemplatesListState.items[it]
                Box(
                    modifier = Modifier
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
                        offset = DpOffset(
                            x = 20.dp,
                            y = (-300).dp
                        )
                    ) {
                        if (!item.isDefault) {
                            DropdownMenuItem(
                                modifier = Modifier.height(44.dp),
                                onClick = {
                                    onTypeEvent(TypeEvent.OnTemplateMenuClick.SetAsDefault(item))
                                    isMenuExpanded = false
                                }
                            ) {
                                Text(
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
        )
    }
}