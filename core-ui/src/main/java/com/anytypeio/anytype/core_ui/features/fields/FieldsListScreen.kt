package com.anytypeio.anytype.core_ui.features.fields

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
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

@Composable
fun FieldListScreen(
    state: List<Model>,
    onRelationClicked: (Model.Item) -> Unit,
    onTypeIconClicked: () -> Unit,
    onLocalInfoIconClicked: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.widget_background))
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
                                    modifier = Modifier.noRippleThrottledClickable {
                                        onRelationClicked(item)
                                    },
                                    title = field.name,
                                    isCheck = field.isChecked
                                )
                            }

                            is ObjectRelationView.Date -> {
                                val relativeDate = field.relativeDate
                                if (relativeDate != null) {
                                    FieldTypeDate(
                                        modifier = Modifier.noRippleThrottledClickable {
                                            onRelationClicked(item)
                                        },
                                        title = field.name,
                                        relativeDate = relativeDate
                                    )
                                } else {
                                    FieldEmpty(
                                        modifier = Modifier.noRippleThrottledClickable {
                                            onRelationClicked(item)
                                        },
                                        title = field.name,
                                        fieldFormat = RelationFormat.DATE
                                    )
                                }
                            }

                            is ObjectRelationView.Default -> {
                                val textValue = field.value
                                if (field.key == Relations.ORIGIN) {
                                    val code = textValue?.toInt() ?: -1
                                    FieldTypeText(
                                        modifier = Modifier.noRippleThrottledClickable {
                                            onRelationClicked(item)
                                        },
                                        title = field.name,
                                        text = stringResource(code.resRelationOrigin())
                                    )
                                } else {
                                    if (textValue.isNullOrEmpty() == true) {
                                        FieldEmpty(
                                            modifier = Modifier.noRippleThrottledClickable {
                                                onRelationClicked(item)
                                            },
                                            title = field.name,
                                            fieldFormat = RelationFormat.LONG_TEXT
                                        )
                                    } else {
                                        FieldTypeText(
                                            modifier = Modifier.noRippleThrottledClickable {
                                                onRelationClicked(item)
                                            },
                                            title = field.name,
                                            text = textValue
                                        )
                                    }
                                }
                            }

                            is ObjectRelationView.File -> {
                                if (field.files.isEmpty()) {
                                    FieldEmpty(
                                        modifier = Modifier.noRippleThrottledClickable {
                                            onRelationClicked(item)
                                        },
                                        title = field.name,
                                        fieldFormat = RelationFormat.FILE
                                    )
                                } else {
                                    FieldTypeFile(
                                        modifier = Modifier.noRippleThrottledClickable {
                                            onRelationClicked(item)
                                        },
                                        fieldObject = field
                                    )
                                }
                            }

                            is ObjectRelationView.Object -> {
                                if (field.objects.isEmpty()) {
                                    FieldEmpty(
                                        modifier = Modifier.noRippleThrottledClickable {
                                            onRelationClicked(item)
                                        },
                                        title = field.name,
                                        fieldFormat = RelationFormat.OBJECT
                                    )
                                } else {
                                    FieldTypeObject(
                                        modifier = Modifier.noRippleThrottledClickable {
                                            onRelationClicked(item)
                                        },
                                        fieldObject = field
                                    )
                                }
                            }

                            is ObjectRelationView.Status -> {
                                if (field.status.isEmpty()) {
                                    FieldEmpty(
                                        modifier = Modifier.noRippleThrottledClickable {
                                            onRelationClicked(item)
                                        },
                                        title = field.name,
                                        fieldFormat = RelationFormat.STATUS
                                    )
                                } else {
                                    FieldTypeSelect(
                                        modifier = Modifier.noRippleThrottledClickable {
                                            onRelationClicked(item)
                                        },
                                        title = field.name,
                                        status = field.status.first()
                                    )
                                }
                            }

                            is ObjectRelationView.Tags -> {
                                if (field.tags.isEmpty()) {
                                    FieldEmpty(
                                        modifier = Modifier.noRippleThrottledClickable {
                                            onRelationClicked(item)
                                        },
                                        title = field.name,
                                        fieldFormat = RelationFormat.TAG
                                    )
                                } else {
                                    FieldTypeMultiSelect(
                                        modifier = Modifier.noRippleThrottledClickable {
                                            onRelationClicked(item)
                                        },
                                        title = field.name,
                                        tags = field.tags
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Text(
            modifier = Modifier
                .padding(bottom = 7.dp, start = 20.dp)
                .align(Alignment.BottomStart),
            text = stringResource(id = R.string.object_type_fields_section_local_fields),
            style = BodyCalloutMedium,
            color = colorResource(R.color.text_primary),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .height(37.dp)
                .width(44.dp)
                .noRippleThrottledClickable {
                    onLocalInfoIconClicked()
                }
        ) {
            Image(
                modifier = Modifier
                    .padding(bottom = 9.dp, end = 20.dp)
                    .wrapContentSize()
                    .align(Alignment.BottomEnd),
                painter = painterResource(R.drawable.ic_section_local_fields),
                contentDescription = "Section local fields info"
            )
        }
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
                .padding(vertical = 17.dp)
                .padding(start = 16.dp)
        )
    }
}

@DefaultPreviews
@Composable
fun FieldListScreenPreview() {
    FieldListScreen(
        state = listOf(Model.Section.Local),
        onRelationClicked = {},
        onLocalInfoIconClicked = {},
        onTypeIconClicked = {}
    )
}
