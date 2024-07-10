package com.anytypeio.anytype.feature_discussions.ui

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text2.BasicTextField2
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineTitle
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.core_utils.const.DateConst.DEFAULT_DATE_FORMAT
import com.anytypeio.anytype.core_utils.ext.formatTimeInMillis
import com.anytypeio.anytype.feature_discussions.R
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionView
import kotlin.time.DurationUnit
import kotlin.time.toDuration


/**
 * TODO: do date formating before rendering?
 */
@Composable
fun DiscussionScreen(
    title: String,
    messages: List<DiscussionView.Message>
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            style = HeadlineTitle,
            text = title,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.padding(
                top = 20.dp,
                start = 20.dp,
                end = 20.dp,
                bottom = 8.dp
            )
        )
        Text(
            style = Relations2,
            text = "Discussion",
            color = colorResource(id = R.color.text_secondary),
            modifier = Modifier.padding(
                bottom = 8.dp,
                start = 20.dp
            )
        )
        Discussion(
            modifier = Modifier.weight(1.0f),
            messages = messages
        )
        Divider(
            paddingStart = 0.dp,
            paddingEnd = 0.dp
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_plus_32),
                contentDescription = "Plus button",
                modifier = Modifier
                    .padding(start = 8.dp)
                    .align(Alignment.CenterVertically)
            )

            var textField by rememberSaveable { mutableStateOf("") }

            BasicTextField(
                value = textField,
                onValueChange = { textField = it },
                textStyle = BodyRegular.copy(
                    color = colorResource(id = R.color.text_primary)
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = 8.dp,
                        end = 8.dp
                    )
                    .align(Alignment.CenterVertically)
                ,
                cursorBrush = SolidColor(colorResource(id = R.color.palette_system_blue))
            )

            Image(
                painter = painterResource(id = R.drawable.ic_send_message),
                contentDescription = "Plus button",
                modifier = Modifier
                    .padding(end = 8.dp)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}


@Composable
fun Discussion(
    modifier: Modifier = Modifier,
    messages: List<DiscussionView.Message>
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(
            messages,
            key = { _, msg -> msg.id }
        ) { idx, msg ->
            if (idx == 0)
                Spacer(modifier = Modifier.height(36.dp))
            Row(
                modifier = Modifier.padding(horizontal = 48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            colorResource(id = R.color.palette_system_blue),
                            shape = CircleShape
                        )
                        .align(Alignment.Bottom)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Bubble(
                    name = msg.author,
                    msg = msg.msg,
                    timestamp = msg.timestamp
                )
            }
        }
    }
}

@Composable
fun Bubble(
    name: String,
    msg: String,
    timestamp: Long
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorResource(id = R.color.palette_very_light_grey),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp
            )
        ) {
            Text(
                text = name,
                style = PreviewTitle2Medium,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = timestamp.formatTimeInMillis(
                    DEFAULT_DATE_FORMAT
                ),
                style = Caption1Regular,
                color = colorResource(id = R.color.text_secondary),
                maxLines = 1
            )
        }
        Text(
            modifier = Modifier.padding(
                top = 32.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 12.dp
            ),
            text = msg,
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary)
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
@Composable
fun BubblePreview() {
    Bubble(
        name = "Leo Marx",
        msg = stringResource(id = R.string.default_text_placeholder),
        timestamp = System.currentTimeMillis()
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
@Composable
fun DiscussionPreview() {
    Discussion(
        messages = listOf(
            DiscussionView.Message(
                id = "1",
                msg = stringResource(id = R.string.default_text_placeholder),
                author = "Walter",
                timestamp = System.currentTimeMillis()
            ),
            DiscussionView.Message(
                id = "2",
                msg = stringResource(id = R.string.default_text_placeholder),
                author = "Leo",
                timestamp = System.currentTimeMillis()
            ),
            DiscussionView.Message(
                id = "3",
                msg = stringResource(id = R.string.default_text_placeholder),
                author = "Gilbert",
                timestamp = System.currentTimeMillis()
            )
        )
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
@Composable
fun DiscussionScreenPreview() {
    DiscussionScreen(
        title = "Conversations with friends",
        messages = buildList {
            repeat(30) { idx ->
                add(
                    DiscussionView.Message(
                        id = idx.toString(),
                        msg = stringResource(id = R.string.default_text_placeholder),
                        author = "User ${idx.inc()}",
                        timestamp =
                        System.currentTimeMillis()
                                - 30.toDuration(DurationUnit.DAYS).inWholeMilliseconds
                                + idx.toDuration(DurationUnit.DAYS).inWholeMilliseconds
                    )
                )
            }
        }
    )
}