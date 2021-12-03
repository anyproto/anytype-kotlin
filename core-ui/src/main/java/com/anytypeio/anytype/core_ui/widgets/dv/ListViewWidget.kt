package com.anytypeio.anytype.core_ui.widgets.dv

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.sets.model.Viewer

class ListViewWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {

    var onListItemClicked: (Id) -> Unit = {}

    private val listViewAdapter = ListViewAdapter(
        onListItemClicked = { onListItemClicked(it) },
        onTaskClicked =  {}
    )

    init {
        layoutManager = LinearLayoutManager(context)
        adapter = listViewAdapter
        val divider = AppCompatResources.getDrawable(context, R.drawable.divider_relations)
        if (divider != null) {
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(divider)
                }
            )
        }
    }

    fun setViews(views: List<Viewer.ListView.Item>) {
        listViewAdapter.submitList(views)
    }
}