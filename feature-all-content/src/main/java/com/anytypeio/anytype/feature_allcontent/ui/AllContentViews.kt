package com.anytypeio.anytype.feature_allcontent.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.feature_allcontent.R

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
                style = Title2,
                color = colorResource(id = R.color.text_primary)
            )
        }
    }
}

sealed class AllContentTitleState {
    data object Hidden : AllContentTitleState()
    data object AllContent : AllContentTitleState()
    data object OnlyUnlinked : AllContentTitleState()
}