package com.anytypeio.anytype.core_ui.features.fields

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.features.editor.holders.relations.resRelationOrigin
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutMedium
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import com.anytypeio.anytype.presentation.relations.RelationListViewModel.Model
import timber.log.Timber

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FieldListScreen(
    state: List<Model>,
    onRelationClicked: (Model.Item) -> Unit,
    onTypeIconClicked: () -> Unit,
    onLocalInfoIconClicked: () -> Unit,
    onAddToTypeClicked: (Model.Item) -> Unit,
    onRemoveFromObjectClicked: (Model.Item) -> Unit,
    onHiddenToggle: (Model.Section.Hidden) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = colorResource(id = R.color.widget_background),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .nestedScroll(rememberNestedScrollInteropConnection())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Dragger(
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(id = R.string.fields_screen_title),
                    style = Title1,
                    color = colorResource(id = R.color.text_primary),
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(56.dp)
                        .height(48.dp)
                        .noRippleThrottledClickable {
                            onTypeIconClicked()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        modifier = Modifier.wrapContentSize(),
                        painter = painterResource(R.drawable.ic_settings_24),
                        contentDescription = "Open object's type"
                    )
                }
            }
        }
        items(
            count = state.size,
            key = { index -> state[index].identifier },
            itemContent = { index ->
                val item = state[index]
                when (item) {
                    is Model.Item -> {
                        val field = item.view
                        when (field) {
                            is ObjectRelationView.Checkbox -> {
                                FieldTypeCheckbox(
                                    modifier = Modifier,
                                    title = field.name,
                                    isCheck = field.isChecked,
                                    isLocal = item.isLocal,
                                    onFieldClick = { onRelationClicked(item) },
                                    onAddToCurrentTypeClick = { onAddToTypeClicked(item) },
                                    onRemoveFromObjectClick = { onRemoveFromObjectClicked(item) }
                                )
                            }

                            is ObjectRelationView.Date -> {
                                val relativeDate = field.relativeDate
                                if (relativeDate != null) {
                                    FieldTypeDate(
                                        modifier = Modifier,
                                        title = field.name,
                                        relativeDate = relativeDate,
                                        isLocal = item.isLocal,
                                        onFieldClick = { onRelationClicked(item) },
                                        onAddToCurrentTypeClick = { onAddToTypeClicked(item) },
                                        onRemoveFromObjectClick = { onRemoveFromObjectClicked(item) }
                                    )
                                } else {
                                    FieldEmpty(
                                        modifier = Modifier,
                                        title = field.name,
                                        fieldFormat = RelationFormat.DATE,
                                        isLocal = item.isLocal,
                                        onFieldClick = { onRelationClicked(item) },
                                        onAddToCurrentTypeClick = { onAddToTypeClicked(item) },
                                        onRemoveFromObjectClick = { onRemoveFromObjectClicked(item) }
                                    )
                                }
                            }

                            is ObjectRelationView.Default -> {
                                val textValue = field.value
                                if (field.key == Relations.ORIGIN) {
                                    val code = textValue?.toInt() ?: -1
                                    FieldTypeText(
                                        modifier = Modifier,
                                        title = field.name,
                                        text = stringResource(code.resRelationOrigin()),
                                        isLocal = item.isLocal,
                                        onFieldClick = { onRelationClicked(item) },
                                        onAddToCurrentTypeClick = { onAddToTypeClicked(item) },
                                        onRemoveFromObjectClick = { onRemoveFromObjectClicked(item) }
                                    )
                                } else {
                                    if (textValue.isNullOrEmpty() == true) {
                                        FieldEmpty(
                                            modifier = Modifier,
                                            title = field.name,
                                            fieldFormat = RelationFormat.LONG_TEXT,
                                            isLocal = item.isLocal,
                                            onFieldClick = { onRelationClicked(item) },
                                            onAddToCurrentTypeClick = { onAddToTypeClicked(item) },
                                            onRemoveFromObjectClick = { onRemoveFromObjectClicked(item) }
                                        )
                                    } else {
                                        FieldTypeText(
                                            modifier = Modifier,
                                            title = field.name,
                                            text = textValue,
                                            isLocal = item.isLocal,
                                            onFieldClick = { onRelationClicked(item) },
                                            onAddToCurrentTypeClick = { onAddToTypeClicked(item) },
                                            onRemoveFromObjectClick = { onRemoveFromObjectClicked(item) }
                                        )
                                    }
                                }
                            }

                            is ObjectRelationView.File -> {
                                if (field.files.isEmpty()) {
                                    FieldEmpty(
                                        modifier = Modifier,
                                        title = field.name,
                                        fieldFormat = RelationFormat.FILE,
                                        isLocal = item.isLocal,
                                        onFieldClick = { onRelationClicked(item) },
                                        onAddToCurrentTypeClick = { onAddToTypeClicked(item) },
                                        onRemoveFromObjectClick = { onRemoveFromObjectClicked(item) }
                                    )
                                } else {
                                    FieldTypeFile(
                                        modifier = Modifier,
                                        fieldObject = field,
                                        isLocal = item.isLocal,
                                        onFieldClick = { onRelationClicked(item) },
                                        onAddToCurrentTypeClick = { onAddToTypeClicked(item) },
                                        onRemoveFromObjectClick = { onRemoveFromObjectClicked(item) }
                                    )
                                }
                            }

                            is ObjectRelationView.Object -> {
                                if (field.objects.isEmpty()) {
                                    FieldEmpty(
                                        modifier = Modifier,
                                        title = field.name,
                                        fieldFormat = RelationFormat.OBJECT,
                                        isLocal = item.isLocal,
                                        onFieldClick = { onRelationClicked(item) },
                                        onAddToCurrentTypeClick = { onAddToTypeClicked(item) },
                                        onRemoveFromObjectClick = { onRemoveFromObjectClicked(item) }
                                    )
                                } else {
                                    FieldTypeObject(
                                        modifier = Modifier,
                                        fieldObject = field,
                                        isLocal = item.isLocal,
                                        onFieldClick = { onRelationClicked(item) },
                                        onAddToCurrentTypeClick = { onAddToTypeClicked(item) },
                                        onRemoveFromObjectClick = { onRemoveFromObjectClicked(item) }
                                    )
                                }
                            }

                            is ObjectRelationView.Status -> {
                                if (field.status.isEmpty()) {
                                    FieldEmpty(
                                        modifier = Modifier,
                                        title = field.name,
                                        fieldFormat = RelationFormat.STATUS,
                                        isLocal = item.isLocal,
                                        onFieldClick = { onRelationClicked(item) },
                                        onAddToCurrentTypeClick = { onAddToTypeClicked(item) },
                                        onRemoveFromObjectClick = { onRemoveFromObjectClicked(item) }
                                    )
                                } else {
                                    FieldTypeSelect(
                                        modifier = Modifier,
                                        title = field.name,
                                        status = field.status.first(),
                                        isLocal = item.isLocal,
                                        onFieldClick = { onRelationClicked(item) },
                                        onAddToCurrentTypeClick = { onAddToTypeClicked(item) },
                                        onRemoveFromObjectClick = { onRemoveFromObjectClicked(item) }
                                    )
                                }
                            }

                            is ObjectRelationView.Tags -> {
                                if (field.tags.isEmpty()) {
                                    FieldEmpty(
                                        modifier = Modifier,
                                        title = field.name,
                                        fieldFormat = RelationFormat.TAG,
                                        isLocal = item.isLocal,
                                        onFieldClick = { onRelationClicked(item) },
                                        onAddToCurrentTypeClick = { onAddToTypeClicked(item) },
                                        onRemoveFromObjectClick = { onRemoveFromObjectClicked(item) }
                                    )
                                } else {
                                    FieldTypeMultiSelect(
                                        modifier = Modifier,
                                        title = field.name,
                                        tags = field.tags,
                                        isLocal = item.isLocal,
                                        onFieldClick = { onRelationClicked(item) },
                                        onAddToCurrentTypeClick = { onAddToTypeClicked(item) },
                                        onRemoveFromObjectClick = { onRemoveFromObjectClicked(item) }
                                    )
                                }
                            }

                            is ObjectRelationView.Links.Backlinks,
                            is ObjectRelationView.Links.From,
                            is ObjectRelationView.ObjectType.Base,
                            is ObjectRelationView.ObjectType.Deleted,
                            is ObjectRelationView.Source -> {
                                Timber.e("Unsupported field type: $field, shouldn't be in the fields list")
                            }
                        }
                    }

                    is Model.Section.Header -> {
                        Section(item)
                    }
                    is Model.Section.SideBar -> {
                        Section(item)
                    }
                    Model.Section.Local -> {
                        SectionLocal(onLocalInfoIconClicked)
                    }

                    is Model.Section.Hidden -> SectionHidden(
                        item = item,
                        onToggle = onHiddenToggle
                    )
                }
            }
        )
        item {
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Composable
private fun SectionLocal(
    onLocalInfoIconClicked: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 23.dp)
            .height(22.dp).
        noRippleThrottledClickable{
            onLocalInfoIconClicked()
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.padding(start = 20.dp).weight(1.0f),
            text = stringResource(id = R.string.object_properties_section_local),
            style = BodyCalloutMedium,
            color = colorResource(R.color.text_primary),
        )
        Image(
            modifier = Modifier
                .wrapContentSize()
                .padding(end = 14.dp),
            painter = painterResource(R.drawable.ic_section_local_fields),
            contentDescription = "Section local fields info"
        )
    }
}

