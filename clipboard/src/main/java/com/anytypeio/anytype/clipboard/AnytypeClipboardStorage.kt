package com.anytypeio.anytype.clipboard

import android.content.Context
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.data.auth.mapper.Serializer
import com.anytypeio.anytype.data.auth.repo.clipboard.ClipboardDataStore

class AnytypeClipboardStorage(
    private val context: Context,
    private val serializer: Serializer
) : ClipboardDataStore.Storage {

    override fun persist(blocks: List<Block>) {
        val serialized = serializer.serialize(blocks)
        context.openFileOutput(CLIPBOARD_FILE_NAME, Context.MODE_PRIVATE).use {
            it.write(serialized)
            it.flush()
        }
    }

    override fun fetch(): List<Block> {
        val stream = context.openFileInput(CLIPBOARD_FILE_NAME)
        val blob = stream.use { it.readBytes() }
        return serializer.deserialize(blob)
    }

    companion object {
        const val CLIPBOARD_FILE_NAME = "anytype_clipboard"
    }
}