package com.anytypeio.anytype.core_ui.features.multiplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.ButtonWarning
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.presentation.multiplayer.SpaceJoinRequestViewModel.ViewState

@Preview
@Composable
fun SpaceJoinRequestScreenPreview() {
    SpaceJoinRequestScreen(
        onAddEditorClicked = {},
        onAddViewerClicked = {},
        onRejectClicked = {},
        state = ViewState.Success(
            memberName = "Merk",
            spaceName = "Investors"
        )
    )
}

@Composable
fun SpaceJoinRequestScreen(
    state: ViewState.Success,
    onAddViewerClicked: () -> Unit,
    onAddEditorClicked: () -> Unit,
    onRejectClicked: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Dragger(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 6.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.Blue)
                    .align(Alignment.Center)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(
                R.string.multiplayer_space_join_request_header,
                state.memberName,
                state.spaceName
            ),
            style = HeadlineHeading,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(
                horizontal = 48.dp
            ),
            color = colorResource(id = R.color.text_primary)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(start = 20.dp, end = 20.dp)
                .background(
                    color = colorResource(id = R.color.shape_tertiary),
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            Text(
                text = "",
                style = BodyCalloutRegular,
                color = colorResource(id = R.color.text_primary),
                modifier = Modifier.padding(16.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(
                        color = colorResource(id = R.color.glyph_active),
                        shape = RoundedCornerShape(
                            topStart = 4.dp,
                            bottomStart = 4.dp
                        )
                    )
            )
        }
        Spacer(modifier = Modifier.height(30.dp))
        ButtonSecondary(
            text = stringResource(R.string.multiplayer_space_add_viewer),
            onClick = throttledClick(
                onClick = { onAddViewerClicked() }
            ),
            size = ButtonSize.Large,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        ButtonSecondary(
            text = stringResource(R.string.multiplayer_space_add_editor),
            onClick = throttledClick(
                onClick = { onAddEditorClicked() }
            ),
            size = ButtonSize.Large,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        ButtonWarning(
            text = stringResource(R.string.multiplayer_space_request_reject),
            onClick = throttledClick(
                onClick = { onRejectClicked() }
            ),
            size = ButtonSize.Large,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}
