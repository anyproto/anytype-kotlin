package com.anytypeio.anytype.presentation.editor.editor

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.editor.pattern.Matcher
import com.anytypeio.anytype.presentation.editor.editor.pattern.Pattern
import com.anytypeio.anytype.presentation.editor.editor.styling.combineWith
import com.anytypeio.anytype.presentation.editor.model.TextUpdate

interface Interactor {

    class TextInteractor(
        private val proxies: Editor.Proxer,
        private val stores: Editor.Storage,
        private val matcher: Matcher<Pattern>
    ) {

        suspend fun consume(update: TextUpdate, context: Id) {
            if (update is TextUpdate.Default)
                proxies.saves.send(update)
            else if (update is TextUpdate.Pattern) {
                val patterns = matcher.match(update.text)
                when {
                    patterns.isEmpty() -> proxies.saves.send(update)
                    patterns.contains(Pattern.NUMBERED) -> replaceBy(
                        context = context,
                        target = update.target,
                        prototype = Block.Prototype.Text(
                            style = Block.Content.Text.Style.NUMBERED
                        )
                    )
                    patterns.contains(Pattern.CHECKBOX) -> replaceBy(
                        context = context,
                        target = update.target,
                        prototype = Block.Prototype.Text(
                            style = Block.Content.Text.Style.CHECKBOX
                        )
                    )
                    patterns.contains(Pattern.BULLET) -> replaceBy(
                        context = context,
                        target = update.target,
                        prototype = Block.Prototype.Text(
                            style = Block.Content.Text.Style.BULLET
                        )
                    )
                    patterns.contains(Pattern.H1) -> replaceBy(
                        context = context,
                        target = update.target,
                        prototype = Block.Prototype.Text(
                            style = combinedStyle(
                                update = update,
                                requested = Block.Content.Text.Style.H1
                            )
                        )
                    )
                    patterns.contains(Pattern.H2) -> replaceBy(
                        context = context,
                        target = update.target,
                        prototype = Block.Prototype.Text(
                            style = combinedStyle(
                                update = update,
                                requested = Block.Content.Text.Style.H2
                            )
                        )
                    )
                    patterns.contains(Pattern.H3) -> replaceBy(
                        context = context,
                        target = update.target,
                        prototype = Block.Prototype.Text(
                            style = combinedStyle(
                                update = update,
                                requested = Block.Content.Text.Style.H3
                            )
                        )
                    )
                    patterns.contains(Pattern.QUOTE) -> replaceBy(
                        context = context,
                        target = update.target,
                        prototype = Block.Prototype.Text(
                            style = Block.Content.Text.Style.QUOTE
                        )
                    )
                    patterns.contains(Pattern.TOGGLE) -> replaceBy(
                        context = context,
                        target = update.target,
                        prototype = Block.Prototype.Text(
                            style = combinedStyle(
                                update = update,
                                requested = Block.Content.Text.Style.TOGGLE
                            )
                        )
                    )
                    patterns.contains(Pattern.DIVIDER_LINE) -> replaceBy(
                        context = context,
                        target = update.target,
                        prototype = Block.Prototype.DividerLine
                    )
                    patterns.contains(Pattern.DIVIDER_DOTS) -> replaceBy(
                        context = context,
                        target = update.target,
                        prototype = Block.Prototype.DividerDots
                    )
                    patterns.contains(Pattern.SNIPPET) -> replaceBy(
                        context = context,
                        target = update.target,
                        prototype = Block.Prototype.Text(
                            style = Block.Content.Text.Style.CODE_SNIPPET
                        )
                    )
                    else -> proxies.saves.send(update)
                }
            }
        }

        /**
         * A header shortcut in a toggle, or a toggle shortcut in a header, gives a toggle header.
         * A toggle shortcut in a toggle header keeps the toggle header.
         */
        private fun combinedStyle(
            update: TextUpdate.Pattern,
            requested: Block.Content.Text.Style
        ): Block.Content.Text.Style {
            val block = stores.document.get().find { it.id == update.target }
            val current = (block?.content as? Block.Content.Text)?.style
                ?: update.style
                ?: return requested
            val isToggleHeader = current == Block.Content.Text.Style.TOGGLE_H1
                    || current == Block.Content.Text.Style.TOGGLE_H2
                    || current == Block.Content.Text.Style.TOGGLE_H3
            return if (requested == Block.Content.Text.Style.TOGGLE && isToggleHeader)
                current
            else
                current.combineWith(requested)
        }

        private suspend fun replaceBy(
            context: Id,
            target: Id,
            prototype: Block.Prototype
        ) {
            proxies.intents.send(
                Intent.CRUD.Replace(
                    context = context,
                    target = target,
                    prototype = prototype
                )
            )
        }
    }
}