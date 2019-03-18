package com.agileburo.anytype.feature_editor.data

import android.content.Context
import com.agileburo.anytype.feature_editor.domain.Block
import com.agileburo.anytype.feature_editor.domain.toBlockType
import com.agileburo.anytype.feature_editor.domain.toContentType
import io.reactivex.Single
import io.reactivex.SingleEmitter
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

interface EditorRepo {

    fun getBlocks(): Single<List<Block>>
}

class EditorRepoImpl @Inject constructor(
    private val context: Context
) : EditorRepo {

    override fun getBlocks(): Single<List<Block>> {
        return Single.create<List<Block>> { emitter: SingleEmitter<List<Block>> ->
            try {
                val json = context.assets.open("test.json").bufferedReader().use {
                    it.readText()
                }
                val jsonObject = JSONObject(json)
                val blocks = jsonObject.getJSONArray("blocks")?.let {
                    0.until(it.length()).map { i -> it.optJSONObject(i) }
                        .map {jsonObject -> fromJson(jsonObject) }
                }?.toList()

                emitter.onSuccess(blocks ?: emptyList())
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }
}


fun fromJson(jsonObject: JSONObject): Block = with(jsonObject) {
    var block: Block? = null
    try {
        block = Block(
            id = getString("id"),
            parentId = getString("parentId"),
            content = getString("content"),
            contentType = getInt("contentType").toContentType(),
            type = getInt("type").toBlockType()
        )
    } catch (e: JSONException) {
        e.printStackTrace()
    }
    return@with block ?: Block()
}
