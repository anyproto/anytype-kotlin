package com.anytypeio.anytype.presentation.editor.render

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Block.Content
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds.BOOKMARK
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_models.ext.parseThemeTextColor
import com.anytypeio.anytype.core_models.ext.textColor
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.domain.editor.Editor.Cursor
import com.anytypeio.anytype.domain.editor.Editor.Focus
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.BuildConfig.NESTED_DECORATION_ENABLED
import com.anytypeio.anytype.presentation.editor.Editor
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
import com.anytypeio.anytype.presentation.relations.BasicObjectCoverWrapper
import com.anytypeio.anytype.presentation.relations.BlockFieldsCoverWrapper
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import com.anytypeio.anytype.presentation.relations.getCover
import com.anytypeio.anytype.presentation.relations.objectTypeRelation
import com.anytypeio.anytype.presentation.relations.view
import timber.log.Timber
import javax.inject.Inject
import com.anytypeio.anytype.presentation.editor.Editor.Mode as EditorMode

class DefaultBlockViewRenderer @Inject constructor(
    private val urlBuilder: UrlBuilder,
    private val toggleStateHolder: ToggleStateHolder,
    private val coverImageHashProvider: CoverImageHashProvider,
    private val storeOfRelations: StoreOfRelations,
    private val storeOfObjectTypes: StoreOfObjectTypes
) : BlockViewRenderer, ToggleStateHolder by toggleStateHolder {

    override suspend fun Map<Id, List<Block>>.render(
        mode: EditorMode,
        root: Block,
        focus: Focus,
        anchor: Id,
        indent: Int,
        details: Block.Details,
        relationLinks: List<RelationLink>,
        restrictions: List<ObjectRestriction>,
        selection: Set<Id>,
        count: Int,
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
                                parentScheme = parentScheme
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
                                        relationLinks = relationLinks,
                                        restrictions = restrictions,
                                        selection = selection,
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
                                parentScheme = parentScheme
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
                                        relationLinks = relationLinks,
                                        restrictions = restrictions,
                                        selection = selection,
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
                                parentScheme = parentScheme
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
                                        relationLinks = relationLinks,
                                        restrictions = restrictions,
                                        selection = selection,
                                        onRenderFlag = onRenderFlag,
                                        parentScheme = blockDecorationScheme
                                    )
                                )
                            }
                        }
                        Content.Text.Style.H1 -> {
                            mCounter = 0
                            val blockDecorationScheme = buildNestedDecorationData(
                                block = block,
                                parentScheme = parentScheme,
                                currentDecoration = DecorationData(
                                    style = DecorationData.Style.Header.H1,
                                    background = block.parseThemeBackgroundColor()
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
                                        relationLinks = relationLinks,
                                        restrictions = restrictions,
                                        selection = selection,
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
                                currentDecoration = DecorationData(
                                    style = DecorationData.Style.Header.H2,
                                    background = block.parseThemeBackgroundColor()
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
                                        relationLinks = relationLinks,
                                        restrictions = restrictions,
                                        selection = selection,
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
                                currentDecoration = DecorationData(
                                    style = DecorationData.Style.Header.H3,
                                    background = block.parseThemeBackgroundColor()
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
                                        relationLinks = relationLinks,
                                        restrictions = restrictions,
                                        selection = selection,
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
                                emptyList()
                            }
                            val current = DecorationData(
                                style = DecorationData.Style.Highlight(
                                    start = block.id,
                                    end = block.children.lastOrNull() ?: block.id
                                ),
                                background = block.parseThemeBackgroundColor()
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
                                    scheme = if (NESTED_DECORATION_ENABLED) normalized else emptyList()
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
                                        relationLinks = relationLinks,
                                        restrictions = restrictions,
                                        selection = selection,
                                        onRenderFlag = onRenderFlag,
                                        parentScheme = if (NESTED_DECORATION_ENABLED)
                                            (normalized + current)
                                        else
                                            emptyList()
                                    )
                                )
                            }
                        }
                        Content.Text.Style.BULLET -> {
                            mCounter = 0
                            val blockDecorationScheme = buildNestedDecorationData(
                                block = block,
                                parentScheme = parentScheme
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
                                        relationLinks = relationLinks,
                                        restrictions = restrictions,
                                        selection = selection,
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
                                parentScheme = parentScheme
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
                                        relationLinks = relationLinks,
                                        restrictions = restrictions,
                                        selection = selection,
                                        onRenderFlag = onRenderFlag,
                                        parentScheme = blockDecorationScheme
                                    )
                                )
                            }
                        }
                        Content.Text.Style.CODE_SNIPPET -> {
                            mCounter = 0
                            val blockDecorationScheme = buildNestedDecorationData(
                                block = block,
                                parentScheme = parentScheme,
                                currentDecoration = DecorationData(
                                    style = DecorationData.Style.Code,
                                    background = block.parseThemeBackgroundColor()
                                )
                            )
                            result.add(
                                code(
                                    mode = mode,
                                    block = block,
                                    content = content,
                                    focus = focus,
                                    indent = indent,
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
                                        relationLinks = relationLinks,
                                        restrictions = restrictions,
                                        selection = selection,
                                        onRenderFlag = onRenderFlag,
                                        parentScheme = blockDecorationScheme
                                    )
                                )
                            }
                        }
                        Content.Text.Style.CALLOUT -> {
                            mCounter = 0
                            val blockDecorationScheme: NestedDecorationData =
                                buildNestedDecorationData(
                                    block = block,
                                    parentScheme = parentScheme,
                                    currentDecoration = DecorationData(
                                        style = DecorationData.Style.Callout(
                                            start = block.id,
                                            end = block.children.lastOrNull() ?: block.id
                                        ),
                                        background = block.parseThemeBackgroundColor()
                                    )
                                )
                            result.add(
                                callout(
                                    mode = mode,
                                    block = block,
                                    content = content,
                                    focus = focus,
                                    indent = indent,
                                    details = details,
                                    selection = selection,
                                    scheme = blockDecorationScheme
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
                                        relationLinks = relationLinks,
                                        restrictions = restrictions,
                                        selection = selection,
                                        onRenderFlag = onRenderFlag,
                                        parentScheme = blockDecorationScheme
                                    )
                                )
                            }
                        }
                    }
                }
                is Content.Bookmark -> {
                    mCounter = 0
                    val blockDecorationScheme = buildNestedDecorationData(
                        block = block,
                        parentScheme = parentScheme,
                        currentDecoration = DecorationData(
                            style = DecorationData.Style.Card,
                            background = block.parseThemeBackgroundColor()
                        )
                    )
                    result.add(
                        bookmark(
                            mode = mode,
                            content = content,
                            block = block,
                            indent = indent,
                            selection = selection,
                            isPreviousBlockMedia = isPreviousBlockMedia,
                            schema = blockDecorationScheme,
                            details = details
                        )
                    )
                    isPreviousBlockMedia = true
                }
                is Content.Divider -> {
                    isPreviousBlockMedia = false
                    mCounter = 0
                    val blockDecorationScheme = buildNestedDecorationData(
                        block = block,
                        parentScheme = parentScheme,
                        currentDecoration = DecorationData(
                            style = DecorationData.Style.None,
                            background = block.parseThemeBackgroundColor()
                        )
                    )
                    result.add(
                        divider(
                            block = block,
                            content = content,
                            indent = indent,
                            mode = mode,
                            selection = selection,
                            schema = blockDecorationScheme
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
                        parentSchema = parentScheme
                    )
                    result.add(link)
                    isPreviousBlockMedia = link is BlockView.LinkToObject.Default.Card
                }
                is Content.File -> {
                    mCounter = 0
                    val blockDecorationScheme = buildNestedDecorationData(
                        block = block,
                        parentScheme = parentScheme,
                        currentDecoration = DecorationData(
                            style = if ((content.type == Content.File.Type.FILE || content.type == Content.File.Type.PDF) && content.state == Content.File.State.DONE)
                                DecorationData.Style.None
                            else
                                DecorationData.Style.Card,
                            background = block.parseThemeBackgroundColor()
                        )
                    )
                    result.add(
                        file(
                            mode = mode,
                            content = content,
                            block = block,
                            indent = indent,
                            selection = selection,
                            isPreviousBlockMedia = isPreviousBlockMedia,
                            schema = blockDecorationScheme
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
                            relationLinks = relationLinks,
                            restrictions = restrictions,
                            selection = selection,
                            count = mCounter,
                            onRenderFlag = onRenderFlag,
                        )
                    )
                }
                is Content.RelationBlock -> {
                    isPreviousBlockMedia = false
                    mCounter = 0
                    val blockDecorationScheme = buildNestedDecorationData(
                        block = block,
                        parentScheme = parentScheme
                    )
                    result.add(
                        relation(
                            ctx = root.id,
                            block = block,
                            content = content,
                            indent = indent,
                            details = details,
                            urlBuilder = urlBuilder,
                            schema = blockDecorationScheme
                        )
                    )
                }
                is Content.FeaturedRelations -> {
                    isPreviousBlockMedia = false
                    mCounter = 0
                    val featured = featured(
                        ctx = root.id,
                        block = block,
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
                is Content.Table -> {
                    isPreviousBlockMedia = false
                    mCounter = 0
                    val decorations = buildNestedDecorationData(
                        block = block,
                        parentScheme = parentScheme,
                        currentDecoration = DecorationData(
                            style = DecorationData.Style.None,
                            background = block.parseThemeBackgroundColor()
                        )
                    ).toBlockViewDecoration(block)
                    result.add(
                        table(
                            mode = mode,
                            block = block,
                            focus = focus,
                            indent = indent,
                            details = details,
                            selection = selection,
                            blocks = this,
                            decorations = decorations
                        )
                    )
                }
                is Content.DataView -> {
                    isPreviousBlockMedia = false
                    mCounter = 0
                    result.add(
                        dataView(
                            mode = mode,
                            block = block,
                            content = content,
                            details = details,
                            selection = selection,
                            schema = parentScheme
                        )
                    )
                }
                else -> {}
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
            color = content.parseThemeTextColor(),
            background = block.parseThemeBackgroundColor(),
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
            color = content.parseThemeTextColor(),
            isFocused = block.id == focus.id,
            marks = normalizedMarks,
            background = block.parseThemeBackgroundColor(),
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
            color = content.parseThemeTextColor(),
            isFocused = block.id == focus.id,
            marks = normalizedMarks,
            background = block.parseThemeBackgroundColor(),
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
            color = content.parseThemeTextColor(),
            isFocused = block.id == focus.id,
            marks = normalizedMarks,
            background = block.parseThemeBackgroundColor(),
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
            color = content.parseThemeTextColor(),
            background = block.parseThemeBackgroundColor(),
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

    fun bulleted(
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
            color = content.parseThemeTextColor(),
            background = block.parseThemeBackgroundColor(),
            cursor = if (block.id == focus.id) setCursor(focus, content) else null,
            isSelected = checkIfSelected(
                mode = mode,
                block = block,
                selection = selection
            ),
            decorations = schema.toBlockViewDecoration(block)
        )
    }

    private fun code(
        mode: EditorMode,
        block: Block,
        content: Content.Text,
        focus: Focus,
        indent: Int,
        selection: Set<Id>,
        schema: NestedDecorationData
    ): BlockView.Code = BlockView.Code(
        mode = if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ,
        id = block.id,
        text = content.text,
        background = block.parseThemeBackgroundColor(),
        color = content.parseThemeTextColor(),
        isFocused = block.id == focus.id,
        indent = indent,
        lang = block.fields.lang,
        isSelected = checkIfSelected(
            mode = mode,
            block = block,
            selection = selection
        ),
        decorations = schema.toBlockViewDecoration(block)
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
            listOf(
                BlockView.Decoration(
                    background = block.parseThemeBackgroundColor(),
                    style = BlockView.Decoration.Style.Highlight.Itself(
                        hasChildren = block.children.isNotEmpty()
                    )
                )
            )
        } else {
            emptyList()
        }
        return BlockView.Text.Highlight(
            mode = if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ,
            id = block.id,
            isFocused = block.id == focus.id,
            text = normalizedText,
            marks = normalizedMarks,
            indent = indent,
            alignment = content.align?.toView(), color = content.parseThemeTextColor(),
            background = block.parseThemeBackgroundColor(),
            cursor = if (block.id == focus.id) setCursor(focus, content) else null,
            isSelected = checkIfSelected(
                mode = mode,
                block = block,
                selection = selection
            ),
            decorations = scheme.toBlockViewDecoration(block) + current
        )
    }

    private fun callout(
        mode: EditorMode,
        block: Block,
        focus: Focus,
        content: Content.Text,
        indent: Int,
        details: Block.Details,
        selection: Set<Id>,
        scheme: NestedDecorationData
    ): BlockView.Text.Callout {
        val marks = content.marks(details = details, urlBuilder = urlBuilder)
        val (normalizedText, normalizedMarks) = content.getTextAndMarks(
            details = details,
            marks = marks
        )
        val iconImage = content.iconImage
        val iconEmoji = content.iconEmoji
        val icon = when {
            !iconImage.isNullOrBlank() -> ObjectIcon.Basic.Image(urlBuilder.thumbnail(iconImage))
            !iconEmoji.isNullOrBlank() -> ObjectIcon.Basic.Emoji(iconEmoji)
            else -> ObjectIcon.Basic.Emoji("💡")
        }
        return BlockView.Text.Callout(
            mode = if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ,
            id = block.id,
            isFocused = block.id == focus.id,
            text = normalizedText,
            marks = normalizedMarks,
            indent = indent,
            color = content.parseThemeTextColor(),
            background = block.parseThemeBackgroundColor(),
            cursor = if (block.id == focus.id) setCursor(focus, content) else null,
            isSelected = checkIfSelected(
                mode = mode,
                block = block,
                selection = selection
            ),
            decorations = scheme.toBlockViewDecoration(block),
            icon = icon
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
            color = content.parseThemeTextColor(),
            background = block.parseThemeBackgroundColor(),
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
            color = content.parseThemeTextColor(),
            background = block.parseThemeBackgroundColor(),
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
        isPreviousBlockMedia: Boolean,
        schema: NestedDecorationData,
        details: Block.Details
    ): BlockView = when (content.state) {
        Content.Bookmark.State.EMPTY -> {
            BlockView.MediaPlaceholder.Bookmark(
                id = block.id,
                indent = indent,
                mode = if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ,
                isSelected = checkIfSelected(
                    mode = mode,
                    block = block,
                    selection = selection
                ),
                background = block.parseThemeBackgroundColor(),
                isPreviousBlockMedia = isPreviousBlockMedia,
                decorations = schema.toBlockViewDecoration(block),
                isLoading = false
            )
        }
        Content.Bookmark.State.FETCHING -> {
            BlockView.Upload.Bookmark(
                id = block.id,
                indent = indent,
                mode = if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ,
                isSelected = checkIfSelected(
                    mode = mode,
                    block = block,
                    selection = selection
                ),
                background = block.parseThemeBackgroundColor(),
                decorations = schema.toBlockViewDecoration(block),
                url = content.url
            )
        }
        Content.Bookmark.State.DONE -> {
            val obj = ObjectWrapper.Bookmark(
                details.details[content.targetObjectId]?.map ?: emptyMap()
            )
            if (obj.isDeleted == true) {
                linkDeleted(
                    block = block,
                    indent = indent,
                    mode = mode,
                    selection = selection,
                    parentSchema = schema
                )
            } else if (obj.isArchived == true) {
                linkArchive(
                    block = block,
                    indent = indent,
                    mode = mode,
                    selection = selection,
                    parentSchema = schema,
                    obj = ObjectWrapper.Basic(obj.map)
                )
            } else {
                BlockView.Media.Bookmark(
                    id = block.id,
                    url = obj.source.orEmpty(),
                    title = obj.name,
                    description = obj.description,
                    imageUrl = obj.picture?.ifEmpty { null }?.let { urlBuilder.image(it) },
                    faviconUrl = obj.iconImage?.ifEmpty { null }?.let { urlBuilder.image(it) },
                    indent = indent,
                    mode = if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ,
                    isSelected = checkIfSelected(
                        mode = mode,
                        block = block,
                        selection = selection
                    ),
                    background = block.parseThemeBackgroundColor(),
                    isPreviousBlockMedia = isPreviousBlockMedia,
                    decorations = schema.toBlockViewDecoration(block)
                )
            }
        }
        Content.Bookmark.State.ERROR -> {
            BlockView.Error.Bookmark(
                id = block.id,
                url = content.url.orEmpty(),
                indent = indent,
                mode = if (mode == EditorMode.Edit) BlockView.Mode.EDIT else BlockView.Mode.READ,
                isSelected = checkIfSelected(
                    mode = mode,
                    block = block,
                    selection = selection
                ),
                decorations = schema.toBlockViewDecoration(block),
                background = block.parseThemeBackgroundColor()
            )
        }
    }

    private fun divider(
        block: Block,
        content: Content.Divider,
        indent: Int,
        mode: EditorMode,
        selection: Set<Id>,
        schema: NestedDecorationData
    ): BlockView = when (content.style) {
        Content.Divider.Style.LINE -> BlockView.DividerLine(
            id = block.id,
            indent = indent,
            isSelected = checkIfSelected(
                mode = mode,
                block = block,
                selection = selection
            ),
            background = block.parseThemeBackgroundColor(),
            decorations = schema.toBlockViewDecoration(block)
        )
        Content.Divider.Style.DOTS -> BlockView.DividerDots(
            id = block.id,
            indent = indent,
            isSelected = checkIfSelected(
                mode = mode,
                block = block,
                selection = selection
            ),
            background = block.parseThemeBackgroundColor(),
            decorations = schema.toBlockViewDecoration(block)
        )
    }

    private fun file(
        mode: EditorMode,
        content: Content.File,
        block: Block,
        indent: Int,
        selection: Set<Id>,
        isPreviousBlockMedia: Boolean,
        schema: NestedDecorationData
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
            background = block.parseThemeBackgroundColor(),
            isPreviousBlockMedia = isPreviousBlockMedia,
            decorations = schema.toBlockViewDecoration(block)
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
            background = block.parseThemeBackgroundColor(),
            isPrevBlockMedia = isPreviousBlockMedia,
            decorations = schema.toBlockViewDecoration(block)
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
            background = block.parseThemeBackgroundColor(),
            isPrevBlockMedia = isPreviousBlockMedia,
            decorations = schema.toBlockViewDecoration(block)
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
            background = block.parseThemeBackgroundColor(),
            isPrevBlockMedia = isPreviousBlockMedia,
            decorations = schema.toBlockViewDecoration(block)
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
            background = block.parseThemeBackgroundColor(),
            isPrevBlockMedia = isPreviousBlockMedia,
            decorations = schema.toBlockViewDecoration(block)
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
            background = block.parseThemeBackgroundColor(),
            isPrevBlockMedia = isPreviousBlockMedia,
            decorations = schema.toBlockViewDecoration(block)
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

        val coverContainer = BlockFieldsCoverWrapper(rootDetails)
            .getCover(urlBuilder, coverImageHashProvider)

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
                        name.ifEmpty { null }
                    },
                    image = details.details[root.id]?.iconImage?.let { name ->
                        if (name.isNotEmpty())
                            urlBuilder.thumbnail(name)
                        else
                            null
                    },
                    isFocused = block.id == focus.id,
                    cursor = cursor,
                    coverColor = coverContainer.coverColor,
                    coverImage = coverContainer.coverImage,
                    coverGradient = coverContainer.coverGradient,
                    background = block.parseThemeBackgroundColor(),
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
                    coverColor = coverContainer.coverColor,
                    coverImage = coverContainer.coverImage,
                    coverGradient = coverContainer.coverGradient,
                    isChecked = content.isChecked == true,
                    background = block.parseThemeBackgroundColor(),
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
                    coverColor = coverContainer.coverColor,
                    coverImage = coverContainer.coverImage,
                    coverGradient = coverContainer.coverGradient,
                    background = block.parseThemeBackgroundColor(),
                    color = block.textColor()
                )
            }
            ObjectType.Layout.FILE, ObjectType.Layout.IMAGE, ObjectType.Layout.BOOKMARK -> {
                BlockView.Title.Basic(
                    mode = blockMode,
                    id = block.id,
                    text = content.text,
                    emoji = details.details[root.id]?.iconEmoji?.let { name ->
                        name.ifEmpty { null }
                    },
                    image = details.details[root.id]?.iconImage?.let { image ->
                        if (image.isNotEmpty() && layout != ObjectType.Layout.BOOKMARK)
                            urlBuilder.thumbnail(image)
                        else
                            null
                    },
                    isFocused = block.id == focus.id,
                    cursor = cursor,
                    coverColor = coverContainer.coverColor,
                    coverImage = coverContainer.coverImage,
                    coverGradient = coverContainer.coverGradient,
                    background = block.parseThemeBackgroundColor(),
                    color = block.textColor()
                )
            }

            else -> throw IllegalStateException("Unexpected layout: $layout")
        }
    }

    private suspend fun toLinks(
        block: Block,
        content: Content.Link,
        indent: Int,
        obj: ObjectWrapper.Basic,
        mode: EditorMode,
        selection: Set<Id>,
        isPreviousBlockMedia: Boolean,
        parentSchema: NestedDecorationData
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
                selection = selection,
                parentSchema = parentSchema
            )
        } else {
            if (isArchived == true) {
                linkArchive(
                    block = block,
                    indent = indent,
                    obj = obj,
                    mode = mode,
                    selection = selection,
                    parentSchema = parentSchema
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
                    parentSchema = parentSchema
                )
            }
        }
    }

    private suspend fun link(
        mode: Editor.Mode,
        block: Block,
        content: Content.Link,
        indent: Int,
        obj: ObjectWrapper.Basic,
        selection: Set<Id>,
        isPreviousBlockMedia: Boolean,
        parentSchema: NestedDecorationData
    ): BlockView.LinkToObject.Default {
        val factory = LinkAppearanceFactory(content, obj.layout)
        val inEditorAppearance = factory.createInEditorLinkAppearance()
        val isCard = inEditorAppearance.isCard
        val objectIcon = if (inEditorAppearance.showIcon) {
            ObjectIcon.getEditorLinkToObjectIcon(
                obj = obj,
                layout = obj.layout,
                builder = urlBuilder
            )
        } else {
            ObjectIcon.None
        }

        val name = obj.getProperObjectName()

        val description = when (inEditorAppearance.description) {
            InEditor.Description.NONE -> null
            InEditor.Description.RELATION -> obj.description
            InEditor.Description.SNIPPET -> obj.snippet
        }

        val objectTypeName = if (inEditorAppearance.showType) {
            val typeUrl = obj.type.firstOrNull()
            if (typeUrl != null) storeOfObjectTypes.get(typeUrl)?.name else null
        } else {
            null
        }

        val isSelected = checkIfSelected(
            mode = mode,
            block = block,
            selection = selection
        )

        val background = block.parseThemeBackgroundColor()

        return if (isCard) {
            val decorations = buildNestedDecorationData(
                block = block,
                parentScheme = parentSchema,
                currentDecoration = DecorationData(
                    style = DecorationData.Style.Card,
                    background = block.parseThemeBackgroundColor()
                )
            ).toBlockViewDecoration(block)
            linkToObjectCard(
                id = block.id,
                name = name,
                objectIcon = objectIcon,
                indent = indent,
                obj = obj,
                isSelected = isSelected,
                isPreviousBlockMedia = isPreviousBlockMedia,
                description = description,
                inEditorAppearance = inEditorAppearance,
                background = background,
                decorations = decorations,
                objectTypeName = objectTypeName
            )
        } else {
            val decorations = buildNestedDecorationData(
                block = block,
                parentScheme = parentSchema,
                currentDecoration = DecorationData(
                    style = DecorationData.Style.None,
                    background = block.parseThemeBackgroundColor()
                )
            ).toBlockViewDecoration(block)
            BlockView.LinkToObject.Default.Text(
                id = block.id,
                icon = objectIcon,
                text = name,
                indent = indent,
                isSelected = isSelected,
                background = background,
                decorations = decorations,
                description = description,
                objectTypeName = objectTypeName
            )
        }
    }

    private fun linkToObjectCardCover(
        obj: ObjectWrapper.Basic,
        urlBuilder: UrlBuilder
    ): BlockView.LinkToObject.Default.Card.Cover? {
        val coverContainer =
            BasicObjectCoverWrapper(obj).getCover(urlBuilder, coverImageHashProvider)

        if (coverContainer.coverImage != null)
            return BlockView.LinkToObject.Default.Card.Cover.Image(coverContainer.coverImage)

        if (coverContainer.coverGradient != null)
            return BlockView.LinkToObject.Default.Card.Cover.Gradient(coverContainer.coverGradient)

        if (coverContainer.coverColor != null)
            return BlockView.LinkToObject.Default.Card.Cover.Color(coverContainer.coverColor)

        return null
    }

    private fun linkToObjectCard(
        id: Id,
        name: String?,
        objectIcon: ObjectIcon,
        indent: Int,
        obj: ObjectWrapper.Basic,
        isSelected: Boolean,
        isPreviousBlockMedia: Boolean,
        description: String?,
        inEditorAppearance: InEditor,
        background: ThemeColor,
        decorations: List<BlockView.Decoration>,
        objectTypeName: String?
    ): BlockView.LinkToObject.Default.Card {
        val isWithCover = inEditorAppearance.showCover
        val iconSize = inEditorAppearance.icon
        return if (isWithCover) {
            val cover = linkToObjectCardCover(obj, urlBuilder)
            if (obj.layout != ObjectType.Layout.TODO && iconSize == InEditor.Icon.MEDIUM) {
                BlockView.LinkToObject.Default.Card.MediumIconCover(
                    id = id,
                    text = name,
                    icon = objectIcon,
                    indent = indent,
                    isSelected = isSelected,
                    description = description,
                    background = background,
                    decorations = decorations,
                    objectTypeName = objectTypeName,
                    isPreviousBlockMedia = isPreviousBlockMedia,
                    cover = cover
                )
            } else {
                BlockView.LinkToObject.Default.Card.SmallIconCover(
                    id = id,
                    text = name,
                    icon = objectIcon,
                    indent = indent,
                    isSelected = isSelected,
                    description = description,
                    background = background,
                    decorations = decorations,
                    objectTypeName = objectTypeName,
                    isPreviousBlockMedia = isPreviousBlockMedia,
                    cover = cover
                )
            }
        } else {
            if (iconSize == InEditor.Icon.MEDIUM) {
                BlockView.LinkToObject.Default.Card.MediumIcon(
                    id = id,
                    text = name,
                    icon = objectIcon,
                    indent = indent,
                    isSelected = isSelected,
                    description = description,
                    background = background,
                    decorations = decorations,
                    objectTypeName = objectTypeName,
                    isPreviousBlockMedia = isPreviousBlockMedia
                )
            } else {
                BlockView.LinkToObject.Default.Card.SmallIcon(
                    id = id,
                    text = name,
                    icon = objectIcon,
                    indent = indent,
                    isSelected = isSelected,
                    description = description,
                    background = background,
                    decorations = decorations,
                    objectTypeName = objectTypeName,
                    isPreviousBlockMedia = isPreviousBlockMedia
                )
            }
        }
    }

    private fun linkArchive(
        block: Block,
        indent: Int,
        obj: ObjectWrapper.Basic,
        mode: EditorMode,
        selection: Set<Id>,
        parentSchema: NestedDecorationData
    ): BlockView.LinkToObject.Archived = BlockView.LinkToObject.Archived(
        id = block.id,
        isEmpty = true,
        emoji = null,
        image = null,
        text = obj.getProperObjectName(),
        indent = indent,
        isSelected = checkIfSelected(
            mode = mode,
            block = block,
            selection = selection
        ),
        decorations = buildNestedDecorationData(
            block = block,
            parentScheme = parentSchema,
            currentDecoration = DecorationData(
                style = DecorationData.Style.None,
                background = block.parseThemeBackgroundColor()
            )
        ).toBlockViewDecoration(block)
    )

    private fun linkDeleted(
        block: Block,
        indent: Int,
        mode: EditorMode,
        selection: Set<Id>,
        parentSchema: NestedDecorationData
    ): BlockView.LinkToObject.Deleted = BlockView.LinkToObject.Deleted(
        id = block.id,
        indent = indent,
        isSelected = checkIfSelected(
            mode = mode,
            block = block,
            selection = selection
        ),
        decorations = buildNestedDecorationData(
            block = block,
            parentScheme = parentSchema,
            currentDecoration = DecorationData(
                style = DecorationData.Style.None,
                background = block.parseThemeBackgroundColor()
            )
        ).toBlockViewDecoration(block)
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
        background = block.parseThemeBackgroundColor(),
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
            background = block.parseThemeBackgroundColor(),
            isSelected = checkIfSelected(
                mode = mode,
                block = block,
                selection = selection
            )
        )
    }

    private fun table(
        mode: EditorMode,
        block: Block,
        focus: Focus,
        indent: Int,
        details: Block.Details,
        selection: Set<Id>,
        blocks: Map<String, List<Block>>,
        decorations: List<BlockView.Decoration>
    ): BlockView.Table {

        var cells: List<BlockView.Table.Cell> = emptyList()
        var rows: List<BlockView.Table.Row> = emptyList()
        var columns: List<BlockView.Table.Column> = emptyList()

        blocks.getValue(block.id).forEach { container ->
            val containerContent = container.content
            if (containerContent !is Content.Layout) return@forEach
            if (containerContent.type == Content.Layout.Type.TABLE_COLUMN) {
                columns = blocks.getValue(container.id).mapIndexed { index, column ->
                    BlockView.Table.Column(
                        id = BlockView.Table.ColumnId(column.id),
                        index = BlockView.Table.ColumnIndex(index)
                    )
                }
            }
            if (containerContent.type == Content.Layout.Type.TABLE_ROW) {
                rows = blocks.getValue(container.id).mapIndexed { index, row ->
                    BlockView.Table.Row(
                        id = BlockView.Table.RowId(row.id),
                        index = BlockView.Table.RowIndex(index),
                        isHeader = (row.content as? Content.TableRow)?.isHeader ?: false
                    )
                }
                cells = tableCells(
                    mode = mode,
                    focus = focus,
                    indent = indent,
                    details = details,
                    selection = selection,
                    rows = rows,
                    columns = columns,
                    blocks = blocks,
                    tableId = block.id
                )
            }
        }
        return BlockView.Table(
            id = block.id,
            columns = columns,
            rows = rows,
            cells = cells,
            isSelected = checkIfSelected(
                mode = mode,
                block = block,
                selection = selection
            ),
            background = block.parseThemeBackgroundColor(),
            selectedCellsIds = selection.toList(),
            tab = (mode as? EditorMode.Table)?.tab,
            decorations = decorations
        )
    }

    private fun tableCells(
        tableId: Id,
        blocks: Map<String, List<Block>>,
        rows: List<BlockView.Table.Row>,
        columns: List<BlockView.Table.Column>,
        mode: EditorMode,
        focus: Focus,
        indent: Int,
        details: Block.Details,
        selection: Set<Id>
    ): List<BlockView.Table.Cell> {
        val cells = mutableListOf<BlockView.Table.Cell>()
        columns.forEach { column ->
            rows.forEach { row ->
                val cell = BlockView.Table.Cell(
                    tableId = tableId,
                    row = row,
                    column = column,
                    block = null
                )
                val rowCellBlocks = blocks.getValue(row.id.value)
                val block = rowCellBlocks.firstOrNull { it.id == cell.getId() }
                val content = block?.content
                val paragraph = if (block != null && content is Content.Text) {
                    if (content.style == Content.Text.Style.P) {
                        paragraph(
                            mode = mode,
                            block = block,
                            content = content,
                            focus = focus,
                            indent = indent,
                            details = details,
                            selection = selection,
                            schema = emptyList()
                        )
                    } else {
                        Timber.w("Block should be paragraph")
                        null
                    }
                } else {
                    null
                }
                if (paragraph != null) {
                    cells.add(cell.copy(block = paragraph))
                } else {
                    cells.add(cell)
                }
            }
        }
        return cells
    }

    private suspend fun relation(
        ctx: Id,
        block: Block,
        content: Content.RelationBlock,
        indent: Int,
        details: Block.Details,
        urlBuilder: UrlBuilder,
        schema: NestedDecorationData
    ): BlockView.Relation {
        val relationKey = content.key
        if (relationKey.isNullOrEmpty()) {
            return BlockView.Relation.Placeholder(
                id = block.id,
                indent = indent,
                decorations = schema.toBlockViewDecoration(block),
                background = block.parseThemeBackgroundColor()
            )
        } else {
            val relation = storeOfRelations.getByKey(relationKey)
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
                        background = block.parseThemeBackgroundColor(),
                        decorations = schema.toBlockViewDecoration(block)
                    )
                } else {
                    BlockView.Relation.Deleted(
                        id = block.id,
                        indent = indent,
                        decorations = schema.toBlockViewDecoration(block),
                        background = block.parseThemeBackgroundColor()
                    )
                }
            } else {
                return BlockView.Relation.Deleted(
                    id = block.id,
                    indent = indent,
                    decorations = schema.toBlockViewDecoration(block),
                    background = block.parseThemeBackgroundColor()
                )
            }
        }
    }

    private suspend fun featured(
        ctx: Id,
        block: Block,
        details: Block.Details
    ): BlockView.FeaturedRelation {
        val obj = ObjectWrapper.Basic(details.details[ctx]?.map ?: emptyMap())
        val views = mapFeaturedRelations(
            ctx = ctx,
            keys = obj.featuredRelations ?: emptyList(),
            details = details
        )
        return BlockView.FeaturedRelation(
            id = block.id,
            relations = views,
            allowChangingObjectType = !obj.type.contains(BOOKMARK)
        )
    }

    private suspend fun mapFeaturedRelations(
        ctx: Id,
        keys: List<Key>,
        details: Block.Details
    ): List<ObjectRelationView> = keys.mapNotNull { key ->
        when (key) {
            Relations.DESCRIPTION -> null
            Relations.TYPE -> {
                val objectTypeId = details.details[ctx]?.type?.firstOrNull()
                if (objectTypeId != null) {
                    details.objectTypeRelation(
                        relationKey = key,
                        isFeatured = true,
                        objectTypeId = objectTypeId
                    )
                } else {
                    null
                }
            }
            else -> {
                val relation = storeOfRelations.getByKey(key)
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

    private fun dataView(
        mode: EditorMode,
        block: Block,
        content: Content.DataView,
        details: Block.Details,
        selection: Set<Id>,
        schema: NestedDecorationData
    ): BlockView.DataView {
        val targetObjectId = content.targetObjectId
        val isCollection = content.isCollection
        val isSelected = checkIfSelected(
            mode = mode,
            block = block,
            selection = selection
        )
        val background = block.parseThemeBackgroundColor()
        val decorations = buildNestedDecorationData(
            block = block,
            parentScheme = schema,
            currentDecoration = DecorationData(
                style = DecorationData.Style.Card,
                background = background
            )
        ).toBlockViewDecoration(block)
        if (targetObjectId.isBlank()) {
            return BlockView.DataView.EmptySource(
                id = block.id,
                decorations = decorations,
                isSelected = isSelected,
                background = background,
                icon = ObjectIcon.None,
                title = null,
                isCollection = isCollection
            )
        } else {
            val targetSet = ObjectWrapper.Basic(
                map = details.details[content.targetObjectId]?.map ?: emptyMap()
            )
            if (targetSet.isDeleted == true) {
                return BlockView.DataView.Deleted(
                    id = block.id,
                    decorations = decorations,
                    isSelected = isSelected,
                    background = background,
                    icon = ObjectIcon.None,
                    title = null,
                    isCollection = isCollection
                )
            }
            val icon = ObjectIcon.getEditorLinkToObjectIcon(
                obj = targetSet,
                layout = targetSet.layout,
                builder = urlBuilder
            )
            if (targetSet.setOf.isEmpty() && !content.isCollection) {
                return BlockView.DataView.EmptyData(
                    id = block.id,
                    decorations = decorations,
                    isSelected = isSelected,
                    title = targetSet.name,
                    background = background,
                    icon = icon,
                    isCollection = isCollection
                )
            } else {
                return BlockView.DataView.Default(
                    id = block.id,
                    decorations = decorations,
                    isSelected = isSelected,
                    title = targetSet.name,
                    background = background,
                    icon = icon,
                    isCollection = isCollection
                )
            }
        }
    }
}