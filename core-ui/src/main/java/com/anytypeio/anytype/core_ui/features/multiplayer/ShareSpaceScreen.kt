package com.anytypeio.anytype.core_ui.features.multiplayer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.multiplayer.ParticipantStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLinkAccessLevel
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.AlertConfig
import com.anytypeio.anytype.core_ui.foundation.BUTTON_PRIMARY
import com.anytypeio.anytype.core_ui.foundation.BUTTON_SECONDARY
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.GenericAlert
import com.anytypeio.anytype.core_ui.foundation.Section
import com.anytypeio.anytype.core_ui.foundation.Toolbar
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonIncentiveSecond
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingPrimaryLarge
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.ButtonUpgradeBlack
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_ui.views.Title3
import com.anytypeio.anytype.core_ui.views.UxSmallTextMedium
import com.anytypeio.anytype.core_ui.views.animations.DotsLoadingIndicator
import com.anytypeio.anytype.core_ui.views.animations.FadeAnimationSpecs
import com.anytypeio.anytype.presentation.multiplayer.SpaceLimitsState
import com.anytypeio.anytype.presentation.multiplayer.SpaceMemberView
import com.anytypeio.anytype.presentation.objects.SpaceMemberIconView

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ShareSpaceScreen(
    isLoadingInProgress: Boolean,
    isCurrentUserOwner: Boolean,
    members: List<SpaceMemberView>,
    incentiveState: SpaceLimitsState,
    inviteLinkAccessLevel: SpaceInviteLinkAccessLevel,
    inviteLinkAccessLoading: Boolean,
    confirmationDialogLevel: SpaceInviteLinkAccessLevel?,
    onContextActionClicked: (SpaceMemberView, SpaceMemberView.ActionType) -> Unit,
    onIncentiveClicked: () -> Unit,
    onManageSpacesClicked: () -> Unit,
    onMemberClicked: (ObjectWrapper.SpaceMember) -> Unit,

    onInviteLinkAccessLevelSelected: (SpaceInviteLinkAccessLevel) -> Unit,
    onInviteLinkAccessChangeConfirmed: () -> Unit,
    onInviteLinkAccessChangeCancel: () -> Unit,

    onShareInviteLinkClicked: (String) -> Unit,
    onCopyInviteLinkClicked: (String, String) -> Unit,
    onShareQrCodeClicked: (String, String) -> Unit,
    onMakePrivateClicked: () -> Unit,
    spaceAccessType: SpaceAccessType?,
    isMakePrivateEnabled: Boolean
) {
    val nestedScrollInteropConnection = rememberNestedScrollInteropConnection()
    var showInviteLinkAccessSelector by remember(false) { mutableStateOf(false) }
    val sheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = colorResource(id = R.color.background_primary),
            )
            .nestedScroll(nestedScrollInteropConnection)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Dragger(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(vertical = 6.dp)
                )
            }
            // Header with title and menu
            ShareSpaceHeader(
                title = stringResource(R.string.multiplayer_members),
                inviteLinkAccessLevel = inviteLinkAccessLevel,
                isMakePrivateEnabled = isMakePrivateEnabled,
                isCurrentUserOwner = isCurrentUserOwner,
                onCopyClicked = onCopyInviteLinkClicked,
                onShareClicked = onShareInviteLinkClicked,
                onQrCodeClicked = onShareQrCodeClicked,
                onMakePrivateClicked = onMakePrivateClicked
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {

                item {
                    Incentive(
                        incentiveState = incentiveState,
                        onIncentiveClicked = onIncentiveClicked,
                        onManageSpacesClicked = onManageSpacesClicked
                    )

                    Section(
                        title = stringResource(R.string.multiplayer_members_invite_links_section)
                    )
                    val item = inviteLinkAccessLevel.getInviteLinkItemParams()
                    val isAccessLevelDisabled =
                        (inviteLinkAccessLevel as? SpaceInviteLinkAccessLevel.LinkDisabled)?.possibleToUpdate == false
                    AccessLevelOption(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 12.dp)
                            .noRippleThrottledClickable {
                                // Only owners can modify invite link access settings
                                if (isCurrentUserOwner && !isAccessLevelDisabled) {
                                    showInviteLinkAccessSelector = !showInviteLinkAccessSelector
                                }
                            },
                        uiItemUI = item,
                        isCurrentUserOwner = isCurrentUserOwner,
                        isDisabled = isAccessLevelDisabled
                    )
                }

                item {
                    // Show invite link and copy button when not LINK_DISABLED
                    when (inviteLinkAccessLevel) {
                        is SpaceInviteLinkAccessLevel.EditorAccess -> InviteLinkDisplay(
                            link = inviteLinkAccessLevel.link,
                            onCopyClicked = onCopyInviteLinkClicked,
                            onShareClicked = onShareInviteLinkClicked,
                            onQrCodeClicked = onShareQrCodeClicked
                        )

                        is SpaceInviteLinkAccessLevel.RequestAccess -> InviteLinkDisplay(
                            link = inviteLinkAccessLevel.link,
                            onCopyClicked = onCopyInviteLinkClicked,
                            onShareClicked = onShareInviteLinkClicked,
                            onQrCodeClicked = onShareQrCodeClicked
                        )

                        is SpaceInviteLinkAccessLevel.ViewerAccess -> InviteLinkDisplay(
                            link = inviteLinkAccessLevel.link,
                            onCopyClicked = onCopyInviteLinkClicked,
                            onShareClicked = onShareInviteLinkClicked,
                            onQrCodeClicked = onShareQrCodeClicked
                        )

                        is SpaceInviteLinkAccessLevel.LinkDisabled -> {}
                    }
                }
                item {
                    Section(
                        title = stringResource(R.string.multiplayer_members_and_requests)
                    )
                }
                members.forEachIndexed { index, member ->
                    item {
                        SpaceMember(
                            memberView = member,
                            onContextActionClicked = onContextActionClicked,
                            onMemberClicked = onMemberClicked
                        )
                        Divider(paddingStart = 16.dp, paddingEnd = 16.dp)
                    }
                }
                if (members.size > 2) {
                    // Workaround adding footer to prevent content invisible behind link card
                    item {
                        Spacer(modifier = Modifier.height(150.dp))
                    }
                }
            }
        }

        val density = LocalDensity.current
        val draggedDownAnchorTop = with(density) { 250.dp.toPx() }

        val anchors = DraggableAnchors {
            DragValue.DRAGGED_DOWN at draggedDownAnchorTop
            DragValue.DRAGGED_UP at 0f
        }

        val decayAnimation = rememberSplineBasedDecay<Float>()

        val anchoredDraggableState = remember {
            AnchoredDraggableState(
                initialValue = DragValue.DRAGGED_UP,
                positionalThreshold = { distance: Float -> distance * 0.5f },
                velocityThreshold = { with(density) { 100.dp.toPx() } },
                snapAnimationSpec = tween(),
                decayAnimationSpec = decayAnimation
            )
        }
        SideEffect {
            anchoredDraggableState.updateAnchors(anchors)
        }
        //Invite Link Access Selector
        if (showInviteLinkAccessSelector) {
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = { showInviteLinkAccessSelector = false },
                dragHandle = null,
                containerColor = Color.Transparent,
                contentColor = Color.Transparent,
            ) {
                InviteLinkAccessSelector(
                    currentAccessLevel = inviteLinkAccessLevel,
                    onAccessLevelChanged = {
                        showInviteLinkAccessSelector = false
                        onInviteLinkAccessLevelSelected(it)
                    }
                )
            }
        }
        val loadingAlpha by animateFloatAsState(targetValue = if (isLoadingInProgress) 1f else 0f)

        if (isLoadingInProgress) {
            DotsLoadingIndicator(
                animating = true,
                modifier = Modifier
                    .graphicsLayer { alpha = loadingAlpha }
                    .align(Alignment.Center),
                animationSpecs = FadeAnimationSpecs(itemCount = 3),
                color = colorResource(id = R.color.text_primary),
                size = ButtonSize.Large
            )
        }

        // Confirmation dialog for invite link access changes
        if (confirmationDialogLevel != null) {
            showConfirmScreen(
                onInviteLinkAccessChangeConfirmed = onInviteLinkAccessChangeConfirmed,
                onInviteLinkAccessChangeCancel = onInviteLinkAccessChangeCancel,
                onDismissRequest = onInviteLinkAccessChangeCancel,
                isLoading = inviteLinkAccessLoading
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun showConfirmScreen(
    onInviteLinkAccessChangeConfirmed: () -> Unit,
    onInviteLinkAccessChangeCancel: () -> Unit,
    onDismissRequest: () -> Unit,
    isLoading: Boolean
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = colorResource(id = R.color.background_secondary),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = null
    ) {
        GenericAlert(
            onFirstButtonClicked = onInviteLinkAccessChangeCancel,
            onSecondButtonClicked = {
                onInviteLinkAccessChangeConfirmed()
            },
            config = AlertConfig.WithTwoButtons(
                title = stringResource(id = R.string.multiplayer_link_will_be_invalidated),
                description = stringResource(R.string.multiplayer_link_will_be_invalidated_desc),
                firstButtonText = stringResource(R.string.cancel),
                secondButtonText = stringResource(R.string.confirm),
                firstButtonType = BUTTON_SECONDARY,
                secondButtonType = BUTTON_PRIMARY,
                icon = R.drawable.ic_popup_alert_56,
                isSecondButtonLoading = isLoading,
            )
        )
    }
}

@Composable
private fun ShareSpaceHeader(
    title: String,
    inviteLinkAccessLevel: SpaceInviteLinkAccessLevel,
    isMakePrivateEnabled: Boolean,
    isCurrentUserOwner: Boolean,
    onCopyClicked: (String, String) -> Unit,
    onShareClicked: (String) -> Unit,
    onQrCodeClicked: (String, String) -> Unit,
    onMakePrivateClicked: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp)
    ) {
        // Title on the left
        Text(
            text = title,
            style = PreviewTitle2Medium,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.align(Alignment.Center)
        )

        // Three dots menu on the right
        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
            Image(
                painter = painterResource(id = R.drawable.ic_action_more),
                contentDescription = "More options",
                modifier = Modifier
                    .size(24.dp)
                    .noRippleClickable {
                        showMenu = true
                    },
                contentScale = ContentScale.Inside
            )

            // Dropdown menu
            val link = when (inviteLinkAccessLevel) {
                is SpaceInviteLinkAccessLevel.EditorAccess -> inviteLinkAccessLevel.link
                is SpaceInviteLinkAccessLevel.RequestAccess -> inviteLinkAccessLevel.link
                is SpaceInviteLinkAccessLevel.ViewerAccess -> inviteLinkAccessLevel.link
                is SpaceInviteLinkAccessLevel.LinkDisabled -> ""
            }

            val isLinkDisabled = inviteLinkAccessLevel is SpaceInviteLinkAccessLevel.LinkDisabled

            DropdownMenu(
                modifier = Modifier.widthIn(min = 252.dp),
                containerColor = colorResource(R.color.background_secondary),
                shape = RoundedCornerShape(12.dp),
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                // Copy link
                DropdownMenuItem(
                    modifier = Modifier.alpha(if (isLinkDisabled) 0.3f else 1.0f),
                    onClick = {
                        if (!isLinkDisabled) {
                            onCopyClicked(link, EventsDictionary.CopyLinkRoutes.MENU)
                        }
                        showMenu = false
                    }
                ) {
                    Text(
                        text = stringResource(R.string.multiplayer_copy_link),
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_primary),
                        modifier = Modifier.weight(1.0f)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_object_action_copy_link),
                        contentDescription = "Copy link icon",
                        modifier = Modifier
                            .size(22.dp)
                            .align(Alignment.CenterVertically),
                        colorFilter = ColorFilter.tint(
                            colorResource(id = R.color.text_primary)
                        )
                    )
                }

                Divider(paddingStart = 0.dp, paddingEnd = 0.dp)

                // Share link
                DropdownMenuItem(
                    modifier = Modifier.alpha(if (isLinkDisabled) 0.3f else 1.0f),
                    onClick = {
                        if (!isLinkDisabled) {
                            onShareClicked(link)
                        }
                        showMenu = false
                    }
                ) {
                    Text(
                        text = stringResource(R.string.multiplayer_share_invite_link_menu),
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_primary),
                        modifier = Modifier.weight(1.0f)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_menu_share_link),
                        contentDescription = "Share link icon",
                        modifier = Modifier
                            .size(22.dp)
                            .align(Alignment.CenterVertically)
                    )
                }

                Divider(paddingStart = 0.dp, paddingEnd = 0.dp)

                // Show QR code
                DropdownMenuItem(
                    modifier = Modifier.alpha(if (isLinkDisabled) 0.3f else 1.0f),
                    onClick = {
                        if (!isLinkDisabled) {
                            onQrCodeClicked(link, EventsDictionary.CopyLinkRoutes.MENU)
                        }
                        showMenu = false
                    }
                ) {
                    Text(
                        text = stringResource(R.string.multiplayer_qr_invite_link_menu),
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_primary),
                        modifier = Modifier.weight(1.0f)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_qr_code_24),
                        contentDescription = "QR code icon",
                        modifier = Modifier
                            .size(22.dp)
                            .align(Alignment.CenterVertically),
                    )
                }

                // Make Private - only visible for owners
                if (isCurrentUserOwner) {
                    Divider(paddingStart = 0.dp, paddingEnd = 0.dp)

                    DropdownMenuItem(
                        modifier = Modifier.alpha(if (isMakePrivateEnabled) 1.0f else 0.3f),
                        onClick = {
                            if (isMakePrivateEnabled) {
                                onMakePrivateClicked()
                            }
                            showMenu = false
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.multiplayer_make_private),
                            style = BodyRegular,
                            color = colorResource(id = R.color.text_primary),
                            modifier = Modifier.weight(1.0f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Incentive(
    incentiveState: SpaceLimitsState,
    onIncentiveClicked: () -> Unit,
    onManageSpacesClicked: () -> Unit = {}
) {
    when (incentiveState) {
        is SpaceLimitsState.ViewersLimit -> {
            AddEditorsIncentive(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                text = stringResource(
                    id = R.string.members_limits_incentive_viewers,
                    incentiveState.count
                )
            ) {
                onIncentiveClicked()
            }
        }
        is SpaceLimitsState.EditorsLimit -> {
            AddEditorsIncentive(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                text = stringResource(
                    id = R.string.members_limits_incentive_editors,
                    incentiveState.count
                )
            ){
                onIncentiveClicked()
            }
        }

        SpaceLimitsState.Init -> {
            //show nothing
        }

        is SpaceLimitsState.SharableLimit -> {
            SharedSpacesIncentiveItem(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                count = incentiveState.count,
                onAddMoreSpacesClicked = {
                    onIncentiveClicked()
                },
                onManageSpacesClicked = {
                    onManageSpacesClicked()
                }
            )
        }
    }
}

@Composable
fun SharedSpacesIncentiveItem(
    modifier: Modifier = Modifier,
    count: Int,
    onAddMoreSpacesClicked: () -> Unit,
    onManageSpacesClicked: () -> Unit
) {
    Column(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorResource(R.color.incentive_gradient_start),
                        colorResource(R.color.incentive_gradient_end)
                    ),
                    startY = 0.0f,
                    endY = Float.POSITIVE_INFINITY // vertical (180deg)
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(
                id = R.string.membership_space_settings_share_limit,
                count
            ),
            color = colorResource(id = R.color.text_primary),
            style = Title2
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.membership_space_settings_share_limit_2),
            color = colorResource(id = R.color.text_primary),
            style = Title3
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ButtonIncentiveSecond(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 12.dp)
                    .height(36.dp),
                onClick = {
                    onManageSpacesClicked()
                },
                text = stringResource(id = R.string.multiplayer_manage_spaces),
                style = UxSmallTextMedium
            )
            ButtonUpgradeBlack(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 12.dp)
                    .height(36.dp),
                onClick = {
                    onAddMoreSpacesClicked()
                },
                text = stringResource(id = R.string.multiplayer_upgrade_button),
                style = UxSmallTextMedium
            )
        }
    }
}

