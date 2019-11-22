package com.agileburo.anytype.ui.desktop

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.agileburo.anytype.R
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.presentation.desktop.HomeDashboardViewModel
import com.agileburo.anytype.presentation.desktop.HomeDashboardViewModel.ViewState
import com.agileburo.anytype.presentation.desktop.HomeDashboardViewModelFactory
import com.agileburo.anytype.presentation.profile.ProfileView
import com.agileburo.anytype.ui.base.ViewStateFragment
import kotlinx.android.synthetic.main.fragment_desktop.*
import javax.inject.Inject


class HomeDashboardFragment : ViewStateFragment<ViewState>(R.layout.fragment_desktop) {

    private val profileObserver = Observer<ProfileView> { profile ->
        greeting.text = getString(R.string.greet, profile.name)
        avatar.bind(name = profile.name)
    }

    private val imageObserver = Observer<ByteArray> { blob -> avatar.bind(blob) }

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(HomeDashboardViewModel::class.java)
    }

    @Inject
    lateinit var factory: HomeDashboardViewModelFactory

    private val dashboardAdapter by lazy {
        DashboardAdapter(
            data = mutableListOf(),
            onDocumentClicked = { vm.onDocumentClicked() }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()

        with(vm) {
            state.observe(viewLifecycleOwner, this@HomeDashboardFragment)
            navigation.observe(viewLifecycleOwner, navObserver)
            profile.observe(viewLifecycleOwner, profileObserver)
            image.observe(viewLifecycleOwner, imageObserver)
        }

        vm.onViewCreated()
    }

    override fun render(state: ViewState) {
        when (state) {
            is ViewState.Success -> {
                dashboardAdapter.update(state.data)
            }
        }
    }

    private fun setup() {
        desktopRecycler.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = dashboardAdapter
        }
        fab.setOnClickListener { vm.onAddNewDocumentClicked() }
        avatar.setOnClickListener { vm.onProfileClicked() }
    }

    override fun injectDependencies() {
        componentManager().desktopComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().desktopComponent.release()
    }
}