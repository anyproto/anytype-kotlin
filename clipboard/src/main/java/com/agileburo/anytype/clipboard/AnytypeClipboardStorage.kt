package com.agileburo.anytype.clipboard

import android.content.Context
import com.agileburo.anytype.data.auth.mapper.Serializer
import com.agileburo.anytype.data.auth.model.BlockEntity
import com.agileburo.anytype.data.auth.repo.clipboard.ClipboardDataStore

class AnytypeClipboardStorage(
    private val context: Context,
    private val serializer: Serializer
) : ClipboardDataStore.Storage {

    override fun persist(blocks: List<BlockEntity>) {
        val serialized = serializer.serialize(blocks)
        context.openFileOutput(CLIPBOARD_FILE_NAME, Context.MODE_PRIVATE).use {
            it.write(serialized)
            it.flush()
        }
    }

    override fun fetch(): List<BlockEntity> {
        val stream = context.openFileInput(CLIPBOARD_FILE_NAME)
        val blob = stream.use { it.readBytes() }
        return serializer.deserialize(blob)
    }

    companion object {
        const val CLIPBOARD_FILE_NAME = "anytype_clipboard"
    }
}