@DefaultPreviews
@Composable
private fun PrivateSpaceSharingPreview() {
    SharedSpacesIncentiveItem(
        count = 3,
        modifier = Modifier.fillMaxWidth(),
        onAddMoreSpacesClicked = {},
        onManageSpacesClicked = {}
    )
}

@Composable
private fun AddEditorsIncentive(
    modifier: Modifier = Modifier,
    text: String,
    onButtonClicked: () -> Unit
) {
    Column(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorResource(R.color.incentive_gradient_start),
                        colorResource(R.color.incentive_gradient_end)
                    ),
                    startY = 0.0f,
                    endY = Float.POSITIVE_INFINITY // vertical (180deg)
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = text,
            color = colorResource(id = R.color.text_primary),
            style = Title2
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.members_limits_incentive_editors_sub),
            color = colorResource(id = R.color.text_primary),
            style = Title3
        )
        ButtonUpgradeBlack(
            modifier = Modifier
                .padding(top = 12.dp)
                .height(36.dp),
            onClick = { onButtonClicked() },
            text = stringResource(id = R.string.multiplayer_upgrade_button),
            style = UxSmallTextMedium
        )
    }
}

enum class DragValue { DRAGGED_DOWN, DRAGGED_UP }

/**
 * Displays a notification badge for pending join requests.
 * The badge consists of a white background circle (12dp) with a blue dot (8dp) centered inside.
 */
