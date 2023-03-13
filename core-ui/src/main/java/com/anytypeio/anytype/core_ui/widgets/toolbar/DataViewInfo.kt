package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ViewDataviewInfoBinding
import com.anytypeio.anytype.core_ui.reactive.checkMainThread
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

class DataViewInfo @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    val binding = ViewDataviewInfoBinding.inflate(LayoutInflater.from(context), this, true)
    private var type: TYPE = TYPE.INIT

    fun show(type: TYPE, extra: String = "") {
        this.type = type
        visible()
        when (type) {
            TYPE.INIT -> {
                binding.title.text = null
                binding.description.text = null
                binding.button.text = ""
            }
            TYPE.COLLECTION_NO_ITEMS -> {
                binding.title.text = resources.getString(R.string.collection_no_items_title)
                binding.description.text =
                    resources.getString(R.string.collection_no_items_description)
                binding.button.text = resources.getString(R.string.collection_no_items_button)
            }
            TYPE.SET_NO_QUERY -> {
                binding.title.text = resources.getString(R.string.set_no_query_title)
                binding.description.text = resources.getString(R.string.set_no_query_description)
                binding.button.text = resources.getString(R.string.set_no_query_button)
            }
            TYPE.SET_NO_ITEMS -> {
                binding.title.text = resources.getString(R.string.set_no_items_title)
                binding.description.text =
                    resources.getString(R.string.set_no_items_description, extra)
                binding.button.text = resources.getString(R.string.set_no_items_button)
            }
        }
    }

    fun hide() {
        invisible()
    }

    fun clicks(): Flow<TYPE> = callbackFlow {
        checkMainThread()
        val listener = OnClickListener {
            trySend(this@DataViewInfo.type)
        }
        binding.button.setOnClickListener(listener)
        awaitClose { binding.button.setOnClickListener(null) }
    }.conflate()

    enum class TYPE {
        INIT,
        COLLECTION_NO_ITEMS,
        SET_NO_QUERY,
        SET_NO_ITEMS
    }
}