package com.agileburo.anytype.sample

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import anytype.clipboard.ClipboardOuterClass.Clipboard
import anytype.model.Models.Block
import com.agileburo.anytype.core_ui.common.ThemeColor
import kotlinx.android.synthetic.main.activity_clipboard.*

class ClipboardActivity : AppCompatActivity() {

    private val cm: ClipboardManager
        get() = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clipboard)
        setup()
    }

    private fun setup() {
        write.setOnClickListener { write() }
        read.setOnClickListener { read() }
        copy.setOnClickListener { copy() }
        paste.setOnClickListener { paste() }
    }

    private fun write() {

        val text = Block.Content.Text
            .newBuilder()
            .setText("Everything was in confusion")
            .setColor(ThemeColor.ICE.title)
            .setStyle(Block.Content.Text.Style.Checkbox)
            .build()

        val blocks = listOf(
            Block.newBuilder()
                .setId("1")
                .setText(text)
                .build(),
            Block.newBuilder()
                .setId("2")
                .setText(text)
                .build()
        )

        val clipboard = Clipboard.newBuilder().addAllBlocks(blocks).build()

        val stream = openFileOutput(DEFAULT_FILE_NAME, Context.MODE_PRIVATE)

        clipboard.writeTo(stream)

        stream.flush()

        stream.close()
    }

    private fun read() {
        val stream = openFileInput(DEFAULT_FILE_NAME)
        val board = Clipboard.parseFrom(stream)
        output.text = board.toString()
    }

    private fun copy() {
        output.clearComposingText()
        val uri = Uri.parse(BASE_URI)
        val clip = ClipData.newUri(contentResolver, "URI", uri)
        cm.setPrimaryClip(clip)
    }

    private fun paste() {
        output.text = cm.primaryClip.toString()
    }

    companion object {
        private const val DEFAULT_FILE_NAME = "test"
        private const val AUTHORITY = "com.agileburo.anytype.sample"
        private const val BASE_URI = "content://$AUTHORITY"
    }
}
