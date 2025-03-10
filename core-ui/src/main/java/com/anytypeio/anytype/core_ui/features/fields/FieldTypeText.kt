package com.anytypeio.anytype.core_ui.features.fields

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.Relations1

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FieldTypeText(
    modifier: Modifier = Modifier,
    title: String,
    text: String,
    isLocal: Boolean,
    onFieldClick: () -> Unit,
    onAddToCurrentTypeClick: () -> Unit,
    onRemoveFromObjectClick: () -> Unit,
) {
    val isMenuExpanded = remember { mutableStateOf(false) }

    val defaultModifier = modifier
        .combinedClickable(
            onClick = onFieldClick,
            onLongClick = {
                if (isLocal) isMenuExpanded.value = true
            }
        )
        .fillMaxWidth()
        .border(
            width = 1.dp,
            color = colorResource(id = R.color.shape_secondary),
            shape = RoundedCornerShape(12.dp)
        )
        .padding(vertical = 16.dp)
        .padding(horizontal = 16.dp)

    Column(
        modifier = defaultModifier
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp),
            text = title,
            style = Relations1,
            color = colorResource(id = R.color.text_secondary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = text,
            style = Relations1,
            color = colorResource(id = R.color.text_primary),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        FieldItemDropDownMenu(
            showMenu = isMenuExpanded.value,
            onDismissRequest = {
                isMenuExpanded.value = false
            },
            onAddToCurrentTypeClick = {
                isMenuExpanded.value = false
                onAddToCurrentTypeClick()
            },
            onRemoveFromObjectClick = {
                isMenuExpanded.value = false
                onRemoveFromObjectClick()
            }
        )
    }
}

@DefaultPreviews
@Composable
fun FieldTypeTextPreview() {
    FieldTypeText(
        title = "Description",
        text = "Upon creating your profile, you’ll receive your very own 12 word mnemonic ‘Recovery’ phrase to protect your account. This phrase is generated on-device and represents your master key generated upon signup, similar to a Bitcoin wallet. It also prevents anyone - including Anytype - from accessing your account and decrypting your data.\n" +
                "\n" +
                "All data you create will be stored locally (on-device) first. We use zero-knowledge encryption, meaning that your data is encrypted before it leaves your device to sync with other devices or backup nodes.",
        isLocal = true,
        onRemoveFromObjectClick = {},
        onAddToCurrentTypeClick = {},
        onFieldClick = {}
    )
}