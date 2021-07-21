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
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.navigation.ObjectView
import com.anytypeio.anytype.presentation.navigation.filterBy
import kotlinx.android.synthetic.main.view_page_links_filter.view.*
import kotlinx.android.synthetic.main.widget_search_view.view.*

class FilterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val recycler: RecyclerView
    private val cancel: TextView
    private val sorting: View
    private var links: MutableList<ObjectView> = mutableListOf()

    val inputField : EditText get() = filterInputField

    var cancelClicked: (() -> Unit)? = null
    var pageClicked: ((String) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_page_links_filter, this, true)
        recycler = recyclerView
        cancel = btnCancel
        sorting = icSorting
        recycler.layoutManager = LinearLayoutManager(context)
        cancel.setOnClickListener { cancelClicked?.invoke() }
        sorting.setOnClickListener { context.toast("Not implemented yet") }
        clearSearchText.setOnClickListener {
            filterInputField.setText(EMPTY_FILTER_TEXT)
            clearSearchText.invisible()
        }
        filterInputField.doAfterTextChanged { newText ->
            if (newText != null && recycler.adapter != null) {
                        (recycler.adapter as PageLinksAdapter).let {
                            val filtered = links.filterBy(newText.toString())
                            it.updateLinks(filtered)
                        }
                    }
            if (newText.isNullOrEmpty()) {
                clearSearchText.invisible()
            } else {
                clearSearchText.visible()
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