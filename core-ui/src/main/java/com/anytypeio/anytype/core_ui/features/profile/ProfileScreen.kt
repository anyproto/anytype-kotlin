package com.anytypeio.anytype.core_ui.features.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.relations.CircleIcon
import com.anytypeio.anytype.core_ui.views.AvatarTitle
import com.anytypeio.anytype.core_ui.views.ButtonPrimaryLarge
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.presentation.profile.ProfileIconView
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        dragHandle = {
            Column {
                Spacer(modifier = Modifier.height(6.dp))
                Dragger()
                Spacer(modifier = Modifier.height(6.dp))
            }
        },
        scrimColor = colorResource(id = R.color.modal_screen_outside_background),
        containerColor = colorResource(id = R.color.background_secondary),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetState = bottomSheetState,
        onDismissRequest = {
            //onDateEvent(DateEvent.FieldsSheet.OnSheetDismiss)
        },
        content = {
            ProfileTitle(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(4.dp))
            ProfileAnyTitle(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(4.dp))
            ProfileDescription(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
            ProfileButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 32.dp)
                    .align(Alignment.CenterHorizontally)
            )
        },
    )
}

@Composable
private fun ProfileContent() {
//    ProfileImageBlock(
//        modifier = Modifier.size(112.dp),
//        name =
//
//    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProfileImageBlock(
    modifier: Modifier,
    name: String,
    icon: ProfileIconView,
    fontSize: TextUnit,
    onProfileIconClick: () -> Unit
) {
    when (icon) {
        is ProfileIconView.Image -> {
            GlideImage(
                model = icon.url,
                contentDescription = "Custom image profile",
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .clip(shape = CircleShape)
                    .noRippleClickable {
                        onProfileIconClick.invoke()
                    }
            )
        }
        else -> {
            val nameFirstChar = if (name.isEmpty()) {
                stringResource(id = R.string.account_default_name)
            } else {
                name.first().uppercaseChar().toString()
            }
            Box(
                modifier = modifier
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.shape_tertiary))
                    .noRippleClickable {
                        onProfileIconClick.invoke()
                    }
            ) {
                Text(
                    text = nameFirstChar,
                    style = AvatarTitle.copy(
                        color = colorResource(id = R.color.glyph_active),
                        fontSize = fontSize
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun ProfileIcon() {

}

@Composable
private fun ProfileTitle(modifier: Modifier) {
    Text(
        modifier = modifier,
        text = "Ivanov Konstantin",
        style = HeadlineHeading,
        color = colorResource(id = R.color.text_primary)
    )
}

@Composable
private fun ProfileAnyTitle(modifier: Modifier) {
    Text(
        modifier = modifier,
        text = "ivanov.any",
        style = Caption1Regular,
        color = colorResource(id = R.color.text_secondary)
    )
}

@Composable
private fun ProfileDescription(modifier: Modifier) {
    Text(
        modifier = modifier,
        text = "Web3 activist growing a digital garden of knowledge for decentralization and digital rights. Sharing insights on blockchain, transparency, and open data.",
        style = PreviewTitle2Regular,
        color = colorResource(id = R.color.text_primary),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun ProfileButton(modifier: Modifier) {
    ButtonSecondary(
        modifier = modifier,
        text = stringResource(R.string.profile_view_edit_button),
        size = ButtonSize.LargeSecondary,
        onClick = {}
    )
}

@DefaultPreviews
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}