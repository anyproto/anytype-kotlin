package com.anytypeio.anytype.core_ui.widgets.text

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.isRangeValid
import com.anytypeio.anytype.core_ui.extensions.disable
import com.anytypeio.anytype.core_ui.widgets.text.setClickableSpan
import com.anytypeio.anytype.core_ui.extensions.drawable
import com.anytypeio.anytype.core_ui.widgets.getDrawableAndTintColor
import com.anytypeio.anytype.presentation.editor.editor.Markup

fun Editable.proceedWithSettingMentionSpan(
    onImageReady: (String) -> Unit = {},
    mark: Markup.Mark.Mention,
    context: Context,
    click: ((String) -> Unit)? = null,
    mentionImageSize: Int = 0,
    mentionImagePadding: Int = 0,
    mentionCheckedIcon: Drawable?,
    mentionUncheckedIcon: Drawable?,
    mentionInitialsSize: Float,
    textColor: Int
) {
    if (!isRangeValid(mark, length)) return
    when (mark) {
        is Markup.Mark.Mention.Deleted -> {
            val placeholder = context.drawable(R.drawable.ic_non_existent_object)
            setSpan(
                MentionSpan(
                    context = context,
                    placeholder = placeholder,
                    imageSize = mentionImageSize,
                    imagePadding = mentionImagePadding,
                    param = mark.param,
                    isDeleted = true,
                    isArchived = false
                ),
                mark.from,
                mark.to,
                Markup.Companion.MENTION_SPANNABLE_FLAG
            )
        }
        is Markup.Mark.Mention.Loading -> {
            val placeholder = context.drawable(R.drawable.ic_mention_loading_state)
            setSpan(
                MentionSpan(
                    onImageResourceReady = onImageReady,
                    context = context,
                    placeholder = placeholder,
                    imageSize = mentionImageSize,
                    imagePadding = mentionImagePadding,
                    param = mark.param,
                    isArchived = false
                ),
                mark.from,
                mark.to,
                Markup.Companion.MENTION_SPANNABLE_FLAG
            )
        }
        is Markup.Mark.Mention.Base -> {
            setSpan(
                MentionSpan(
                    onImageResourceReady = {},
                    context = context,
                    imageSize = mentionImageSize,
                    imagePadding = mentionImagePadding,
                    param = mark.param,
                    isArchived = mark.isArchived
                ),
                mark.from,
                mark.to,
                Markup.Companion.MENTION_SPANNABLE_FLAG
            )
            if (!mark.isArchived) setClickableSpan(click, mark)
        }
        is Markup.Mark.Mention.WithEmoji -> {
            setSpan(
                MentionSpan(
                    onImageResourceReady = onImageReady,
                    context = context,
                    imageSize = mentionImageSize,
                    imagePadding = mentionImagePadding,
                    param = mark.param,
                    emoji = mark.emoji,
                    isArchived = mark.isArchived
                ),
                mark.from,
                mark.to,
                Markup.Companion.MENTION_SPANNABLE_FLAG
            )
            if (!mark.isArchived) setClickableSpan(click, mark)
        }
        is Markup.Mark.Mention.WithImage -> {
            setSpan(
                MentionSpan(
                    onImageResourceReady = onImageReady,
                    context = context,
                    imageSize = mentionImageSize,
                    imagePadding = mentionImagePadding,
                    param = mark.param,
                    image = mark.image,
                    isArchived = mark.isArchived
                ),
                mark.from,
                mark.to,
                Markup.Companion.MENTION_SPANNABLE_FLAG
            )
            if (!mark.isArchived) setClickableSpan(click, mark)
        }
        is Markup.Mark.Mention.Task.Checked -> {
            setSpan(
                MentionSpan(
                    onImageResourceReady = onImageReady,
                    context = context,
                    imageSize = mentionImageSize,
                    imagePadding = mentionImagePadding,
                    placeholder = mentionCheckedIcon,
                    param = mark.param,
                    isArchived = mark.isArchived
                ),
                mark.from,
                mark.to,
                Markup.Companion.MENTION_SPANNABLE_FLAG
            )
            if (!mark.isArchived) setClickableSpan(click, mark)
        }
        is Markup.Mark.Mention.Task.Unchecked -> {
            setSpan(
                MentionSpan(
                    onImageResourceReady = onImageReady,
                    context = context,
                    imageSize = mentionImageSize,
                    imagePadding = mentionImagePadding,
                    placeholder = mentionUncheckedIcon,
                    param = mark.param,
                    isArchived = mark.isArchived
                ),
                mark.from,
                mark.to,
                Markup.Companion.MENTION_SPANNABLE_FLAG
            )
            if (!mark.isArchived) setClickableSpan(click, mark)
        }
        is Markup.Mark.Mention.Profile.WithImage -> {
            setSpan(
                MentionSpan(
                    onImageResourceReady = onImageReady,
                    context = context,
                    imageSize = mentionImageSize,
                    imagePadding = mentionImagePadding,
                    param = mark.param,
                    profile = mark.imageUrl,
                    isArchived = mark.isArchived
                ),
                mark.from,
                mark.to,
                Markup.Companion.MENTION_SPANNABLE_FLAG
            )
            if (!mark.isArchived) setClickableSpan(click, mark)
        }
        is Markup.Mark.Mention.Profile.WithInitials -> {
            val placeholder =
                ContextCompat.getDrawable(
                    context,
                    R.drawable.object_in_list_background_profile_initial
                )
            setSpan(
                MentionSpan(
                    onImageResourceReady = onImageReady,
                    context = context,
                    imageSize = mentionImageSize,
                    imagePadding = mentionImagePadding,
                    placeholder = placeholder,
                    param = mark.param,
                    initials = mark.initials.toString(),
                    initialsTextSize = mentionInitialsSize,
                    isArchived = mark.isArchived
                ),
                mark.from,
                mark.to,
                Markup.Companion.MENTION_SPANNABLE_FLAG
            )
            if (!mark.isArchived) setClickableSpan(click, mark)
        }

        is Markup.Mark.Mention.Date -> {
            val placeholder =
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_obj_date_20
                )
            setSpan(
                MentionSpan(
                    onImageResourceReady = onImageReady,
                    context = context,
                    imageSize = mentionImageSize,
                    imagePadding = mentionImagePadding,
                    param = mark.param,
                    placeholder = placeholder,
                    isArchived = false
                ),
                mark.from,
                mark.to,
                Markup.Companion.MENTION_SPANNABLE_FLAG
            )
            setClickableSpan(click, mark)
        }
        is Markup.Mark.Mention.ObjectType -> {
            val (drawableRes, tint) = mark.icon.getDrawableAndTintColor(context)

            val baseDrawable = when {
                drawableRes == 0 -> null
                else -> try {
                    context.drawable(drawableRes).mutate().apply {
                        DrawableCompat.setTint(this, tint)
                    }
                } catch (e: Exception) {
                    null
                }
            }

            val finalDrawable = baseDrawable ?: context.drawable(R.drawable.ic_empty_state_type)
            setSpan(
                MentionSpan(
                    onImageResourceReady = onImageReady,
                    context = context,
                    imageSize = mentionImageSize,
                    imagePadding = mentionImagePadding,
                    param = mark.param,
                    emoji = null,
                    placeholder = finalDrawable,
                    isArchived = false
                ),
                mark.from,
                mark.to,
                Markup.Companion.MENTION_SPANNABLE_FLAG
            )
            setClickableSpan(click, mark)
        }
    }
}

