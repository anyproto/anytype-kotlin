package com.agileburo.anytype.feature_editor.data.datasource

import android.content.Context
import com.agileburo.anytype.feature_editor.data.BlockModel
import com.google.gson.Gson
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 20.03.2019.
 */
interface BlockDataSource {
    fun getBlocks(): Single<List<BlockModel>>
}

class IPFSDataSourceImpl @Inject constructor(
    private val context: Context,
    private val gson: Gson
) : BlockDataSource {

    override fun getBlocks(): Single<List<BlockModel>> {
        return Single.create<List<BlockModel>> { emitter ->
            try {
                val json = context.assets.open("test.json").bufferedReader().use {
                    it.readText()
                }

                val ipfsResponse = gson.fromJson<IpfsResponse>(json, IpfsResponse::class.java)

                emitter.onSuccess(ipfsResponse.blocks)

            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }
}

data class IpfsResponse(val blocks: List<BlockModel> = emptyList())