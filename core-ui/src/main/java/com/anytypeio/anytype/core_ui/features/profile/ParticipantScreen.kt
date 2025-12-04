package com.anytypeio.anytype.core_ui.features.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.AvatarTitle
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.presentation.profile.ParticipantEvent
import com.anytypeio.anytype.presentation.profile.ParticipantViewModel.UiParticipantScreenState
import com.anytypeio.anytype.presentation.profile.ProfileIconView
import coil3.compose.AsyncImage
import com.anytypeio.anytype.core_ui.views.ButtonSecondaryLoading
import com.anytypeio.anytype.core_ui.views.ButtonSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantScreen(
    uiState: UiParticipantScreenState,
    onEvent: (ParticipantEvent) -> Unit
) {

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp),
        dragHandle = null,
        scrimColor = colorResource(id = R.color.modal_screen_outside_background),
        containerColor = colorResource(id = R.color.transparent_black),
        shape = RoundedCornerShape(20.dp),
        sheetState = bottomSheetState,
        onDismissRequest = {
            onEvent(ParticipantEvent.OnDismiss)
        },
        content = {
            Column(
                Modifier
                    .navigationBarsPadding()
                    .background(
                        shape = RoundedCornerShape(20.dp),
                        color = colorResource(id = R.color.background_secondary)
                    )
            ) {
                val (spacer, iconSize, textSize) = if (uiState.description.isNullOrBlank()) {
                    Triple(68.dp, 184.dp, 88.sp)
                } else {
                    Triple(62.dp, 112.dp, 64.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Dragger(modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(6.dp))
                if (uiState.isOwner) {
                    EditIcon(
                        modifier = Modifier
                            .padding(end = 16.dp, bottom = 16.dp, start = 16.dp)
                            .size(32.dp)
                            .align(Alignment.End)
                            .noRippleThrottledClickable {
                                onEvent(ParticipantEvent.OnCardClicked)
                            }
                    )
                    Spacer(
                        modifier = Modifier.height(spacer - 32.dp - 16.dp)
                    )
                } else {
                    Spacer(
                        modifier = Modifier.height(spacer)
                    )
                }
                ImageBlock(
                    modifier = Modifier
                        .size(iconSize)
                        .align(Alignment.CenterHorizontally),
                    name = uiState.name,
                    icon = uiState.icon,
                    fontSize = textSize
                )
                Spacer(modifier = Modifier.height(12.dp))
                Title(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .align(Alignment.CenterHorizontally),
                    name = uiState.name
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (uiState.identity != null) {
                    AnyIdentity(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                            .align(Alignment.CenterHorizontally),
                        identity = uiState.identity!!
                    )
                }
                if (!uiState.description.isNullOrBlank()) {
                    Description(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .padding(horizontal = 32.dp)
                            .align(Alignment.CenterHorizontally),
                        description = uiState.description!!
                    )
                }
                if (!uiState.isOwner) {
                    Spacer(modifier = Modifier.height(24.dp))
                    ButtonSecondaryLoading(
                        text = stringResource(R.string.participant_btn_connect),
                        onClick = { onEvent(ParticipantEvent.OnConnectClicked) },
                        enabled = !uiState.isConnecting,
                        size = ButtonSize.Large,
                        loading = uiState.isConnecting,
                        modifierButton = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    )
                }
                val h = spacer + 16.dp
                Spacer(
                    modifier = Modifier.height(h)
                )
            }
        },
    )
}

@Composable
private fun EditIcon(modifier: Modifier) {
    Image(
        modifier = modifier,
        painter = painterResource(R.drawable.ic_participant_edit),
        contentDescription = "Edit participant"
    )
}

@Composable
private fun ImageBlock(
    modifier: Modifier,
    name: String,
    icon: ProfileIconView,
    fontSize: TextUnit = 24.sp
) {
    when (icon) {
        is ProfileIconView.Image -> {
            AsyncImage(
                model = icon.url,
                contentDescription = "Custom image profile",
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .clip(shape = CircleShape),
                placeholder = painterResource(R.drawable.ic_loading_state_112)
            )
        }

        else -> {
            val nameFirstChar = if (name.isEmpty()) {
                stringResource(id = R.string.account_default_name)
            } else {
                name.first().uppercaseChar().toString()
            }
            Box(
                modifier = modifier
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.shape_tertiary))
            ) {
                Text(
                    text = nameFirstChar,
                    style = AvatarTitle.copy(
                        color = colorResource(id = R.color.glyph_active),
                        fontSize = fontSize
                    ),
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun Title(modifier: Modifier, name: String) {
    SelectionContainer(modifier = modifier) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = name,
            style = HeadlineHeading,
            color = colorResource(id = R.color.text_primary),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AnyIdentity(modifier: Modifier, identity: String) {
    SelectionContainer(
        modifier = modifier
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = identity,
            style = Caption1Regular,
            color = colorResource(id = R.color.text_secondary),
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun Description(modifier: Modifier, description: String) {
    SelectionContainer(
        modifier = modifier
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = description,
            style = PreviewTitle2Regular,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@DefaultPreviews
@Composable
fun ParticipantScreenPreview() {
    ParticipantScreen(
        uiState = UiParticipantScreenState(
            name = "Jetpack Compose",
            icon = ProfileIconView.Placeholder("M"),
            identity = "AnyId43",
            description = "some description",
            isOwner = true
        ),
        onEvent = {}
    )
}