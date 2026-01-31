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
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.home.InteractionMode
import com.anytypeio.anytype.presentation.widgets.WidgetView

/**
 * Grouped widget card for displaying object types as a navigation list.
 * Shows type rows with icon and name - no nested objects displayed.
 */
@Composable
fun ObjectTypesGroupWidgetCard(
    item: WidgetView.ObjectTypesGroup,
    mode: InteractionMode,
    onTypeClicked: (Id) -> Unit,
    onCreateObjectOfType: (Id) -> Unit,
    onCreateNewType: () -> Unit,
    onTypeReordered: (List<Id>) -> Unit,
    modifier: Modifier = Modifier
) {
    // Note: We're inside a LazyColumn (WidgetsScreen), so we cannot use LazyColumn here.
    // Drag-and-drop will be implemented differently or disabled for now.
    // Type rows are simple enough that a regular Column is fine.
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .background(
                shape = RoundedCornerShape(24.dp),
                color = colorResource(id = R.color.dashboard_card_background)
            )
            .clip(RoundedCornerShape(24.dp))
    ) {
        if (item.types.isNotEmpty() || mode !is InteractionMode.ReadOnly) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Type rows - using regular Column since we're already inside a LazyColumn
                item.types.forEachIndexed { index, typeRow ->
                    TypeRowItem(
                        typeRow = typeRow,
                        mode = mode,
                        onTypeClicked = onTypeClicked,
                        onCreateObjectOfType = onCreateObjectOfType,
                        isDragging = false
                    )
                    
                    // Add divider if not last item or if "New type" button will follow
                    if (index != item.types.lastIndex || mode !is InteractionMode.ReadOnly) {
                        Divider(
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = colorResource(id = R.color.widget_divider)
                        )
                    }
                }
                
                // "New type" row - only shown when not in ReadOnly mode
                if (mode !is InteractionMode.ReadOnly) {
                    NewTypeRowItem(
                        onClick = onCreateNewType
                    )
                }
            }
        }
    }
}

/**
 * Single type row within the grouped widget.
 */
@Composable
private fun TypeRowItem(
    typeRow: WidgetView.ObjectTypesGroup.TypeRow,
    mode: InteractionMode,
    onTypeClicked: (Id) -> Unit,
    onCreateObjectOfType: (Id) -> Unit,
    isDragging: Boolean = false
) {
    val alpha = if (isDragging) 0.5f else 1f
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 16.dp)
            .noRippleClickable { onTypeClicked(typeRow.id) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Type icon
        ListWidgetObjectIcon(
            iconSize = 18.dp,
            icon = typeRow.icon,
            modifier = Modifier.padding(end = 12.dp),
            iconWithoutBackgroundMaxSize = 200.dp
        )
        
        // Type name
        val (name, color) = typeRow.name.getPrettyNameAndColor()
        Text(
            text = name,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = PreviewTitle2Medium,
            color = color
        )
        
        // Create object button - only shown when not in ReadOnly mode
        if (mode !is InteractionMode.ReadOnly) {
            Image(
                painter = painterResource(id = R.drawable.ic_default_plus),
                contentDescription = "Create object",
                modifier = Modifier
                    .size(18.dp)
                    .noRippleClickable { onCreateObjectOfType(typeRow.id) },
                contentScale = ContentScale.Inside
            )
        }
    }
}

/**
 * "New type" row at the bottom of the grouped widget.
 */
@Composable
private fun NewTypeRowItem(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 16.dp)
            .noRippleClickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Plus icon
        Image(
            painter = painterResource(id = R.drawable.ic_default_plus),
            contentDescription = "New type",
            modifier = Modifier
                .size(18.dp)
                .padding(end = 12.dp),
            contentScale = ContentScale.Inside
        )
        
        // "New type" text
        Text(
            text = stringResource(id = R.string.all_content_new_type),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = PreviewTitle2Medium,
            color = colorResource(id = R.color.text_primary)
        )
    }
}
