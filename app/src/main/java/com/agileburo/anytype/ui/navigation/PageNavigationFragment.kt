package com.agileburo.anytype.ui.navigation

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.features.navigation.PageNavigationAdapter
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.presentation.navigation.PageNavigationView
import com.agileburo.anytype.presentation.navigation.PageNavigationViewModel
import com.agileburo.anytype.presentation.navigation.PageNavigationViewModelFactory
import com.agileburo.anytype.ui.base.ViewStateFragment
import kotlinx.android.synthetic.main.fragment_page_navigation.*
import javax.inject.Inject

class PageNavigationFragment
    : ViewStateFragment<ViewState<PageNavigationView>>(R.layout.fragment_page_navigation) {

    @Inject
    lateinit var factory: PageNavigationViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(PageNavigationViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.state.observe(viewLifecycleOwner, this)
        vm.navigation.observe(viewLifecycleOwner, navObserver)
        vm.onViewCreated()
    }

    override fun render(state: ViewState<PageNavigationView>) {
        when (state) {
            ViewState.Init -> {
                viewPager.adapter = PageNavigationAdapter(requireActivity())
            }
            ViewState.Loading -> TODO()
            is ViewState.Success -> {
            }
            is ViewState.Error -> TODO()
        }
    }

    override fun injectDependencies() {
        componentManager().navigationComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().navigationComponent.release()
    }

    companion object {
        const val TARGET_ID_KEY = "ID_KEY"
    }
}