package com.anytypeio.anytype.core_ui.relations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.light
import com.anytypeio.anytype.core_ui.views.Relations1
import com.anytypeio.anytype.presentation.relations.model.RelationsListItem

@Composable
fun TagItem(state: RelationsListItem.Item.Tag) {
    CommonContainer {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 56.dp)
                .align(alignment = Alignment.CenterStart)
        ) {
            TagItemText(state = state)
        }
        CircleIcon(
            number = if (state.isSelected) state.number.toString() else null,
            isSelected = state.isSelected,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterEnd)
        )
        Divider(modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun TagItemText(state: RelationsListItem.Item.Tag) {
    Text(
        text = state.text,
        color = dark(state.color),
        modifier = Modifier
            .wrapContentWidth()
            .background(
                color = light(color = state.color),
                shape = RoundedCornerShape(size = 3.dp)
            )
            .padding(start = 6.dp, end = 6.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = Relations1
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewTagItem() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        TagItem(
            state = RelationsListItem.Item.Tag(
                text = "Urgent",
                color = ThemeColor.RED,
                number = 1,
                isSelected = true
            )
        )
        TagItem(
            state = RelationsListItem.Item.Tag(
                text = "Personal",
                color = ThemeColor.ORANGE,
                number = 1,
                isSelected = false
            )
        )
        ItemTagOrStatusCreate(
            state = RelationsListItem.CreateItem.Tag(
                text = "Done"
            )
        )
    }
}