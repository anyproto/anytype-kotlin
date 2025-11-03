package com.anytypeio.anytype.ui.vault

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.Title1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseSpaceTypeScreen(
    onCreateChatClicked: () -> Unit,
    onCreateSpaceClicked: () -> Unit,
    onJoinViaQrClicked: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        modifier = Modifier
            .fillMaxWidth(),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        contentColor = Color.Transparent,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .background(
                    shape = RoundedCornerShape(16.dp),
                    color = colorResource(id = R.color.background_secondary)
                )
        ) {
            Dragger(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 8.dp)
            )

            if (BuildConfig.SHOW_CHATS) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .padding(horizontal = 16.dp)
                        .noRippleThrottledClickable {
                            onCreateChatClicked()
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_space_type_chat),
                        contentDescription = "Create Chat",
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(id = R.string.vault_create_chat),
                            style = Title1,
                            color = colorResource(id = R.color.text_primary)
                        )
                        Text(
                            text = stringResource(id = R.string.vault_create_chat_description),
                            style = Caption1Regular,
                            color = colorResource(id = R.color.control_transparent_secondary),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 16.dp)
                    .noRippleThrottledClickable {
                        onCreateSpaceClicked()
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_space_type_space),
                    contentDescription = "Create Space",
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(id = R.string.vault_create_space),
                        style = Title1,
                        color = colorResource(id = R.color.text_primary)
                    )
                    Text(
                        text = stringResource(id = R.string.vault_create_space_description),
                        style = Caption1Regular,
                        color = colorResource(id = R.color.control_transparent_secondary),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 16.dp)
                    .noRippleThrottledClickable {
                        onJoinViaQrClicked()
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_join_via_qr_code_32),
                    contentDescription = "Join via QR Code",
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            shape = CircleShape, color =
                                colorResource(id = R.color.shape_transparent_secondary)
                        ),
                    contentScale = ContentScale.Inside
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(id = R.string.vault_join_via_qr),
                        style = Title1,
                        color = colorResource(id = R.color.text_primary)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@DefaultPreviews
@Composable
private fun ChooseSpaceTypeScreenPreview() {
    ChooseSpaceTypeScreen(
        onCreateChatClicked = {},
        onCreateSpaceClicked = {}
    )
}