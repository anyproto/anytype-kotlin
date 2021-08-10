package com.anytypeio.anytype.presentation.editor.editor

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.CoverType
import com.anytypeio.anytype.core_models.Hash
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.const.DetailsKeys
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

interface DetailModificationManager {

    val modifications: SharedFlow<List<Modification>>

    suspend fun setEmojiIcon(target: Id, unicode: String)
    suspend fun setImageIcon(target: Id, hash: Hash)
    suspend fun removeIcon(target: Id)

    suspend fun setDocCoverColor(target: Id, color: String)
    suspend fun setDocCoverGradient(target: Id, gradient: String)
    suspend fun setDocCoverImage(target: Id, hash: Hash)
    suspend fun removeDocCover(target: Id)

    suspend fun updateRelationValue(target: Id, key: String, value: Any?)
}

class InternalDetailModificationManager(private val store: Store.Details) : DetailModificationManager {

    private val details: Block.Details get() = store.current()
    private val _modifications = MutableSharedFlow<List<Modification>>()
    override val modifications: SharedFlow<List<Modification>> = _modifications

    override suspend fun setEmojiIcon(target: Id, unicode: String) {
        val detail = details.details[target] ?: return
        val updated = detail.copy(
            map = detail.map.toMutableMap().apply {
                set(DetailsKeys.ICON_EMOJI, unicode)
                set(DetailsKeys.ICON_IMAGE, null)
            }
        )
        store.update(
            Block.Details(
                details.details.toMutableMap().apply {
                    set(target, updated)
                }
            )
        )
        _modifications.emit(listOf(ICON_UPDATED))
    }

    override suspend fun setImageIcon(target: Id, hash: Hash) {
        val detail = details.details[target] ?: return
        val updated = detail.copy(
            map = detail.map.toMutableMap().apply {
                set(DetailsKeys.ICON_EMOJI, null)
                set(DetailsKeys.ICON_IMAGE, hash)
            }
        )
        store.update(
            Block.Details(
                details.details.toMutableMap().apply {
                    set(target, updated)
                }
            )
        )
        _modifications.emit(listOf(ICON_UPDATED))
    }

    override suspend fun removeIcon(target: Id) {
        val detail = details.details[target] ?: return
        val updated = detail.copy(
            map = detail.map.toMutableMap().apply {
                set(DetailsKeys.ICON_EMOJI, null)
                set(DetailsKeys.ICON_IMAGE, null)
            }
        )
        store.update(
            Block.Details(
                details.details.toMutableMap().apply {
                    set(target, updated)
                }
            )
        )
        _modifications.emit(listOf(ICON_CLEARED))
    }

    override suspend fun setDocCoverColor(target: Id, color: String) {
        val detail = details.details[target] ?: return
        val updated = detail.copy(
            map = detail.map.toMutableMap().apply {
                set(DetailsKeys.COVER_ID, color)
                set(DetailsKeys.COVER_TYPE, CoverType.COLOR.code.toDouble())
            }
        )
        store.update(
            Block.Details(
                details.details.toMutableMap().apply {
                    set(target, updated)
                }
            )
        )
        _modifications.emit(listOf(COVER_CHANGED))
    }

    override suspend fun setDocCoverGradient(target: Id, gradient: String) {
        val detail = details.details[target] ?: return
        val updated = detail.copy(
            map = detail.map.toMutableMap().apply {
                set(DetailsKeys.COVER_ID, gradient)
                set(DetailsKeys.COVER_TYPE, CoverType.GRADIENT.code.toDouble())
            }
        )
        store.update(
            Block.Details(
                details.details.toMutableMap().apply {
                    set(target, updated)
                }
            )
        )
        _modifications.emit(listOf(COVER_CHANGED))
    }

    override suspend fun setDocCoverImage(target: Id, hash: Hash) {
        val detail = details.details[target] ?: return
        val updated = detail.copy(
            map = detail.map.toMutableMap().apply {
                set(DetailsKeys.COVER_ID, hash)
                set(DetailsKeys.COVER_TYPE, CoverType.UPLOADED_IMAGE.code.toDouble())
            }
        )
        store.update(
            Block.Details(
                details.details.toMutableMap().apply {
                    set(target, updated)
                }
            )
        )
        _modifications.emit(listOf(COVER_CHANGED))
    }

    override suspend fun removeDocCover(target: Id) {
        val detail = details.details[target] ?: return
        val updated = detail.copy(
            map = detail.map.toMutableMap().apply {
                set(DetailsKeys.COVER_ID, null)
                set(DetailsKeys.COVER_TYPE, CoverType.NONE.code.toDouble())
            }
        )
        store.update(
            Block.Details(
                details.details.toMutableMap().apply {
                    set(target, updated)
                }
            )
        )
        _modifications.emit(listOf(COVER_CLEARED))
    }

    override suspend fun updateRelationValue(target: Id, key: String, value: Any?) {
        val detail = details.details[target] ?: return
        val updated = detail.copy(
            map = detail.map.toMutableMap().apply {
                set(key, value)
            }
        )
        store.update(
            Block.Details(
                details.details.toMutableMap().apply {
                    set(target, updated)
                }
            )
        )
        _modifications.emit(listOf(RELATION_VALUE_CHANGED))
    }

    companion object {
        const val ICON_UPDATED = 1
        const val ICON_CLEARED = 2
        const val COVER_CHANGED = 3
        const val COVER_CLEARED = 4
        const val RELATION_VALUE_CHANGED = 5
    }
}

typealias Modification = Int
