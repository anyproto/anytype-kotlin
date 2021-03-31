package com.anytypeio.anytype.presentation.page.editor

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.page.Editor
import com.anytypeio.anytype.presentation.page.editor.pattern.Matcher
import com.anytypeio.anytype.presentation.page.editor.pattern.Pattern
import com.anytypeio.anytype.presentation.page.model.TextUpdate

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
                            style = Block.Content.Text.Style.H1
                        )
                    )
                    patterns.contains(Pattern.H2) -> replaceBy(
                        context = context,
                        target = update.target,
                        prototype = Block.Prototype.Text(
                            style = Block.Content.Text.Style.H2
                        )
                    )
                    patterns.contains(Pattern.H3) -> replaceBy(
                        context = context,
                        target = update.target,
                        prototype = Block.Prototype.Text(
                            style = Block.Content.Text.Style.H3
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
                            style = Block.Content.Text.Style.TOGGLE
                        )
                    )
                    patterns.contains(Pattern.DIVIDER) -> replaceBy(
                        context = context,
                        target = update.target,
                        prototype = Block.Prototype.DividerLine
                    )
                    else -> proxies.saves.send(update)
                }
            }
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