package com.anytypeio.anytype.feature_allcontent.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.feature_allcontent.R

//region AllContentTopBarContainer
@Composable
fun AllContentTopBarContainer(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
            .align(Alignment.TopStart),
        content = content
    )
}


//endregion


//region AllContentTitle
sealed class AllContentTitleState {
    data object Hidden : AllContentTitleState()
    data object AllContent : AllContentTitleState()
    data object OnlyUnlinked : AllContentTitleState()
}

@Composable
fun BoxScope.AllContentTitle(state: AllContentTitleState) {
    when (state) {
        AllContentTitleState.Hidden -> return
        AllContentTitleState.AllContent -> {
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Center),
                text = stringResource(id = R.string.all_content_title_all_content),
                style = Title1,
                color = colorResource(id = R.color.text_primary)
            )
        }

        AllContentTitleState.OnlyUnlinked -> {
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Center),
                text = stringResource(id = R.string.all_content_title_only_unlinked),
                style = Title1,
                color = colorResource(id = R.color.text_primary)
            )
        }
    }
}
//endregion

//region AllContentMenuButton
sealed class AllContentMenuButtonState {
    data object Hidden : AllContentMenuButtonState()
    data object Visible : AllContentMenuButtonState()
}

@Composable
fun BoxScope.AllContentMenuButton(state: AllContentMenuButtonState) {
    when (state) {
        AllContentMenuButtonState.Hidden -> return
        AllContentMenuButtonState.Visible -> {
            Image(
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.CenterEnd),
                painter = painterResource(id = R.drawable.ic_space_list_dots),
                contentDescription = "Menu icon",
                contentScale = ContentScale.Inside
            )
        }
    }
}
//endregion