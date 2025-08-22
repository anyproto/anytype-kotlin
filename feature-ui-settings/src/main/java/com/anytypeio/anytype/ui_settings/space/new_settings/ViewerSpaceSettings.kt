package com.anytypeio.anytype.ui_settings.space.new_settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.spaces.SpaceTechInfo
import com.anytypeio.anytype.presentation.spaces.UiEvent
import com.anytypeio.anytype.ui_settings.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerSpaceSettings(
    title: String,
    description: String,
    icon: SpaceIconView,
    info: SpaceTechInfo,
    inviteLink: String? = null,
    uiEvent: (UiEvent) -> Unit,
) {

    val isTheeDotsMenuExpanded = remember { mutableStateOf(false) }
    var showTechInfo by remember { mutableStateOf(false) }

    Column(
        Modifier
            .padding(horizontal = 8.dp)
            .padding(bottom = 16.dp)
            .fillMaxWidth()
            .background(
                color = colorResource(id = R.color.background_secondary),
                shape = RoundedCornerShape(20.dp),
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Dragger(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 6.dp)
        )
        Box(
            modifier = Modifier
                .height(48.dp)
                .width(56.dp)
                .align(Alignment.End)
                .clickable {
                    isTheeDotsMenuExpanded.value = true
                }
        ) {
            Image(
                modifier = Modifier.align(Alignment.Center),
                painter = painterResource(R.drawable.ic_more_32),
                contentDescription = "Three dots button"
            )
            ThreeDotsMenu(
                isTheeDotsMenuExpanded = isTheeDotsMenuExpanded,
                onShowTechInfoClicked = {
                    showTechInfo = true
                },
                onLeaveSpaceClicked = {
                    uiEvent(UiEvent.OnLeaveSpaceClicked)
                }
            )
        }
        NewSpaceIcon(
            modifier = Modifier.fillMaxWidth(),
            icon = icon,
            isEditEnabled = false,
            uiEvent = uiEvent
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            text = title.ifEmpty { stringResource(R.string.untitled) },
            style = HeadlineHeading,
            color = colorResource(R.color.text_primary),
            textAlign = TextAlign.Center
        )
        if (!inviteLink.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            MultiplayerButtons(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                link = inviteLink,
                uiEvent = uiEvent
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showTechInfo) {
        ModalBottomSheet(
            containerColor = colorResource(R.color.background_secondary),
            onDismissRequest = { showTechInfo = false },
            dragHandle = null,
            content = { SpaceInfoScreen(spaceTechInfo = info) }
        )
    }
}

@Composable
private fun ThreeDotsMenu(
    isTheeDotsMenuExpanded: MutableState<Boolean>,
    onLeaveSpaceClicked: () -> Unit,
    onShowTechInfoClicked: () -> Unit
) {
    MaterialTheme(
        shapes = MaterialTheme.shapes.copy(medium = RoundedCornerShape(10.dp))
    ) {
        DropdownMenu(
            modifier = Modifier
                .background(
                    shape = RoundedCornerShape(10.dp),
                    color = colorResource(id = R.color.background_secondary)
                )
            ,
            expanded = isTheeDotsMenuExpanded.value,
            offset = DpOffset(x = (-12).dp, y = (-6).dp),
            onDismissRequest = {
                isTheeDotsMenuExpanded.value = false
            }
        ) {
            DropdownMenuItem(
                onClick = {
                    onShowTechInfoClicked()
                    isTheeDotsMenuExpanded.value = false
                },
            ) {
                Text(
                    text = stringResource(R.string.tech_info),
                    style = BodyCalloutRegular,
                    color = colorResource(id = R.color.text_primary),
                    modifier = Modifier.padding(end = 48.dp)
                )
            }
            Divider(paddingEnd = 0.dp, paddingStart = 0.dp)
            DropdownMenuItem(
                onClick = {
                    onLeaveSpaceClicked()
                    isTheeDotsMenuExpanded.value = false
                },
            ) {
                Text(
                    text = stringResource(R.string.multiplayer_leave_space),
                    style = BodyCalloutRegular,
                    color = colorResource(id = R.color.palette_system_red),
                    modifier = Modifier.padding(end = 48.dp)
                )
            }
        }
    }
}

@DefaultPreviews
@Composable
fun ViewerSpaceSettingsWithDescriptionPreview() {
    ViewerSpaceSettings(
        title = "Susan Sontag",
        description = stringResource(R.string.default_text_placeholder),
        uiEvent = {},
        icon = SpaceIconView.DataSpace.Placeholder(
            name = "Susan",
            color = SystemColor.SKY
        ),
        info = SpaceTechInfo(
            spaceId = SpaceId("space-id"),
            createdBy = "Thomas",
            creationDateInMillis = null,
            networkId = "random network id",
            isDebugVisible = false,
            deviceToken = null
        )
    )
}

@DefaultPreviews
@Composable
fun ViewerSpaceSettingsWithoutDescriptionPreview() {
    ViewerSpaceSettings(
        title = "Susan Sontag",
        description = "",
        uiEvent = {},
        icon = SpaceIconView.DataSpace.Placeholder(
            name = "Susan",
            color = SystemColor.SKY
        ),
        info = SpaceTechInfo(
            spaceId = SpaceId("space-id"),
            createdBy = "Thomas",
            creationDateInMillis = null,
            networkId = "random network id",
            isDebugVisible = false,
            deviceToken = null
        )
    )
}