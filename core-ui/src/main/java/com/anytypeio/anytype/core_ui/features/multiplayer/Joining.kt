package com.anytypeio.anytype.core_ui.features.multiplayer

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.ColorTextInputCursor
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_ui.foundation.AlertConfig
import com.anytypeio.anytype.core_ui.foundation.AlertIcon
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.GRADIENT_TYPE_BLUE
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimaryLoading
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular

@Composable
@Preview
fun JoinSpaceScreenPreview() {
    JoinSpaceScreen(
        onRequestJoinSpaceClicked = {},
        spaceName = "Anytype Android App",
        createdByName = "Konstantin"
    )
}

@Composable
@Preview
fun JoinSpaceScreenPreviewWithEmptyNames() {
    JoinSpaceScreen(
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
    ) {
        Dragger(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 6.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        AlertIcon(
            icon = AlertConfig.Icon(
                gradient = GRADIENT_TYPE_BLUE,
                icon = R.drawable.ic_alert_message
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.multiplayer_join_a_space),
            style = HeadlineHeading,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.padding(horizontal = 48.dp),
            text = stringResource(
                id = R.string.multiplayer_space_request_to_join_msg,
                spaceName.ifEmpty { stringResource(id = R.string.untitled) },
                createdByName.ifEmpty { stringResource(id = R.string.untitled) }
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp)
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
private fun CommentBox(commentInputValue: String): String {
    var commentInputValue1 = commentInputValue
    Box(
        modifier = Modifier
            .height(128.dp)
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(width = 1.dp, color = colorResource(id = R.color.shape_primary))
            .clip(RoundedCornerShape(10.dp))
    ) {
        Text(
            text = stringResource(R.string.multiplayer_private_comment_for_a_space_owner),
            style = Caption1Medium,
            color = colorResource(id = R.color.text_secondary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp)
        )
        TextField(
            value = commentInputValue1,
            onValueChange = { commentInputValue1 = it },
            textStyle = PreviewTitle2Regular.copy(
                color = colorResource(id = R.color.text_primary)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 31.dp),
            placeholder = {
                Text(
                    text = stringResource(R.string.multiplayer_tap_to_write_request_to_join_comment),
                    color = colorResource(id = R.color.text_tertiary)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color.Transparent,
                errorBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = ColorTextInputCursor
            )
        )
    }
    return commentInputValue1
}