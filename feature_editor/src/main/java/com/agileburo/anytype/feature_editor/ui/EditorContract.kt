package com.agileburo.anytype.feature_editor.ui

import com.agileburo.anytype.feature_editor.domain.Block
import com.agileburo.anytype.feature_editor.domain.ContentType

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 21.03.2019.
 */
sealed class EditorAction {
    object PressButton : EditorAction()
    data class PressBlock(val id: String) : EditorAction()
}

sealed class EditBlockAction {
    data class TextClick(val block: Block) : EditBlockAction()
    data class Header1Click(val block: Block) : EditBlockAction()
    data class Header2Click(val block: Block) : EditBlockAction()
    data class Header3Click(val block: Block) : EditBlockAction()
    data class Header4Click(val block: Block) : EditBlockAction()
    data class HighLightClick(val block: Block) : EditBlockAction()
    data class BulletClick(val block: Block) : EditBlockAction()
    data class NumberedClick(val block: Block) : EditBlockAction()
    data class CheckBoxClick(val block: Block) : EditBlockAction()
    data class CodeClick(val block: Block) : EditBlockAction()
    data class ArchiveBlock(val id: String) : EditBlockAction()
}

sealed class EditorState {
    data class ShowToolbar(val block: Block, val typesToHide: Set<ContentType>) : EditorState()
    object HideToolbar : EditorState()
    data class Result(val blocks: List<Block>) : EditorState()
    data class Updates(val blocks : List<Block>) : EditorState()
    data class Update(val block: Block) : EditorState()
    data class Archive(val id: String): EditorState()
    data class Error(val msg: String): EditorState()
    object Loading : EditorState()
}