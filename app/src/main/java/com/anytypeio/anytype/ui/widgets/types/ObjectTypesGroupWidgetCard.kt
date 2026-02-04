package com.anytypeio.anytype.ui.widgets.types

import android.view.View
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
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.HeadlineSubheading
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.widgets.WidgetView
import sh.calvin.reorderable.ReorderableColumn
import sh.calvin.reorderable.ReorderableScope

/**
 * Widget card that displays all object types as rows within a single grouped container.
 * Based on Figma design: https://www.figma.com/design/pQ6LLLxEn5y4Gn7Eei7SDu?node-id=14142-11754
 * 
 * Structure:
 * - Single card container with rounded corners
 * - Type rows: each with [icon] [name] ... [+ button]
 * - "New type" button at the bottom (non-draggable)
 * - Type rows can be dragged and reordered within the card
 * - Card itself cannot be dragged
 *
 * @param typeRows The list of type rows to display (managed externally for drag state)
 * @param onTypeClicked Callback when a type row is clicked
 * @param onCreateObjectClicked Callback when the + button is clicked on a type row
 * @param onCreateNewTypeClicked Callback when the "New type" button is clicked
 * @param onTypeRowsReordered Callback when drag ends with the new order (fromIndex, toIndex)
 */
@Composable
fun ObjectTypesGroupWidgetCard(
    typeRows: List<WidgetView.ObjectTypesGroup.TypeRow>,
    onTypeClicked: (String) -> Unit,
    onCreateObjectClicked: (String) -> Unit,
    onCreateNewTypeClicked: () -> Unit,
    onTypeRowsReordered: (fromIndex: Int, toIndex: Int) -> Unit
) {
    val view = LocalView.current
    
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
            // Reorderable type rows
            ReorderableColumn(
                modifier = Modifier.fillMaxWidth(),
                list = typeRows,
                onSettle = { fromIndex, toIndex ->
                    onTypeRowsReordered(fromIndex, toIndex)
                },
                onMove = {
                    ViewCompat.performHapticFeedback(
                        view,
                        HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK
                    )
                }
            ) { index, typeRow, isDragging ->
                key(typeRow.id) {
                    DraggableTypeRowContent(
                        typeRow = typeRow,
                        isDragging = isDragging,
                        onTypeClicked = onTypeClicked,
                        onCreateObjectClicked = onCreateObjectClicked,
                        view = view
                    )
                }
            }
            
            // "New type" button at the bottom (non-draggable)
            NewTypeButton(
                onClick = onCreateNewTypeClicked
            )
        }
    }
}

/**
 * Draggable type row content for use within ReorderableItem.
 */
@Composable
private fun ReorderableScope.DraggableTypeRowContent(
    typeRow: WidgetView.ObjectTypesGroup.TypeRow,
    isDragging: Boolean,
    onTypeClicked: (String) -> Unit,
    onCreateObjectClicked: (String) -> Unit,
    view: View
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .longPressDraggableHandle(
                onDragStarted = {
                    ViewCompat.performHapticFeedback(
                        view,
                        HapticFeedbackConstantsCompat.GESTURE_START
                    )
                },
                onDragStopped = {
                    ViewCompat.performHapticFeedback(
                        view,
                        HapticFeedbackConstantsCompat.GESTURE_END
                    )
                }
            )
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
