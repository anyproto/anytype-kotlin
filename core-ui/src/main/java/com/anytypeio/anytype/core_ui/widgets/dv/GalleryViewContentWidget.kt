package com.anytypeio.anytype.core_ui.widgets.dv

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.use
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.light
import com.anytypeio.anytype.core_utils.ext.formatTimestamp
import com.anytypeio.anytype.core_utils.ext.setDrawableColor
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.relations.model.DefaultObjectRelationValueView
import com.anytypeio.anytype.presentation.sets.model.ObjectView
import com.bumptech.glide.Glide
import timber.log.Timber

class GalleryViewContentWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private var firstItemMargin = 0
    private var intervalItemMargin = 0
    private var marginAfterIcon = 0
    private var marginAfterText = 0
    private var defaultBottomMargin = resources.getDimension(R.dimen.dp_1).toInt()
    private var defaultTextSize = 0f

    private val defaultTextColor = context.getColor(R.color.text_secondary)

    init {
        orientation = VERTICAL
        if (attrs != null) {
            context.obtainStyledAttributes(attrs, R.styleable.GalleryViewContentWidget, 0, 0).use { props ->
                firstItemMargin = props.getDimension(R.styleable.GalleryViewContentWidget_firstItemMargin, 0f).toInt()
                intervalItemMargin = props.getDimension(R.styleable.GalleryViewContentWidget_itemIntervalMargin, 0f).toInt()
                marginAfterIcon = props.getDimension(R.styleable.GalleryViewContentWidget_marginAfterIcon, 0f).toInt()
                marginAfterText = props.getDimension(R.styleable.GalleryViewContentWidget_marginAfterText, 0f).toInt()
                defaultTextSize = props.getDimensionPixelSize(R.styleable.GalleryViewContentWidget_defaultTextSize, 0).toFloat()
            }
        }
    }

    fun setItems(relations: List<DefaultObjectRelationValueView>) {
        removeAllViews()
        relations.forEach { relation ->
            when (relation) {
                is DefaultObjectRelationValueView.Text -> {
                    if (!relation.text.isNullOrEmpty()) {
                        val view = TextView(context).apply {
                            id = generateViewId()
                            isSingleLine = true
                            maxLines = 1
                            ellipsize = TextUtils.TruncateAt.END
                            text = relation.text
                            setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
                            setTextColor(defaultTextColor)
                        }
                        addView(view)
                        view.updateLayoutParams<LayoutParams> {
                            marginStart = firstItemMargin
                            bottomMargin = defaultBottomMargin
                            marginEnd = firstItemMargin
                        }
                    }
                }
                is DefaultObjectRelationValueView.Url -> {
                    if (!relation.url.isNullOrEmpty()) {
                        val view = TextView(context).apply {
                            id = generateViewId()
                            isSingleLine = true
                            maxLines = 1
                            ellipsize = TextUtils.TruncateAt.END
                            text = relation.url
                            setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
                            setTextColor(defaultTextColor)
                        }
                        addView(view)
                        view.updateLayoutParams<LayoutParams> {
                            marginStart = firstItemMargin
                            marginEnd = firstItemMargin
                            bottomMargin = defaultBottomMargin
                        }
                    }
                }
                is DefaultObjectRelationValueView.Email -> {
                    if (!relation.email.isNullOrEmpty()) {
                        val view = TextView(context).apply {
                            id = generateViewId()
                            isSingleLine = true
                            maxLines = 1
                            ellipsize = TextUtils.TruncateAt.END
                            text = relation.email
                            setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
                            setTextColor(defaultTextColor)
                        }
                        addView(view)
                        view.updateLayoutParams<LayoutParams> {
                            marginStart = firstItemMargin
                            marginEnd = firstItemMargin
                            bottomMargin = defaultBottomMargin
                        }
                    }
                }
                is DefaultObjectRelationValueView.Number -> {
                    if (relation.number != null) {
                        val view = TextView(context).apply {
                            id = generateViewId()
                            isSingleLine = true
                            maxLines = 1
                            ellipsize = TextUtils.TruncateAt.END
                            text = relation.number
                            setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
                            setTextColor(defaultTextColor)
                        }
                        addView(view)
                        view.updateLayoutParams<LayoutParams> {
                            marginStart = firstItemMargin
                            marginEnd = firstItemMargin
                            bottomMargin = defaultBottomMargin
                        }
                    }
                }
                is DefaultObjectRelationValueView.Phone -> {
                    if (relation.phone != null) {
                        val view = TextView(context).apply {
                            id = generateViewId()
                            isSingleLine = true
                            maxLines = 1
                            ellipsize = TextUtils.TruncateAt.END
                            text = relation.phone
                            setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
                            setTextColor(defaultTextColor)
                        }
                        addView(view)
                        view.updateLayoutParams<LayoutParams> {
                            marginStart = firstItemMargin
                            marginEnd = firstItemMargin
                            bottomMargin = defaultBottomMargin
                        }
                    }
                }
                is DefaultObjectRelationValueView.Date -> {
                    if (relation.timeInMillis != null) {
                        val view = TextView(context).apply {
                            id = generateViewId()
                            isSingleLine = true
                            maxLines = 1
                            ellipsize = TextUtils.TruncateAt.END
                            text = relation.timeInMillis?.formatTimestamp(
                                isMillis = true,
                                format = relation.dateFormat
                            )
                            setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
                            setTextColor(defaultTextColor)
                        }
                        addView(view)
                        view.updateLayoutParams<LayoutParams> {
                            marginStart = firstItemMargin
                            marginEnd = firstItemMargin
                            bottomMargin = defaultBottomMargin
                        }
                    }
                }
                is DefaultObjectRelationValueView.Status -> {
                    val status = relation.status.firstOrNull()
                    if (status != null) {
                        val color = ThemeColor.values().find { v -> v.code == status.color }
                        val defaultTextColor = resources.getColor(R.color.text_primary, null)
                        val view = TextView(context).apply {
                            id = generateViewId()
                            text = status.status
                            isSingleLine = true
                            maxLines = 1
                            setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
                            ellipsize = TextUtils.TruncateAt.END
                            if (color != null) {
                                setTextColor(resources.dark(color, defaultTextColor))
                            } else {
                                setTextColor(defaultTextColor)
                            }
                        }
                        addView(view)
                        view.updateLayoutParams<LayoutParams> {
                            marginStart = firstItemMargin
                            bottomMargin = defaultBottomMargin * 2
                        }
                    }
                }
                is DefaultObjectRelationValueView.Checkbox -> {
                    val image = ImageView(context)
                    if (relation.isChecked) {
                        image.setImageResource(R.drawable.ic_checkbox_gallery_view_checked)
                    } else {
                        image.setImageResource(R.drawable.ic_checkbox_gallery_view_unchecked)
                    }
                    addView(image)
                    image.updateLayoutParams<LayoutParams> {
                        width = resources.getDimension(R.dimen.dp_16).toInt()
                        height = resources.getDimension(R.dimen.dp_16).toInt()
                        marginStart = firstItemMargin
                        bottomMargin = defaultBottomMargin * 2
                    }
                }
                is DefaultObjectRelationValueView.Object -> {
                    if (relation.objects.isNotEmpty()) {
                        val group = LinearLayout(context).apply {
                            id = generateViewId()
                            orientation = HORIZONTAL
                        }
                        relation.objects.forEachIndexed { idx, obj ->
                            if (obj is ObjectView.Default) {
                                when(val icon = obj.icon) {
                                    is ObjectIcon.None -> {
                                        // Do nothing
                                    }
                                    is ObjectIcon.Basic.Avatar -> {
                                        // Do nothing
                                    }
                                    is ObjectIcon.Basic.Emoji -> {
                                        try {
                                            val image = ImageView(context)
                                            group.addView(image)
                                            image.updateLayoutParams<LayoutParams> {
                                                width = resources.getDimension(R.dimen.dp_16).toInt()
                                                height = resources.getDimension(R.dimen.dp_16).toInt()
                                                gravity = Gravity.CENTER_VERTICAL
                                                marginStart = if (idx == 0) {
                                                    firstItemMargin
                                                } else {
                                                    intervalItemMargin
                                                }
                                                marginEnd = marginAfterIcon
                                            }
                                            Glide
                                                .with(this)
                                                .load(Emojifier.uri(icon.unicode))
                                                .into(image)
                                        } catch (e: Throwable) {
                                            Timber.e(e, "Error while setting emoji icon for: ${icon.unicode}")
                                        }
                                    }
                                    is ObjectIcon.Basic.Image -> {
                                        val image = ImageView(context)
                                        group.addView(image)
                                        image.updateLayoutParams<LayoutParams> {
                                            width = resources.getDimension(R.dimen.dp_16).toInt()
                                            height = resources.getDimension(R.dimen.dp_16).toInt()
                                            gravity = Gravity.CENTER_VERTICAL
                                            marginStart = if (idx == 0) {
                                                firstItemMargin
                                            } else {
                                                0
                                            }
                                            marginEnd = marginAfterIcon
                                        }
                                        Glide
                                            .with(this)
                                            .load(icon.hash)
                                            .centerCrop()
                                            .into(image)
                                    }
                                    is ObjectIcon.Profile.Avatar -> {
                                        val avatar = TextView(context).apply {
                                            gravity = Gravity.CENTER
                                            isAllCaps = true
                                            setBackgroundResource(R.drawable.circle_default_avatar_background)
                                            setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.defaultGalleryViewAvatarTextSize))
                                            typeface = ResourcesCompat.getFont(context, R.font.inter_medium)
                                            setTextColor(Color.WHITE)
                                            text = if (icon.name.isNotEmpty())
                                                icon.name.first().toString()
                                            else
                                                resources.getText(R.string.u)
                                        }
                                        group.addView(avatar)
                                        avatar.updateLayoutParams<LayoutParams> {
                                            width = resources.getDimension(R.dimen.dp_16).toInt()
                                            height = resources.getDimension(R.dimen.dp_16).toInt()
                                            marginStart = if (idx == 0) {
                                                firstItemMargin
                                            } else {
                                                0
                                            }
                                            marginEnd = marginAfterIcon
                                        }
                                    }
                                    is ObjectIcon.Profile.Image -> {
                                        val image = ImageView(context)
                                        group.addView(image)
                                        image.updateLayoutParams<LayoutParams> {
                                            width = resources.getDimension(R.dimen.dp_16).toInt()
                                            height = resources.getDimension(R.dimen.dp_16).toInt()
                                            gravity = Gravity.CENTER_VERTICAL
                                            marginStart = if (idx == 0) {
                                                firstItemMargin
                                            } else {
                                                0
                                            }
                                            marginEnd = marginAfterIcon
                                        }
                                        Glide
                                            .with(this)
                                            .load(icon.hash)
                                            .circleCrop()
                                            .into(image)
                                    }
                                    is ObjectIcon.Task -> {
                                        val image = ImageView(context).apply {
                                            if (icon.isChecked) {
                                                setImageResource(R.drawable.ic_gallery_view_task_checked)
                                            } else {
                                                setImageResource(R.drawable.ic_gallery_view_task_unchecked)
                                            }
                                        }
                                        group.addView(image)
                                        image.updateLayoutParams<LayoutParams> {
                                            width = resources.getDimension(R.dimen.dp_16).toInt()
                                            height = resources.getDimension(R.dimen.dp_16).toInt()
                                            gravity = Gravity.CENTER_VERTICAL
                                            marginStart = if (idx == 0) {
                                                firstItemMargin
                                            } else {
                                                0
                                            }
                                            marginEnd = marginAfterIcon
                                        }
                                    }
                                }

                                val noIcon = obj.icon == ObjectIcon.None || obj.icon is ObjectIcon.Basic.Avatar

                                val view = TextView(context).apply {
                                    id = generateViewId()
                                    isSingleLine = true
                                    maxLines = 1
                                    ellipsize = TextUtils.TruncateAt.END
                                    setHint(R.string.untitled)
                                    text = obj.name
                                    setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
                                    setTextColor(defaultTextColor)
                                }
                                group.addView(view)
                                view.updateLayoutParams<LayoutParams> {
                                    marginStart = if (idx == 0 && noIcon) firstItemMargin else 0
                                    marginEnd = marginAfterText
                                }
                            }
                        }
                        addView(group)
                        group.updateLayoutParams<LayoutParams> {
                            bottomMargin = defaultBottomMargin
                            marginEnd = firstItemMargin
                        }
                    }
                }
                is DefaultObjectRelationValueView.Tag -> {
                    if (relation.tags.isNotEmpty()) {
                        val group = LinearLayout(context).apply {
                            id = generateViewId()
                            orientation = HORIZONTAL
                        }
                        relation.tags.forEachIndexed { idx, tag ->
                            val color = ThemeColor.values().find { v -> v.code == tag.color }
                            val defaultTextColor = resources.getColor(R.color.text_primary, null)
                            val defaultBackground = resources.getColor(R.color.shape_primary, null)
                            val view = TextView(context).apply {
                                id = generateViewId()
                                isSingleLine = true
                                maxLines = 1
                                ellipsize = TextUtils.TruncateAt.END
                                text = tag.tag
                                setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
                                if (color != null) {
                                    setTextColor(resources.dark(color, defaultTextColor))
                                    setBackgroundResource(R.drawable.rect_dv_cell_tag_item)
                                    background.setDrawableColor(resources.light(color, defaultBackground))
                                } else {
                                    setTextColor(defaultTextColor)
                                    setBackgroundResource(R.drawable.rect_dv_cell_tag_item)
                                    background.setDrawableColor(defaultBackground)
                                }
                            }
                            group.addView(view)
                            view.updateLayoutParams<LayoutParams> {
                                marginStart = if (idx == 0) {
                                    firstItemMargin
                                } else {
                                    intervalItemMargin
                                }
                            }
                        }
                        addView(group)
                        group.updateLayoutParams<LayoutParams> {
                            bottomMargin = defaultBottomMargin
                            marginEnd = firstItemMargin
                        }
                    }
                }
                is DefaultObjectRelationValueView.File -> {
                    if (relation.files.isNotEmpty()) {
                        val group = LinearLayout(context).apply {
                            id = generateViewId()
                            orientation = HORIZONTAL
                        }
                        relation.files.forEachIndexed { idx, file ->
                            val filename = file.name + "." + file.ext
                            val view = TextView(context).apply {
                                id = generateViewId()
                                isSingleLine = true
                                maxLines = 1
                                ellipsize = TextUtils.TruncateAt.END
                                text = filename
                                setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
                                setTextColor(defaultTextColor)
                                setHint(R.string.untitled)
                            }
                            group.addView(view)
                            view.updateLayoutParams<LayoutParams> {
                                marginStart = if (idx == 0) {
                                    firstItemMargin
                                } else {
                                    intervalItemMargin
                                }
                            }
                        }
                        addView(group)
                        group.updateLayoutParams<LayoutParams> {
                            bottomMargin = defaultBottomMargin
                            marginEnd = firstItemMargin
                        }
                    }
                }
                else -> {
                    val view = TextView(context).apply {
                        id = generateViewId()
                        text = relation::class.java.simpleName
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
                    }
                    addView(view)
                    view.updateLayoutParams<LayoutParams> {
                        marginStart = firstItemMargin
                        bottomMargin = defaultBottomMargin
                    }
                }
            }
        }
    }

}
