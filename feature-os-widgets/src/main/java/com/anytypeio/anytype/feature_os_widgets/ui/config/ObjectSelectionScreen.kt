package com.anytypeio.anytype.feature_os_widgets.ui.config

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
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
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_ui.widgets.SearchField

/**
 * UI model for object list items in widget configuration
 */
data class ObjectItemView(
    val obj: ObjectWrapper.Basic,
    val icon: ObjectIcon,
    val typeName: String
)

@Composable
fun ObjectSelectionScreen(
    spaceName: String,
    objectItems: List<ObjectItemView>,
    isLoading: Boolean,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onObjectSelected: (ObjectItemView) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.background_primary))
            .statusBarsPadding()
    ) {
        // Header with back button and space name
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
                text = spaceName.ifEmpty { stringResource(R.string.untitled) },
                style = Title1,
                color = colorResource(id = R.color.text_primary),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Spacer to balance the back button
            Spacer(modifier = Modifier.size(48.dp))
        }

        // Search bar
        SearchField(
            horizontalPadding = 20.dp,
            query = searchQuery,
            onQueryChanged = onSearchQueryChanged,
            enabled = true,
            onFocused = {}
        )

        Spacer(modifier = Modifier.height(22.dp))

        // Object list
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = colorResource(id = R.color.palette_system_amber_50)
                )
            } else if (objectItems.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_doc_search),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.nothing_found),
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_secondary),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items = objectItems, key = { it.obj.id }) { item ->
                        ObjectListItem(
                            item = item,
                            onClick = { onObjectSelected(item) }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
private fun ObjectListItem(
    item: ObjectItemView,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Object icon (48dp like sharing extension)
        ListWidgetObjectIcon(
            icon = item.icon,
            modifier = Modifier.size(48.dp),
            iconSize = 48.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Object name and type
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.obj.name.orEmpty().ifEmpty { stringResource(R.string.untitled) },
                style = Title2,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (item.typeName.isNotEmpty()) {
                Text(
                    text = item.typeName,
                    style = Caption1Regular,
                    color = colorResource(id = R.color.text_secondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
