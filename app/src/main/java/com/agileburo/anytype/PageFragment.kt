package com.agileburo.anytype

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.agileburo.anytype.document.DocumentAdapter
import com.agileburo.anytype.document.DragAndDropBehavior
import com.agileburo.anytype.model.Block
import kotlinx.android.synthetic.main.fragment_page.*

class PageFragment : Fragment() {

    companion object {
        fun getInstance(): PageFragment {
            return PageFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_page, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        pageRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = configureAdapter()
        }
    }

    private fun configureAdapter(): DocumentAdapter {
        return DocumentAdapter(
            blocks = mutableListOf(Block(), Block(), Block(), Block(), Block(), Block(), Block())
        ).apply {
            val callback = DragAndDropBehavior(this)
            val touchHelper = ItemTouchHelper(callback)
            touchHelper.attachToRecyclerView(pageRecycler)
        }
    }
}