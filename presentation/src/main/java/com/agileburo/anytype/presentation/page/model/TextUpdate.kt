package com.agileburo.anytype.presentation.page.model

import com.agileburo.anytype.core_ui.features.page.pattern.Pattern
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.common.Id

/**
 * Editor text update event data.
 * @property target id of the text block, in which this change occurs
 * @property text new text for this [target]
 * @property markup markup, associated with this [text]
 * @property patterns editor patterns found in this [text]
 */
class TextUpdate(
    val target: Id,
    val text: String,
    val markup: List<Block.Content.Text.Mark>,
    val patterns: List<Pattern>
)