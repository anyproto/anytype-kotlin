package com.anytypeio.anytype.core_ui.relations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyCallout
import com.anytypeio.anytype.presentation.relations.value.tagstatus.RelationsListItem
import com.anytypeio.anytype.presentation.relations.value.tagstatus.TagStatusAction

@Composable
fun ItemMenu(
    item: RelationsListItem.Item?,
    action: (TagStatusAction) -> Unit,
) {
    if (item != null) {
        DropdownMenu(expanded = true, onDismissRequest = { /*TODO*/ }) {
            Column(
                modifier = Modifier
                    .width(220.dp)
                    .wrapContentHeight()
                    .shadow(
                        elevation = 40.dp,
                        spotColor = Color(0xCC000000),
                        ambientColor = Color(0xE6000000),
                        shape = RoundedCornerShape(size = 10.dp)
                    )
                    .background(
                        color = colorResource(id = R.color.context_menu_background),
                        shape = RoundedCornerShape(size = 10.dp)
                    )
            ) {
                Text(
                    text = stringResource(R.string.edit),
                    style = BodyCallout,
                    color = colorResource(id = R.color.text_primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .padding(start = 16.dp, top = 11.dp)
                        .noRippleThrottledClickable {
                            action(TagStatusAction.Edit(item.optionId))
                        }
                )
                Divider(paddingEnd = 0.dp, paddingStart = 0.dp)
                Text(
                    text = stringResource(R.string.duplicate),
                    style = BodyCallout,
                    color = colorResource(id = R.color.text_primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .padding(start = 16.dp, top = 11.dp)
                        .noRippleThrottledClickable {
                            action(TagStatusAction.Duplicate(item.optionId))
                        }
                )
                Divider(paddingEnd = 0.dp, paddingStart = 0.dp)
                Text(
                    text = stringResource(R.string.delete_view),
                    style = BodyCallout,
                    color = colorResource(id = R.color.palette_system_red),
                    modifier = Modifier
                        .height(44.dp)
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 11.dp)
                        .noRippleThrottledClickable {
                            action(TagStatusAction.Delete(item.optionId))
                        }
                )
            }
        }

    }
}