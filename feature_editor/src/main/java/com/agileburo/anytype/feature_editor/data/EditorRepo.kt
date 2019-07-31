package com.agileburo.anytype.feature_editor.data

import com.agileburo.anytype.feature_editor.data.datasource.BlockDataSource
import com.agileburo.anytype.feature_editor.domain.Block
import io.reactivex.Single
import javax.inject.Inject

interface EditorRepo {

    fun getBlocks(): Single<List<Block>>
    fun saveState(list: List<Block>)
}

class EditorRepoImpl @Inject constructor(
    private val dataSource: BlockDataSource,
    private val blockConverter: BlockConverter
) : EditorRepo {

    override fun getBlocks(): Single<List<Block>> {
        return dataSource.getBlocks().map { it.map(blockConverter::modelTreeToDomainTree) }
    }

    override fun saveState(list: List<Block>) {
        wrap(list)
    }

    // TODO перевести в отдельный маппер, пусть репозиторий отвечает толька за CRUD-операции
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

    //TODO предполагаем, что понадобится перевод листа блоков в дерево для отдачи беку
    private fun wrap(list: List<Block>): List<BlockModel> {
        return emptyList()
    }
}