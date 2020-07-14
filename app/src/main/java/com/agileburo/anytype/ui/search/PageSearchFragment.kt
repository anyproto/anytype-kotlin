package com.agileburo.anytype.ui.search

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.features.navigation.PageLinksAdapter
import com.agileburo.anytype.core_utils.ext.invisible
import com.agileburo.anytype.core_utils.ext.visible
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.presentation.search.PageSearchView
import com.agileburo.anytype.presentation.search.PageSearchViewModel
import com.agileburo.anytype.presentation.search.PageSearchViewModelFactory
import com.agileburo.anytype.ui.base.ViewStateFragment
import kotlinx.android.synthetic.main.fragment_page_search.*
import javax.inject.Inject

class PageSearchFragment : ViewStateFragment<PageSearchView>(R.layout.fragment_page_search) {

    @Inject
    lateinit var factory: PageSearchViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(PageSearchViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.state.observe(viewLifecycleOwner, this)
        vm.navigation.observe(viewLifecycleOwner, navObserver)
        vm.onViewCreated()
    }

    override fun render(state: PageSearchView) {
        when (state) {
            PageSearchView.Init -> {
                recyclerView.invisible()
                tvScreenStateMessage.invisible()
                progressBar.invisible()
                with(searchView) {
                    setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            clearFocus()
                            return false
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            if (newText != null) {
                                vm.onSearchTextChanged(newText)
                            }
                            return false
                        }
                    })
                }
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter =
                    PageLinksAdapter(
                        data = mutableListOf(),
                        onClick = vm::onOpenPageClicked
                    )
                vm.onGetPageList(searchText = "")
            }
            PageSearchView.Loading -> {
                recyclerView.invisible()
                tvScreenStateMessage.invisible()
                progressBar.visible()
            }
            is PageSearchView.Success -> {
                progressBar.invisible()
                tvScreenStateMessage.invisible()
                recyclerView.visible()
                (recyclerView.adapter as PageLinksAdapter).updateLinks(state.pages)
            }
            PageSearchView.EmptyPages -> {
                progressBar.invisible()
                recyclerView.invisible()
                tvScreenStateMessage.visible()
                tvScreenStateMessage.text = getString(R.string.search_empty_pages)
            }
            is PageSearchView.NoResults -> {
                progressBar.invisible()
                recyclerView.invisible()
                tvScreenStateMessage.visible()
                tvScreenStateMessage.text = getString(R.string.search_no_results, state.searchText)
            }
            is PageSearchView.Error -> {
                progressBar.invisible()
                recyclerView.invisible()
                tvScreenStateMessage.visible()
                tvScreenStateMessage.text = state.error
            }
        }
    }

    override fun injectDependencies() {
        componentManager().pageSearchComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().pageSearchComponent.release()
    }
}