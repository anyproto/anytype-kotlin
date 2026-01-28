package com.anytypeio.anytype.feature_object_type.ui.alerts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.ButtonWarning
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.feature_object_type.ui.DeleteAlertObjectItem
import com.anytypeio.anytype.feature_object_type.ui.TypeEvent
import com.anytypeio.anytype.feature_object_type.ui.UiDeleteTypeAlertState
import com.anytypeio.anytype.core_models.ui.ObjectIcon

/**
 * Bottom sheet for confirming object type deletion (move to bin).
 * Shows a list of objects that use this type, allowing users to select
 * which ones should also be moved to bin along with the type.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteTypeAlertScreen(
    state: UiDeleteTypeAlertState.Visible,
    onTypeEvent: (TypeEvent) -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        dragHandle = {
            Column {
                Spacer(modifier = Modifier.height(6.dp))
                Dragger()
                Spacer(modifier = Modifier.height(6.dp))
            }
        },
        scrimColor = colorResource(id = R.color.modal_screen_outside_background),
        containerColor = colorResource(id = R.color.background_secondary),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetState = bottomSheetState,
        onDismissRequest = {
            onTypeEvent(TypeEvent.OnDeleteTypeAlertDismiss)
        },
        content = {
            DeleteTypeAlertContent(
                state = state,
                onTypeEvent = onTypeEvent
            )
        }
    )
}

@Composable
private fun DeleteTypeAlertContent(
    state: UiDeleteTypeAlertState.Visible,
    onTypeEvent: (TypeEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = stringResource(R.string.move_type_to_bin_title, state.typeName),
            style = HeadlineHeading,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Description
        Text(
            text = stringResource(R.string.move_type_to_bin_description),
            style = BodyCalloutRegular,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.objects.isEmpty()) {
            // Empty state - no objects of this type
            EmptyObjectsState()
        } else {
            // Select all row
            SelectAllRow(
                isSelected = state.isAllSelected,
                onSelectAll = { isSelected ->
                    onTypeEvent(TypeEvent.OnDeleteTypeAlertSelectAll(isSelected))
                }
            )

            Divider(paddingStart = 0.dp, paddingEnd = 0.dp)

            // Objects list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                items(
                    items = state.objects,
                    key = { it.id }
                ) { item ->
                    ObjectSelectableItem(
                        item = item,
                        isSelected = state.selectedObjectIds.contains(item.id),
                        onClick = {
                            onTypeEvent(TypeEvent.OnDeleteTypeAlertToggleObject(item.id))
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Buttons
        ActionButtons(
            onMoveClick = { onTypeEvent(TypeEvent.OnDeleteTypeAlertConfirm) },
            onCancelClick = { onTypeEvent(TypeEvent.OnDeleteTypeAlertDismiss) }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SelectAllRow(
    isSelected: Boolean,
    onSelectAll: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .noRippleThrottledClickable { onSelectAll(!isSelected) }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier.size(24.dp),
            painter = painterResource(
                id = if (isSelected) R.drawable.ic_checkbox_checked
                else R.drawable.ic_checkbox_unchecked
            ),
            contentDescription = "Select all checkbox"
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = stringResource(R.string.move_type_to_bin_select_all),
            style = Title2,
            color = colorResource(id = R.color.text_secondary)
        )
    }
}

@Composable
private fun ObjectSelectableItem(
    item: DeleteAlertObjectItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .noRippleThrottledClickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier.size(24.dp),
            painter = painterResource(
                id = if (isSelected) R.drawable.ic_checkbox_checked
                else R.drawable.ic_checkbox_unchecked
            ),
            contentDescription = if (isSelected) "Selected" else "Not selected"
        )

        Spacer(modifier = Modifier.width(12.dp))

        ListWidgetObjectIcon(
            icon = item.icon,
            modifier = Modifier.size(24.dp),
            iconSize = 24.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = item.name.ifEmpty { stringResource(R.string.untitled) },
            style = Title2,
            color = colorResource(id = R.color.text_primary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun EmptyObjectsState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.move_type_to_bin_no_objects),
            style = BodyCalloutRegular,
            color = colorResource(id = R.color.text_secondary),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ActionButtons(
    onMoveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        ButtonWarning(
            text = stringResource(R.string.move_type_to_bin_to_bin),
            onClick = onMoveClick,
            size = ButtonSize.Large,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        ButtonSecondary(
            text = stringResource(R.string.cancel),
            onClick = onCancelClick,
            size = ButtonSize.Large,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ============================================
// PREVIEWS
// ============================================

@DefaultPreviews
@Composable
private fun DeleteTypeAlertScreenPreview() {
    val state = UiDeleteTypeAlertState.Visible(
        typeName = "Pages",
        objects = listOf(
            DeleteAlertObjectItem(
                id = "1",
                name = "Version History - Review",
                icon = ObjectIcon.Basic.Emoji("\uD83D\uDCD8")
            ),
            DeleteAlertObjectItem(
                id = "2",
                name = "Lists 2.0",
                icon = ObjectIcon.Basic.Emoji("\uD83D\uDCDD")
            ),
            DeleteAlertObjectItem(
                id = "3",
                name = "Primitives",
                icon = ObjectIcon.Basic.Emoji("\uD83D\uDD27")
            ),
            DeleteAlertObjectItem(
                id = "4",
                name = "Sync Status",
                icon = ObjectIcon.Basic.Emoji("\uD83D\uDD04")
            )
        ),
        selectedObjectIds = setOf("1")
    )

    DeleteTypeAlertContent(
        state = state,
        onTypeEvent = {}
    )
}

@DefaultPreviews
@Composable
private fun DeleteTypeAlertScreenEmptyPreview() {
    val state = UiDeleteTypeAlertState.Visible(
        typeName = "Pages",
        objects = emptyList(),
        selectedObjectIds = emptySet()
    )

    DeleteTypeAlertContent(
        state = state,
        onTypeEvent = {}
    )
}
