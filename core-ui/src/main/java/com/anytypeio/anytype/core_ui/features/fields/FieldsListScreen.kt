package com.anytypeio.anytype.core_ui.features.fields

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import com.anytypeio.anytype.presentation.relations.RelationListViewModel.Model

@Composable
fun FieldListScreen(
    state: List<Model>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(rememberNestedScrollInteropConnection())
    ) {
        item {
            Dragger(
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
        item {
            Text(
                text = "Fields",
                style = Title1,
                color = colorResource(id = R.color.text_secondary),
            )
        }
        items(
            count = state.size,
            key = { index -> state[index].identifier },
            itemContent = { index ->
                val item = state[index]
                when (item) {
                    is Model.Item -> {
                        val field =item.view
                        when (field) {
                            is ObjectRelationView.Checkbox -> TODO()
                            is ObjectRelationView.Date -> TODO()
                            is ObjectRelationView.Default -> TODO()
                            is ObjectRelationView.File -> TODO()
                            is ObjectRelationView.Links.Backlinks -> TODO()
                            is ObjectRelationView.Links.From -> TODO()
                            is ObjectRelationView.Object -> {
                                FieldTypeObject()
                            }
                            is ObjectRelationView.ObjectType.Base -> TODO()
                            is ObjectRelationView.ObjectType.Deleted -> TODO()
                            is ObjectRelationView.Source -> TODO()
                            is ObjectRelationView.Status -> TODO()
                            is ObjectRelationView.Tags -> TODO()
                        }
                    }
                    Model.Section.Featured -> TODO()
                    Model.Section.Other -> TODO()
                    is Model.Section.TypeFrom -> TODO()
                }
            }
        )
        items(state) { item ->
            when (item) {
                is Model.Field -> {
                    FieldEmpty(item = item)
                }

                is Model.FieldTypeText -> {
                    FieldTypeText(item = item)
                }

                is Model.FieldTypeDate -> {
                    FieldTypeDate(item = item)
                }

                is Model.FieldTypeMultiSelect -> {
                    FieldTypeMultiSelect(item = item)
                }
            }
        }
    }
}