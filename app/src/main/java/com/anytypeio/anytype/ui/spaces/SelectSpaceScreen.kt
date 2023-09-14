package com.anytypeio.anytype.ui.spaces

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.presentation.spaces.SelectSpaceView
import com.anytypeio.anytype.presentation.spaces.WorkspaceView

@Composable
fun SelectSpaceScreen(
    spaces: List<SelectSpaceView>,
    onAddClicked: () -> Unit,
    onSpaceClicked: (WorkspaceView) -> Unit,
    onSpaceSettingsClicked: () -> Unit,
    onProfileClicked: () -> Unit
) {
    Column() {
        Dragger(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.CenterHorizontally)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(count = 3),
            modifier = Modifier.padding(
                bottom = 16.dp
            )
        ) {
            spaces.forEach { item ->
                when (item) {
                    is SelectSpaceView.Profile -> {
                        item(
                            span = {
                                GridItemSpan(MAX_SPAN_COUNT)
                            }
                        ) {
                            SelectSpaceProfileHeader(
                                onSpaceSettingsClicked = onSpaceSettingsClicked,
                                onProfileClicked = onProfileClicked
                            )
                        }
                    }

                    is SelectSpaceView.Space -> {
                        item(
                            span = {
                                GridItemSpan(1)
                            }
                        ) {
                            SelectSpaceSpaceItem(item, onSpaceClicked)
                        }
                    }

                    is SelectSpaceView.Create -> {
                        item(
                            span = {
                                GridItemSpan(1)
                            }
                        ) {
                            SelectSpaceCreateButton(onAddClicked)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectSpaceCreateButton(onAddClicked: () -> Unit) {
    Column(modifier = Modifier) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x33FFFFFF))
                .clickable { onAddClicked() }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_plus_32),
                contentDescription = "Plus icon",
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Spacer(modifier = Modifier.height(28.dp))
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SelectSpaceSpaceItem(
    item: SelectSpaceView.Space,
    onSpaceClicked: (WorkspaceView) -> Unit
) {
    Column(modifier = Modifier) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(8.dp))
                .then(
                    if (item.view.isSelected)
                        Modifier.border(
                            width = if (item.view.isSelected) 2.dp else 0.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp)
                        )
                    else
                        Modifier
                )
                .background(Color.Blue)
                .clickable { onSpaceClicked(item.view) }
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            modifier = Modifier.fillMaxSize(),
            text = item.view.name.orEmpty(),
            textAlign = TextAlign.Center,
            style = Caption1Medium,
            color = Color.White,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SelectSpaceProfileHeader(
    onSpaceSettingsClicked: () -> Unit,
    onProfileClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .padding(bottom = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(start = 30.dp)
                .align(Alignment.CenterStart)
                .clip(CircleShape)
                .background(Color.Red)
                .size(32.dp)
                .clickable { onProfileClicked() }
        )
        Text(
            text = "Patrick Atkinson",
            style = HeadlineHeading,
            color = Color.White,
            modifier = Modifier
                .align(
                    Alignment.CenterStart
                )
                .padding(
                    start = 74.dp,
                    end = 74.dp
                )
                .clickable { onProfileClicked() }
        )
        Box(
            modifier = Modifier
                .padding(end = 30.dp)
                .size(32.dp)
                .align(Alignment.CenterEnd)
                .clickable { onSpaceSettingsClicked() }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_space_settings),
                contentDescription = "Space settings icon",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

const val MAX_SPAN_COUNT = 3