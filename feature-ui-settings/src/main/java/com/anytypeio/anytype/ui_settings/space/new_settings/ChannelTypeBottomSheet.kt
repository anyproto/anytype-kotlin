package com.anytypeio.anytype.ui_settings.space.new_settings

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.presentation.spaces.UiSpaceSettingsItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelTypeBottomSheet(
    currentType: UiSpaceSettingsItem.ChangeType,
    onTypeSelected: (UiSpaceSettingsItem.ChangeType) -> Unit,
    onDismiss: () -> Unit
) {
    val contentModifier = if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK) {
        Modifier
            .windowInsetsPadding(WindowInsets.systemBars)
            .fillMaxSize()
    } else {
        Modifier
            .fillMaxSize()
    }
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = contentModifier,
        containerColor = colorResource(R.color.background_secondary),
        onDismissRequest = onDismiss,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Dragger(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 6.dp)
            )
            Text(
                text = stringResource(R.string.space_settings_space_type_chat_title),
                style = Title1,
                color = colorResource(id = R.color.text_primary),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )
            val isChatChecked = currentType is UiSpaceSettingsItem.ChangeType.Chat
            TypeOption(
                modifier = Modifier
                    .noRippleThrottledClickable {
                        if (!isChatChecked) {
                            onTypeSelected(UiSpaceSettingsItem.ChangeType.Chat())
                        } else {
                            onDismiss()
                        }
                    },
                title = stringResource(R.string.space_settings_space_type_chat_item_title),
                subtitle = stringResource(R.string.space_settings_space_type_chat_descr),
                icon = R.drawable.ic_chat_type_24,
                checked = isChatChecked
            )
            Divider(
                paddingStart = 16.dp,
                paddingEnd = 16.dp,
            )
            val isSpaceChecked = currentType is UiSpaceSettingsItem.ChangeType.Data
            TypeOption(
                modifier = Modifier
                    .noRippleThrottledClickable {
                        if (!isSpaceChecked) {
                            onTypeSelected(UiSpaceSettingsItem.ChangeType.Data())
                        } else {
                            onDismiss()
                        }
                    },
                title = stringResource(R.string.space_settings_space_type_space_item_title),
                subtitle = stringResource(R.string.space_settings_space_type_space_descr),
                icon = R.drawable.ic_space_type_24,
                checked = isSpaceChecked
            )
            Divider(
                paddingStart = 16.dp,
                paddingEnd = 16.dp,
            )
        }
    }
}

@Composable
fun TypeOption(
    modifier: Modifier,
    title: String,
    subtitle: String,
    icon: Int,
    checked: Boolean
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    shape = CircleShape,
                    color = colorResource(id = R.color.shape_transparent_secondary)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = "Change type icon",
                modifier = Modifier.wrapContentSize()
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = Title2,
                color = colorResource(id = R.color.text_primary)
            )
            Text(
                modifier = Modifier.padding(end = 12.dp),
                text = subtitle,
                style = Relations3,
                color = colorResource(id = R.color.text_secondary),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (checked) {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = R.drawable.ic_check_black_14),
                contentDescription = "End icon",
                contentScale = ContentScale.Inside
            )
        }
    }
}

@DefaultPreviews
@Composable
fun ChannelTypeBottomSheetPreview() {
    ChannelTypeBottomSheet(
        currentType = UiSpaceSettingsItem.ChangeType.Chat(),
        onTypeSelected = {},
        onDismiss = {},
    )
}
