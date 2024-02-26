package com.anytypeio.anytype.core_ui.features.multiplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Section
import com.anytypeio.anytype.core_ui.foundation.Toolbar
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.presentation.multiplayer.ParticipantView
import com.anytypeio.anytype.presentation.multiplayer.ShareSpaceViewModel

@Composable
@Preview
fun ShareSpaceScreenPreview() {
    ShareSpaceScreen(
        viewState = ShareSpaceViewModel.ViewState.Share(
            link = "https://anytype.io/ibafyrfhfsag6rea3ifffsasssg..."
        ),
        onShareInviteLinkClicked = {},
        onRegenerateInviteLinkClicked = {},
        participants = buildList {
            add(
                ParticipantView(
                    obj = ObjectWrapper.Basic(
                        mapOf(
                            Relations.ID to "1",
                            Relations.NAME to "Konstantin"
                        )
                    )
                )
            )
            add(
                ParticipantView(
                    obj = ObjectWrapper.Basic(
                        mapOf(
                            Relations.ID to "2",
                            Relations.NAME to "Evgenii"
                        )
                    )
                )
            )
        }
    )
}

@Composable
fun ShareSpaceScreen(
    participants: List<ParticipantView>,
    viewState: ShareSpaceViewModel.ViewState,
    onRegenerateInviteLinkClicked: () -> Unit,
    onShareInviteLinkClicked: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Dragger(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(vertical = 6.dp)
                    )
                }
            }
            item {
                Toolbar(title = stringResource(R.string.multiplayer_share_space))
            }
            item {
                Section(
                    title = stringResource(R.string.multiplayer_members_and_requests)
                )
            }
            participants.forEach { p ->
                item { 
                    Participant(participant = p)
                }
            }
        }
        when(viewState) {
            ShareSpaceViewModel.ViewState.Init -> {
                // Do nothing.
            }
            is ShareSpaceViewModel.ViewState.Share -> {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomStart)
                ) {
                    ShareInviteLinkCard(
                        link = viewState.link,
                        onShareInviteClicked = onShareInviteLinkClicked,
                        onRegenerateInviteLinkClicked = onRegenerateInviteLinkClicked
                    )
                }
            }
        }
    }
}

@Composable
@Preview
private fun ParticipantPreview() {
    Participant(
        participant = ParticipantView(
            obj = ObjectWrapper.Basic(
                mapOf(
                    Relations.ID to "2",
                    Relations.NAME to "Evgenii"
                )
            )
        )
    )
}

@Composable
private fun Participant(
    participant: ParticipantView
) {
    Box(
        modifier = Modifier
            .height(72.dp)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(color = Color.Red)
                .align(Alignment.CenterStart)
        )
        Column(
            modifier = Modifier
                .padding(start = 76.dp)
                .align(Alignment.CenterStart)
        ) {
            Text(
                text = participant.obj.name.orEmpty(),
                style = PreviewTitle2Medium,
                color = colorResource(id = R.color.text_primary)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Can edit",
                style = Relations3,
                color = colorResource(id = R.color.text_secondary)
            )
        }
    }
}