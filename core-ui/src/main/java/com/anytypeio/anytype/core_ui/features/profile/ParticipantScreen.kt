package com.anytypeio.anytype.core_ui.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder

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
            .padding(start = 12.dp, end = 12.dp, bottom = 32.dp),
        dragHandle = {
            Column {
                Spacer(modifier = Modifier.height(6.dp))
                Dragger()
                Spacer(modifier = Modifier.height(6.dp))
            }
        },
        scrimColor = colorResource(id = R.color.modal_screen_outside_background),
        containerColor = colorResource(id = R.color.background_secondary),
        shape = RoundedCornerShape(20.dp),
        sheetState = bottomSheetState,
        onDismissRequest = {
            onEvent(ParticipantEvent.OnDismiss)
        },
        content = {
            val (spacer, iconSize) = if (uiState.description.isNullOrBlank()) {
                68.dp to 184.dp
            } else {
                62.dp to 112.dp
            }
            Spacer(
                modifier = Modifier.height(spacer)
            )
            ImageBlock(
                modifier = Modifier
                    .size(iconSize)
                    .align(Alignment.CenterHorizontally)
                    .noRippleThrottledClickable {
                        onEvent(ParticipantEvent.OnCardClicked)
                    },
                name = uiState.name,
                icon = uiState.icon,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Title(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .noRippleThrottledClickable {
                        onEvent(ParticipantEvent.OnCardClicked)
                    },
                name = uiState.name
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (uiState.identity != null) {
                AnyIdentity(
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .align(Alignment.CenterHorizontally)
                        .noRippleThrottledClickable {
                            onEvent(ParticipantEvent.OnCardClicked)
                        },
                    identity = uiState.identity!!
                )
            }
            if (!uiState.description.isNullOrBlank()) {
                Description(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .padding(horizontal = 32.dp)
                        .align(Alignment.CenterHorizontally)
                        .noRippleThrottledClickable {
                            onEvent(ParticipantEvent.OnCardClicked)
                        },
                    description = uiState.description!!
                )
            }
            val h = spacer - 8.dp
            Spacer(
                modifier = Modifier.height(h)
            )
        },
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun ImageBlock(
    modifier: Modifier,
    name: String,
    icon: ProfileIconView,
    fontSize: TextUnit = 24.sp
) {
    when (icon) {
        is ProfileIconView.Image -> {
            GlideImage(
                model = icon.url,
                contentDescription = "Custom image profile",
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .clip(shape = CircleShape),
                loading = placeholder(R.drawable.ic_loading_state_112)
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
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun Title(modifier: Modifier, name: String) {
    Text(
        modifier = modifier,
        text = name,
        style = HeadlineHeading,
        color = colorResource(id = R.color.text_primary),
        maxLines = 3
    )
}

@Composable
private fun AnyIdentity(modifier: Modifier, identity: String) {
    Text(
        modifier = modifier,
        text = identity,
        style = Caption1Regular,
        color = colorResource(id = R.color.text_secondary),
        maxLines = 2,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun Description(modifier: Modifier, description: String) {
    Text(
        modifier = modifier,
        text = description,
        style = PreviewTitle2Regular,
        color = colorResource(id = R.color.text_primary),
        textAlign = TextAlign.Center
    )
}

@DefaultPreviews
@Composable
fun ParticipantScreenPreview() {
    ParticipantScreen(
        uiState = UiParticipantScreenState(
            name = "Ivanov Konstantin",
            icon = ProfileIconView.Image("dsdas"),
            description = "some desc",
            isOwner = true
        ),
        onEvent = {}
    )
}