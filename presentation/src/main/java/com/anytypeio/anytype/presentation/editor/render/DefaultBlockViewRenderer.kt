package com.anytypeio.anytype.presentation.editor.render

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Block.Content
import com.anytypeio.anytype.core_models.CoverType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.ext.textColor
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.domain.editor.Editor.Cursor
import com.anytypeio.anytype.domain.editor.Editor.Focus
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.BuildConfig.NESTED_DECORATION_ENABLED
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.cover.CoverColor
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.editor.editor.ext.getTextAndMarks
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.InEditor
import com.anytypeio.anytype.presentation.editor.toggle.ToggleStateHolder
import com.anytypeio.anytype.presentation.extension.getProperObjectName
import com.anytypeio.anytype.presentation.mapper.marks
import com.anytypeio.anytype.presentation.mapper.toFileView
import com.anytypeio.anytype.presentation.mapper.toPictureView
import com.anytypeio.anytype.presentation.mapper.toVideoView
import com.anytypeio.anytype.presentation.mapper.toView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.appearance.LinkAppearanceFactory
import com.anytypeio.anytype.presentation.relations.DocumentRelationView
import com.anytypeio.anytype.presentation.relations.view
import timber.log.Timber
import javax.inject.Inject
import com.anytypeio.anytype.presentation.editor.Editor.Mode as EditorMode

