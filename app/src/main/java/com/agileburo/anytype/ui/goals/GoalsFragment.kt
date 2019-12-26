package com.agileburo.anytype.ui.goals

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.GoalAdapter
import com.agileburo.anytype.core_utils.ui.BaseFragment
import kotlinx.android.synthetic.main.fragment_goals.*

class GoalsFragment : BaseFragment(R.layout.fragment_goals) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        goalRecycler.apply {
            adapter = GoalAdapter()
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}
}