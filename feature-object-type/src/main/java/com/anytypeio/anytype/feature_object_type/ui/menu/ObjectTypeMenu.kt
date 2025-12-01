package com.anytypeio.anytype.feature_object_type.ui.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.Caption2Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Regular

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectTypeMenu(
    isPinned: Boolean,
    canDelete: Boolean,
    isDescriptionFeatured: Boolean,
    canEditDetails: Boolean,
    onEvent: (ObjectTypeMenuEvent) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        sheetState = sheetState,
        containerColor = Color.Transparent,
        onDismissRequest = { onEvent(ObjectTypeMenuEvent.OnDismiss) },
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(bottom = 16.dp)
                .fillMaxWidth()
                .background(
                    color = colorResource(id = R.color.background_secondary),
                    shape = RoundedCornerShape(20.dp),
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dragger
            Dragger(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 6.dp)
            )

            // Icon cell
            MenuCell(
                iconRes = R.drawable.ic_obj_settings_icon_24,
                title = stringResource(R.string.icon),
                trailingContent = {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_forward),
                        contentDescription = null,
                        tint = colorResource(R.color.glyph_inactive),
                        modifier = Modifier.size(24.dp)
                    )
                },
                onClick = { onEvent(ObjectTypeMenuEvent.OnIconClick) }
            )

            if (canEditDetails) {
                Divider(paddingStart = 20.dp, paddingEnd = 20.dp)

                // Description cell
                MenuCell(
                    iconRes = R.drawable.ic_obj_settings_description_24,
                    title = stringResource(R.string.description),
                    trailingContent = {
                        Text(
                            text = if (isDescriptionFeatured)
                                stringResource(R.string.modal_hide)
                            else
                                stringResource(R.string.show),
                            style = PreviewTitle1Regular,
                            color = colorResource(R.color.text_secondary)
                        )
                    },
                    onClick = { onEvent(ObjectTypeMenuEvent.OnDescriptionClick) }
                )
            }

            // Divider before bottom section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(colorResource(R.color.shape_tertiary))
            )

            // Bottom button section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 13.dp)
                    .padding(horizontal = 16.dp)
            ) {
                // Pin/Unpin button
                BottomActionButton(
                    iconRes = if (isPinned) R.drawable.ic_state_unpin_24 else R.drawable.ic_state_pin_24,
                    text = if (isPinned)
                        stringResource(R.string.object_action_unpin)
                    else
                        stringResource(R.string.object_action_pin),
                    onClick = { onEvent(ObjectTypeMenuEvent.OnPinToggleClick) }
                )

                // To Bin button (only show if user has delete permission)
                if (canDelete) {
                    Spacer(modifier = Modifier.width(12.dp))

                    BottomActionButton(
                        iconRes = R.drawable.ic_object_action_archive,
                        text = stringResource(R.string.action_bar_to_bin),
                        isDestructive = true,
                        onClick = { onEvent(ObjectTypeMenuEvent.OnToBinClick) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MenuCell(
    iconRes: Int,
    title: String,
    trailingContent: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .noRippleThrottledClickable { onClick() }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            style = PreviewTitle1Regular,
            color = colorResource(R.color.text_primary),
            modifier = Modifier.weight(1f)
        )

        trailingContent()
    }
}

@Composable
private fun BottomActionButton(
    iconRes: Int,
    text: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Column(
        modifier = Modifier
            .width(52.dp)
            .noRippleThrottledClickable { onClick() }
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            contentScale = ContentScale.Inside,
            modifier = Modifier
                .size(52.dp)
                .background(
                    color = colorResource(R.color.shape_transparent_secondary),
                    shape = RoundedCornerShape(12.dp)
                )
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = text,
            style = Caption2Regular,
            color = colorResource(R.color.text_secondary),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@DefaultPreviews
@Composable
fun ObjectTypeMenuPreview() {
    ObjectTypeMenu(
        isPinned = false,
        canDelete = true,
        isDescriptionFeatured = false,
        canEditDetails = true,
        onEvent = {}
    )
}
