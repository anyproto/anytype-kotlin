package com.anytypeio.anytype.feature_allcontent.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.feature_allcontent.R
import com.anytypeio.anytype.feature_allcontent.models.UiContentItem

@Composable
fun AllContentItemMenu(
    item: UiContentItem.Item,
    expanded: Boolean,
    onDismiss: () -> Unit,
    onOpenAsObject: (UiContentItem.Item) -> Unit,
) {
    DropdownMenu(
        modifier = Modifier.width(254.dp),
        expanded = expanded,
        onDismissRequest = onDismiss,
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
                onOpenAsObject(item)
            },
            text = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    style = BodyRegular,
                    color = colorResource(id = R.color.text_primary),
                    text = stringResource(R.string.all_content_item_menu_open)
                )
            }
        )
    }
}