@Composable
private fun Section(item: Model.Section) {
    val text = when (item) {
        Model.Section.Header -> stringResource(id = R.string.object_type_fields_section_header)
        Model.Section.SideBar -> stringResource(id = R.string.object_type_fields_section_fields_menu)
        else -> ""
    }
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = BodyCalloutMedium,
            color = colorResource(id = R.color.text_secondary),
            modifier = Modifier
                .padding(vertical = 11.dp)
                .padding(start = 16.dp)
        )
    }
}

@Composable
private fun SectionHidden(
    item: Model.Section.Hidden,
    onToggle: (Model.Section.Hidden) -> Unit = {}
) {
    val text = stringResource(id = R.string.object_properties_section_hidden)
    val iconRes = when (item) {
        is Model.Section.Hidden.Shown -> R.drawable.ic_arrow_up_18
        is Model.Section.Hidden.Unshown -> R.drawable.ic_list_arrow_18
    }
    Box(
        modifier = Modifier.fillMaxWidth()
            .noRippleThrottledClickable{
                onToggle(item)
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 7.dp)
                .padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier.size(18.dp),
                painter = painterResource(iconRes),
                contentDescription = "Hidden section icon",
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = BodyCalloutMedium,
                color = colorResource(id = R.color.text_secondary),
                modifier = Modifier
            )
        }
    }
}