class DefaultBlockViewRenderer @Inject constructor(
    private val urlBuilder: UrlBuilder,
    private val toggleStateHolder: ToggleStateHolder,
    private val coverImageHashProvider: CoverImageHashProvider
) : BlockViewRenderer, ToggleStateHolder by toggleStateHolder {

    override suspend fun Map<Id, List<Block>>.render(
        mode: EditorMode,
        root: Block,
        focus: Focus,
        anchor: Id,
        indent: Int,
        details: Block.Details,
        relations: List<Relation>,
        restrictions: List<ObjectRestriction>,
        selection: Set<Id>,
        count: Int,
        objectTypes: List<ObjectType>,
        parentScheme: NestedDecorationData,
        onRenderFlag: (BlockViewRenderer.RenderFlag) -> Unit,
    ): List<BlockView> {

        val children = getValue(anchor)

        val result = mutableListOf<BlockView>()

        if (anchor == root.id) {
            root.content.let { cnt ->
                if (cnt is Content.Smart && cnt.type == SmartBlockType.ARCHIVE) {
                    result.add(
                        BlockView.Title.Archive(
                            mode = BlockView.Mode.READ,
                            id = anchor,
                            text = details.details[root.id]?.name.orEmpty()
                        )
                    )
                }
            }
        }

        var mCounter = count

        var isPreviousBlockMedia = false
        children.forEach { block ->
            when (val content = block.content) {
                is Content.Text -> {
                    isPreviousBlockMedia = false
                    when (content.style) {
                        Content.Text.Style.TITLE -> {
                            mCounter = 0
                            result.add(
                                title(
                                    mode = mode,
                                    block = block,
                                    content = content,
                                    focus = focus,
                                    root = root,
                                    details = details,
                                    restrictions = restrictions
                                )
                            )
                        }
                        Content.Text.Style.P -> {
                            mCounter = 0
                            val blockDecorationScheme = buildNestedDecorationData(
                                block = block,
                                parentScheme = parentScheme,
                                currentIndent = indent
                            )
                            result.add(
                                paragraph(
                                    mode = mode,
                                    block = block,
                                    content = content,
                                    focus = focus,
                                    indent = indent,
                                    details = details,
                                    selection = selection,
                                    schema = blockDecorationScheme
                                )
                            )
                            if (block.children.isNotEmpty()) {
                                result.addAll(
                                    render(
                                        mode = mode,
                                        root = root,
                                        focus = focus,
                                        anchor = block.id,
                                        indent = indent.inc(),
                                        details = details,
                                        relations = relations,
                                        restrictions = restrictions,
                                        selection = selection,
                                        objectTypes = objectTypes,
                                        onRenderFlag = onRenderFlag,
                                        parentScheme = blockDecorationScheme
                                    )
                                )
                            }
                        }
                        Content.Text.Style.NUMBERED -> {
                            val last =
                                result.lastOrNull { it is BlockView.Indentable && it.indent == indent }
                            mCounter = if (last is BlockView.Text.Numbered) {
                                last.number.inc()
                            } else {
                                mCounter.inc()
                            }

                            val blockDecorationScheme = buildNestedDecorationData(
                                block = block,
                                parentScheme = parentScheme,
                                currentIndent = indent
                            )

                            result.add(
                                numbered(
                                    mode = mode,
                                    block = block,
                                    content = content,
                                    number = mCounter,
                                    focus = focus,
                                    indent = indent,
                                    details = details,
                                    selection = selection,
                                    schema = blockDecorationScheme
                                )
                            )
                            if (block.children.isNotEmpty()) {
                                result.addAll(
                                    render(
                                        mode = mode,
                                        root = root,
                                        focus = focus,
                                        anchor = block.id,
                                        indent = indent.inc(),
                                        details = details,
                                        relations = relations,
                                        restrictions = restrictions,
                                        selection = selection,
                                        objectTypes = objectTypes,
                                        onRenderFlag = onRenderFlag,
                                        parentScheme = blockDecorationScheme
                                    )
                                )
                            }
                        }
                        Content.Text.Style.TOGGLE -> {
                            mCounter = 0
                            val blockDecorationScheme = buildNestedDecorationData(
                                block = block,
                                parentScheme = parentScheme,
                                currentIndent = indent
                            )
                            result.add(
                                toggle(
                                    mode = mode,
                                    block = block,
                                    content = content,
                                    indent = indent,
                                    isEmpty = block.children.isEmpty(),
                                    focus = focus,
                                    details = details,
                                    selection = selection,
                                    scheme = blockDecorationScheme
                                )
                            )
                            if (toggleStateHolder.isToggled(block.id)) {
                                result.addAll(
                                    render(
                                        mode = mode,
                                        root = root,
                                        focus = focus,
                                        anchor = block.id,
                                        indent = indent.inc(),
                                        details = details,
                                        relations = relations,
                                        restrictions = restrictions,
                                        selection = selection,
                                        onRenderFlag = onRenderFlag,
                                        parentScheme = blockDecorationScheme,
                                        objectTypes = objectTypes
                                    )
                                )
                            }
                        }
                        Content.Text.Style.H1 -> {
                            mCounter = 0
                            val blockDecorationScheme = buildNestedDecorationData(
                                block = block,
                                parentScheme = parentScheme,
                                currentIndent = indent,
                                currentDecoration = DecorationData(
                                    style = DecorationData.Style.Header.H1,
                                    background = block.backgroundColor
                                )
                            )
                            result.add(
                                headerOne(
                                    mode = mode,
                                    block = block,
                                    content = content,
                                    focus = focus,
                                    indent = indent,
                                    details = details,
                                    selection = selection,
                                    schema = blockDecorationScheme
                                )
                            )
                            if (block.children.isNotEmpty()) {
                                result.addAll(
                                    render(
                                        mode = mode,
                                        root = root,
                                        focus = focus,
                                        anchor = block.id,
                                        indent = indent.inc(),
                                        details = details,
                                        relations = relations,
                                        restrictions = restrictions,
                                        selection = selection,
                                        objectTypes = objectTypes,
                                        onRenderFlag = onRenderFlag,
                                        parentScheme = blockDecorationScheme
                                    )
                                )
                            }
                        }
                        Content.Text.Style.H2 -> {
                            mCounter = 0
                            val blockDecorationScheme = buildNestedDecorationData(
                                block = block,
                                parentScheme = parentScheme,
                                currentIndent = indent,
                                currentDecoration = DecorationData(
                                    style = DecorationData.Style.Header.H2,
                                    background = block.backgroundColor
                                )
                            )
                            result.add(
                                headerTwo(
                                    mode = mode,
                                    block = block,
                                    content = content,
                                    focus = focus,
                                    indent = indent,
                                    details = details,
                                    selection = selection,
                                    schema = blockDecorationScheme
                                )
                            )
                            if (block.children.isNotEmpty()) {
                                result.addAll(
                                    render(
                                        mode = mode,
                                        root = root,
                                        focus = focus,
                                        anchor = block.id,
                                        indent = indent.inc(),
                                        details = details,
                                        relations = relations,
                                        restrictions = restrictions,
                                        selection = selection,
                                        objectTypes = objectTypes,
                                        onRenderFlag = onRenderFlag,
                                        parentScheme = blockDecorationScheme
                                    )
                                )
                            }
                        }
                        Content.Text.Style.H3, Content.Text.Style.H4 -> {
                            mCounter = 0
                            val blockDecorationScheme = buildNestedDecorationData(
                                block = block,
                                parentScheme = parentScheme,
                                currentIndent = indent,
                                currentDecoration = DecorationData(
                                    style = DecorationData.Style.Header.H3,
                                    background = block.backgroundColor
                                )
                            )
                            result.add(
                                headerThree(
                                    mode = mode,
                                    block = block,
                                    content = content,
                                    focus = focus,
                                    indent = indent,
                                    details = details,
                                    selection = selection,
                                    schema = blockDecorationScheme
                                )
                            )
                            if (block.children.isNotEmpty()) {
                                result.addAll(
                                    render(
                                        mode = mode,
                                        root = root,
                                        focus = focus,
                                        anchor = block.id,
                                        indent = indent.inc(),
                                        details = details,
                                        relations = relations,
                                        restrictions = restrictions,
                                        selection = selection,
                                        objectTypes = objectTypes,
                                        onRenderFlag = onRenderFlag,
                                        parentScheme = blockDecorationScheme
                                    )
                                )
                            }
                        }
                        Content.Text.Style.QUOTE -> {
                            mCounter = 0
                            val normalized: NestedDecorationData = if (NESTED_DECORATION_ENABLED) {
                                normalizeNestedDecorationData(
                                    block = block,
                                    parentScheme = parentScheme
                                )
                            } else {
                                emptyMap()
                            }
                            val current = indent to DecorationData(
                                style = DecorationData.Style.Highlight(
                                    start = block.id,
                                    end = block.children.lastOrNull() ?: block.id
                                ),
                                background = block.backgroundColor
                            )
                            result.add(
                                highlight(
                                    mode = mode,
                                    block = block,
                                    content = content,
                                    focus = focus,
                                    indent = indent,
                                    details = details,
                                    selection = selection,
                                    scheme = if (NESTED_DECORATION_ENABLED) normalized else emptyMap()
                                )
                            )
                            if (block.children.isNotEmpty()) {
                                result.addAll(
                                    render(
                                        mode = mode,
                                        root = root,
                                        focus = focus,
                                        anchor = block.id,
                                        indent = indent.inc(),
                                        details = details,
                                        relations = relations,
                                        restrictions = restrictions,
                                        selection = selection,
                                        objectTypes = objectTypes,
                                        onRenderFlag = onRenderFlag,
                                        parentScheme = if (NESTED_DECORATION_ENABLED)
                                            (normalized + current)
                                        else
                                            emptyMap()
                                    )
                                )
                            }
                        }
                        Content.Text.Style.BULLET -> {
                            mCounter = 0
                            val blockDecorationScheme = buildNestedDecorationData(
                                block = block,
                                parentScheme = parentScheme,
                                currentIndent = indent
                            )
                            result.add(
                                bulleted(
                                    mode = mode,
                                    block = block,
                                    content = content,
                                    focus = focus,
                                    indent = indent,
                                    details = details,
                                    selection = selection,
                                    schema = blockDecorationScheme
                                )
                            )
                            if (block.children.isNotEmpty()) {
                                result.addAll(
                                    render(
                                        mode = mode,
                                        root = root,
                                        focus = focus,
                                        anchor = block.id,
                                        indent = indent.inc(),
                                        details = details,
                                        relations = relations,
                                        restrictions = restrictions,
                                        selection = selection,
                                        objectTypes = objectTypes,
                                        onRenderFlag = onRenderFlag,
                                        parentScheme = blockDecorationScheme
                                    )
                                )
                            }
                        }
                        Content.Text.Style.DESCRIPTION -> {
                            val detail = details.details.getOrDefault(root.id, Block.Fields.empty())
                            val featured = detail.featuredRelations ?: emptyList()
                            if (featured.contains(Relations.DESCRIPTION)) {
                                mCounter = 0
                                result.add(
                                    description(
                                        block = block,
                                        content = content,
                                        mode = mode,
                                        restrictions = restrictions,
                                        focus = focus
                                    )
                                )
                            }
                        }
                        Content.Text.Style.CHECKBOX -> {
                            mCounter = 0
                            val blockDecorationScheme = buildNestedDecorationData(
                                block = block,
                                parentScheme = parentScheme,
                                currentIndent = indent
                            )
                            result.add(
                                checkbox(
                                    mode = mode,
                                    block = block,
                                    content = content,
                                    focus = focus,
                                    indent = indent,
                                    details = details,
                                    selection = selection,
                                    schema = blockDecorationScheme
                                )
                            )
                            if (block.children.isNotEmpty()) {
                                result.addAll(
                                    render(
                                        mode = mode,
                                        root = root,
                                        focus = focus,
                                        anchor = block.id,
                                        indent = indent.inc(),
                                        details = details,
                                        relations = relations,
                                        restrictions = restrictions,
                                        selection = selection,
                                        objectTypes = objectTypes,
                                        onRenderFlag = onRenderFlag,
                                        parentScheme = blockDecorationScheme
                                    )
                                )
                            }
                        }
                        Content.Text.Style.CODE_SNIPPET -> {
                            mCounter = 0
                            // TODO Add block decoration scheme
                            result.add(
                                code(
                                    mode = mode,
                                    block = block,
                                    content = content,
                                    focus = focus,
                                    indent = indent,
                                    selection = selection
                                )
                            )
                            if (block.children.isNotEmpty()) {
                                result.addAll(
                                    render(
                                        mode = mode,
                                        root = root,
                                        focus = focus,
                                        anchor = block.id,
                                        indent = indent.inc(),
                                        details = details,
                                        relations = relations,
                                        restrictions = restrictions,
                                        selection = selection,
                                        objectTypes = objectTypes,
                                        onRenderFlag = onRenderFlag,
                                    )
                                )
                            }
                        }
                    }
                }
                is Content.Bookmark -> {
                    mCounter = 0
                    result.add(
                        bookmark(
                            mode = mode,
                            content = content,
                            block = block,
                            indent = indent,
                            selection = selection,
                            isPreviousBlockMedia = isPreviousBlockMedia
                        )
                    )
                    isPreviousBlockMedia = true
                }
                is Content.Divider -> {
                    isPreviousBlockMedia = false
                    mCounter = 0
                    result.add(
                        divider(
                            block = block,
                            content = content,
                            indent = indent,
                            mode = mode,
                            selection = selection
                        )
                    )
                }
                is Content.Link -> {
                    mCounter = 0
                    val obj = ObjectWrapper.Basic(
                        map = details.details[content.target]?.map ?: emptyMap()
                    )
                    val link = toLinks(
                        block = block,
                        content = content,
                        indent = indent,
                        obj = obj,
                        mode = mode,
                        selection = selection,
                        isPreviousBlockMedia = isPreviousBlockMedia,
                        objectTypes = objectTypes,
                    )
                    result.add(link)
                    isPreviousBlockMedia = link is BlockView.LinkToObject.Default.Card
                }
                is Content.File -> {
                    mCounter = 0
                    result.add(
                        file(
                            mode = mode,
                            content = content,
                            block = block,
                            indent = indent,
                            selection = selection,
                            isPreviousBlockMedia = isPreviousBlockMedia
                        )
                    )
                    isPreviousBlockMedia = true
                }
                is Content.Layout -> {
                    isPreviousBlockMedia = false
                    if (content.type != Content.Layout.Type.DIV) {
                        mCounter = 0
                    } else {
                        val last = result.lastOrNull()
                        if (last is BlockView.Text.Numbered) {
                            mCounter = last.number
                        }
                    }
                    result.addAll(
                        render(
                            mode = mode,
                            root = root,
                            focus = focus,
                            anchor = block.id,
                            indent = indent,
                            details = details,
                            relations = relations,
                            restrictions = restrictions,
                            selection = selection,
                            count = mCounter,
                            objectTypes = objectTypes,
                            onRenderFlag = onRenderFlag,
                        )
                    )
                }
                is Content.RelationBlock -> {
                    isPreviousBlockMedia = false
                    mCounter = 0
                    result.add(
                        relation(
                            ctx = root.id,
                            block = block,
                            content = content,
                            indent = indent,
                            details = details,
                            relations = relations,
                            urlBuilder = urlBuilder
                        )
                    )
                }
                is Content.FeaturedRelations -> {
                    isPreviousBlockMedia = false
                    mCounter = 0
                    val featured = featured(
                        ctx = root.id,
                        block = block,
                        relations = relations,
                        details = details
                    )

                    if (featured.relations.isNotEmpty()) {
                        result.add(featured)
                    }
                }
                is Content.Latex -> {
                    isPreviousBlockMedia = false
                    mCounter = 0
                    result.add(
                        latex(
                            block = block,
                            content = content,
                            indent = indent,
                            selection = selection,
                            mode = mode
                        )
                    )
                }
                is Content.TableOfContents -> {
                    isPreviousBlockMedia = false
                    mCounter = 0
                    onRenderFlag(BlockViewRenderer.RenderFlag.ContainsTableOfContents)
                    result.add(
                        toc(
                            block = block,
                            mode = mode,
                            selection = selection,
                        )
                    )
                }
                is Content.Unsupported -> {
                    isPreviousBlockMedia = false
                    mCounter = 0
                    result.add(
                        unsupported(
                            block = block,
                            indent = indent,
                            selection = selection,
                            mode = mode
                        )
                    )
                }
            }
        }

        return result
    }

    private fun paragraph(
        mode: EditorMode,
        block: Block,
        content: Content.Text,
        focus: Focus,
        indent: Int,
        details: Block.Details,
        selection: Set<Id>,
        schema: NestedDecorationData
    ): BlockView.Text.Paragraph {
        val marks = content.marks(details = details, urlBuilder = urlBuilder)
        val (normalizedText, normalizedMarks) = content.getTextAndMarks(
            details = details,
            marks = marks
        )
        return BlockView.Text.Paragraph(
            mode = if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ,
            id = block.id,
            text = normalizedText,
            marks = normalizedMarks,
            isFocused = block.id == focus.id,
            color = content.color,
            backgroundColor = block.backgroundColor,
            indent = indent,
            alignment = content.align?.toView(),
            cursor = if (block.id == focus.id) setCursor(focus, content) else null,
            isSelected = checkIfSelected(
                mode = mode,
                block = block,
                selection = selection
            ),
            decorations = schema.toBlockViewDecoration(block)
        )
    }

    private fun description(
        block: Block,
        content: Content.Text,
        mode: EditorMode,
        restrictions: List<ObjectRestriction>,
        focus: Focus
    ): BlockView.Description {
        val blockMode = if (restrictions.contains(ObjectRestriction.RELATIONS)) {
            BlockView.Mode.READ
        } else {
            if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ
        }
        return BlockView.Description(
            id = block.id,
            text = content.text,
            mode = blockMode,
            isFocused = block.id == focus.id
        )
    }

    private fun headerThree(
        mode: EditorMode,
        block: Block,
        focus: Focus,
        content: Content.Text,
        indent: Int,
        details: Block.Details,
        selection: Set<Id>,
        schema: NestedDecorationData
    ): BlockView.Text.Header.Three {
        val marks = content.marks(details = details, urlBuilder = urlBuilder)
        val (normalizedText, normalizedMarks) = content.getTextAndMarks(
            details = details,
            marks = marks
        )
        return BlockView.Text.Header.Three(
            mode = if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ,
            id = block.id,
            text = normalizedText,
            color = content.color,
            isFocused = block.id == focus.id,
            marks = normalizedMarks,
            backgroundColor = block.backgroundColor,
            indent = indent,
            alignment = content.align?.toView(),
            cursor = if (block.id == focus.id) setCursor(focus, content) else null,
            isSelected = checkIfSelected(
                mode = mode,
                block = block,
                selection = selection
            ),
            decorations = schema.toBlockViewDecoration(block)
        )
    }

    private fun headerTwo(
        mode: EditorMode,
        block: Block,
        focus: Focus,
        content: Content.Text,
        indent: Int,
        details: Block.Details,
        selection: Set<Id>,
        schema: NestedDecorationData
    ): BlockView.Text.Header.Two {
        val marks = content.marks(details = details, urlBuilder = urlBuilder)
        val (normalizedText, normalizedMarks) = content.getTextAndMarks(
            details = details,
            marks = marks
        )
        return BlockView.Text.Header.Two(
            mode = if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ,
            id = block.id,
            text = normalizedText,
            color = content.color,
            isFocused = block.id == focus.id,
            marks = normalizedMarks,
            backgroundColor = block.backgroundColor,
            indent = indent,
            alignment = content.align?.toView(),
            cursor = if (block.id == focus.id) setCursor(focus, content) else null,
            isSelected = checkIfSelected(
                mode = mode,
                block = block,
                selection = selection
            ),
            decorations = schema.toBlockViewDecoration(block)
        )
    }

    private fun headerOne(
        mode: EditorMode,
        block: Block,
        focus: Focus,
        content: Content.Text,
        indent: Int,
        details: Block.Details,
        selection: Set<Id>,
        schema: NestedDecorationData
    ): BlockView.Text.Header.One {
        val marks = content.marks(details = details, urlBuilder = urlBuilder)
        val (normalizedText, normalizedMarks) = content.getTextAndMarks(
            details = details,
            marks = marks
        )
        return BlockView.Text.Header.One(
            mode = if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ,
            id = block.id,
            text = normalizedText,
            color = content.color,
            isFocused = block.id == focus.id,
            marks = normalizedMarks,
            backgroundColor = block.backgroundColor,
            indent = indent,
            alignment = content.align?.toView(),
            cursor = if (block.id == focus.id) setCursor(focus, content) else null,
            isSelected = checkIfSelected(
                mode = mode,
                block = block,
                selection = selection
            ),
            decorations = schema.toBlockViewDecoration(block)
        )
    }

    private fun checkbox(
        mode: EditorMode,
        block: Block,
        content: Content.Text,
        focus: Focus,
        indent: Int,
        details: Block.Details,
        selection: Set<Id>,
        schema: NestedDecorationData
    ): BlockView.Text.Checkbox {
        val marks = content.marks(details = details, urlBuilder = urlBuilder)
        val (normalizedText, normalizedMarks) = content.getTextAndMarks(
            details = details,
            marks = marks
        )
        return BlockView.Text.Checkbox(
            mode = if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ,
            id = block.id,
            text = normalizedText,
            marks = normalizedMarks,
            isChecked = content.isChecked == true,
            color = content.color,
            backgroundColor = block.backgroundColor,
            isFocused = block.id == focus.id,
            indent = indent,
            cursor = if (block.id == focus.id) setCursor(focus, content) else null,
            isSelected = checkIfSelected(
                mode = mode,
                block = block,
                selection = selection
            ),
            decorations = schema.toBlockViewDecoration(block)
        )
    }

    private fun bulleted(
        mode: EditorMode,
        block: Block,
        content: Content.Text,
        focus: Focus,
        indent: Int,
        details: Block.Details,
        selection: Set<Id>,
        schema: NestedDecorationData
    ): BlockView.Text.Bulleted {
        val marks = content.marks(details = details, urlBuilder = urlBuilder)
        val (normalizedText, normalizedMarks) = content.getTextAndMarks(
            details = details,
            marks = marks
        )
        return BlockView.Text.Bulleted(
            mode = if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ,
            id = block.id,
            text = normalizedText,
            indent = indent,
            marks = normalizedMarks,
            isFocused = block.id == focus.id,
            color = content.color,
            backgroundColor = block.backgroundColor,
            cursor = if (block.id == focus.id) setCursor(focus, content) else null,
            isSelected = checkIfSelected(
                mode = mode,
                block = block,
                selection = selection
            ),
            decorations = schema.toBlockViewDecoration(block)
        )
    }

    // TODO add decoration style
    private fun code(
        mode: EditorMode,
        block: Block,
        content: Content.Text,
        focus: Focus,
        indent: Int,
        selection: Set<Id>
    ): BlockView.Code = BlockView.Code(
        mode = if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ,
        id = block.id,
        text = content.text,
        backgroundColor = block.backgroundColor,
        color = content.color,
        isFocused = block.id == focus.id,
        indent = indent,
        lang = block.fields.lang,
        isSelected = checkIfSelected(
            mode = mode,
            block = block,
            selection = selection
        )
    )

    private fun highlight(
        mode: EditorMode,
        block: Block,
        focus: Focus,
        content: Content.Text,
        indent: Int,
        details: Block.Details,
        selection: Set<Id>,
        scheme: NestedDecorationData
    ): BlockView.Text.Highlight {
        val marks = content.marks(details = details, urlBuilder = urlBuilder)
        val (normalizedText, normalizedMarks) = content.getTextAndMarks(
            details = details,
            marks = marks
        )
        val current = if (NESTED_DECORATION_ENABLED) {
            mapOf(
                indent to BlockView.Decoration(
                    background = block.backgroundColor,
                    style = BlockView.Decoration.Style.None
                )
            )
        } else {
            emptyMap()
        }
        return BlockView.Text.Highlight(
            mode = if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ,
            id = block.id,
            isFocused = block.id == focus.id,
            text = normalizedText,
            marks = normalizedMarks,
            indent = indent,
            alignment = content.align?.toView(), color = content.color,
            backgroundColor = block.backgroundColor,
            cursor = if (block.id == focus.id) setCursor(focus, content) else null,
            isSelected = checkIfSelected(
                mode = mode,
                block = block,
                selection = selection
            ),
            decorations = scheme.toBlockViewDecoration(block) + current
        )
    }

    private fun toggle(
        mode: EditorMode,
        block: Block,
        content: Content.Text,
        indent: Int,
        focus: Focus,
        isEmpty: Boolean,
        details: Block.Details,
        selection: Set<Id>,
        scheme: NestedDecorationData
    ): BlockView.Text.Toggle {
        val marks = content.marks(details = details, urlBuilder = urlBuilder)
        val (normalizedText, normalizedMarks) = content.getTextAndMarks(
            details = details,
            marks = marks
        )
        return BlockView.Text.Toggle(
            mode = if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ,
            id = block.id,
            text = normalizedText,
            marks = normalizedMarks,
            color = content.color,
            backgroundColor = block.backgroundColor,
            indent = indent,
            isFocused = block.id == focus.id,
            toggled = toggleStateHolder.isToggled(block.id),
            isEmpty = isEmpty,
            cursor = if (block.id == focus.id) setCursor(focus, content) else null,
            isSelected = checkIfSelected(
                mode = mode,
                block = block,
                selection = selection
            ),
            decorations = scheme.toBlockViewDecoration(block)
        )
    }

    private fun numbered(
        mode: EditorMode,
        block: Block,
        content: Content.Text,
        number: Int,
        focus: Focus,
        indent: Int,
        details: Block.Details,
        selection: Set<Id>,
        schema: NestedDecorationData
    ): BlockView.Text.Numbered {
        val marks = content.marks(details = details, urlBuilder = urlBuilder)
        val (normalizedText, normalizedMarks) = content.getTextAndMarks(
            details = details,
            marks = marks
        )
        return BlockView.Text.Numbered(
            mode = if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ,
            id = block.id,
            text = normalizedText,
            number = number,
            isFocused = block.id == focus.id,
            color = content.color,
            backgroundColor = block.backgroundColor,
            indent = indent,
            marks = normalizedMarks,
            cursor = if (block.id == focus.id) setCursor(focus, content) else null,
            isSelected = checkIfSelected(
                mode = mode,
                block = block,
                selection = selection
            ),
            decorations = schema.toBlockViewDecoration(block)
        )
    }

    private fun bookmark(
        mode: EditorMode,
        content: Content.Bookmark,
        block: Block,
        indent: Int,
        selection: Set<Id>,
        isPreviousBlockMedia: Boolean
    ): BlockView = content.url?.let { url ->
        if (content.title != null && content.description != null) {
            BlockView.Media.Bookmark(
                id = block.id,
                url = url,
                title = content.title,
                description = content.description,
                imageUrl = content.image?.let { urlBuilder.image(it) },
                faviconUrl = content.favicon?.let { urlBuilder.image(it) },
                indent = indent,
                mode = if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ,
                isSelected = checkIfSelected(
                    mode = mode,
                    block = block,
                    selection = selection
                ),
                backgroundColor = block.backgroundColor,
                isPreviousBlockMedia = isPreviousBlockMedia
            )
        } else {
            // TODO maybe refact: if title is null, it does not mean that we have an error state.
            BlockView.Error.Bookmark(
                id = block.id,
                url = url,
                indent = indent,
                mode = if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ,
                isSelected = checkIfSelected(
                    mode = mode,
                    block = block,
                    selection = selection
                )
            )
        }
    } ?: BlockView.MediaPlaceholder.Bookmark(
        id = block.id,
        indent = indent,
        mode = if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ,
        isSelected = checkIfSelected(
            mode = mode,
            block = block,
            selection = selection
        ),
        backgroundColor = block.backgroundColor,
        isPreviousBlockMedia = isPreviousBlockMedia
    )

    private fun divider(
        block: Block,
        content: Content.Divider,
        indent: Int,
        mode: EditorMode,
        selection: Set<Id>
    ): BlockView = when (content.style) {
        Content.Divider.Style.LINE -> BlockView.DividerLine(
            id = block.id,
            indent = indent,
            isSelected = checkIfSelected(
                mode = mode,
                block = block,
                selection = selection
            ),
            backgroundColor = block.backgroundColor
        )
        Content.Divider.Style.DOTS -> BlockView.DividerDots(
            id = block.id,
            indent = indent,
            isSelected = checkIfSelected(
                mode = mode,
                block = block,
                selection = selection
            ),
            backgroundColor = block.backgroundColor
        )
    }

    private fun file(
        mode: EditorMode,
        content: Content.File,
        block: Block,
        indent: Int,
        selection: Set<Id>,
        isPreviousBlockMedia: Boolean
    ): BlockView = when (content.type) {
        Content.File.Type.IMAGE -> content.toPictureView(
            id = block.id,
            urlBuilder = urlBuilder,
            indent = indent,
            mode = if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ,
            isSelected = checkIfSelected(
                mode = mode,
                block = block,
                selection = selection
            ),
            backgroundColor = block.backgroundColor,
            isPreviousBlockMedia = isPreviousBlockMedia
        )
        Content.File.Type.FILE -> content.toFileView(
            id = block.id,
            urlBuilder = urlBuilder,
            indent = indent,
            mode = if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ,
            isSelected = checkIfSelected(
                mode = mode,
                block = block,
                selection = selection
            ),
            backgroundColor = block.backgroundColor,
            isPrevBlockMedia = isPreviousBlockMedia,
        )
        Content.File.Type.VIDEO -> content.toVideoView(
            id = block.id,
            urlBuilder = urlBuilder,
            indent = indent,
            mode = if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ,
            isSelected = checkIfSelected(
                mode = mode,
                block = block,
                selection = selection
            ),
            backgroundColor = block.backgroundColor,
            isPrevBlockMedia = isPreviousBlockMedia
        )
        Content.File.Type.AUDIO -> content.toFileView(
            id = block.id,
            urlBuilder = urlBuilder,
            indent = indent,
            mode = if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ,
            isSelected = checkIfSelected(
                mode = mode,
                block = block,
                selection = selection
            ),
            backgroundColor = block.backgroundColor,
            isPrevBlockMedia = isPreviousBlockMedia
        )
        Content.File.Type.PDF -> content.toFileView(
            id = block.id,
            urlBuilder = urlBuilder,
            indent = indent,
            mode = if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ,
            isSelected = checkIfSelected(
                mode = mode,
                block = block,
                selection = selection
            ),
            backgroundColor = block.backgroundColor,
            isPrevBlockMedia = isPreviousBlockMedia
        )
        Content.File.Type.NONE -> content.toFileView(
            id = block.id,
            urlBuilder = urlBuilder,
            indent = indent,
            mode = if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ,
            isSelected = checkIfSelected(
                mode = mode,
                block = block,
                selection = selection
            ),
            backgroundColor = block.backgroundColor,
            isPrevBlockMedia = isPreviousBlockMedia
        )
        else -> throw IllegalStateException("Unexpected file type: ${content.type}")
    }

    private fun title(
        mode: EditorMode,
        block: Block,
        content: Content.Text,
        root: Block,
        focus: Focus,
        details: Block.Details,
        restrictions: List<ObjectRestriction>
    ): BlockView.Title {

        val cursor: Int? = if (focus.id == block.id) {
            focus.cursor?.let { crs ->
                when (crs) {
                    is Cursor.Start -> 0
                    is Cursor.End -> content.text.length
                    is Cursor.Range -> crs.range.first
                }
            }
        } else {
            null
        }

        val rootContent = root.content
        val rootDetails = details.details[root.id]

        check(rootContent is Content.Smart)

        var coverColor: CoverColor? = null
        var coverImage: Url? = null
        var coverGradient: String? = null

        when (val type = rootDetails?.coverType?.toInt()) {
            CoverType.UPLOADED_IMAGE.code -> {
                coverImage = rootDetails.coverId?.let { id ->
                    urlBuilder.image(id)
                }
            }
            CoverType.BUNDLED_IMAGE.code -> {
                val hash = rootDetails.coverId?.let { id ->
                    coverImageHashProvider.provide(id)
                }
                if (hash != null) coverImage = urlBuilder.image(hash)
            }
            CoverType.COLOR.code -> {
                coverColor = rootDetails.coverId?.let { id ->
                    CoverColor.values().find { it.code == id }
                }
            }
            CoverType.GRADIENT.code -> {
                coverGradient = rootDetails.coverId
            }
            else -> Timber.d("Missing cover type: $type")
        }

        val layoutCode = details.details[root.id]?.layout?.toInt()

        var layout = ObjectType.Layout.values().find { it.code == layoutCode }

        if (layout == null) {
            // Retrieving layout based on smart block type:
            layout = if (rootContent.type == SmartBlockType.PROFILE_PAGE)
                ObjectType.Layout.PROFILE
            else {
                // Falling back to default layout if layout is not defined
                ObjectType.Layout.BASIC
            }
        }

        val blockMode = if (restrictions.contains(ObjectRestriction.DETAILS)) {
            BlockView.Mode.READ
        } else {
            if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ
        }

        return when (layout) {
            ObjectType.Layout.BASIC -> {
                BlockView.Title.Basic(
                    mode = blockMode,
                    id = block.id,
                    text = content.text,
                    emoji = details.details[root.id]?.iconEmoji?.let { name ->
                        if (name.isNotEmpty())
                            name
                        else
                            null
                    },
                    image = details.details[root.id]?.iconImage?.let { name ->
                        if (name.isNotEmpty())
                            urlBuilder.thumbnail(name)
                        else
                            null
                    },
                    isFocused = block.id == focus.id,
                    cursor = cursor,
                    coverColor = coverColor,
                    coverImage = coverImage,
                    coverGradient = coverGradient,
                    backgroundColor = block.backgroundColor,
                    color = block.textColor()
                )
            }
            ObjectType.Layout.TODO -> {
                BlockView.Title.Todo(
                    mode = blockMode,
                    id = block.id,
                    text = content.text,
                    isFocused = block.id == focus.id,
                    cursor = cursor,
                    coverColor = coverColor,
                    coverImage = coverImage,
                    coverGradient = coverGradient,
                    isChecked = content.isChecked == true,
                    backgroundColor = block.backgroundColor,
                    color = block.textColor()
                )
            }
            ObjectType.Layout.PROFILE -> {
                BlockView.Title.Profile(
                    mode = blockMode,
                    id = block.id,
                    text = content.text,
                    image = details.details[root.id]?.iconImage?.let { name ->
                        if (name.isNotEmpty())
                            urlBuilder.thumbnail(name)
                        else
                            null
                    },
                    isFocused = block.id == focus.id,
                    cursor = cursor,
                    coverColor = coverColor,
                    coverImage = coverImage,
                    coverGradient = coverGradient,
                    backgroundColor = block.backgroundColor,
                    color = block.textColor()
                )
            }
            ObjectType.Layout.FILE, ObjectType.Layout.IMAGE -> {

                BlockView.Title.Basic(
                    mode = blockMode,
                    id = block.id,
                    text = content.text,
                    emoji = details.details[root.id]?.iconEmoji?.let { name ->
                        if (name.isNotEmpty())
                            name
                        else
                            null
                    },
                    image = details.details[root.id]?.iconImage?.let { name ->
                        if (name.isNotEmpty())
                            urlBuilder.thumbnail(name)
                        else
                            null
                    },
                    isFocused = block.id == focus.id,
                    cursor = cursor,
                    coverColor = coverColor,
                    coverImage = coverImage,
                    coverGradient = coverGradient,
                    backgroundColor = block.backgroundColor,
                    color = block.textColor()
                )
            }
            else -> throw IllegalStateException("Unexpected layout: $layout")
        }
    }

    private fun toLinks(
        block: Block,
        content: Content.Link,
        indent: Int,
        obj: ObjectWrapper.Basic,
        mode: EditorMode,
        selection: Set<Id>,
        isPreviousBlockMedia: Boolean,
        objectTypes: List<ObjectType>
    ): BlockView.LinkToObject {
        if (obj.isEmpty()) {
            return BlockView.LinkToObject.Loading(
                id = block.id,
                indent = indent
            )
        }
        val isDeleted = obj.isDeleted
        val isArchived = obj.isArchived
        return if (isDeleted == true) {
            linkDeleted(
                block = block,
                indent = indent,
                mode = mode,
                selection = selection
            )
        } else {
            if (isArchived == true) {
                linkArchive(
                    block = block,
                    indent = indent,
                    obj = obj,
                    mode = mode,
                    selection = selection
                )
            } else {
                link(
                    block = block,
                    content = content,
                    indent = indent,
                    obj = obj,
                    mode = mode,
                    selection = selection,
                    isPreviousBlockMedia = isPreviousBlockMedia,
                    objectTypes = objectTypes,
                )
            }
        }
    }

    private fun link(
        mode: Editor.Mode,
        block: Block,
        content: Content.Link,
        indent: Int,
        obj: ObjectWrapper.Basic,
        selection: Set<Id>,
        isPreviousBlockMedia: Boolean,
        objectTypes: List<ObjectType>,
    ): BlockView.LinkToObject.Default {
        val factory = LinkAppearanceFactory(content, obj.layout)
        val inEditorAppearance = factory.createInEditorLinkAppearance()
        val isCard = inEditorAppearance.isCard
        val icon = if (inEditorAppearance.showIcon) {
            ObjectIcon.from(
                obj = obj,
                layout = obj.layout,
                builder = urlBuilder
            )
        } else {
            ObjectIcon.None
        }
        val name = obj.getProperObjectName()

        return if (isCard) {
            val description = when(inEditorAppearance.description) {
                InEditor.Description.NONE -> null
                InEditor.Description.RELATION -> obj.description
                InEditor.Description.SNIPPET -> obj.snippet
            }
            val type = if (inEditorAppearance.showType) {
                val typeUrl = obj.type.firstOrNull()
                objectTypes.find { it.url == typeUrl }?.name
            } else {
                null
            }

            var coverColor: CoverColor? = null
            var coverImage: Url? = null
            var coverGradient: String? = null

            if (inEditorAppearance.showCover) {
                when (val type = obj.coverType) {
                    CoverType.UPLOADED_IMAGE -> {
                        coverImage = obj.coverId?.let { id ->
                            urlBuilder.image(id)
                        }
                    }
                    CoverType.BUNDLED_IMAGE -> {
                        val hash = obj.coverId?.let { id ->
                            coverImageHashProvider.provide(id)
                        }
                        if (hash != null) coverImage = urlBuilder.image(hash)
                    }
                    CoverType.COLOR -> {
                        coverColor = obj.coverId?.let { id ->
                            CoverColor.values().find { it.code == id }
                        }
                    }
                    CoverType.GRADIENT -> {
                        coverGradient = obj.coverId
                    }
                    else -> Timber.d("Missing cover type: $type")
                }
            }

            BlockView.LinkToObject.Default.Card(
                id = block.id,
                icon = icon,
                text = name,
                description = description,
                indent = indent,
                isSelected = checkIfSelected(
                    mode = mode,
                    block = block,
                    selection = selection
                ),
                coverColor = coverColor,
                coverImage = coverImage,
                coverGradient = coverGradient,
                backgroundColor = block.backgroundColor,
                isPreviousBlockMedia = isPreviousBlockMedia,
                objectTypeName = type
            )
        } else {
            BlockView.LinkToObject.Default.Text(
                id = block.id,
                icon = icon,
                text = name,
                indent = indent,
                isSelected = checkIfSelected(
                    mode = mode,
                    block = block,
                    selection = selection
                ),
                backgroundColor = block.backgroundColor
            )
        }
    }

    private fun linkArchive(
        block: Block,
        indent: Int,
        obj: ObjectWrapper.Basic,
        mode: EditorMode,
        selection: Set<Id>
    ): BlockView.LinkToObject.Archived = BlockView.LinkToObject.Archived(
        id = block.id,
        isEmpty = true,
        emoji = obj.iconEmoji?.let { name ->
            name.ifEmpty { null }
        },
        image = obj.iconImage?.let { name ->
            if (name.isNotEmpty())
                urlBuilder.image(name)
            else
                null
        },
        text = obj.getProperObjectName(),
        indent = indent,
        isSelected = checkIfSelected(
            mode = mode,
            block = block,
            selection = selection
        )
    )

    private fun linkDeleted(
        block: Block,
        indent: Int,
        mode: EditorMode,
        selection: Set<Id>
    ): BlockView.LinkToObject.Deleted = BlockView.LinkToObject.Deleted(
        id = block.id,
        indent = indent,
        isSelected = checkIfSelected(
            mode = mode,
            block = block,
            selection = selection
        )
    )

    private fun unsupported(
        block: Block,
        indent: Int,
        mode: EditorMode,
        selection: Set<Id>
    ) = BlockView.Unsupported(
        id = block.id,
        indent = indent,
        isSelected = checkIfSelected(
            mode = mode,
            block = block,
            selection = selection
        )
    )

    private fun latex(
        block: Block,
        content: Content.Latex,
        indent: Int,
        mode: EditorMode,
        selection: Set<Id>
    ) = BlockView.Latex(
        id = block.id,
        indent = indent,
        latex = content.latex,
        backgroundColor = block.backgroundColor,
        isSelected = checkIfSelected(
            mode = mode,
            block = block,
            selection = selection
        )
    )

    private fun toc(
        block: Block,
        mode: EditorMode,
        selection: Set<Id>
    ): BlockView.TableOfContents {
        return BlockView.TableOfContents(
            id = block.id,
            items = listOf(),
            backgroundColor = block.backgroundColor,
            isSelected = checkIfSelected(
                mode = mode,
                block = block,
                selection = selection
            )
        )
    }

    private fun relation(
        ctx: Id,
        block: Block,
        content: Content.RelationBlock,
        indent: Int,
        details: Block.Details,
        relations: List<Relation>,
        urlBuilder: UrlBuilder
    ): BlockView.Relation {
        if (content.key.isNullOrEmpty()) {
            return BlockView.Relation.Placeholder(
                id = block.id,
                indent = indent
            )
        } else {
            val relation = relations.firstOrNull { it.key == content.key }
            if (relation != null) {
                val view = relation.view(
                    details = details,
                    values = details.details[ctx]?.map ?: emptyMap(),
                    urlBuilder = urlBuilder
                )
                return if (view != null) {
                    BlockView.Relation.Related(
                        id = block.id,
                        view = view,
                        indent = indent,
                        background = block.backgroundColor
                    )
                } else {
                    BlockView.Relation.Placeholder(
                        id = block.id,
                        indent = indent
                    )
                }
            } else {
                return BlockView.Relation.Placeholder(
                    id = block.id,
                    indent = indent
                )
            }
        }
    }

    private fun featured(
        ctx: Id,
        block: Block,
        details: Block.Details,
        relations: List<Relation>
    ): BlockView.FeaturedRelation {
        val featured = details.details[ctx]?.featuredRelations ?: emptyList()
        val views = mutableListOf<DocumentRelationView>()
        views.addAll(
            mapFeaturedRelations(
                ctx = ctx,
                ids = featured,
                details = details,
                relations = relations
            )
        )
        return BlockView.FeaturedRelation(
            id = block.id,
            relations = views
        )
    }

    private fun mapFeaturedRelations(
        ctx: Id,
        ids: List<String>,
        details: Block.Details,
        relations: List<Relation>
    ): List<DocumentRelationView> = ids.mapNotNull { id ->
        when (id) {
            Relations.DESCRIPTION -> null
            Relations.TYPE -> {
                val objectTypeId = details.details[ctx]?.type?.firstOrNull()
                if (objectTypeId != null) {
                    DocumentRelationView.ObjectType(
                        relationId = id,
                        name = details.details[objectTypeId]?.name.orEmpty(),
                        isFeatured = true,
                        type = objectTypeId
                    )
                } else {
                    null
                }
            }
            else -> {
                val relation = relations.firstOrNull { it.key == id }
                relation?.view(
                    details = details,
                    values = details.details[ctx]?.map ?: emptyMap(),
                    urlBuilder = urlBuilder,
                    isFeatured = true
                )
            }
        }
    }

    private fun checkIfSelected(
        mode: Editor.Mode,
        block: Block,
        selection: Set<Id>
    ) = when (mode) {
        is EditorMode.Styling.Single -> mode.target == block.id
        is EditorMode.Styling.Multi -> mode.targets.contains(block.id)
        is EditorMode.Select -> selection.contains(block.id)
        else -> false
    }

    private fun setCursor(
        focus: Focus,
        content: Content.Text
    ): Int? = focus.cursor?.let { cursor ->
        when (cursor) {
            is Cursor.Start -> 0
            is Cursor.End -> content.text.length
            is Cursor.Range -> cursor.range.first
        }
    }
}