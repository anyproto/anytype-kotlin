package com.anytypeio.anytype.core_ui.features.multiplayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.restrictions.SpaceStatus
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.SpaceIconView
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Header
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.spaces.SpaceListViewModel.SpaceListItemView

@Composable
fun SpaceListScreen(
    state: ViewState<List<SpaceListItemView>>,
    onDeleteSpaceClicked: (SpaceListItemView) -> Unit,
    onLeaveSpaceClicked: (SpaceListItemView) -> Unit,
    onCancelJoinRequestClicked: (SpaceListItemView) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(rememberNestedScrollInteropConnection())
    ) {
        Dragger(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.CenterHorizontally)
        )
        Header(text = stringResource(id = R.string.multiplayer_spaces))
        LazyColumn {
            if (state is ViewState.Success) {
                itemsIndexed(
                    items = state.data,
                    itemContent = { idx, item ->
                        SpaceListCardItem(
                            spaceName = item.space.name.orEmpty(),
                            spaceStatus = item.space.spaceAccountStatus,
                            permissions = item.permissions,
                            spaceIcon = item.icon,
                            modifier = Modifier.padding(
                                start = 10.dp,
                                end = 10.dp,
                                top = 7.dp,
                                bottom = if (idx == state.data.lastIndex) 24.dp else 7.dp
                            ),
                            onDeleteSpaceClicked = {
                                onDeleteSpaceClicked(item)
                            },
                            onCancelJoinRequestClicked = {
                                onCancelJoinRequestClicked(item)
                            },
                            onLeaveSpaceClicked = {
                                onLeaveSpaceClicked(item)
                            },
                            actions = item.actions
                        )
                    },
                    key = { _, item -> item.space.id }
                )
            }
        }
    }
}

