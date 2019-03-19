package com.agileburo.anytype.feature_editor.data

import android.content.Context
import com.agileburo.anytype.feature_editor.domain.Block
import com.google.gson.Gson
import io.reactivex.Single
import io.reactivex.SingleEmitter
import javax.inject.Inject

interface EditorRepo {

    fun getBlocks(): Single<List<Block>>
}

class EditorRepoImpl @Inject constructor(
    private val context: Context,
    private val gson: Gson
) : EditorRepo {

    override fun getBlocks(): Single<List<Block>> {
        return Single.create<List<Block>> { emitter: SingleEmitter<List<Block>> ->
            try {
                val json = context.assets.open("test.json").bufferedReader().use {
                    it.readText()
                }
                val blocksResponse = gson.fromJson<BlocksResponse>(json, BlocksResponse::class.java)
                emitter.onSuccess(blocksResponse.blocks)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }
}

data class BlocksResponse(val blocks: List<Block> = emptyList())