package com.agileburo.anytype.presentation.page.editor

import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Position
import com.agileburo.anytype.domain.common.Id

sealed class Intent {

    sealed class Document : Intent() {

        class Undo(
            val context: Id
        ) : Document()

        class Redo(
            val context: Id
        ) : Document()

        class UpdateTitle(
            val context: Id,
            val title: String
        ) : Document()
    }

    sealed class CRUD : Intent() {

        class Replace(
            val context: Id,
            val target: Id,
            val prototype: Block.Prototype
        ) : CRUD()

        class Create(
            val context: Id,
            val target: Id,
            val position: Position,
            val prototype: Block.Prototype
        ) : CRUD()

        class Duplicate(
            val context: Id,
            val target: Id
        ) : CRUD()

        class Unlink(
            val context: Id,
            val targets: List<Id>,
            val previous: Id?,
            val next: Id?,
            val effects: List<SideEffect> = emptyList()
        ) : CRUD()
    }

    sealed class Clipboard : Intent() {
        class Paste(
            val context: Id,
            val focus: Id,
            val selected: List<Id>,
            val range: IntRange,
            val text: String,
            val html: String?,
            val blocks: List<Block>
        ) : Clipboard()
    }

    sealed class Text : Intent() {

        class UpdateColor(
            val context: Id,
            val target: Id,
            val color: String
        ) : Text()

        class UpdateBackgroundColor(
            val context: Id,
            val targets: List<Id>,
            val color: String
        ) : Text()

        class Split(
            val context: Id,
            val target: Id,
            val index: Int
        ) : Text()

        class Merge(
            val context: Id,
            val previous: Id,
            val pair: Pair<Id, Id>
        ) : Text()

        class UpdateStyle(
            val context: Id,
            val targets: List<Id>,
            val style: Block.Content.Text.Style
        ) : Text()

        class UpdateCheckbox(
            val context: Id,
            val target: Id,
            val isChecked: Boolean
        ) : Text()

        class UpdateText(
            val context: Id,
            val target: Id,
            val text: String,
            val marks: List<Block.Content.Text.Mark>
        ) : Text()

        class Align(
            val context: Id,
            val target: Id,
            val alignment: Block.Align
        ) : Text()
    }

    sealed class Media : Intent() {

        class DownloadFile(
            val url: String,
            val name: String
        ) : Media()

    }

    sealed class Bookmark : Intent() {

        class SetupBookmark(
            val context: Id,
            val target: Id,
            val url: String
        ) : Bookmark()
    }
}