@Composable
fun SpaceListCardItem(
    spaceName: String,
    spaceStatus: SpaceStatus,
    spaceIcon: SpaceIconView,
    permissions: SpaceMemberPermissions,
    modifier: Modifier = Modifier,
    onDeleteSpaceClicked: () -> Unit,
    onLeaveSpaceClicked: () -> Unit,
    onCancelJoinRequestClicked: () -> Unit,
    actions: List<SpaceListItemView.Action>
) {
    ConstraintLayout(
        modifier = modifier
            .border(
                width = 0.5.dp,
                color = colorResource(id = R.color.shape_primary),
                shape = RoundedCornerShape(12.dp)
            )
            .fillMaxWidth()
            .clickable {
                onDeleteSpaceClicked()
            }

    ) {
        val isCardMenuExpanded = remember { mutableStateOf(false) }

        val (icon, title, subtitle, divider, dots, circle, network, status, footer) = createRefs()

        SpaceIconView(
            icon = spaceIcon,
            onSpaceIconClick = {},
            modifier = Modifier
                .constrainAs(icon) {
                    top.linkTo(parent.top, margin = 16.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                },
            mainSize = 48.dp,
            gradientSize = 32.dp,
            gradientCornerRadius = 4.dp
        )

        Box(
            modifier = Modifier
                .size(24.dp)
                .constrainAs(dots) {
                    top.linkTo(parent.top, margin = 24.dp)
                    end.linkTo(parent.end, margin = 12.dp)
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_space_list_dots),
                contentDescription = "Three-dots button",
                modifier = Modifier.noRippleClickable {
                    isCardMenuExpanded.value = !isCardMenuExpanded.value
                }
            )
            DropdownMenu(
                expanded = isCardMenuExpanded.value,
                onDismissRequest = {
                    isCardMenuExpanded.value = false
                },
                offset = DpOffset(x = 0.dp, y = 6.dp)
            ) {
                actions.forEachIndexed { idx, action ->
                    when(action) {
                        SpaceListItemView.Action.CancelJoinRequest -> {
                            DropdownMenuItem(
                                onClick = {
                                   onCancelJoinRequestClicked()
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.multiplayer_cancel_join_request),
                                    style = BodyRegular,
                                    color = colorResource(id = R.color.palette_light_red)
                                )
                            }
                        }
                        SpaceListItemView.Action.DeleteSpace -> {
                            DropdownMenuItem(
                                onClick = {
                                    onDeleteSpaceClicked()
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.delete_space),
                                    style = BodyRegular,
                                    color = colorResource(id = R.color.palette_system_red)
                                )
                            }
                        }
                        SpaceListItemView.Action.LeaveSpace -> {
                            DropdownMenuItem(
                                onClick = {
                                    onLeaveSpaceClicked()
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.multiplayer_leave_space),
                                    style = BodyRegular,
                                    color = colorResource(id = R.color.palette_system_red)
                                )
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = spaceName.ifEmpty { stringResource(id = R.string.untitled) },
            style = Title2,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.constrainAs(title) {
                top.linkTo(parent.top, margin = 21.dp)
                start.linkTo(icon.end, margin = 12.dp)
                end.linkTo(dots.start, margin = 12.dp)
                width = Dimension.fillToConstraints
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = when(permissions) {
                SpaceMemberPermissions.OWNER -> stringResource(id = R.string.multiplayer_owner)
                SpaceMemberPermissions.READER -> stringResource(id = R.string.multiplayer_can_view)
                SpaceMemberPermissions.WRITER -> stringResource(id = R.string.multiplayer_can_edit)
                SpaceMemberPermissions.NO_PERMISSIONS -> EMPTY_STRING_VALUE
            },
            style = Relations2,
            color = colorResource(id = R.color.text_secondary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.constrainAs(subtitle) {
                top.linkTo(title.bottom)
                start.linkTo(icon.end, margin = 12.dp)
                end.linkTo(dots.start, margin = 12.dp)
                width = Dimension.fillToConstraints
            }
        )

        Divider(
            modifier = Modifier.constrainAs(divider) {
                top.linkTo(icon.bottom, margin = 16.dp)
                start.linkTo(parent.start, margin = 16.dp)
                end.linkTo(parent.end, margin = 12.dp)
            }
        )

        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = when (spaceStatus) {
                        SpaceStatus.SPACE_ACTIVE, SpaceStatus.UNKNOWN -> colorResource(
                            id = R.color.palette_system_green
                        )

                        SpaceStatus.SPACE_JOINING -> colorResource(
                            id = R.color.palette_system_amber_100
                        )

                        SpaceStatus.SPACE_REMOVING -> colorResource(
                            id = R.color.palette_system_red
                        )

                        else -> colorResource(
                            id = R.color.palette_dark_grey
                        )
                    },
                    shape = CircleShape
                )
                .constrainAs(circle) {
                    start.linkTo(parent.start, margin = 16.dp)
                    top.linkTo(network.top)
                    bottom.linkTo(network.bottom)
                }
        )

        Text(
            text = stringResource(id = R.string.network_with_colon),
            color = colorResource(id = R.color.text_secondary),
            style = Relations3,
            modifier = Modifier.constrainAs(network) {
                top.linkTo(divider.bottom, margin = 12.dp)
                start.linkTo(circle.end, margin = 6.dp)
            }
        )

        Text(
            text = when(spaceStatus) {
                SpaceStatus.SPACE_ACTIVE, SpaceStatus.UNKNOWN -> stringResource(
                    id = R.string.multiplayer_space_status_active
                )
                SpaceStatus.SPACE_JOINING -> stringResource(
                    id = R.string.multiplayer_space_status_joining
                )
                SpaceStatus.SPACE_REMOVING -> stringResource(
                    id = R.string.multiplayer_space_status_removing
                )
                else -> spaceStatus.code.toString()
            },
            style = Relations3,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.constrainAs(status) {
                top.linkTo(divider.bottom, margin = 12.dp)
                start.linkTo(network.end, margin = 4.dp)
            }
        )

        Spacer(
            modifier = Modifier
                .height(16.dp)
                .constrainAs(footer) {
                    top.linkTo(network.bottom)
                }
        )
    }
}

@Preview
@Composable
private fun SpaceListScreenPreview() {
    SpaceListScreen(
        state = ViewState.Loading,
        onCancelJoinRequestClicked = {},
        onDeleteSpaceClicked = {},
        onLeaveSpaceClicked = {}
    )
}

@Preview
@Composable
private fun SpaceCardItemPreview() {
    SpaceListCardItem(
        spaceName = "Architecture",
        spaceStatus = SpaceStatus.SPACE_ACTIVE,
        permissions = SpaceMemberPermissions.OWNER,
        spaceIcon = SpaceIconView.Placeholder,
        onCancelJoinRequestClicked = {},
        onLeaveSpaceClicked = {},
        onDeleteSpaceClicked = {},
        actions = emptyList()
    )
}



