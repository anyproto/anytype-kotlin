package com.anytypeio.anytype.feature_vault.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ui.SpaceMemberIconView
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.features.multiplayer.SpaceMemberIcon
import com.anytypeio.anytype.core_ui.foundation.DefaultSearchBar
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.feature_vault.presentation.MemberItem
import com.anytypeio.anytype.feature_vault.presentation.SelectMembersUiState
import com.anytypeio.anytype.core_ui.R as CoreR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectMembersContent(
    modifier: Modifier = Modifier,
    uiState: SelectMembersUiState,
    onMemberToggled: (Id) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onNext: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(
                    color = colorResource(id = CoreR.color.background_secondary),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Dragger(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 6.dp)
            )

            val subtitle = (uiState as? SelectMembersUiState.Content)?.subtitle.orEmpty()
            SelectMembersTopBar(
                subtitle = subtitle,
                onBackClick = onDismissRequest,
                onNextClick = onNext
            )

            when (uiState) {
                SelectMembersUiState.Loading -> {
                    Box(modifier = Modifier.weight(1f))
                }
                is SelectMembersUiState.Content -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    DefaultSearchBar(
                        value = uiState.searchQuery,
                        onQueryChanged = onSearchQueryChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(
                            items = uiState.members,
                            key = { it.identity }
                        ) { member ->
                            MemberRow(
                                member = member,
                                onToggle = { onMemberToggled(member.identity) }
                            )
                            Divider(
                                paddingStart = 16.dp,
                                paddingEnd = 16.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectMembersTopBar(
    subtitle: String = "",
    onBackClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Image(
            painter = painterResource(id = CoreR.drawable.ic_back_24),
            contentDescription = "Back",
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.CenterStart)
                .noRippleThrottledClickable { onBackClick() },
            contentScale = ContentScale.Inside,
            colorFilter = ColorFilter.tint(colorResource(id = CoreR.color.text_primary))
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(com.anytypeio.anytype.localization.R.string.channel_select_members_title),
                style = Title1,
                color = colorResource(id = CoreR.color.text_primary)
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = Caption1Medium,
                    color = colorResource(id = CoreR.color.text_secondary)
                )
            }
        }

        Text(
            text = stringResource(com.anytypeio.anytype.localization.R.string.channel_next),
            style = Title2,
            color = colorResource(id = CoreR.color.text_primary),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .noRippleThrottledClickable { onNextClick() }
        )
    }
}

@Composable
private fun SelectionCircle(
    selectionOrder: Int?
) {
    if (selectionOrder != null) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(colorResource(id = CoreR.color.palette_system_blue)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = selectionOrder.toString(),
                style = Caption1Medium,
                color = colorResource(id = CoreR.color.text_white),
                textAlign = TextAlign.Center
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(24.dp)
                .border(
                    width = 1.dp,
                    color = colorResource(id = CoreR.color.shape_primary),
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun MemberRow(
    member: MemberItem,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .noRippleClickable(onClick = onToggle)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpaceMemberIcon(
            icon = member.icon,
            iconSize = 48.dp,
            modifier = Modifier
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                text = member.name,
                style = Title2,
                color = colorResource(id = CoreR.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val subtitle = member.globalName ?: member.identity
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = Caption1Regular,
                    color = colorResource(id = CoreR.color.text_secondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
        }

        SelectionCircle(selectionOrder = member.selectionOrder)
    }
}

@DefaultPreviews
@Composable
private fun SelectMembersContentPreview() {
    SelectMembersContent(
        uiState = SelectMembersUiState.Content(
            members = listOf(
                MemberItem(
                    identity = "1",
                    name = "Alice Johnson",
                    globalName = "@alice",
                    icon = SpaceMemberIconView.Placeholder("Alice Johnson"),
                    isSelected = true,
                    selectionOrder = 1
                ),
                MemberItem(
                    identity = "2",
                    name = "Bob Smith",
                    globalName = "@bob_smith",
                    icon = SpaceMemberIconView.Placeholder("Bob Smith"),
                    isSelected = false
                ),
                MemberItem(
                    identity = "fklsdjflkjsdkfjksadjfkljsdlkjfkldsajlkfjkasdjfkjasdkjfklajsdklfjakljfkjasdkl",
                    name = "Charlie",
                    globalName = null,
                    icon = SpaceMemberIconView.Placeholder("Charlie"),
                    isSelected = true,
                    selectionOrder = 2
                )
            ),
            searchQuery = ""
        ),
        onMemberToggled = {},
        onSearchQueryChanged = {},
        onNext = {},
        onDismissRequest = {}
    )
}

@DefaultPreviews
@Composable
private fun SelectMembersEmptyPreview() {
    SelectMembersContent(
        uiState = SelectMembersUiState.Content(
            members = emptyList(),
            searchQuery = ""
        ),
        onMemberToggled = {},
        onSearchQueryChanged = {},
        onNext = {},
        onDismissRequest = {}
    )
}

@DefaultPreviews
@Composable
private fun SelectMembersLoadingPreview() {
    SelectMembersContent(
        uiState = SelectMembersUiState.Loading,
        onMemberToggled = {},
        onSearchQueryChanged = {},
        onNext = {},
        onDismissRequest = {}
    )
}
