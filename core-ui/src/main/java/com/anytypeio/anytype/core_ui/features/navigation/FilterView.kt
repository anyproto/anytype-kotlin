package com.anytypeio.anytype.core_ui.features.navigation

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.databinding.ViewPageLinksFilterBinding
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.navigation.ObjectView
import com.anytypeio.anytype.presentation.navigation.filterBy

class FilterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val recycler: RecyclerView
    private val cancel: TextView
    private val sorting: View
    private var links: MutableList<ObjectView> = mutableListOf()


    val binding = ViewPageLinksFilterBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    val inputField: EditText get() = binding.searchWidget.filterInputField

    var cancelClicked: (() -> Unit)? = null
    var pageClicked: ((String) -> Unit)? = null

    init {
        recycler = binding.recyclerView
        cancel = binding.btnCancel
        sorting = binding.icSorting
        recycler.layoutManager = LinearLayoutManager(context)
        cancel.setOnClickListener { cancelClicked?.invoke() }
        sorting.setOnClickListener { context.toast("Not implemented yet") }
        binding.searchWidget.clearSearchText.setOnClickListener {
            binding.searchWidget.filterInputField.setText(EMPTY_FILTER_TEXT)
            binding.searchWidget.clearSearchText.invisible()
        }
        binding.searchWidget.filterInputField.doAfterTextChanged { newText ->
            if (newText != null && recycler.adapter != null) {
                (recycler.adapter as PageLinksAdapter).let {
                    val filtered = links.filterBy(newText.toString())
                    it.updateLinks(filtered)
                }
            }
            if (newText.isNullOrEmpty()) {
                binding.searchWidget.clearSearchText.invisible()
            } else {
                binding.searchWidget.clearSearchText.visible()
            }
        }
    }

    fun bind(links: MutableList<ObjectView>) {
        this.links.clear()
        this.links.addAll(links)
        if (recycler.adapter == null) {
            recycler.adapter = PageLinksAdapter(
                data = links,
                onClick = { obj, layout -> pageClicked?.invoke(obj) }
            )
        } else {
            (recycler.adapter as PageLinksAdapter).updateLinks(links)
        }
    }

    companion object {
        private const val EMPTY_FILTER_TEXT = ""
    }
}