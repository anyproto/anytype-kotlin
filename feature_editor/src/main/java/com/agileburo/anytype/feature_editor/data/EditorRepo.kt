package com.agileburo.anytype.feature_editor.data

import com.agileburo.anytype.feature_editor.data.datasource.IPFSDataSource
import com.agileburo.anytype.feature_editor.domain.Block
import io.reactivex.Single
import javax.inject.Inject

interface EditorRepo {

    fun getBlocks(): Single<List<Block>>
    fun saveState(list: MutableList<Block>)
}

class EditorRepoImpl @Inject constructor(
    private val dataSource: IPFSDataSource,
    private val blockConverter: BlockConverter
) : EditorRepo {

    override fun getBlocks(): Single<List<Block>> {
        return dataSource.getBlocks()
            .flatMap { t: List<BlockModel> -> Single.just(unwrap(t)) }
    }

    override fun saveState(list: MutableList<Block>) {
        wrap(list)
    }

    private fun unwrap(blocks: List<BlockModel>): List<Block> {
        val result = mutableListOf<Block>()
        if (blocks.isEmpty()) return result
        blocks.forEach {
            result.add(blockConverter.modelToDomain(it))
            if (it.children.isNotEmpty()) {
                result.addAll(unwrap(it.children))
            }
        }
        return result
    }

    //TODO доделать функцию
    private fun wrap(list: List<Block>): List<BlockModel> {
        val models = mutableListOf<BlockModel>()
        list.forEach { models.add(blockConverter.domainToModel(it)) }
        models.forEachIndexed { index, blockModel ->
            if (blockModel.parentId.isNotEmpty()) {

            }
        }
        return emptyList()
    }
}