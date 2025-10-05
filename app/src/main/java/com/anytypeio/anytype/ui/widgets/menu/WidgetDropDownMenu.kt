package com.anytypeio.anytype.ui.widgets.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.SectionType
import com.anytypeio.anytype.presentation.widgets.Widget
import com.anytypeio.anytype.presentation.widgets.WidgetView

@Composable
fun WidgetLongClickMenu(
    widgetView: WidgetView,
    isCardMenuExpanded: MutableState<Boolean>,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit
) {
    when (widgetView.sectionType) {
        SectionType.PINNED -> {
            DropdownMenu(
                modifier = Modifier.width(254.dp),
                expanded = isCardMenuExpanded.value,
                onDismissRequest = { isCardMenuExpanded.value = false },
                containerColor = colorResource(R.color.background_secondary),
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 8.dp,
                offset = DpOffset(
                    x = 16.dp,
                    y = 8.dp
                )
            ) {
                if (widgetView.canCreateObjectOfType) {
                    DropdownMenuItem(
                        onClick = {
                            onDropDownMenuAction(DropDownMenuAction.CreateObjectOfType(widgetView.id)).also {
                                isCardMenuExpanded.value = false
                            }
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    style = BodyRegular,
                                    color = colorResource(id = R.color.text_primary),
                                    text = stringResource(R.string.widgets_menu_new_object_type)
                                )
                                Image(
                                    painter = painterResource(id = R.drawable.ic_menu_item_create),
                                    contentDescription = "New object icon",
                                    modifier = Modifier.size(24.dp),
                                    colorFilter = ColorFilter.tint(
                                        colorResource(id = R.color.text_primary)
                                    )
                                )
                            }
                        }
                    )
                    Divider(
                        thickness = 8.dp,
                        color = colorResource(id = R.color.shape_primary)
                    )
                }
                if(widgetView.canChangeWidgetType()) {
                    DropdownMenuItem(
                        onClick = {
                            onDropDownMenuAction(DropDownMenuAction.ChangeWidgetType).also {
                                isCardMenuExpanded.value = false
                            }
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    style = BodyRegular,
                                    color = colorResource(id = R.color.text_primary),
                                    text = stringResource(R.string.widget_change_type)
                                )
                                Image(
                                    painter = painterResource(id = R.drawable.ic_menu_item_change_type),
                                    contentDescription = "Change Widget Type icon",
                                    modifier = Modifier.size(24.dp),
                                    colorFilter = ColorFilter.tint(
                                        colorResource(id = R.color.text_primary)
                                    )
                                )
                            }
                        }
                    )
                }
                Divider(
                    thickness = 0.5.dp,
                    color = colorResource(id = R.color.shape_primary)
                )
                DropdownMenuItem(
                    onClick = {
                        onDropDownMenuAction(DropDownMenuAction.RemoveWidget).also {
                            isCardMenuExpanded.value = false
                        }
                    },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                modifier = Modifier.weight(1f),
                                style = BodyRegular,
                                color = colorResource(id = R.color.text_primary),
                                text = stringResource(R.string.widget_unpin)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.ic_unpin_24),
                                contentDescription = "Unpin widget icon",
                                modifier = Modifier.size(24.dp),
                                colorFilter = ColorFilter.tint(
                                    colorResource(id = R.color.text_primary)
                                )
                            )
                        }
                    }
                )
            }
        }

        SectionType.TYPES -> {
            if (widgetView.canCreateObjectOfType) {
                DropdownMenu(
                    modifier = Modifier.width(254.dp),
                    expanded = isCardMenuExpanded.value,
                    onDismissRequest = { isCardMenuExpanded.value = false },
                    containerColor = colorResource(R.color.background_secondary),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 8.dp,
                    offset = DpOffset(
                        x = 16.dp,
                        y = 8.dp
                    )
                ) {
                    DropdownMenuItem(
                        onClick = {
                            onDropDownMenuAction(DropDownMenuAction.CreateObjectOfType(widgetView.id)).also {
                                isCardMenuExpanded.value = false
                            }
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                androidx.compose.material.Text(
                                    modifier = Modifier.weight(1f),
                                    style = BodyRegular,
                                    color = colorResource(id = R.color.text_primary),
                                    text = stringResource(R.string.widgets_menu_new_object_type)
                                )
                                Image(
                                    painter = painterResource(id = R.drawable.ic_menu_item_create),
                                    contentDescription = "New object icon",
                                    modifier = Modifier.size(24.dp),
                                    colorFilter = ColorFilter.tint(
                                        colorResource(id = R.color.text_primary)
                                    )
                                )
                            }
                        }
                    )
                }
            } else {
                isCardMenuExpanded.value = false
            }
        }

        null -> {
            //do nothing
        }
    }
}

private fun WidgetView.canChangeWidgetType(): Boolean {
    return when (this) {
        is WidgetView.Gallery -> {
            val source = this.source
            return source !is Widget.Source.Bundled
        }
        is WidgetView.Link -> {
            val source = this.source
            return source !is Widget.Source.Bundled
        }
        is WidgetView.ListOfObjects -> {
            val source = this.source
            return source !is Widget.Source.Bundled
        }
        is WidgetView.SetOfObjects -> {
            val source = this.source
            return source !is Widget.Source.Bundled
        }
        is WidgetView.Tree -> {
            val source = this.source
            return source !is Widget.Source.Bundled
        }
        else -> false
    }
}

@Composable
fun BinWidgetMenu(
    widgetView: WidgetView,
    isCardMenuExpanded: MutableState<Boolean>,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit
) {
    DropdownMenu(
        modifier = Modifier.width(254.dp),
        expanded = isCardMenuExpanded.value,
        onDismissRequest = { isCardMenuExpanded.value = false },
        containerColor = colorResource(R.color.background_secondary),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 8.dp,
        offset = DpOffset(
            x = 16.dp,
            y = 8.dp
        )
    ) {
        DropdownMenuItem(
            onClick = {
                onDropDownMenuAction(DropDownMenuAction.EmptyBin).also {
                    isCardMenuExpanded.value = false
                }
            },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.material.Text(
                        modifier = Modifier.weight(1f),
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_primary),
                        text = stringResource(R.string.widget_empty_bin)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_widget_bin),
                        contentDescription = "Empty bin icon",
                        modifier = Modifier
                            .wrapContentSize(),
                        colorFilter = ColorFilter.tint(
                            colorResource(id = R.color.palette_system_red)
                        )
                    )
                }
            }
        )
    }
}

// Preview Helper Components
@DefaultPreviews
@Composable
fun WidgetLongClickMenuPreview_PinnedSection_WithCreateOption() {
    val isExpanded = remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray.copy(alpha = 0.1f))
    ) {
        WidgetLongClickMenu(
            widgetView = WidgetView.SetOfObjects(
                id = "widget-1",
                icon = ObjectIcon.None,
                source = Widget.Source.Default(
                    obj = ObjectWrapper.Basic(
                        mapOf(
                            Relations.ID to "obj-1",
                            Relations.UNIQUE_KEY to "Obj 1",
                            Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                            Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
                        )
                    )
                ),
                elements = emptyList(),
                isExpanded = true,
                isCompact = false,
                sectionType = SectionType.PINNED,
                tabs = emptyList(),
                name = WidgetView.Name.Default("My Widget")
            ),
            isCardMenuExpanded = isExpanded,
            onDropDownMenuAction = { /* Preview action */ }
        )
    }
}