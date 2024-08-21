package com.anytypeio.anytype.ui.vault

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyBold
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.vault.VaultViewModel.VaultSpaceView


@Composable
fun VaultScreen(
    spaces: List<VaultSpaceView>,
    onSpaceClicked: (VaultSpaceView) -> Unit,
    onCreateSpaceClicked: () -> Unit
) {
    Box(
        Modifier.fillMaxSize()
    ) {
       VaultScreenToolbar(
           onPlusClicked = onCreateSpaceClicked
       )
       LazyColumn(
           Modifier
               .fillMaxSize()
               .padding(
                   top = 48.dp
               )
       ) {
           items(
               items = spaces,
               key = { item ->
                   item.space.id
               }
           ) { item ->
               VaultSpaceCard(
                   title = item.space.name.orEmpty(),
                   subtitle = when(item.space.spaceAccessType) {
                       SpaceAccessType.PRIVATE -> stringResource(id = R.string.space_type_private_space)
                       SpaceAccessType.DEFAULT -> stringResource(id = R.string.space_type_default_space)
                       SpaceAccessType.SHARED -> stringResource(id = R.string.space_type_shared_space)
                       else -> EMPTY_STRING_VALUE
                   },
                   onCardClicked = {
                       onSpaceClicked(item)
                   }
               )
           }
       }
    }
}


@Composable
fun VaultScreenToolbar(
    onPlusClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(
            text = stringResource(R.string.vault_my_spaces),
            style = Title1,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.align(Alignment.Center)
        )
        Image(
            painter = painterResource(id = R.drawable.ic_space_settings),
            contentDescription = "Settings icon",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
        )
        Image(
            // TODO change icon
            painter = painterResource(id = R.drawable.ic_plus),
            contentDescription = "Plus button",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .noRippleClickable {
                    onPlusClicked()
                }
        )
    }
}

@Composable
fun VaultSpaceCard(
    title: String,
    subtitle: String,
    onCardClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .padding(horizontal = 8.dp)
            .clickable {
                onCardClicked()
            }
    ) {
        // TODO render space icon
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = Color.Red,
                    shape = RoundedCornerShape(8.dp)
                )
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 24.dp,
                    start = 96.dp,
                    end = 16.dp
                )
        ) {
            Text(
                text = title.ifEmpty { stringResource(id = R.string.untitled) },
                style = BodyBold,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = Relations3,
                color = colorResource(id = R.color.text_primary),
                modifier = Modifier.alpha(0.6f)
            )
        }
    }
}

@Composable
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
fun VaultScreenToolbarPreview() {
    VaultScreenToolbar(
        onPlusClicked = {}
    )
}

@Composable
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
fun VaultSpaceCardPreview() {
    VaultSpaceCard(
        title = "B&O Museum",
        subtitle = "Private space",
        onCardClicked = {}
    )
}

@Composable
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
fun VaultScreenPreview() {
    VaultScreen(
        spaces = buildList {
            add(
                VaultSpaceView(
                    space = ObjectWrapper.SpaceView(
                        mapOf(
                            Relations.NAME to "B&O Museum",
                            Relations.SPACE_ACCESS_TYPE to SpaceAccessType.SHARED.code.toDouble()
                        )
                    ),
                    icon = SpaceIconView.Placeholder
                )
            )
        },
        onSpaceClicked = {},
        onCreateSpaceClicked = {}
    )
}
