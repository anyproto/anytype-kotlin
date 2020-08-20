package com.agileburo.anytype.core_ui.features.editor.new

import android.content.Context
import android.os.Build
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.agileburo.anytype.core_ui.features.page.BlockTextEvent
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.ListenerType
import com.agileburo.anytype.core_ui.features.page.models.BlockTextAdapter
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

//@Config(sdk = [Build.VERSION_CODES.O])
//@RunWith(RobolectricTestRunner::class)
//open class EditorAdapterTestSetup {
//
//    val context: Context = ApplicationProvider.getApplicationContext()
//
//    fun adapter(
//        views: List<BlockView>,
//        click: (ListenerType) -> Unit = {},
//        event: (BlockTextEvent) -> Unit = {}
//    ): BlockTextAdapter = BlockTextAdapter(
//        blocks = views,
//        click = click,
//        event = event
//    )
//
//    fun recycler(adapter: BlockTextAdapter): RecyclerView {
//        return RecyclerView(context).apply {
//            layoutManager = LinearLayoutManager(context)
//            this.adapter = adapter
//        }
//    }
//
//}