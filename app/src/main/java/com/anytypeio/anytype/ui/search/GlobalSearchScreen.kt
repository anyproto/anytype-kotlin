package com.anytypeio.anytype.ui.search

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.light
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.presentation.search.GlobalSearchItemView

@Composable
fun GlobalSearchScreen(
    items: List<GlobalSearchItemView>,
    onQueryChanged: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(rememberNestedScrollInteropConnection())
    ) {
        BasicTextField(
            value = query,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textStyle = BodyRegular.copy(
                color = colorResource(id = R.color.text_primary)
            ),
            onValueChange = {
                query = it.also {
                    onQueryChanged(it)
                }
            },
        )
        LazyColumn(
            modifier = Modifier.weight(1.0f)
        ) {
            items.forEachIndexed { idx, item ->
                item(key = item.id) {
                    GlobalSearchItem(globalSearchItemView = item)
                    if (idx != items.lastIndex) {
                        Divider(paddingStart = 16.dp, paddingEnd = 16.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun GlobalSearchItem(
    globalSearchItemView: GlobalSearchItemView
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    top = 12.dp,
                    bottom = 12.dp
                )
                .size(48.dp)
                .background(Color.Red)
        )
        Column(
            modifier = Modifier
                .padding(
                    start = 76.dp,
                    bottom = 12.dp,
                    top = 12.dp,
                    end = 16.dp
                )
                .align(Alignment.CenterStart)
        ) {
            Text(
                text = globalSearchItemView.title,
                style = PreviewTitle2Medium,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            when(val meta = globalSearchItemView.meta) {
                is GlobalSearchItemView.Meta.Block -> {
                    if (meta.highlights.isEmpty()) {
                        Text(
                            text = meta.snippet,
                            style = Relations2,
                            color = colorResource(id = R.color.text_primary)
                        )
                    } else {
                        DefaultMetaBlockWithHighlights(
                            text = meta.snippet,
                            highlights = meta.highlights
                        )
                    }
                }
                is GlobalSearchItemView.Meta.Default -> {
                    if (meta.highlights.isEmpty()) {
                        Text(
                            text = "${meta.name}: ${meta.value}",
                            style = Relations2,
                            color = colorResource(id = R.color.text_primary)
                        )
                    } else {
                        DefaultMetaRelationWithHighlights(
                            title = meta.name,
                            value = meta.value,
                            highlights = meta.highlights
                        )
                    }
                }
                is GlobalSearchItemView.Meta.Status -> {
                    DefaultMetaStatusRelation(
                        title = meta.name,
                        value = meta.value,
                        color = meta.color
                    )
                }
                is GlobalSearchItemView.Meta.Tag -> {
                    DefaultMetaTagRelation(
                        title = meta.name,
                        value = meta.value,
                        color = meta.color
                    )
                }
                is GlobalSearchItemView.Meta.None -> {
                    // Draw nothing.
                }
            }
            Text(
                text = globalSearchItemView.type,
                style = Relations2,
                color = colorResource(id = R.color.text_secondary)
            )
        }
    }
}

@Composable
private fun DefaultMetaBlockWithHighlights(
    text: String,
    highlights: List<IntRange>
) {
    Text(
        text = AnnotatedString(
            text = text,
            spanStyles = highlights.map { range ->
                AnnotatedString.Range(
                    SpanStyle(
                        background = colorResource(id = R.color.palette_light_ice)
                    ),
                    range.first,
                    range.last
                )
            }
        ),
        style = Relations2,
        color = colorResource(id = R.color.text_primary)
    )
}

@Composable
private fun DefaultMetaRelationWithHighlights(
    title: String,
    value: String,
    highlights: List<IntRange>
) {
    Text(
        text = buildAnnotatedString {
            append("${title}: ")
            append(
                AnnotatedString(
                    text = value,
                    spanStyles = highlights.map { range ->
                        AnnotatedString.Range(
                            SpanStyle(
                                background = colorResource(id = R.color.palette_light_ice)
                            ),
                            range.first,
                            range.last
                        )
                    }
                )
            )
        },
        style = Relations2,
        color = colorResource(id = R.color.text_primary)
    )
}

@Composable
private fun DefaultMetaTagRelation(
    title: String,
    value: String,
    color: ThemeColor
) {
    Row {
        Text(
            text = "${title}: ",
            style = Relations2,
            color = colorResource(id = R.color.text_primary)
        )
        Text(
            text = value,
            style = Relations2,
            color = dark(color),
            modifier = Modifier
                .background(
                    color = light(color = color),
                    shape = RoundedCornerShape(size = 3.dp)
                )
                .padding(start = 6.dp, end = 6.dp),
        )
    }
}

@Composable
private fun DefaultMetaStatusRelation(
    title: String,
    value: String,
    color: ThemeColor
) {
    Text(
        text = buildAnnotatedString {
            append("${title}: ")
            append(
                AnnotatedString(
                    text = value,
                    spanStyles = listOf(
                        AnnotatedString.Range(
                            SpanStyle(color = dark(color = color)),
                            0,
                            value.length
                        )
                    )
                )
            )
        },
        style = Relations2,
        color = colorResource(id = R.color.text_primary)
    )
}

@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Composable
private fun DefaultGlobalSearchItemViewPreview() {
    GlobalSearchItem(
        GlobalSearchItemView(
            id = "ID",
            title = "Autechre",
            type = "Band",
            meta = GlobalSearchItemView.Meta.None
        )
    )
}

@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Composable
private fun DefaultGlobalSearchItemViewWithBlockMetaPreview() {
    GlobalSearchItem(
        GlobalSearchItemView(
            id = "ID",
            title = "Autechre",
            type = "Band",
            meta = GlobalSearchItemView.Meta.Block(
                snippet = "Autechre are an English electronic music duo consisting of Rob Brown and Sean Booth, both from Rochdale, Greater Manchester. ",
                highlights = emptyList()
            )
        )
    )
}

@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Composable
private fun DefaultGlobalSearchItemViewBlockTwoHighlightsMetaPreview() {
    GlobalSearchItem(
        GlobalSearchItemView(
            id = "ID",
            title = "Autechre",
            type = "Band",
            meta = GlobalSearchItemView.Meta.Block(
                snippet = "Autechre are an English electronic music duo consisting of Rob Brown and Sean Booth, both from Rochdale, Greater Manchester. ",
                highlights = listOf(
                    IntRange(0, 8),
                    IntRange(15, 23)
                )
            )
        )
    )
}

@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Composable
private fun DefaultGlobalSearchItemViewRelationTwoHighlightsMetaPreview() {
    GlobalSearchItem(
        GlobalSearchItemView(
            id = "ID",
            title = "Autechre",
            type = "Band",
            meta = GlobalSearchItemView.Meta.Default(
                name = "Description",
                value = "Autechre are an English electronic music duo consisting of Rob Brown and Sean Booth, both from Rochdale, Greater Manchester. ",
                highlights = listOf(
                    IntRange(0, 8),
                    IntRange(15, 23)
                )
            )
        )
    )
}

@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Composable
private fun DefaultGlobalSearchItemViewTagRelationPreview() {
    GlobalSearchItem(
        GlobalSearchItemView(
            id = "ID",
            title = "Autechre",
            type = "Band",
            meta = GlobalSearchItemView.Meta.Tag(
                name = "Style",
                value = "IDM",
                color = ThemeColor.TEAL
            )
        )
    )
}

@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Composable
private fun DefaultGlobalSearchItemViewStatusRelationPreview() {
    GlobalSearchItem(
        GlobalSearchItemView(
            id = "ID",
            title = "Autechre",
            type = "Band",
            meta = GlobalSearchItemView.Meta.Status(
                name = "Style",
                value = "IDM",
                color = ThemeColor.TEAL
            )
        )
    )
}

@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Composable
private fun DefaultGlobalSearchItemViewStatusRelationScreenPreview() {
    GlobalSearchScreen(
        onQueryChanged = {},
        items = listOf(
            GlobalSearchItemView(
                id = "ID1",
                title = "Autechre",
                type = "Band",
                meta = GlobalSearchItemView.Meta.None
            ),
            GlobalSearchItemView(
                id = "ID2",
                title = "Autechre",
                type = "Band",
                meta = GlobalSearchItemView.Meta.Status(
                    name = "Style",
                    value = "IDM",
                    color = ThemeColor.TEAL
                )
            ),
            GlobalSearchItemView(
                id = "ID3",
                title = "Autechre",
                type = "Band",
                meta = GlobalSearchItemView.Meta.Tag(
                    name = "Style",
                    value = "IDM",
                    color = ThemeColor.TEAL
                )
            ),
            GlobalSearchItemView(
                id = "ID4",
                title = "Autechre",
                type = "Band",
                meta = GlobalSearchItemView.Meta.Default(
                    name = "Description",
                    value = "Autechre are an English electronic music duo consisting of Rob Brown and Sean Booth, both from Rochdale, Greater Manchester. ",
                    highlights = listOf(
                        IntRange(0, 8),
                        IntRange(15, 23)
                    )
                )
            ),
            GlobalSearchItemView(
                id = "ID5",
                title = "Autechre",
                type = "Band",
                meta = GlobalSearchItemView.Meta.Block(
                    snippet = "Autechre are an English electronic music duo consisting of Rob Brown and Sean Booth, both from Rochdale, Greater Manchester. ",
                    highlights = emptyList()
                )
            )
        )
    )
}