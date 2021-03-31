package com.anytypeio.anytype.ui.page.modals.actions

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.widgets.GridCellFileItem
import com.anytypeio.anytype.core_ui.widgets.RelationObjectItem
import com.anytypeio.anytype.core_ui.widgets.text.TagWidget
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.page.editor.ThemeColor
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import com.anytypeio.anytype.presentation.relations.DocumentRelationView

class RelationDefaultActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Relation.Related

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        view.findViewById<FrameLayout>(R.id.root).apply {
            updateLayoutParams<FrameLayout.LayoutParams> {
                topMargin = 0
                bottomMargin = 0
            }
            setPadding(0, 0, 0, 0)
            processBackgroundColor(
                root = this,
                color = block.background,
                bgImage = backgroundView
            )
        }
        view.findViewById<ViewGroup>(R.id.content).apply {
            val paddingStart = context.dimen(R.dimen.default_document_content_padding_start)
            val paddingEnd = context.dimen(R.dimen.default_document_content_padding_end)
            updatePadding(left = paddingStart.toInt(), right = paddingEnd.toInt())
        }
        val item = block.view as DocumentRelationView.Default
        view.findViewById<TextView>(R.id.tvRelationTitle).text = item.name
        view.findViewById<TextView>(R.id.tvRelationValue).text = item.value
        setConstraints()
    }

    override fun getBlock(): BlockView = block
    override fun blockLayout(): Int = R.layout.item_block_relation_default
}

class RelationStatusActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Relation.Related

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        view.findViewById<FrameLayout>(R.id.root).apply {
            updateLayoutParams<FrameLayout.LayoutParams> {
                topMargin = 0
                bottomMargin = 0
            }
            setPadding(0, 0, 0, 0)
            processBackgroundColor(
                root = this,
                color = block.background,
                bgImage = backgroundView
            )
        }
        view.findViewById<ViewGroup>(R.id.content).apply {
            val paddingStart = context.dimen(R.dimen.default_document_content_padding_start)
            val paddingEnd = context.dimen(R.dimen.default_document_content_padding_end)
            updatePadding(left = paddingStart.toInt(), right = paddingEnd.toInt())
        }
        val item = block.view as DocumentRelationView.Status
        view.findViewById<TextView>(R.id.tvRelationTitle).text = item.name
        view.findViewById<TextView>(R.id.tvRelationValue).apply {
            if (item.status.isNotEmpty()) {
                val status = item.status.first()
                text = status.status
                val color = ThemeColor.values().find { v -> v.title == status.color }
                if (color != null) {
                    setTextColor(color.text)
                } else {
                    setTextColor(context.color(R.color.default_filter_status_text_color))
                }
            } else {
                text = null
            }
        }
        setConstraints()
    }

    override fun getBlock(): BlockView = block
    override fun blockLayout(): Int = R.layout.item_block_relation_status
}

class RelationTagActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Relation.Related

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        view.findViewById<FrameLayout>(R.id.root).apply {
            updateLayoutParams<FrameLayout.LayoutParams> {
                topMargin = 0
                bottomMargin = 0
            }
            setPadding(0, 0, 0, 0)
            processBackgroundColor(
                root = this,
                color = block.background,
                bgImage = backgroundView
            )
        }
        view.findViewById<ViewGroup>(R.id.content).apply {
            val paddingStart = context.dimen(R.dimen.default_document_content_padding_start)
            val paddingEnd = context.dimen(R.dimen.default_document_content_padding_end)
            updatePadding(left = paddingStart.toInt(), right = paddingEnd.toInt())
        }
        val item = block.view as DocumentRelationView.Tags
        view.findViewById<TextView>(R.id.tvRelationTitle).text = item.name
        item.tags.forEachIndexed { index, tag ->
            when (index) {
                0 -> view.findViewById<TagWidget>(R.id.tag0).apply { setup(tag.tag, tag.color) }
                1 -> view.findViewById<TagWidget>(R.id.tag1).apply { setup(tag.tag, tag.color) }
                2 -> view.findViewById<TagWidget>(R.id.tag2).apply { setup(tag.tag, tag.color) }
                3 -> view.findViewById<TagWidget>(R.id.tag3).apply { setup(tag.tag, tag.color) }
                4 -> view.findViewById<TagWidget>(R.id.tag4).apply { setup(tag.tag, tag.color) }
                5 -> view.findViewById<TagWidget>(R.id.tag5).apply { setup(tag.tag, tag.color) }
            }
        }
        setConstraints()
    }

    override fun getBlock(): BlockView = block
    override fun blockLayout(): Int = R.layout.item_block_relation_tag
}

class RelationObjectActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Relation.Related

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        view.findViewById<FrameLayout>(R.id.root).apply {
            updateLayoutParams<FrameLayout.LayoutParams> {
                topMargin = 0
                bottomMargin = 0
            }
            setPadding(0, 0, 0, 0)
            processBackgroundColor(
                root = this,
                color = block.background,
                bgImage = backgroundView
            )
        }
        view.findViewById<ViewGroup>(R.id.content).apply {
            val paddingStart = context.dimen(R.dimen.default_document_content_padding_start)
            val paddingEnd = context.dimen(R.dimen.default_document_content_padding_end)
            updatePadding(left = paddingStart.toInt(), right = paddingEnd.toInt())
        }
        val item = block.view as DocumentRelationView.Object
        view.findViewById<TextView>(R.id.tvRelationTitle).text = item.name
        item.objects.forEachIndexed { index, objectView ->
            when (index) {
                0 -> view.findViewById<RelationObjectItem>(R.id.obj0).apply {
                    visible()
                    setup(objectView.name, objectView.emoji, objectView.image)
                }
                1 -> view.findViewById<RelationObjectItem>(R.id.obj1).apply {
                    visible()
                    setup(objectView.name, objectView.emoji, objectView.image)
                }
                2 -> view.findViewById<RelationObjectItem>(R.id.obj2).apply {
                    visible()
                    setup(objectView.name, objectView.emoji, objectView.image)
                }
                3 -> view.findViewById<RelationObjectItem>(R.id.obj3).apply {
                    visible()
                    setup(objectView.name, objectView.emoji, objectView.image)
                }
            }
        }
        setConstraints()
    }

    override fun getBlock(): BlockView = block
    override fun blockLayout(): Int = R.layout.item_block_relation_object
}

class RelationFileActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Relation.Related

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        view.findViewById<FrameLayout>(R.id.root).apply {
            updateLayoutParams<FrameLayout.LayoutParams> {
                topMargin = 0
                bottomMargin = 0
            }
            setPadding(0, 0, 0, 0)
            processBackgroundColor(
                root = this,
                color = block.background,
                bgImage = backgroundView
            )
        }
        view.findViewById<ViewGroup>(R.id.content).apply {
            val paddingStart = context.dimen(R.dimen.default_document_content_padding_start)
            val paddingEnd = context.dimen(R.dimen.default_document_content_padding_end)
            updatePadding(left = paddingStart.toInt(), right = paddingEnd.toInt())
        }
        val item = block.view as DocumentRelationView.File
        view.findViewById<TextView>(R.id.tvRelationTitle).text = item.name
        item.files.forEachIndexed { index, file ->
            when (index) {
                0 -> view.findViewById<GridCellFileItem>(R.id.file0).apply {
                    setup(name = file.name, mime = file.mime)
                }
                1 -> view.findViewById<GridCellFileItem>(R.id.file1).apply {
                    setup(name = file.name, mime = file.mime)
                }
                2 -> view.findViewById<GridCellFileItem>(R.id.file2).apply {
                    setup(name = file.name, mime = file.mime)
                }
            }
        }
        setConstraints()
    }

    override fun getBlock(): BlockView = block
    override fun blockLayout(): Int = R.layout.item_block_relation_file
}