fun Editable.setClickableSpan(click: ((String) -> Unit)?, mark: Markup.Mark.Mention) {
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            (widget as? TextInputWidget)?.disable()
            click?.invoke(mark.param)
        }
    }
    setSpan(
        clickableSpan,
        mark.from,
        mark.to,
        Markup.Companion.MENTION_SPANNABLE_FLAG
    )
}

fun Editable.setMentionSpan(
    onImageReady: (String) -> Unit = {},
    mark: Markup.Mark.Mention,
    context: Context,
    click: ((String) -> Unit)? = null,
    mentionImageSize: Int = 0,
    mentionImagePadding: Int = 0,
    mentionCheckedIcon: Drawable?,
    mentionUncheckedIcon: Drawable?,
    mentionInitialsSize: Float,
    textColor: Int
): Editable = this.apply {
    if (isRangeValid(mark, length)) {
        proceedWithSettingMentionSpan(
            onImageReady = onImageReady,
            mark = mark,
            context = context,
            click = click,
            mentionImageSize = mentionImageSize,
            mentionImagePadding = mentionImagePadding,
            mentionCheckedIcon = mentionCheckedIcon,
            mentionUncheckedIcon = mentionUncheckedIcon,
            mentionInitialsSize = mentionInitialsSize,
            textColor = textColor
        )
    }
}