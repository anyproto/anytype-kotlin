package com.anytypeio.anytype.ui.search

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.common.keyboardAsState
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.light
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.core_ui.widgets.DefaultBasicAvatarIcon
import com.anytypeio.anytype.core_ui.widgets.DefaultEmojiObjectIcon
import com.anytypeio.anytype.core_ui.widgets.DefaultFileObjectImageIcon
import com.anytypeio.anytype.core_ui.widgets.DefaultObjectBookmarkIcon
import com.anytypeio.anytype.core_ui.widgets.DefaultObjectImageIcon
import com.anytypeio.anytype.core_ui.widgets.DefaultProfileAvatarIcon
import com.anytypeio.anytype.core_ui.widgets.DefaultTaskObjectIcon
import com.anytypeio.anytype.core_ui.widgets.defaultProfileIconImage
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.search.GlobalSearchItemView

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GlobalSearchScreen(
    items: List<GlobalSearchItemView>,
    onQueryChanged: (String) -> Unit,
    onObjectClicked: (GlobalSearchItemView) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val isKeyboardOpen by keyboardAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(rememberNestedScrollInteropConnection())
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val focus = LocalFocusManager.current

        Dragger(
            modifier = Modifier.padding(vertical = 6.dp).align(Alignment.CenterHorizontally)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 10.dp,
                    start = 16.dp,
                    end = 16.dp
                )
                .background(
                    color = colorResource(id = R.color.shape_transparent),
                    shape = RoundedCornerShape(10.dp)
                )
                .height(40.dp)

        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_search_18),
                contentDescription = "Search icon",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(
                        start = 11.dp
                    )
            )
            BasicTextField(
                value = query,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp)
                    .align(Alignment.CenterVertically)
                ,
                textStyle = BodyRegular.copy(
                    color = colorResource(id = R.color.text_primary)
                ),
                onValueChange = {
                    query = it.also {
                        onQueryChanged(it)
                    }
                },
                singleLine = true,
                maxLines = 1,
                decorationBox = @Composable { innerTextField ->
                    TextFieldDefaults.OutlinedTextFieldDecorationBox(
                        value = query,
                        innerTextField = innerTextField,
                        enabled = true,
                        singleLine = true,
                        visualTransformation = VisualTransformation.None,
                        interactionSource = interactionSource,
                        placeholder =  {
                            Text(
                                text = stringResource(id = R.string.search),
                                style = BodyRegular.copy(
                                    color = colorResource(id = R.color.text_tertiary)
                                )
                            )
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = colorResource(id = R.color.shape_transparent)
                        ),
                        border = {},
                        contentPadding = PaddingValues()
                    )
                }
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1.0f)
        ) {
            items.forEachIndexed { idx, item ->
                item(key = item.id) {
                    if (idx == 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    GlobalSearchItem(
                        globalSearchItemView = item,
                        onObjectClicked = {
                            if (isKeyboardOpen) {
                                focus.clearFocus(true)
                            }
                            focus.clearFocus(true)
                            onObjectClicked(it)
                        }
                    )
                    if (idx != items.lastIndex) {
                        Divider(paddingStart = 16.dp, paddingEnd = 16.dp)
                    } else {
                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun GlobalSearchItem(
    globalSearchItemView: GlobalSearchItemView,
    onObjectClicked: (GlobalSearchItemView) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onObjectClicked(globalSearchItemView)
            }
    ) {
        GlobalSearchObjectIcon(
            icon = globalSearchItemView.icon,
            iconSize = 48.dp,
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    top = 12.dp,
                    bottom = 12.dp
                ),
            onTaskIconClicked = {
                // Do nothing
            }
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

@Composable
fun GlobalSearchObjectIcon(
    icon: ObjectIcon,
    modifier: Modifier,
    iconSize: Dp = 48.dp,
    onTaskIconClicked: (Boolean) -> Unit = {}
) {
    when (icon) {
        is ObjectIcon.Profile.Avatar -> DefaultProfileAvatarIcon(modifier, iconSize, icon)
        is ObjectIcon.Profile.Image -> defaultProfileIconImage(icon, modifier, iconSize)
        is ObjectIcon.Basic.Emoji -> DefaultEmojiObjectIcon(modifier, iconSize, icon)
        is ObjectIcon.Basic.Image -> DefaultObjectImageIcon(icon.hash, modifier, iconSize)
        is ObjectIcon.Basic.Avatar -> DefaultBasicAvatarIcon(modifier, iconSize, icon)
        is ObjectIcon.Bookmark -> DefaultObjectBookmarkIcon(icon.image, modifier, iconSize)
        is ObjectIcon.Task -> DefaultTaskObjectIcon(modifier, iconSize, icon, onTaskIconClicked)
        is ObjectIcon.File -> {
            DefaultFileObjectImageIcon(
                fileName = icon.fileName.orEmpty(),
                mime = icon.mime.orEmpty(),
                modifier = modifier,
                iconSize = iconSize
            )
        }
        else -> {
            // Draw nothing.
        }
    }
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
            space = SpaceId(""),
            title = "Autechre",
            type = "Band",
            meta = GlobalSearchItemView.Meta.None,
            layout = ObjectType.Layout.BASIC,
            icon = ObjectIcon.Basic.Avatar("A")
        ),
        onObjectClicked = {}
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
            space = SpaceId(""),
            title = "Autechre",
            type = "Band",
            meta = GlobalSearchItemView.Meta.Block(
                snippet = "Autechre are an English electronic music duo consisting of Rob Brown and Sean Booth, both from Rochdale, Greater Manchester. ",
                highlights = emptyList()
            ),
            layout = ObjectType.Layout.BASIC,
            icon = ObjectIcon.Basic.Avatar("A")
        ),
        onObjectClicked = {}
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
            space = SpaceId(""),
            title = "Autechre",
            type = "Band",
            meta = GlobalSearchItemView.Meta.Block(
                snippet = "Autechre are an English electronic music duo consisting of Rob Brown and Sean Booth, both from Rochdale, Greater Manchester. ",
                highlights = listOf(
                    IntRange(0, 8),
                    IntRange(15, 23)
                )
            ),
            layout = ObjectType.Layout.BASIC,
            icon = ObjectIcon.Basic.Avatar("A")
        ),
        onObjectClicked = {}
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
            space = SpaceId(""),
            title = "Autechre",
            type = "Band",
            meta = GlobalSearchItemView.Meta.Default(
                name = "Description",
                value = "Autechre are an English electronic music duo consisting of Rob Brown and Sean Booth, both from Rochdale, Greater Manchester. ",
                highlights = listOf(
                    IntRange(0, 8),
                    IntRange(15, 23)
                )
            ),
            layout = ObjectType.Layout.BASIC,
            icon = ObjectIcon.Basic.Avatar("A")
        ),
        onObjectClicked = {}
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
            space = SpaceId(""),
            title = "Autechre",
            type = "Band",
            meta = GlobalSearchItemView.Meta.Tag(
                name = "Style",
                value = "IDM",
                color = ThemeColor.TEAL
            ),
            layout = ObjectType.Layout.BASIC,
            icon = ObjectIcon.Basic.Avatar("A")
        ),
        onObjectClicked = {}
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
            space = SpaceId(""),
            title = "Autechre",
            type = "Band",
            meta = GlobalSearchItemView.Meta.Status(
                name = "Style",
                value = "IDM",
                color = ThemeColor.TEAL
            ),
            layout = ObjectType.Layout.BASIC,
            icon = ObjectIcon.Basic.Avatar("A")
        ),
        onObjectClicked = {}
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
                space = SpaceId(""),
                title = "Autechre",
                type = "Band",
                meta = GlobalSearchItemView.Meta.None,
                layout = ObjectType.Layout.BASIC,
                icon = ObjectIcon.Basic.Avatar("A")
            ),
            GlobalSearchItemView(
                id = "ID2",
                space = SpaceId(""),
                title = "Autechre",
                type = "Band",
                meta = GlobalSearchItemView.Meta.Status(
                    name = "Style",
                    value = "IDM",
                    color = ThemeColor.TEAL
                ),
                layout = ObjectType.Layout.BASIC,
                icon = ObjectIcon.Basic.Avatar("A")
            ),
            GlobalSearchItemView(
                id = "ID3",
                space = SpaceId(""),
                title = "Autechre",
                type = "Band",
                meta = GlobalSearchItemView.Meta.Tag(
                    name = "Style",
                    value = "IDM",
                    color = ThemeColor.TEAL
                ),
                layout = ObjectType.Layout.BASIC,
                icon = ObjectIcon.Basic.Avatar("A")
            ),
            GlobalSearchItemView(
                id = "ID4",
                space = SpaceId(""),
                title = "Autechre",
                type = "Band",
                meta = GlobalSearchItemView.Meta.Default(
                    name = "Description",
                    value = "Autechre are an English electronic music duo consisting of Rob Brown and Sean Booth, both from Rochdale, Greater Manchester. ",
                    highlights = listOf(
                        IntRange(0, 8),
                        IntRange(15, 23)
                    )
                ),
                layout = ObjectType.Layout.BASIC,
                icon = ObjectIcon.Basic.Avatar("A")
            ),
            GlobalSearchItemView(
                id = "ID5",
                space = SpaceId(""),
                title = "Autechre",
                type = "Band",
                meta = GlobalSearchItemView.Meta.Block(
                    snippet = "Autechre are an English electronic music duo consisting of Rob Brown and Sean Booth, both from Rochdale, Greater Manchester. ",
                    highlights = emptyList()
                ),
                layout = ObjectType.Layout.BASIC,
                icon = ObjectIcon.Basic.Avatar("A")
            )
        ),
        onObjectClicked = {}
    )
}