package com.anytypeio.anytype.core_ui.features.multiplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.restrictions.SpaceStatus
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Header
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.presentation.spaces.SpaceListViewModel.SpaceListItemView
import timber.log.Timber

@Composable
fun SpaceListScreen(
    state: ViewState<List<SpaceListItemView>>
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Dragger(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.CenterHorizontally)
        )
        Header(text = "Spaces")
        if (state is ViewState.Success) {
            Timber.d("Got spaces: ${state.data}")
            state.data.forEach { 
                SpaceListCardItem(
                    spaceName = it.space.name.orEmpty(),
                    spaceStatus = SpaceStatus.SPACE_DELETED,
                    permissions = SpaceMemberPermissions.READER
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun SpaceListCardItem(
    spaceName: String,
    spaceStatus: SpaceStatus,
    permissions: SpaceMemberPermissions
) {
    ConstraintLayout(
        modifier = Modifier
            .padding(
                horizontal = 10.dp,
            )
            .border(
                width = 0.5.dp,
                color = colorResource(id = R.color.shape_primary),
                shape = RoundedCornerShape(12.dp)
            )
            .fillMaxWidth()

    ) {
        val (icon, title, subtitle, divider, dots, circle, network, status, footer) = createRefs()
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.Red)
                .constrainAs(icon) {
                    top.linkTo(parent.top, margin = 16.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                }
        )

        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color.Blue)
                .constrainAs(dots) {
                    top.linkTo(parent.top, margin = 24.dp)
                    end.linkTo(parent.end, margin = 12.dp)
                }
        )

        Text(
            text = spaceName.ifEmpty { stringResource(id = R.string.untitled) },
            style = Title2,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.constrainAs(title) {
                top.linkTo(parent.top, margin = 21.dp)
                start.linkTo(icon.end, margin = 12.dp)
                end.linkTo(dots.start, margin = 12.dp)
                width = Dimension.fillToConstraints
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = when(permissions) {
                SpaceMemberPermissions.OWNER -> stringResource(id = R.string.multiplayer_owner)
                SpaceMemberPermissions.READER -> stringResource(id = R.string.multiplayer_can_view)
                SpaceMemberPermissions.WRITER -> stringResource(id = R.string.multiplayer_can_edit)
                SpaceMemberPermissions.NO_PERMISSIONS -> EMPTY_STRING_VALUE
            },
            style = Relations2,
            color = colorResource(id = R.color.text_secondary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.constrainAs(subtitle) {
                top.linkTo(title.bottom)
                start.linkTo(icon.end, margin = 12.dp)
                end.linkTo(dots.start, margin = 12.dp)
                width = Dimension.fillToConstraints
            }
        )

        Divider(
            modifier = Modifier.constrainAs(divider) {
                top.linkTo(icon.bottom, margin = 16.dp)
                start.linkTo(parent.start, margin = 16.dp)
                end.linkTo(parent.end, margin = 12.dp)
            }
        )

        Box(
            modifier = Modifier
                .size(8.dp)
                .background(Color.Green, CircleShape)
                .constrainAs(circle) {
                    start.linkTo(parent.start, margin = 16.dp)
                    top.linkTo(network.top)
                    bottom.linkTo(network.bottom)
                }
        )

        Text(
            text = stringResource(id = R.string.network_with_colon),
            color = colorResource(id = R.color.text_secondary),
            style = Relations3,
            modifier = Modifier.constrainAs(network) {
                top.linkTo(divider.bottom, margin = 12.dp)
                start.linkTo(circle.end, margin = 6.dp)
            }
        )

        Text(
            text = spaceStatus.toString().lowercase(),
            style = Relations3,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.constrainAs(status) {
                top.linkTo(divider.bottom, margin = 12.dp)
                start.linkTo(network.end, margin = 4.dp)
            }
        )

        Spacer(
            modifier = Modifier
                .height(16.dp)
                .constrainAs(footer) {
                    top.linkTo(network.bottom)
                }
        )
    }
}

@Preview
@Composable
private fun SpaceListScreenPreview() {
    SpaceListScreen(ViewState.Loading)
}

@Preview
@Composable
private fun SpaceCardItemPreview() {
    SpaceListCardItem(
        spaceName = "Architecture",
        spaceStatus = SpaceStatus.SPACE_ACTIVE,
        permissions = SpaceMemberPermissions.OWNER
    )
}



