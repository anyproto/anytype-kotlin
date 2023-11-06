package com.anytypeio.anytype.ui.objects.creation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.emojifier.Emojifier

@Preview
@Composable
fun PreviewScreen() {
    CreateObjectOfTypeScreen(
        onTypeClicked = {}
    )
}


sealed class SelectTypeView {
    sealed class Section : SelectTypeView() {
        object Objects: Section()
        object Groups: Section()
    }
    data class Type(
//        val typeId: String,
        val typeKey: String,
        val name: String,
        val icon: String,
    ): SelectTypeView()
}

@Composable
fun CreateObjectOfTypeScreen(
    onTypeClicked: (Key) -> Unit
) {
    val types: List<SelectTypeView> = buildList {
        add(
            SelectTypeView.Section.Objects
        )
        add(
            SelectTypeView.Type(
                typeKey = ObjectTypeUniqueKeys.NOTE,
                name = "Note",
                icon = ""
            )
        )
        add(
            SelectTypeView.Type(
                typeKey = "ot-book",
                name = "Book",
                icon = ""
            )
        )
        add(
            SelectTypeView.Type(
                typeKey = "ot-human",
                name = "Human",
                icon = ""
            )
        )
        add(
            SelectTypeView.Type(
                typeKey = "ot-page",
                name = "Page",
                icon = ""
            )
        )
        add(
            SelectTypeView.Type(
                name = "Task",
                typeKey = "ot-task",
                icon = ""
            )
        )
        add(
            SelectTypeView.Section.Groups
        )
        add(
            SelectTypeView.Type(
                typeKey = "ot-set",
                name = "Set",
                icon = ""
            )
        )
        add(
            SelectTypeView.Type(
                typeKey = "ot-collection",
                name = "Collection",
                icon = ""
            )
        )
    }
    Column(
        modifier = Modifier
    ) {
        Dragger(
            Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 6.dp)
        )
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp
            )
        ) {
            types.forEach { view ->
                when(view) {
                    is SelectTypeView.Section.Groups -> {
                        item(
                            key = "2",
                            span = { GridItemSpan(maxLineSpan) }
                        ) {
                            Section(
                                title = "Groups",
                            )
                        }
                    }
                    is SelectTypeView.Section.Objects -> {
                        item(
                            key = "1",
                            span = { GridItemSpan(maxLineSpan) }
                        ) {
                            Section(
                                title = "Objects"
                            )
                        }
                    }
                    is SelectTypeView.Type -> {
                        item(
                            key = view.name
                        ) {
                            ObjectTypeItem(
                                name = view.name,
                                onItemClicked = throttledClick(
                                    onClick = {
                                        onTypeClicked(view.typeKey)
                                    }
                                )
                            )
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun ObjectTypeItem(
    name: String,
    onItemClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(48.dp)
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.shape_primary),
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = colorResource(id = R.color.background_primary)
            )
            .clickable { onItemClicked() }
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val uri = Emojifier.safeUri("✍")
        Spacer(
            modifier = Modifier.width(14.dp)
        )
        Image(
            painter = rememberAsyncImagePainter(uri),
            contentDescription = "Icon from URI",
            modifier = Modifier
                .size(18.dp)
                .background(Color.Black)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            style = Title2,
            color = colorResource(id = R.color.text_primary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}

@Composable
private fun Section(title: String) {
    Box(modifier = Modifier
        .height(52.dp)
        .fillMaxWidth()) {
        Text(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .align(Alignment.BottomStart),
            text = title,
            color = colorResource(id = R.color.text_secondary),
            style = Caption1Medium
        )
    }
}