package com.anytypeio.anytype.core_ui.relations

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
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
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.Relations1
import com.anytypeio.anytype.presentation.relations.value.tagstatus.RelationsListItem
import com.anytypeio.anytype.presentation.relations.value.tagstatus.TagStatusAction

@Composable
fun StatusItem(state: RelationsListItem.Item.Status, action: (TagStatusAction) -> Unit) {
    CommonContainer(
        modifier = Modifier.noRippleClickable { action(TagStatusAction.Click(state)) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 56.dp)
                .align(alignment = Alignment.CenterStart)
        ) {
            StatusItemText(state = state)
        }
        CheckedIcon(
            isSelected = state.isSelected,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterEnd)
        )
        Divider(modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun StatusItemText(state: RelationsListItem.Item.Status) {
    Text(
        text = state.name,
        color = dark(state.color),
        modifier = Modifier
            .wrapContentWidth()
            .padding(start = 6.dp, end = 6.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = Relations1
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewStatusItem() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        StatusItem(
            state = RelationsListItem.Item.Status(
                optionId = "1",
                name = "In development",
                color = ThemeColor.RED,
                isSelected = true
            ),
            action = {}
        )
        StatusItem(
            state = RelationsListItem.Item.Status(
                optionId = "2",
                name = "Designer",
                color = ThemeColor.TEAL,
                isSelected = false
            ),
            action = {}
        )
        ItemTagOrStatusCreate(
            state = RelationsListItem.CreateItem.Status(
                text = "Personal"
            )
        )
    }
}

