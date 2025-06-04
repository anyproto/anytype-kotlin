package com.anytypeio.anytype.ui.vault

import android.content.res.Configuration
import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.foundation.util.DraggableItem
import com.anytypeio.anytype.core_ui.foundation.util.dragContainer
import com.anytypeio.anytype.core_ui.foundation.util.rememberDragDropState
import com.anytypeio.anytype.core_ui.views.AvatarTitle
import com.anytypeio.anytype.core_ui.views.HeadlineTitle
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.animations.conditionalBackground
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.presentation.profile.AccountProfile
import com.anytypeio.anytype.presentation.profile.ProfileIconView
import com.anytypeio.anytype.presentation.spaces.SelectSpaceViewModel
import com.anytypeio.anytype.presentation.vault.VaultViewModel.VaultSpaceView
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi


@Composable
fun VaultScreen(
    profile: AccountProfile,
    spaces: List<VaultSpaceView>,
    onSpaceClicked: (VaultSpaceView.Space) -> Unit,
    onCreateSpaceClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onOrderChanged: (List<Id>) -> Unit
) {
    var spaceList by remember {
        mutableStateOf<List<VaultSpaceView>>(spaces)
    }

    spaceList = spaces

    val lazyListState = rememberLazyListState()
    val isScrolled = remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0
        }
    }

    val dragDropState = rememberDragDropState(
        lazyListState = lazyListState,
        onDragEnd = {
            onOrderChanged(
                spaceList.map { it.space.id }
            )
        },
        onMove = { fromIndex, toIndex ->
            spaceList = spaceList.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
        }
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = colorResource(id = R.color.background_primary)
            )
            .then(
                if (SDK_INT >= EDGE_TO_EDGE_MIN_SDK)
                    Modifier.windowInsetsPadding(WindowInsets.systemBars)
                else
                    Modifier
            ),
        topBar = {
            VaultScreenToolbar(
                profile = profile,
                onPlusClicked = onCreateSpaceClicked,
                onSettingsClicked = onSettingsClicked,
                spaceCountLimitReached = spaces.size >= SelectSpaceViewModel.MAX_SPACE_COUNT,
                isScrolled = isScrolled.value
            )
        }
    ) { paddings ->
        if (spaces.isEmpty()) {
            VaultEmptyState(
                modifier = Modifier.padding(paddings),
                onCreateSpaceClicked = onCreateSpaceClicked
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddings)
                    .dragContainer(dragDropState),
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = spaceList,
                    key = { _, item ->
                        item.space.id
                    },
                    contentType = { _, item ->
                        when (item) {
                            is VaultSpaceView.Chat -> TYPE_CHAT
                            is VaultSpaceView.Space -> TYPE_SPACE
                            is VaultSpaceView.Loading -> TYPE_LOADING
                        }
                    }
                ) { idx, item ->
                    if (idx == 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    when (item) {
                        is VaultSpaceView.Chat -> {
                            DraggableItem(dragDropState = dragDropState, index = idx) {
                                VaultChatCard(
                                    title = item.space.name.orEmpty(),
                                    onCardClicked = {
                                        //onSpaceClicked(item)
                                    },
                                    icon = item.icon,
                                    previewText = item.previewText,
                                    chatPreview = item.chatPreview
                                )
                            }
                        }
                        is VaultSpaceView.Loading -> {
                            DraggableItem(dragDropState = dragDropState, index = idx) {
                                LoadingSpaceCard()
                            }
                        }
                        is VaultSpaceView.Space -> {
                            DraggableItem(dragDropState = dragDropState, index = idx) {
                                VaultSpaceCard(
                                    title = item.space.name.orEmpty(),
                                    subtitle = item.accessType,
                                    onCardClicked = {
                                        //onSpaceClicked(item)
                                    },
                                    icon = item.icon,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun VaultScreenToolbar(
    profile: AccountProfile,
    spaceCountLimitReached: Boolean = false,
    onPlusClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    isScrolled: Boolean = false
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .conditionalBackground(
                    condition = isScrolled,
                ) {
                    background(
                        color = colorResource(R.color.navigation_panel),
                    )
                }
                .height(44.dp)
        ) {
            if (isScrolled) {
                Text(
                    text = stringResource(R.string.vault_my_spaces),
                    style = Title1,
                    color = colorResource(id = R.color.text_primary),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            when (profile) {
                is AccountProfile.Data -> {
                    Box(
                        Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 16.dp)
                            .size(28.dp)
                            .noRippleClickable {
                                onSettingsClicked()
                            }
                    ) {
                        when (val icon = profile.icon) {
                            is ProfileIconView.Image -> {
                                Image(
                                    painter = rememberAsyncImagePainter(icon.url),
                                    contentDescription = "Custom image profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }

                            else -> {
                                val nameFirstChar = if (profile.name.isEmpty()) {
                                    stringResource(id = com.anytypeio.anytype.ui_settings.R.string.account_default_name)
                                } else {
                                    profile.name.first().uppercaseChar().toString()
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(colorResource(id = com.anytypeio.anytype.ui_settings.R.color.text_tertiary))
                                ) {
                                    Text(
                                        text = nameFirstChar,
                                        style = AvatarTitle.copy(
                                            fontSize = 20.sp
                                        ),
                                        color = colorResource(id = R.color.text_white),
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }

                AccountProfile.Idle -> {
                    // Draw nothing
                }
            }

            if (!spaceCountLimitReached) {
                Image(
                    painter = painterResource(id = R.drawable.ic_plus_18),
                    contentDescription = "Plus button",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                        .size(32.dp)
                        .noRippleClickable {
                            onPlusClicked()
                        }
                )
            }
        }

        if (!isScrolled) {
            Text(
                modifier = Modifier.padding(top = 3.dp, bottom = 8.dp, start = 16.dp),
                text = stringResource(R.string.vault_my_spaces),
                style = HeadlineTitle.copy(
                    fontSize = 34.sp
                ),
                color = colorResource(id = R.color.text_primary),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun VaultSpaceAddCard(
    onCreateSpaceClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                color = colorResource(id = R.color.shape_tertiary),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable {
                onCreateSpaceClicked()
            }
    ) {
        Image(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(vertical = 32.dp),
            painter = painterResource(id = R.drawable.ic_vault_create_space_card_button_plus),
            contentDescription = "Plus icon"
        )
    }
}

@Composable
fun LoadingSpaceCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .padding(horizontal = 8.dp)
            .background(
                color = colorResource(R.color.shape_tertiary),
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
    ) {
        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .size(64.dp)
                .align(Alignment.CenterStart)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            colorResource(R.color.shape_primary),
                            Color.Transparent,
                        )
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
        )

        Box(
            modifier = Modifier
                .padding(start = 96.dp, top = 30.dp)
                .height(12.dp)
                .width(160.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            colorResource(R.color.shape_primary),
                            Color.Transparent,
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .padding(start = 96.dp, bottom = 30.dp)
                .height(8.dp)
                .width(96.dp)
                .align(Alignment.BottomStart)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            colorResource(R.color.shape_primary),
                            Color.Transparent,
                        )
                    )
                )
        )
    }
}

@DefaultPreviews
@Composable
fun LoadingSpaceCardPreview() {
    LoadingSpaceCard()
}

@Composable
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode - Not Scrolled"
)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Light Mode - Not Scrolled"
)
fun VaultScreenToolbarNotScrolledPreview() {
    VaultScreenToolbar(
        onPlusClicked = {},
        onSettingsClicked = {},
        profile = AccountProfile.Data(
            name = "John Doe",
            icon = ProfileIconView.Placeholder(name = "Jd")
        ),
        isScrolled = false
    )
}

@Composable
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode - Scrolled"
)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Light Mode - Scrolled"
)
fun VaultScreenToolbarScrolledPreview() {
    VaultScreenToolbar(
        onPlusClicked = {},
        onSettingsClicked = {},
        profile = AccountProfile.Data(
            name = "John Doe",
            icon = ProfileIconView.Placeholder(name = "Jd")
        ),
        isScrolled = true
    )
}

//@Composable
//@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
//@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
//fun VaultScreenPreview() {
//    VaultScreen(
//        spaces = buildList {
//            add(
//                VaultSpaceView(
//                    space = ObjectWrapper.SpaceView(
//                        mapOf(
//                            Relations.NAME to "B&O Museum",
//                            Relations.SPACE_ACCESS_TYPE to SpaceAccessType.SHARED.code.toDouble()
//                        )
//                    ),
//                    icon = SpaceIconView.Placeholder()
//                )
//            )
//        },
//        onSpaceClicked = {},
//        onCreateSpaceClicked = {},
//        onSettingsClicked = {},
//        onOrderChanged = {},
//        profile = AccountProfile.Idle
//    )
//}

const val TYPE_CHAT = "chat"
const val TYPE_SPACE = "space"
const val TYPE_LOADING = "loading"