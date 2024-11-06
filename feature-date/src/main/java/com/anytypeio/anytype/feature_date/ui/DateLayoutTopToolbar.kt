package com.anytypeio.anytype.feature_date.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncStatus
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.HeadlineTitle
import com.anytypeio.anytype.feature_date.R
import com.anytypeio.anytype.feature_date.models.DateLayoutHeaderState
import com.anytypeio.anytype.feature_date.models.DateLayoutTopToolbarState

@Composable
fun DateLayoutHeader(
    state: DateLayoutHeaderState.Content,
    onAction: (DateLayoutHeaderState.Action) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Image(
            modifier = Modifier
                .height(48.dp)
                .width(52.dp)
                .rotate(180f)
                .clickable {
                    onAction(DateLayoutHeaderState.Action.Previous)
                },
            contentDescription = "Previous day",
            painter = painterResource(id = R.drawable.ic_arrow_disclosure_18),
            contentScale = ContentScale.None
        )
        Text(
            textAlign = TextAlign.Center,
            text = state.title,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .align(Alignment.CenterVertically),
            style = HeadlineTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = colorResource(id = R.color.text_primary)
        )
        Image(
            modifier = Modifier
                .height(48.dp)
                .width(52.dp)
                .clickable {
                    onAction(DateLayoutHeaderState.Action.Next)
                },
            contentDescription = "Next day",
            painter = painterResource(id = R.drawable.ic_arrow_disclosure_18),
            contentScale = ContentScale.None
        )
    }
}

@Composable
fun DateLayoutTopToolbar(
    state: DateLayoutTopToolbarState.Content,
    action: (DateLayoutTopToolbarState.Action) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Image(
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.CenterEnd)
                .clickable {
                    action(DateLayoutTopToolbarState.Action.Calendar)
                },
            contentDescription = null,
            painter = painterResource(id = R.drawable.ic_calendar_24),
            contentScale = ContentScale.None
        )
    }
}

@Composable
@DefaultPreviews
fun DateLayoutTopToolbarPreview() {
    DateLayoutTopToolbar(
        state = DateLayoutTopToolbarState.Content(SpaceSyncStatus.SYNCING),
        action = {}
    )
}

@Composable
@DefaultPreviews
fun DateLayoutHeaderPreview() {
    val state = DateLayoutHeaderState.Content("Tue, 12 Oct")
    DateLayoutHeader(state) {}
}