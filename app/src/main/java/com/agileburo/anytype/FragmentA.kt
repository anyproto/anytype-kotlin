package com.agileburo.anytype

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.agileburo.anytype.ui.EditorToolbar
import kotlinx.android.synthetic.main.fragment_a.*
import java.time.Month

class FragmentA : Fragment() {

    private var editorToolbar: EditorToolbar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_a, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val days = arrayListOf(
            Day(month = Month.MARCH, day = "1", content = getString(R.string.example_1)),
            Day(month = Month.MARCH, day = "2", content = getString(R.string.example_8)),
            Day(month = Month.MARCH, day = "3", content = getString(R.string.example_3)),
            Day(month = Month.MARCH, day = "4", content = getString(R.string.example_4)),
            Day(month = Month.MARCH, day = "5", content = getString(R.string.example_5)),
            Day(month = Month.MARCH, day = "6", content = getString(R.string.example_6)),
            Day(month = Month.MARCH, day = "7", content = getString(R.string.example_7)),
            Day(month = Month.MARCH, day = "7", content = getString(R.string.example_2))
        )

        rvDays.layoutManager = LinearLayoutManager(activity)
        val adapter = DaysAdapter(days = days, onClick = { onClick: Day -> onDayClick(onClick) })
        rvDays.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        rvDays.adapter = adapter
        editorToolbar = view.findViewById(R.id.editor_toolbar)
        editorToolbar!!.setMainActions(
            { adapter.isBoldActive = it },
            { adapter.isItalicActive = it },
            { adapter.isStrokeThroughActive = it },
            { throw NotImplementedError() }
        )
    }

    fun onDayClick(day: Day) {
        (rvDays.adapter as? DaysAdapter)?.update(day)
    }
}