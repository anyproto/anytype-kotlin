package com.anytypeio.anytype.core_ui.lists.objects

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.common.ShimmerEffect
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.presentation.objects.UiObjectsListItem

/**
 * A reusable composable for displaying a single UiObjectsListItem.Item
 */
@Composable
fun ObjectsListItem(
    item: UiObjectsListItem.Item,
    modifier: Modifier = Modifier,
) {
    val createdBy = item.createdBy
    val typeName = item.typeName

    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = colorResource(id = R.color.background_primary),
        ),
        modifier = modifier
            .height(72.dp)
            .fillMaxWidth(),
        headlineContent = {
            Text(
                text = item.name,
                style = PreviewTitle2Regular,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Row {
                if (!typeName.isNullOrBlank()) {
                    Text(
                        text = typeName,
                        style = Relations3,
                        color = colorResource(id = R.color.text_secondary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!createdBy.isNullOrBlank()) {
                    Text(
                        text = "${stringResource(R.string.date_layout_item_created_by)} • $createdBy",
                        style = Relations3,
                        color = colorResource(id = R.color.text_secondary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        leadingContent = {
            ListWidgetObjectIcon(
                icon = item.icon,
                modifier = Modifier,
                iconSize = 48.dp
            )
        }
    )
}

@Composable
fun ListItemLoading(
    modifier: Modifier
) {
    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = colorResource(id = R.color.background_primary),
        ),
        modifier = modifier
            .height(72.dp)
            .fillMaxWidth(),
        headlineContent = {
            ShimmerEffect(
                modifier = Modifier
                    .width(164.dp)
                    .height(18.dp)
            )
        },
        supportingContent = {
            ShimmerEffect(
                modifier = Modifier
                    .width(64.dp)
                    .height(13.dp)
            )
        },
        leadingContent = {
            ShimmerEffect(
                modifier = Modifier
                    .size(48.dp)
            )
        }
    )
}

@DefaultPreviews
@Composable
fun PreviewObjectListItem() {
    ObjectsListItem(
        item = UiObjectsListItem.Item(
            id = "123",
            name = "Some name",
            space = SpaceId("123"),
            type = "123",
            typeName = "Some type",
            createdBy = "Some user",
            layout = ObjectType.Layout.BASIC,
            icon = ObjectIcon.TypeIcon.Default.DEFAULT,
            isPossibleToDelete = true,
            obj = ObjectWrapper.Basic(
                mapOf(
                    "id" to "1",
                    "name" to "Name",
                    "description" to "Description11",
                    Relations.SPACE_ID to "1",
                    Relations.LAYOUT to ObjectType.Layout.BASIC.code
                )
            )
        )
    )
}