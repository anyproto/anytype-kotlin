package com.anytypeio.anytype.presentation.templates

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ext.asMap
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.render.BlockViewRenderer
import com.anytypeio.anytype.presentation.editor.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.templates.TemplateView.Companion.DEFAULT_TEMPLATE_ID_BLANK
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class TemplateBlankViewModel(
    private val renderer: DefaultBlockViewRenderer,
) : BaseViewModel(), BlockViewRenderer by renderer {

    val state = MutableStateFlow<List<BlockView>>(emptyList())
    private val TEMPLATE_TYPE_NAME = "Template"

    fun onStart(typeId: Id, typeName: String, layout: Int) {
        Timber.d("onStart, typeId: $typeId, typeName: $typeName, layout: $layout")
        val blockTitle = Block(
            id = "blockTitle",
            content = Block.Content.Text(
                text = "",
                style = Block.Content.Text.Style.TITLE,
                marks = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty(),
        )
        val featuredRelationsBlock = Block(
            id = Relations.FEATURED_RELATIONS,
            content = Block.Content.FeaturedRelations,
            children = emptyList(),
            fields = Block.Fields.empty(),
        )
        val headerBlock = if (layout != ObjectType.Layout.NOTE.code) {
            Block(
                id = "header",
                content = Block.Content.Layout(
                    type = Block.Content.Layout.Type.HEADER
                ),
                children = listOf(blockTitle.id, featuredRelationsBlock.id),
                fields = Block.Fields.empty(),
            )
        } else {
            Block(
                id = "header",
                content = Block.Content.Layout(
                    type = Block.Content.Layout.Type.HEADER
                ),
                children = listOf(featuredRelationsBlock.id),
                fields = Block.Fields.empty(),
            )
        }
        val rootBlock = Block(
            id = DEFAULT_TEMPLATE_ID_BLANK,
            content = Block.Content.Smart,
            children = listOf(headerBlock.id),
            fields = Block.Fields.empty(),
        )
        val featuredRelations = listOf(Relations.TYPE)
        val page = listOf(rootBlock, headerBlock, blockTitle, featuredRelationsBlock)
        val objectDetails = Block.Fields(
            mapOf(
                Relations.ID to DEFAULT_TEMPLATE_ID_BLANK,
                Relations.LAYOUT to layout,
                Relations.TYPE to typeId,
                Relations.FEATURED_RELATIONS to featuredRelations,
                Relations.IS_DELETED to false
            )
        )

        val typeDetails = Block.Fields(
            mapOf(
                Relations.ID to typeId,
                Relations.UNIQUE_KEY to ObjectTypeIds.TEMPLATE,
                Relations.NAME to TEMPLATE_TYPE_NAME,
                Relations.TYPE_UNIQUE_KEY to ObjectTypeIds.TEMPLATE
            )
        )

        val customDetails =
            Block.Details(mapOf(DEFAULT_TEMPLATE_ID_BLANK to objectDetails, typeId to typeDetails))

        viewModelScope.launch {
            state.value = page.asMap().render(
                mode = Editor.Mode.Read,
                root = page.first(),
                focus = com.anytypeio.anytype.domain.editor.Editor.Focus.empty(),
                anchor = page.first().id,
                indent = EditorViewModel.INITIAL_INDENT,
                details = customDetails,
                relationLinks = emptyList(),
                restrictions = emptyList(),
                selection = emptySet()
            )
        }
    }
}