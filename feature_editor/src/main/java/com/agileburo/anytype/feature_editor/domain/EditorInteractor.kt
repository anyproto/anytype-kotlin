package com.agileburo.anytype.feature_editor.domain

import com.agileburo.anytype.feature_editor.data.EditorRepo
import io.reactivex.Single
import javax.inject.Inject

interface EditorInteractor {

    fun getBlocks(): Single<List<Block>>
}

class EditorInteractorImpl @Inject constructor(private val repo: EditorRepo) : EditorInteractor {

    override fun getBlocks(): Single<List<Block>> =
        repo.getBlocks().flatMap { t: List<Block> -> Single.just(unwrap(t)) }
}

fun unwrap(blocks: List<Block>): List<Block> {
    val result = mutableListOf<Block>()
    if (blocks.isEmpty()) return result
    blocks.forEach {
        result.add(it.toChildless())
        if (it.children.isNotEmpty()) {
            result.addAll(unwrap(it.children))
        }
    }
    return result
}

fun Block.toChildless() =
    this.copy(
        id = this.id, content = this.content,
        parentId = this.parentId, children = emptyList()
    )