@DefaultPreviews
@Composable
fun FieldListScreenPreview() {
    FieldListScreen(
        state = listOf(
            Model.Item(
                view = ObjectRelationView.Default(
                    id = "id3",
                    system = false,
                    key = "key3",
                    name = "Name3",
                    value = "Value3",
                    format = RelationFormat.OBJECT
                ),
                isLocal = false
            ),
            Model.Section.Hidden.Shown(
                listOf(
                    Model.Item(
                        view = ObjectRelationView.Default(
                            id = "id1",
                            system = false,
                            key = "key1",
                            name = "Name1",
                            value = "Value1",
                            format = RelationFormat.LONG_TEXT
                        ),
                        isLocal = false
                    ),
                    Model.Item(
                        view = ObjectRelationView.Default(
                            id = "id2",
                            system = false,
                            key = "key2",
                            name = "Name2",
                            value = "Value2",
                            format = RelationFormat.TAG
                        ),
                        isLocal = false
                    ),
                )
            ),
            Model.Section.Local,
            Model.Item(
                view = ObjectRelationView.Default(
                    id = "id55",
                    system = false,
                    key = "key55",
                    name = "Local 55",
                    value = "Valu55",
                    format = RelationFormat.OBJECT
                ),
                isLocal = false
            ),
        ),
        onRelationClicked = {},
        onLocalInfoIconClicked = {},
        onTypeIconClicked = {},
        onAddToTypeClicked = {},
        onRemoveFromObjectClicked = {}
    )
}
