package com.agileburo.anytype

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.agileburo.anytype.editor.EditorFragment
import kotlinx.android.synthetic.main.fragment_a.*
import timber.log.Timber
import java.time.Month

class FragmentA : Fragment() {

    val days = listOf(
        Day(month = Month.MARCH, day = "1", content = "Прогулка по лесу"),
        Day(month = Month.MARCH, day = "2", content = "Чтение Войны и Мира"),
        Day(month = Month.MARCH, day = "3", content = "Приготовление вкусного обеда"),
        Day(month = Month.MARCH, day = "4", content = "Разборка вещей"),
        Day(month = Month.MARCH, day = "5", content = "Уборка квартиры"),
        Day(month = Month.MARCH, day = "6", content = "Рисерч в Интеренете"),
        Day(month = Month.MARCH, day = "7", content = "Написание статьи"),
        Day(month = Month.MARCH, day = "8", content = "Онлайн шоппинг")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_a, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvDays.layoutManager = LinearLayoutManager(activity)
        val adapter = DaysAdapter(days = days, onClick = { onClick: Day -> onDayClick(onClick) })
        rvDays.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        rvDays.adapter = adapter
    }

    fun onDayClick(day: Day) {
        EditorFragment().show(childFragmentManager, "Editor")
        Timber.d("On day click : $day")
    }
}