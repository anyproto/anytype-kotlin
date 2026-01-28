package com.anytypeio.anytype.feature_vault.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.ui.SpaceIconView
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.BodySemiBold
import com.anytypeio.anytype.core_ui.widgets.SpaceBackground
import com.anytypeio.anytype.core_ui.widgets.objectIcon.SpaceIconView
import com.anytypeio.anytype.feature_vault.R
import com.anytypeio.anytype.feature_vault.presentation.VaultSpaceView


@Composable
fun DataSpaceCard(
    modifier: Modifier,
    title: String,
    icon: SpaceIconView,
    isPinned: Boolean = false,
    spaceBackground: SpaceBackground,
    spaceView: VaultSpaceView.DataSpace,
    expandedSpaceId: String? = null,
    onDismissMenu: () -> Unit = {},
    onPinSpace: (Id) -> Unit = {},
    onUnpinSpace: (Id) -> Unit = {},
    onSpaceSettings: (Id) -> Unit = {},
    onDeleteOrLeaveSpace: (Id, Boolean) -> Unit = { _, _ -> }
) {
    val updatedModifier = when (spaceBackground) {
        is SpaceBackground.SolidColor -> modifier
            .fillMaxSize()
            .height(96.dp)
            .padding(horizontal = 16.dp)
            .background(
                color = spaceBackground.color.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp)

        is SpaceBackground.Gradient -> modifier
            .fillMaxSize()
            .height(96.dp)
            .padding(horizontal = 16.dp)
            .background(
                brush = spaceBackground.brush,
                shape = RoundedCornerShape(20.dp),
                alpha = 0.3f
            )
            .padding(horizontal = 16.dp)

        SpaceBackground.None -> Modifier
            .fillMaxSize()
            .height(96.dp)
            .padding(horizontal = 16.dp)
            .background(
                color = colorResource(id = R.color.background_secondary),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp)
    }

    Row(
        modifier = updatedModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpaceIconView(
            icon = icon,
            mainSize = 64.dp,
            modifier = Modifier
        )
        ContentSpace(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp),
            title = title,
            isPinned = isPinned
        )

        // Include dropdown menu inside the card
        SpaceActionsDropdownMenu(
            expanded = expandedSpaceId == spaceView.space.id,
            onDismiss = onDismissMenu,
            isPinned = spaceView.isPinned,
            isOwner = spaceView.isOwner,
            onPinToggle = {
                spaceView.space.id.let {
                    if (spaceView.isPinned) onUnpinSpace(it) else onPinSpace(it)
                }
            },
            onSpaceSettings = {
                spaceView.space.id.let { onSpaceSettings(it) }
            },
            onDeleteOrLeaveSpace = {
                spaceView.space.targetSpaceId?.let { onDeleteOrLeaveSpace(it, spaceView.isOwner) }
            }
        )
    }
}

@Composable
private fun ContentSpace(
    modifier: Modifier,
    title: String,
    isPinned: Boolean = false,
) {
    Column(modifier = modifier) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp),
                text = title.ifEmpty { stringResource(id = R.string.untitled) },
                style = BodySemiBold,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isPinned) {
                Image(
                    painter = painterResource(R.drawable.ic_pin_18),
                    contentDescription = stringResource(R.string.content_desc_pin),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
@DefaultPreviews
fun DataSpaceCardPreview() {
    Column {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        )
        DataSpaceCard(
            modifier = Modifier.fillMaxWidth(),
            title = "B&O Museum",
            icon = SpaceIconView.ChatSpace.Placeholder(),
            isPinned = true,
            spaceBackground = SpaceBackground.SolidColor(
                color = androidx.compose.ui.graphics.Color(
                    0xFFE0F7FA
                )
            ),
            spaceView = VaultSpaceView.DataSpace(
                space = ObjectWrapper.SpaceView(
                    map = mapOf(
                        "name" to "Space 1",
                        "id" to "spaceId1"
                    )
                ),
                icon = SpaceIconView.ChatSpace.Placeholder(),
                isOwner = true,
                accessType = "Owner"
            ),
        )
    }

}

const val MENTION_COUNT_THRESHOLD = 9
