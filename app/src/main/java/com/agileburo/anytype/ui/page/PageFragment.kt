package com.agileburo.anytype.ui.page

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.features.page.BlockAdapter
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.ui.base.NavigationFragment
import kotlinx.android.synthetic.main.fragment_page.*

class PageFragment : NavigationFragment(R.layout.fragment_page) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val placeholder = getString(R.string.default_text_placeholder)

        recycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = BlockAdapter(
                blocks = mutableListOf(
                    BlockView.Title(
                        id = "1",
                        text = "Разработка Anytype"
                    ),
                    BlockView.Text(
                        id = "2",
                        text = placeholder
                    ),
                    BlockView.HeaderOne(
                        id = "3",
                        text = "Header One"
                    ),
                    BlockView.Text(
                        id = "4",
                        text = placeholder
                    ),
                    BlockView.HeaderTwo(
                        id = "5",
                        text = "Header Two"
                    ),
                    BlockView.Bulleted(
                        id = "6",
                        text = "Первый",
                        indent = 0
                    ),
                    BlockView.Bulleted(
                        id = "7",
                        text = "Второй",
                        indent = 0
                    ),
                    BlockView.Bulleted(
                        id = "8",
                        text = "Третий",
                        indent = 0
                    ),
                    BlockView.HeaderThree(
                        id = "9",
                        text = "Header Three"
                    ),
                    BlockView.Checkbox(
                        id = "10",
                        text = "Checkbox 1"
                    ),
                    BlockView.Checkbox(
                        id = "11",
                        text = "Checkbox 2",
                        checked = true
                    ),
                    BlockView.Checkbox(
                        id = "12",
                        text = "Checkbox 3",
                        checked = true
                    ),
                    BlockView.HeaderThree(
                        id = "13",
                        text = "Header Three"
                    ),
                    BlockView.Numbered(
                        id = "14",
                        text = "Numbered 1",
                        number = "1",
                        indent = 0
                    ),
                    BlockView.Numbered(
                        id = "15",
                        text = "Numbered 2",
                        number = "1",
                        indent = 0
                    ),
                    BlockView.Numbered(
                        id = "16",
                        text = "Numbered 3",
                        number = "1",
                        indent = 0
                    ),
                    BlockView.Contact(
                        id = "17",
                        avatar = "",
                        name = "Konstantin Ivanov"
                    ),
                    BlockView.Task(
                        id = "18",
                        checked = true,
                        text = "Task 1"
                    ),
                    BlockView.Task(
                        id = "19",
                        checked = false,
                        text = "Task 2"
                    ),
                    BlockView.Task(
                        id = "20",
                        checked = true,
                        text = "Task 3"
                    ),
                    BlockView.Page(
                        id = "21",
                        text = "Partnership terms",
                        isArchived = false,
                        isEmpty = true,
                        emoji = null
                    ),
                    BlockView.Page(
                        id = "22",
                        text = "Partnership terms",
                        isArchived = false,
                        isEmpty = false,
                        emoji = null
                    ),
                    BlockView.File(
                        id = "23",
                        filename = "Berlin.pdf",
                        size = "2.1 MB"
                    ),
                    BlockView.Toggle(
                        id = "24",
                        toggled = false,
                        text = "First toggle",
                        indent = 0
                    ),
                    BlockView.Toggle(
                        id = "25",
                        toggled = true,
                        text = "Second toggle",
                        indent = 0
                    ),
                    BlockView.Highlight(
                        id = "26",
                        text = placeholder
                    )
                )
            )
        }
    }

    override fun injectDependencies() {
        // TODO
    }

    override fun releaseDependencies() {
        // TODO
    }
}