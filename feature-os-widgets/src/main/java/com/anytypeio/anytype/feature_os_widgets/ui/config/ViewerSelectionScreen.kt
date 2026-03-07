package com.anytypeio.anytype.feature_os_widgets.ui.config

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.feature_os_widgets.presentation.DataViewWidgetConfigViewModel

@Composable
fun ViewerSelectionScreen(
    objectName: String,
    viewers: List<DataViewWidgetConfigViewModel.ViewerView>,
    onViewerSelected: (DataViewWidgetConfigViewModel.ViewerView) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.background_primary))
            .statusBarsPadding()
    ) {
        // Header with back button and object name
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier
                    .size(48.dp)
                    .noRippleThrottledClickable { onBack() },
                contentScale = androidx.compose.ui.layout.ContentScale.Inside,
                painter = painterResource(R.drawable.ic_back_24),
                contentDescription = "Back",
            )

            Text(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                text = objectName.ifEmpty { stringResource(R.string.untitled) },
                style = Title1,
                color = colorResource(id = R.color.text_primary),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Spacer to balance the back button
            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Section label
        Text(
            text = stringResource(R.string.select_view),
            style = Caption1Regular,
            color = colorResource(id = R.color.text_secondary),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        // Viewer list
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(items = viewers, key = { it.id }) { viewer ->
                ViewerListItem(
                    viewer = viewer,
                    onClick = { onViewerSelected(viewer) }
                )
                Divider()
            }
        }
    }
}

@Composable
private fun ViewerListItem(
    viewer: DataViewWidgetConfigViewModel.ViewerView,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Viewer icon based on type
        Image(
            painter = painterResource(id = viewerTypeIcon(viewer.type)),
            contentDescription = viewer.type.formattedName,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Viewer name and type
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = viewer.name.ifEmpty { stringResource(R.string.untitled) },
                style = Title2,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = viewer.type.formattedName,
                style = Caption1Regular,
                color = colorResource(id = R.color.text_secondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun viewerTypeIcon(type: Block.Content.DataView.Viewer.Type): Int {
    return when (type) {
        Block.Content.DataView.Viewer.Type.GRID -> R.drawable.ic_layout_grid
        Block.Content.DataView.Viewer.Type.LIST -> R.drawable.ic_layout_list
        Block.Content.DataView.Viewer.Type.GALLERY -> R.drawable.ic_layout_gallery
        Block.Content.DataView.Viewer.Type.BOARD -> R.drawable.ic_layout_kanban
        Block.Content.DataView.Viewer.Type.CALENDAR -> R.drawable.ic_layout_grid // fallback
        Block.Content.DataView.Viewer.Type.GRAPH -> R.drawable.ic_layout_grid // fallback
    }
}
