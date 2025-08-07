package com.anytypeio.anytype.core_ui.features.multiplayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.ButtonPrimaryLoading
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_ui.views.Title3

@Composable
fun JoinSpaceScreen(
    onRequestJoinSpaceClicked: () -> Unit,
    onCancelClicked: () -> Unit,
    spaceName: String,
    createdByName: String,
    isLoading: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Dragger(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 6.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            painter = painterResource(R.drawable.ic_join_without_approve),
            contentDescription = "Join without approve"
        )
        Spacer(modifier = Modifier.height(15.dp))
        Text(
            text = stringResource(R.string.multiplayer_join_a_space),
            style = HeadlineHeading,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            style = Title2,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.padding(horizontal = 48.dp),
            text = stringResource(
                id = R.string.multiplayer_space_request_to_join_msg,
                spaceName.ifEmpty { stringResource(id = R.string.untitled) },
                createdByName.ifEmpty { stringResource(id = R.string.untitled) }
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(19.dp))
        Box(
            modifier = Modifier
        ) {
            ButtonPrimaryLoading(
                onClick = throttledClick(
                    onClick = { onRequestJoinSpaceClicked() }
                ),
                size = ButtonSize.Large,
                text = stringResource(R.string.multiplayer_space_request_to_join),
                modifierButton = Modifier.fillMaxWidth(),
                loading = isLoading
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        ButtonSecondary(
            onClick = throttledClick(
                onClick = { onCancelClicked() }
            ),
            text = stringResource(R.string.cancel),
            modifier = Modifier.fillMaxWidth(),
            size = ButtonSize.Large,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.multiplayer_request_to_join_explanation),
            style = Caption1Regular,
            color = colorResource(id = R.color.text_secondary),
            modifier = Modifier.padding(horizontal = 28.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun JoinSpaceWithoutApproveScreen(
    onRequestJoinSpaceClicked: () -> Unit,
    onCancelClicked: () -> Unit,
    spaceName: String,
    createdByName: String,
    isLoading: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Dragger(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 6.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            painter = painterResource(R.drawable.ic_join_without_approve),
            contentDescription = "Join without approve"
        )
        Spacer(modifier = Modifier.height(15.dp))
        Text(
            text = stringResource(
                R.string.multiplayer_request_to_join_without_approve_title,
                spaceName
            ),
            style = HeadlineHeading,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            style = Title3,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = stringResource(
                id = R.string.multiplayer_request_to_join_without_approve_desc,
                spaceName.ifEmpty { stringResource(id = R.string.untitled) },
                createdByName.ifEmpty { stringResource(id = R.string.untitled) }
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(19.dp))
        ButtonPrimaryLoading(
            onClick = throttledClick(
                onClick = { onRequestJoinSpaceClicked() }
            ),
            size = ButtonSize.Large,
            text = stringResource(R.string.multiplayer_request_to_join_without_approve_button),
            modifierButton = Modifier.fillMaxWidth(),
            loading = isLoading
        )
        Spacer(modifier = Modifier.height(8.dp))
        ButtonSecondary(
            onClick = throttledClick(
                onClick = { onCancelClicked() }
            ),
            text = stringResource(R.string.cancel),
            modifier = Modifier.fillMaxWidth(),
            size = ButtonSize.Large,
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun JoinSpaceRequestSentScreen(
    onDoneClicked: () -> Unit,
    onManageSpaces: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Dragger(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 6.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.multiplayer_request_to_join_sent_title,),
            style = HeadlineHeading,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.multiplayer_request_to_join_sent_description,),
            style = Title2,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(19.dp))
        ButtonPrimaryLoading(
            onClick = throttledClick(
                onClick = {  }
            ),
            size = ButtonSize.Large,
            text = stringResource(R.string.done),
            modifierButton = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        ButtonSecondary(
            onClick = throttledClick(
                onClick = {  }
            ),
            text = stringResource(R.string.multiplayer_request_to_join_btn_manage_spaces),
            modifier = Modifier.fillMaxWidth(),
            size = ButtonSize.Large,
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun JoiningLoadingState(
    onCancelLoadingInviteClicked: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(27.dp))
        CircularProgressIndicator(
            modifier = Modifier
                .size(56.dp),
            color = colorResource(R.color.shape_secondary),
            trackColor = colorResource(R.color.shape_primary)
        )
        Spacer(modifier = Modifier.height(15.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            color = colorResource(id = R.color.text_secondary),
            textAlign = TextAlign.Center,
            text = stringResource(R.string.multiplayer_request_to_join_loading_text),
            style = Title2
        )
        Spacer(modifier = Modifier.height(19.dp))
        ButtonSecondary(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 16.dp, end = 16.dp),
            onClick = {
                onCancelLoadingInviteClicked()
            },
            size = ButtonSize.Large,
            text = stringResource(R.string.cancel),
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
@DefaultPreviews
fun JoinSpaceScreenPreviewLoading() {
    JoiningLoadingState(
        onCancelLoadingInviteClicked = {}
    )
}

@Composable
@DefaultPreviews
fun JoinSpaceScreenPreview() {
    JoinSpaceScreen(
        onRequestJoinSpaceClicked = {},
        spaceName = "Anytype Android App",
        createdByName = "Konstantin",
        onCancelClicked = {}
    )
}

@Composable
@DefaultPreviews
fun JoinSpaceScreenPreviewWithEmptyNames() {
    JoinSpaceScreen(
        onRequestJoinSpaceClicked = {},
        spaceName = "",
        createdByName = "",
        onCancelClicked = {}
    )
}

@Composable
@DefaultPreviews
fun JoinSpaceScreenPreviewWithoutApprove() {
    JoinSpaceWithoutApproveScreen(
        onRequestJoinSpaceClicked = {},
        spaceName = "",
        createdByName = "",
        onCancelClicked = {}
    )
}