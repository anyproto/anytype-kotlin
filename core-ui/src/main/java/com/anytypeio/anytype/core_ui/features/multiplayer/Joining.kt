package com.anytypeio.anytype.core_ui.features.multiplayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.ButtonPrimaryLoading
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.views.Title2

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
fun JoinSpaceScreenPreview() {
    JoinSpaceScreen(
        onRequestJoinSpaceClicked = {},
        spaceName = "Anytype Android App",
        createdByName = "Konstantin"
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
fun JoinSpaceScreenPreviewWithEmptyNames() {
    JoinSpaceScreen(
        onRequestJoinSpaceClicked = {},
        spaceName = "",
        createdByName = ""
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
fun JoinSpaceScreenPreviewWithoutApprove() {
    JoinSpaceWithoutApproveScreen(
        onRequestJoinSpaceClicked = {},
        spaceName = "",
        createdByName = ""
    )
}

@Composable
fun JoinSpaceScreen(
    onRequestJoinSpaceClicked: () -> Unit,
    spaceName: String,
    createdByName: String,
    isLoading: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth()
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
        Spacer(modifier = Modifier.height(20.dp))
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
    spaceName: String,
    createdByName: String,
    isLoading: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth()
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
            text = stringResource(R.string.multiplayer_request_to_join_without_approve_title, spaceName),
            style = HeadlineHeading,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            style = Title2,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
                onClick = { onRequestJoinSpaceClicked() }
            ),
            text = stringResource(R.string.cancel),
            modifier = Modifier.fillMaxWidth(),
            size = ButtonSize.Large,
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}