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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
                .background(shape = RoundedCornerShape(16.dp), color = colorResource(id = R.color.widget_background))
        ) {
            Dragger(modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 8.dp))

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
                    contentDescription = "Create Chat Space",
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
                        color = colorResource(id = R.color.text_secondary),
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
                        onCreateSpaceClicked()
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_space_type_space),
                    contentDescription = "Create Chat Space",
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
                        color = colorResource(id = R.color.text_secondary),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
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