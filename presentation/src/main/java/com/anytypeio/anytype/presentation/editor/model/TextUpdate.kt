package com.anytypeio.anytype.presentation.editor.model

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id

/**
 * Editor text update event data.
 * @property target id of the text block, in which this change occurs
 * @property text new text for this [target]
 * @property markup markup, associated with this [text]
 */
sealed class TextUpdate {

    abstract val target: Id
    abstract val text: String
    abstract val markup: List<Block.Content.Text.Mark>

    /**
     * Default text update.
     * @property target id of the text block, in which this change occurs
     * @property text new text for this [target]
     * @property markup markup, associated with this [text]
     */
    data class Default(
        override val target: Id,
        override val text: String,
        override val markup: List<Block.Content.Text.Mark>
    ) : TextUpdate()

    /**
     * Text update qui may contain patterns we need to detect.
     * @property target id of the text block, in which this change occurs
     * @property text new text for this [target]
     * @property markup markup, associated with this [text]
     */
    data class Pattern(
        override val target: Id,
        override val text: String,
        override val markup: List<Block.Content.Text.Mark>
    ) : TextUpdate()
}