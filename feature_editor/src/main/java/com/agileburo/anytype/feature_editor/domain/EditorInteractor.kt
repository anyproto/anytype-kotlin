package com.agileburo.anytype.feature_editor.domain

import com.agileburo.anytype.feature_editor.data.EditorRepo
import io.reactivex.Single
import javax.inject.Inject

interface EditorInteractor {

    fun getBlocks(): Single<List<Block>>
    fun saveState(list: MutableList<Block>)
}

class EditorInteractorImpl @Inject constructor(private val repo: EditorRepo) : EditorInteractor {

    override fun getBlocks(): Single<List<Block>> = repo.getBlocks()

    override fun saveState(list: MutableList<Block>) {
        repo.saveState(list)
    }
}