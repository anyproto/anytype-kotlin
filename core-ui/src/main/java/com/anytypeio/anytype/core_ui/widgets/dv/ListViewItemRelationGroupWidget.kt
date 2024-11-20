package com.anytypeio.anytype.core_ui.widgets.dv

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintLayout
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.getPrettyName
import com.anytypeio.anytype.core_ui.widgets.ListViewRelationObjectValueView
import com.anytypeio.anytype.core_ui.widgets.ListViewRelationTagValueView
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.relations.model.DefaultObjectRelationValueView
import com.anytypeio.anytype.presentation.sets.model.ObjectView

class ListViewItemRelationGroupWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val defaultTextColor: Int = context.resources.getColor(R.color.text_secondary, null)
    private val dividerSize: Int = context.dimen(R.dimen.dp_2).toInt()
    private val verticalGap = context.resources.getDimensionPixelSize(R.dimen.dp_4)

    fun set(relations: List<DefaultObjectRelationValueView>) {
        clear()

        val flow = Flow(context).apply {
            id = View.generateViewId()
            setWrapMode(Flow.WRAP_CHAIN)
            setHorizontalStyle(Flow.CHAIN_PACKED)
            setHorizontalBias(0f)
            setHorizontalAlign(Flow.HORIZONTAL_ALIGN_START)
            setHorizontalGap(15)
            setVerticalGap(verticalGap)
        }

        addView(flow)

        val ids = mutableListOf<Int>()

        relations.forEach { relation ->
            when (relation) {
                is DefaultObjectRelationValueView.Checkbox -> {
                    val view = View(context).apply {
                        id = generateViewId()
                        val size = context.dimen(R.dimen.dv_list_gallery_item_relation_height).toInt()
                        layoutParams = LayoutParams(size, size)
                        setBackgroundResource(R.drawable.ic_relation_checkbox_selector)
                        isSelected = relation.isChecked
                    }
                    addDotView(ids)
                    addView(view)
                    ids.add(view.id)
                }
                is DefaultObjectRelationValueView.Date -> {
                    val value = relation.relativeDate?.getPrettyName(
                        isTimeIncluded = relation.isTimeIncluded,
                        resources = resources
                    )
                    if (value != null) {
                        val view = createView(value = value)
                        addDotView(ids)
                        addView(view)
                        ids.add(view.id)
                    }
                }
                is DefaultObjectRelationValueView.File -> {
                    if (relation.files.isNotEmpty()) {
                        val file = relation.files[0]
                        val view = ListViewRelationObjectValueView(context).apply {
                            id = generateViewId()
                            setup(
                                name = "${file.name}.${file.ext}",
                                icon = ObjectIcon.None,
                                size = relation.files.size
                            )
                        }
                        addDotView(ids)
                        addView(view)
                        ids.add(view.id)
                    }
                }
                is DefaultObjectRelationValueView.Number -> {
                    val value = relation.number
                    if (!value.isNullOrBlank()) {
                        addDotView(ids)
                        val view = createView(value = value)
                        addView(view)
                        ids.add(view.id)
                    }
                }
                is DefaultObjectRelationValueView.Object -> {
                    if (relation.objects.isNotEmpty()) {
                        addDotView(ids)
                        val view = ListViewRelationObjectValueView(context).apply {
                            id = generateViewId()
                            when (val obj = relation.objects[0]) {
                                is ObjectView.Default -> {
                                    setup(
                                        name = obj.name,
                                        icon = obj.icon,
                                        size = relation.objects.size
                                    )
                                }
                                is ObjectView.Deleted -> {
                                    setupAsNonExistent(size = relation.objects.size)
                                }
                            }
                        }
                        addView(view)
                        ids.add(view.id)
                    }
                }
                is DefaultObjectRelationValueView.Phone -> {
                    val value = relation.phone
                    if (!value.isNullOrBlank()) {
                        addDotView(ids)
                        val view = createView(value = value)
                        addView(view)
                        ids.add(view.id)
                    }
                }
                is DefaultObjectRelationValueView.Status -> {
                    if (relation.status.isNotEmpty()) {
                        addDotView(ids)
                        val status = relation.status[0]
                        val color = ThemeColor.values().find { v -> v.code == status.color }
                        val view = createView(
                            value = status.status,
                            color = color
                        )
                        addView(view)
                        ids.add(view.id)
                    }
                }
                is DefaultObjectRelationValueView.Tag -> {
                    if (relation.tags.isNotEmpty()) {
                        addDotView(ids)
                        val view = ListViewRelationTagValueView(context).apply {
                            id = generateViewId()
                            val tag = relation.tags[0]
                            setup(
                                name = tag.tag,
                                tagColor = tag.color,
                                size = relation.tags.size
                            )
                        }
                        addView(view)
                        ids.add(view.id)
                    }
                }
                is DefaultObjectRelationValueView.Text -> {
                    val value = relation.text
                    if (!value.isNullOrBlank()) {
                        addDotView(ids)
                        val view = createView(value = value)
                        addView(view)
                        ids.add(view.id)
                    }
                }
                is DefaultObjectRelationValueView.Email -> {
                    val value = relation.email
                    if (!value.isNullOrBlank()) {
                        addDotView(ids)
                        val view = createView(value = value)
                        addView(view)
                        ids.add(view.id)
                    }
                }
                is DefaultObjectRelationValueView.Url -> {
                    val value = relation.url
                    if (!value.isNullOrBlank()) {
                        addDotView(ids)
                        val view = createView(value = value)
                        addView(view)
                        ids.add(view.id)
                    }
                }
                is DefaultObjectRelationValueView.Empty -> {}
            }
        }

        flow.referencedIds = ids.toIntArray()
    }

    private fun addDotView(ids: MutableList<Int>) {
        if (ids.isEmpty()) return
        val div = View(context).apply {
            id = View.generateViewId()
            layoutParams = LayoutParams(dividerSize, dividerSize)
            setBackgroundResource(R.drawable.divider_dv_viewer_list_item_relations)
        }
        addView(div)
        ids.add(div.id)
    }

    private fun createView(value: String, color: ThemeColor? = null): TextView {
        return TextView(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            setTextAppearance(R.style.TextView_ContentStyle_Relations_3)
            id = generateViewId()
            text = value
            isSingleLine = true
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            if (color != null) {
                setTextColor(resources.dark(color, defaultTextColor))
            } else {
                setTextColor(defaultTextColor)
            }
        }
    }

    fun clear() {
        removeAllViews()
    }
}