package com.agileburo.anytype

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_a.*
import timber.log.Timber
import java.time.Month

class FragmentA : Fragment() {

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

        with(editor_toolbar) {
            findViewById<TextView>(R.id.btnBold).setOnClickListener {
                Timber.d("On Bold click!")
                adapter.setBold()
            }
            findViewById<View>(R.id.btnItalic).setOnClickListener {
                Timber.d("On Italic click!")
                adapter.setItalic()
            }
            findViewById<View>(R.id.btnStrike).setOnClickListener {
                Timber.d("On Strike click!")
            }
        }
    }

    fun onDayClick(day: Day) {
        (rvDays.adapter as? DaysAdapter)?.update(day)
    }
}