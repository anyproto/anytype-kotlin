package com.anytypeio.anytype.feature_discussions.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Regular

@Composable
fun DiscussionCommentInput(
    text: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onSendClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(R.color.background_primary))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Plus button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colorResource(R.color.shape_transparent_primary))
                .clickable { /* TODO: attachments */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(
                    id = com.anytypeio.anytype.feature_discussions.R.drawable.ic_discussion_plus
                ),
                contentDescription = "Add attachment",
                modifier = Modifier.size(16.dp),
                tint = colorResource(id = R.color.glyph_active)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        // Pill-shaped text input
        Box(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colorResource(R.color.shape_transparent_primary)),
            contentAlignment = Alignment.CenterStart
        ) {
            CommentUserInput(
                text = text,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            )
        }
        // Send button
        AnimatedVisibility(
            visible = text.text.isNotEmpty(),
            exit = fadeOut() + scaleOut(),
            enter = fadeIn() + scaleIn()
        ) {
            Image(
                painter = painterResource(
                    id = com.anytypeio.anytype.feature_discussions.R.drawable.ic_send_message
                ),
                contentDescription = "Send comment",
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable { onSendClicked(text.text) }
            )
        }
    }
}

@Composable
private fun CommentUserInput(
    text: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = text,
        onValueChange = onValueChange,
        textStyle = PreviewTitle1Regular.copy(
            color = colorResource(id = R.color.text_primary)
        ),
        modifier = modifier,
        cursorBrush = SolidColor(colorResource(id = R.color.palette_system_blue)),
        maxLines = 5,
        singleLine = false,
        decorationBox = @Composable { innerTextField ->
            CommentHintDecorationBox(
                text = text.text,
                hint = stringResource(
                    id = com.anytypeio.anytype.localization.R.string.discussion_leave_comment
                ),
                innerTextField = innerTextField,
                textStyle = PreviewTitle1Regular.copy(
                    color = colorResource(R.color.text_tertiary)
                )
            )
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentHintDecorationBox(
    text: String,
    hint: String,
    innerTextField: @Composable () -> Unit,
    textStyle: TextStyle
) {
    OutlinedTextFieldDefaults.DecorationBox(
        value = text,
        visualTransformation = VisualTransformation.None,
        innerTextField = innerTextField,
        singleLine = false,
        enabled = true,
        placeholder = {
            Text(
                text = hint,
                color = colorResource(id = R.color.text_tertiary),
                style = textStyle
            )
        },
        interactionSource = remember { MutableInteractionSource() },
        colors = OutlinedTextFieldDefaults.colors(
            disabledBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        contentPadding = PaddingValues()
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun DiscussionCommentInputEmptyPreview() {
    DiscussionCommentInput(
        text = TextFieldValue(""),
        onValueChange = {},
        onSendClicked = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun DiscussionCommentInputWithTextPreview() {
    DiscussionCommentInput(
        text = TextFieldValue("Iagree with"),
        onValueChange = {},
        onSendClicked = {}
    )
}
