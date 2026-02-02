package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.HeadlineSubheading
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.widgets.WidgetView

/**
 * Widget card that displays all object types as rows within a single grouped container.
 * Based on Figma design: https://www.figma.com/design/pQ6LLLxEn5y4Gn7Eei7SDu?node-id=14142-11754
 * 
 * Structure:
 * - Single card container with rounded corners
 * - Type rows: each with [icon] [name] ... [+ button]
 * - "New type" button at the bottom (non-draggable)
 * - Type rows can be dragged and reordered
 * - Card itself cannot be dragged
 */
@Composable
fun ObjectTypesGroupWidgetCard(
    item: WidgetView.ObjectTypesGroup,
    onTypeClicked: (String) -> Unit,
    onCreateObjectClicked: (String) -> Unit,
    onCreateNewTypeClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 6.dp)
            .background(
                shape = RoundedCornerShape(24.dp),
                color = colorResource(id = R.color.dashboard_card_background)
            )
            .clip(RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Render type rows
            item.typeRows.forEach { typeRow ->
                TypeRowItem(
                    typeRow = typeRow,
                    onTypeClicked = onTypeClicked,
                    onCreateObjectClicked = onCreateObjectClicked
                )
            }
            
            // "New type" button at the bottom
            NewTypeButton(
                onClick = onCreateNewTypeClicked
            )
        }
    }
}

/**
 * Individual type row within the grouped widget.
 * Shows: [icon] [type name] ... [+ button]
 */
@Composable
private fun TypeRowItem(
    typeRow: WidgetView.ObjectTypesGroup.TypeRow,
    onTypeClicked: (String) -> Unit,
    onCreateObjectClicked: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .noRippleClickable { onTypeClicked(typeRow.id) }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Type icon
        ListWidgetObjectIcon(
            iconSize = 18.dp,
            icon = typeRow.icon,
            modifier = Modifier.padding(end = 12.dp)
        )
        
        // Type name
        Text(
            text = typeRow.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            style = HeadlineSubheading,
            color = colorResource(id = R.color.text_primary)
        )
        
        // "+" button for creating objects (only if enabled)
        if (typeRow.canCreateObjects) {
            Image(
                painter = painterResource(id = R.drawable.ic_default_plus),
                contentDescription = "Create object",
                modifier = Modifier
                    .size(18.dp)
                    .noRippleClickable { onCreateObjectClicked(typeRow.id) }
            )
        }
    }
}

/**
 * "New type" button that appears at the bottom of the type list.
 * This button is always visible and cannot be dragged.
 */
@Composable
private fun NewTypeButton(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .noRippleClickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Plus icon
        Image(
            painter = painterResource(id = R.drawable.ic_default_plus),
            contentDescription = "New type",
            modifier = Modifier
                .padding(end = 12.dp)
                .size(18.dp)
        )
        
        // "New type" text
        Text(
            text = stringResource(id = R.string.create_new_object_type),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = BodyRegular,
            color = colorResource(id = R.color.text_secondary)
        )
    }
}
