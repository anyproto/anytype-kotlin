package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.home.InteractionMode
import com.anytypeio.anytype.presentation.widgets.WidgetView
import kotlin.math.roundToInt

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
    // Track the list order for drag-and-drop
    var typesList by remember(item.types) { mutableStateOf(item.types) }
    var draggedIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    
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
        if (typesList.isNotEmpty() || mode !is InteractionMode.ReadOnly) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Type rows with manual drag-and-drop
                typesList.forEachIndexed { index, typeRow ->
                    val isDragging = draggedIndex == index
                    val canDrag = mode !is InteractionMode.ReadOnly
                    
                    TypeRowItem(
                        typeRow = typeRow,
                        mode = mode,
                        onTypeClicked = onTypeClicked,
                        onCreateObjectOfType = onCreateObjectOfType,
                        isDragging = isDragging,
                        dragOffset = if (isDragging) dragOffset else 0f,
                        onDragStart = if (canDrag) {
                            {
                                draggedIndex = index
                                dragOffset = 0f
                            }
                        } else null,
                        onDrag = if (canDrag) {
                            { delta ->
                                dragOffset += delta
                                // Calculate target index based on drag distance
                                val itemHeight = 52f // dp to pixels approximation
                                val targetIndex = (index + (dragOffset / itemHeight).roundToInt())
                                    .coerceIn(0, typesList.lastIndex)
                                
                                if (targetIndex != index) {
                                    // Reorder the list
                                    val newList = typesList.toMutableList()
                                    val item = newList.removeAt(index)
                                    newList.add(targetIndex, item)
                                    typesList = newList
                                    draggedIndex = targetIndex
                                    dragOffset = 0f
                                }
                            }
                        } else null,
                        onDragEnd = if (canDrag) {
                            {
                                draggedIndex = -1
                                dragOffset = 0f
                                // Notify parent of reordering
                                onTypeReordered(typesList.map { it.id })
                            }
                        } else null
                    )
                    
                    // Add divider if not last item or if "New type" button will follow
                    if (index != typesList.lastIndex || mode !is InteractionMode.ReadOnly) {
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
    isDragging: Boolean = false,
    dragOffset: Float = 0f,
    onDragStart: (() -> Unit)? = null,
    onDrag: ((Float) -> Unit)? = null,
    onDragEnd: (() -> Unit)? = null
) {
    val alpha = if (isDragging) 0.7f else 1f
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .offset { IntOffset(0, dragOffset.roundToInt()) }
            .zIndex(if (isDragging) 1f else 0f)
            .background(if (isDragging) colorResource(id = R.color.dashboard_card_background).copy(alpha = 0.9f) else colorResource(id = R.color.dashboard_card_background))
            .padding(horizontal = 16.dp)
            .then(
                if (onDragStart != null && onDrag != null && onDragEnd != null) {
                    Modifier.pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { onDragStart() },
                            onDrag = { _, dragAmount -> onDrag(dragAmount.y) },
                            onDragEnd = { onDragEnd() },
                            onDragCancel = { onDragEnd() }
                        )
                    }
                } else {
                    Modifier
                }
            )
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
 * Design spec: 48dp total height (12dp top + 24dp line height + 12dp bottom)
 * Icon: 18dp with 12dp gap to text
 * Text: Inter Semi Bold 17sp, secondary color
 */
@Composable
private fun NewTypeRowItem(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .noRippleClickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Plus icon (18dp size)
        Box(
            modifier = Modifier.size(18.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_default_plus),
                contentDescription = "New type",
                modifier = Modifier.size(14.dp),
                contentScale = ContentScale.Inside
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // "New type" text with secondary color
        Text(
            text = stringResource(id = R.string.all_content_new_type),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = PreviewTitle2Medium,
            color = colorResource(id = R.color.text_secondary)
        )
    }
}