@Composable
private fun PendingRequestBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(12.dp)
            .background(
                color = colorResource(id = R.color.background_primary),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = colorResource(id = R.color.control_accent),
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun SpaceMember(
    memberView: SpaceMemberView,
    onContextActionClicked: (SpaceMemberView, SpaceMemberView.ActionType) -> Unit,
    onMemberClicked: (ObjectWrapper.SpaceMember) -> Unit = {}
) {
    val isViewRequest = memberView.contextActions.any { it.actionType == SpaceMemberView.ActionType.VIEW_REQUEST }

    Row(
        modifier = Modifier
            .height(72.dp)
            .fillMaxWidth()
            .noRippleThrottledClickable { onMemberClicked(memberView.obj) }
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            SpaceMemberIcon(
                icon = memberView.icon,
                modifier = Modifier
            )
            if (isViewRequest) {
                PendingRequestBadge(
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1.0f)
        ) {
            Row {
                Text(
                    text = memberView.obj.name.orEmpty()
                        .ifEmpty { stringResource(id = R.string.untitled) },
                    style = PreviewTitle2Medium,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (memberView.isUser) {
                    Spacer(modifier = Modifier.width(4.dp))
                    val youAsMemberText = stringResource(id = R.string.multiplayer_you_as_member)
                    Text(
                        text = "($youAsMemberText)",
                        style = PreviewTitle2Medium,
                        color = colorResource(id = R.color.text_primary),
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = memberView.obj.globalName ?: memberView.obj.identity,
                color = colorResource(id = R.color.text_secondary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = Caption1Regular
            )
        }

        MemberStatusWithDropdown(
            statusText = memberView.statusText,
            contextActions = memberView.contextActions,
            memberView = memberView,
            onContextActionClicked = onContextActionClicked,
            modifier = Modifier.align(Alignment.CenterVertically),
            isViewRequest = isViewRequest
        )
        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
private fun MemberStatusWithDropdown(
    statusText: String?,
    contextActions: List<SpaceMemberView.ContextAction>,
    memberView: SpaceMemberView,
    onContextActionClicked: (SpaceMemberView, SpaceMemberView.ActionType) -> Unit,
    modifier: Modifier = Modifier,
    isViewRequest: Boolean
) {
    var isMemberMenuExpanded by remember { mutableStateOf(false) }

    if (contextActions.isNotEmpty()) {
        Box(modifier = modifier) {
            Row(
                modifier = Modifier
                    .noRippleClickable { isMemberMenuExpanded = true }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status text
                statusText?.let { text ->
                    Text(
                        text = text,
                        style = Title3,
                        color = colorResource(
                            id = if (isViewRequest) {
                                R.color.text_secondary
                            } else {
                                R.color.text_primary
                            }
                        )
                    )
                }

                // Dropdown arrow icon
                Spacer(modifier = Modifier.width(4.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_arrow_down_18),
                    contentDescription = "Menu button",
                    colorFilter = ColorFilter.tint(
                        colorResource(
                            id = if (isViewRequest) {
                                R.color.text_secondary
                            } else {
                                R.color.text_primary
                            }
                        )
                    )
                )
            }

            // Dropdown menu
            DropdownMenu(
                modifier = Modifier.width(254.dp),
                containerColor = colorResource(R.color.background_secondary),
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 8.dp,
                offset = DpOffset(x = 16.dp, y = 8.dp),
                expanded = isMemberMenuExpanded,
                onDismissRequest = { isMemberMenuExpanded = false }
            ) {
                contextActions.forEachIndexed { index, action ->
                    DropdownMenuItem(
                        modifier = Modifier.alpha(if (action.isEnabled) 1.0f else 0.3f),
                        onClick = {
                            if (action.isEnabled) {
                                onContextActionClicked(memberView, action.actionType)
                            }
                            isMemberMenuExpanded = false
                        }
                    ) {
                        Text(
                            text = action.title,
                            style = BodyRegular,
                            color = colorResource(
                                id = if (action.isDestructive) {
                                    R.color.palette_system_red
                                } else {
                                    R.color.text_primary
                                }
                            ),
                            modifier = Modifier.weight(1.0f)
                        )
                        if (action.isSelected) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_dropdown_menu_check),
                                contentDescription = "Checked icon",
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                    // Add divider between items (except after the last item)
                    if (index < contextActions.size - 1) {
                        Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
                    }
                }
            }
        }
    } else {
        // When no context actions, just show the status text
        statusText?.let { text ->
            Text(
                text = text,
                style = Title3,
                color = colorResource(id = R.color.text_primary),
                modifier = modifier
            )
        }
    }
}

@Composable
fun SpaceMemberIcon(
    icon: SpaceMemberIconView,
    modifier: Modifier,
    iconSize: Dp = 48.dp,
    textSize: TextUnit = 28.sp
) {
    when (icon) {
        is SpaceMemberIconView.Placeholder -> {
            Box(
                modifier = modifier
                    .size(iconSize)
                    .clip(CircleShape)
                    .background(color = colorResource(id = R.color.shape_tertiary))
            ) {
                Text(
                    text = icon
                        .name
                        .ifEmpty { stringResource(id = R.string.u) }
                        .take(1)
                        .uppercase(),
                    modifier = Modifier.align(Alignment.Center),
                    style = TextStyle(
                        fontSize = textSize,
                        fontWeight = FontWeight.SemiBold,
                        color = colorResource(id = R.color.control_secondary)
                    )
                )
            }
        }

        is SpaceMemberIconView.Image -> {
            Image(
                painter = rememberAsyncImagePainter(icon.url),
                contentDescription = "Icon from URI",
                modifier = modifier
                    .size(iconSize)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        }
    }
}


@Composable
fun InviteLinkDisplay(
    modifier: Modifier = Modifier,
    link: String,
    onCopyClicked: (String, String) -> Unit,
    onShareClicked: (String) -> Unit,
    onQrCodeClicked: (String, String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Link text display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = colorResource(id = R.color.transparent_tertiary),
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = link,
                style = BodyCalloutRegular,
                color = colorResource(id = R.color.text_primary),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Copy button
        ButtonOnboardingPrimaryLarge(
            modifierBox = Modifier.fillMaxWidth(),
            text = stringResource(R.string.copy_link),
            onClick = {
                onCopyClicked(link, EventsDictionary.CopyLinkRoutes.BUTTON)
            },
            size = ButtonSize.Large,
        )
    }
}

@Composable
@DefaultPreviews
fun SpaceJoinRequestPreview() {
    val memberView = SpaceMemberView(
        obj = ObjectWrapper.SpaceMember(
            mapOf(
                Relations.ID to "1",
                Relations.NAME to "Konstantin",
                Relations.PARTICIPANT_STATUS to ParticipantStatus.JOINING.code.toDouble(),
                Relations.GLOBAL_NAME to "konstantin.anytype.io"
            )
        ),
        icon = SpaceMemberIconView.Placeholder(name = "Konstantin"),
        isUser = false,
        statusText = "Approve request",
        contextActions = listOf(
            SpaceMemberView.ContextAction(
                title = "View Request",
                actionType = SpaceMemberView.ActionType.VIEW_REQUEST
            )
        )
    )
    SpaceMember(
        memberView = memberView,
        onContextActionClicked = { _, _ -> },
        onMemberClicked = {}
    )
}

@Composable
@DefaultPreviews
fun SpaceJoinLongTitleRequestPreview() {
    val memberView = SpaceMemberView(
        obj = ObjectWrapper.SpaceMember(
            mapOf(
                Relations.ID to "1",
                Relations.NAME to "Very Long Name That Should Be Truncated",
                Relations.PARTICIPANT_STATUS to ParticipantStatus.JOINING.code.toDouble(),
                Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.WRITER.code.toDouble(),
                Relations.GLOBAL_NAME to "konstantin.anytype.io"
            )
        ),
        icon = SpaceMemberIconView.Placeholder(name = "Konstantin"),
        isUser = false,
        statusText = "Pending"
    )
    SpaceMember(
        memberView = memberView,
        onContextActionClicked = { _, _ -> },
        onMemberClicked = {}
    )
}

@Composable
@DefaultPreviews
fun SpaceLeaveRequestPreview() {
    val memberView = SpaceMemberView(
        obj = ObjectWrapper.SpaceMember(
            mapOf(
                Relations.ID to "1",
                Relations.NAME to "Konstantin",
                Relations.PARTICIPANT_STATUS to ParticipantStatus.REMOVING.code.toDouble(),
                Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.WRITER.code.toDouble(),
                Relations.GLOBAL_NAME to "konstantin.anytype.io"
            )
        ),
        icon = SpaceMemberIconView.Placeholder(name = "Konstantin"),
        isUser = true,
        statusText = "Leave request"
    )
    SpaceMember(
        memberView = memberView,
        onContextActionClicked = { _, _ -> },
        onMemberClicked = {}
    )
}

@Composable
@DefaultPreviews
fun ShareSpaceScreenPreview1() {
    ShareSpaceScreen(
        onShareInviteLinkClicked = {},
        members = buildList {
            add(
                SpaceMemberView(
                    obj = ObjectWrapper.SpaceMember(
                        mapOf(
                            Relations.ID to "1",
                            Relations.NAME to "Konstantin",
                            Relations.PARTICIPANT_STATUS to ParticipantStatus.JOINING.code.toDouble(),
                            Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.WRITER.code.toDouble(),
                            Relations.GLOBAL_NAME to "konstantin.anytype.io"
                        )
                    ),
                    icon = SpaceMemberIconView.Placeholder(
                        name = "Konstantin"
                    )
                )
            )
            add(
                SpaceMemberView(
                    obj = ObjectWrapper.SpaceMember(
                        mapOf(
                            Relations.ID to "2",
                            Relations.NAME to "Evgenii",
                            Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble(),
                            Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.WRITER.code.toDouble(),
                            Relations.GLOBAL_NAME to "konstantin.anytype.io"
                        )
                    ),
                    icon = SpaceMemberIconView.Placeholder(
                        name = "Evgenii"
                    )
                )
            )
            add(
                SpaceMemberView(
                    obj = ObjectWrapper.SpaceMember(
                        mapOf(
                            Relations.ID to "2",
                            Relations.NAME to "Aleksey",
                            Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble(),
                            Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.WRITER.code.toDouble(),
                            Relations.GLOBAL_NAME to "konstantin.anytype.io"
                        )
                    ),
                    icon = SpaceMemberIconView.Placeholder(
                        name = "Aleksey"
                    )
                )
            )
            add(
                SpaceMemberView(
                    obj = ObjectWrapper.SpaceMember(
                        mapOf(
                            Relations.ID to "2",
                            Relations.NAME to "Anton",
                            Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble(),
                            Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.WRITER.code.toDouble(),
                            Relations.GLOBAL_NAME to "konstantin.anytype.io"
                        )
                    ),
                    icon = SpaceMemberIconView.Placeholder(
                        name = "Anton"
                    )
                )
            )
        },
        onContextActionClicked = { _, _ -> },
        onShareQrCodeClicked = { _, _ -> },
        incentiveState = SpaceLimitsState.EditorsLimit(4),
        onIncentiveClicked = {},
        isLoadingInProgress = false,
        onMemberClicked = {},
        inviteLinkAccessLevel = SpaceInviteLinkAccessLevel.EditorAccess("https://example.com/invite"),
        inviteLinkAccessLoading = false,
        confirmationDialogLevel = null,
        onInviteLinkAccessLevelSelected = {},
        onInviteLinkAccessChangeConfirmed = {},
        onInviteLinkAccessChangeCancel = {},
        onCopyInviteLinkClicked = { _, _ -> },
        isCurrentUserOwner = true,
        onManageSpacesClicked = {},
        onMakePrivateClicked = {},
        spaceAccessType = SpaceAccessType.SHARED,
        isMakePrivateEnabled = true
    )
}

@Composable
@DefaultPreviews
private fun SpaceOwnerMemberPreview() {
    val memberView = SpaceMemberView(
        obj = ObjectWrapper.SpaceMember(
            mapOf(
                Relations.ID to "2",
                Relations.NAME to "Evgenii",
                Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.OWNER.code.toDouble(),
                Relations.GLOBAL_NAME to "konstantin.anytype.io"
            )
        ),
        icon = SpaceMemberIconView.Placeholder(name = "Evgenii"),
        isUser = true,
        statusText = "Owner"
    )
    SpaceMember(
        memberView = memberView,
        onContextActionClicked = { _, _ -> },
        onMemberClicked = {}
    )
}

@Composable
@DefaultPreviews
private fun SpaceEditorMemberPreview() {
    val memberView = SpaceMemberView(
        obj = ObjectWrapper.SpaceMember(
            mapOf(
                Relations.ID to "2",
                Relations.NAME to "Evgenii",
                Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.WRITER.code.toDouble(),
                Relations.GLOBAL_NAME to "konstantin.anytype.io"
            )
        ),
        icon = SpaceMemberIconView.Placeholder(name = "Evgenii"),
        isUser = true,
        statusText = "Editor",
        contextActions = listOf(
            SpaceMemberView.ContextAction(
                title = "Viewer",
                actionType = SpaceMemberView.ActionType.CAN_VIEW
            ),
            SpaceMemberView.ContextAction(
                title = "Editor",
                isSelected = true,
                actionType = SpaceMemberView.ActionType.CAN_EDIT
            ),
            SpaceMemberView.ContextAction(
                title = "Remove member",
                isDestructive = true,
                actionType = SpaceMemberView.ActionType.REMOVE_MEMBER
            )
        )
    )
    SpaceMember(
        memberView = memberView,
        onContextActionClicked = { _, _ -> },
        onMemberClicked = {}
    )
}

@Composable
@DefaultPreviews
private fun SpaceMemberLongNamePreview() {
    val memberView = SpaceMemberView(
        obj = ObjectWrapper.SpaceMember(
            mapOf(
                Relations.ID to "2",
                Relations.NAME to "Walter Walter Walter Walter Walter Walter",
                Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.WRITER.code.toDouble(),
                Relations.GLOBAL_NAME to "konstantin.anytype.io"
            )
        ),
        icon = SpaceMemberIconView.Placeholder(name = "Walter"),
        isUser = true,
        statusText = "Editor"
    )
    SpaceMember(
        memberView = memberView,
        onContextActionClicked = { _, _ -> },
        onMemberClicked = {}
    )
}