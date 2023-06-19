package com.anytypeio.anytype.ui.widgets

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Toolbar
import com.anytypeio.anytype.core_ui.views.Caption2Regular
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.presentation.widgets.WidgetTypeView

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun MySelectWidgetTypeScreen() {
    SelectWidgetTypeScreen(
        views = listOf(
            WidgetTypeView.List(isSelected = true),
            WidgetTypeView.CompactList(isSelected = true),
            WidgetTypeView.Link(isSelected = true),
            WidgetTypeView.Tree(isSelected = true)
        ),
        onViewClicked = {  }
    )
}

@Composable
fun SelectWidgetTypeScreen(
    views: List<WidgetTypeView>,
    onViewClicked: (WidgetTypeView) -> Unit
) {
    Column {
        Box(
            Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Dragger()
        }

        Toolbar(stringResource(R.string.widget_type))

        views.forEachIndexed { index, type ->
            when (type) {
                is WidgetTypeView.Link -> WidgetTypeItem(
                    title = stringResource(
                        R.string.widget_type_link
                    ),
                    subtitle = stringResource(
                        R.string.widget_type_link_description
                    ),
                    icon = R.drawable.ic_widget_type_link,
                    isChecked = type.isSelected,
                    onClick = { onViewClicked(type) }
                )
                is WidgetTypeView.List -> WidgetTypeItem(
                    title = stringResource(
                        R.string.widget_type_list
                    ),
                    subtitle = stringResource(
                        R.string.widget_type_list_description
                    ),
                    icon = R.drawable.ic_widget_type_list,
                    isChecked = type.isSelected,
                    onClick = { onViewClicked(type) }
                )
                is WidgetTypeView.CompactList -> WidgetTypeItem(
                    title = stringResource(
                        R.string.widget_type_compact_list
                    ),
                    subtitle = stringResource(
                        R.string.widget_type_compact_list_description
                    ),
                    icon = R.drawable.ic_widget_type_compact_list,
                    isChecked = type.isSelected,
                    onClick = { onViewClicked(type) }
                )
                is WidgetTypeView.Tree -> WidgetTypeItem(
                    title = stringResource(
                        R.string.widget_type_tree
                    ),
                    subtitle = stringResource(
                        R.string.widget_type_tree_description
                    ),
                    icon = R.drawable.ic_widget_type_tree,
                    isChecked = type.isSelected,
                    onClick = { onViewClicked(type) }
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun WidgetTypeItem(
    title: String,
    subtitle: String,
    @DrawableRes icon: Int,
    isChecked: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { onClick() }
    ) {
        Image(
            painterResource(icon),
            contentDescription = "Widget type icon",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 20.dp)
        )
        Text(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 76.dp, top = 11.dp),
            text = title,
            style = Title2,
            color = colorResource(id = R.color.text_primary)
        )
        Text(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 76.dp, bottom = 11.dp),
            text = subtitle,
            style = Caption2Regular,
            color = colorResource(id = R.color.text_secondary)
        )
        if (isChecked) {
            Image(
                painterResource(R.drawable.ic_widget_type_checked_checkbox),
                contentDescription = "Widget type checkbox icon",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp)
            )
        }
    }
}