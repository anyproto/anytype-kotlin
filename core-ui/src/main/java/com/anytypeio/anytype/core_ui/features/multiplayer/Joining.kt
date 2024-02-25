package com.anytypeio.anytype.core_ui.features.multiplayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
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
fun JoinSpaceScreen(
    onRequestJoinSpaceClicked: (String) -> Unit,
    spaceName: String,
    createdByName: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        var commentInputValue by remember { mutableStateOf("") }
        Header()
        Text(
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            text = stringResource(
                id = R.string.multiplayer_space_request_to_join_msg, spaceName, createdByName
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .height(128.dp)
                .padding(horizontal = 20.dp)
                .border(width = 1.dp, color = colorResource(id = R.color.shape_primary))
                .clip(RoundedCornerShape(10.dp))
        ) {
            Text(
                text = stringResource(R.string.multiplayer_private_comment_for_a_space_owner),
                style = Caption1Medium,
                color = colorResource(id = R.color.text_secondary),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp)
            )
            TextField(
                value = commentInputValue,
                onValueChange = { commentInputValue = it },
                textStyle = PreviewTitle2Regular
            )
        }
        Spacer(modifier = Modifier.height(30.dp))
        ButtonPrimary(
            onClick = throttledClick(onClick = { onRequestJoinSpaceClicked(commentInputValue) }),
            size = ButtonSize.Large,
            text = stringResource(R.string.multiplayer_space_request_to_join)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.mulitplayer_request_to_join_explanation),
            style = Caption1Regular,
            color = colorResource(id = R.color.text_secondary),
            modifier = Modifier.padding(horizontal = 28.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun Header() {
    Box(
        modifier = Modifier
            .height(64.dp)
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = stringResource(R.string.multiplayer_join_a_space),
            style = HeadlineHeading,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.padding(top = 30.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.ic_navigation_close_cross),
            contentDescription = "Close button",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp)
        )
    }
}