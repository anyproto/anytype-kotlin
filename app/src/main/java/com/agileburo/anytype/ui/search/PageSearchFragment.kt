package com.agileburo.anytype.ui.search

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
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

    private lateinit var clearSearchText: View
    private lateinit var filterInputField: EditText

    private val vm by viewModels<PageSearchViewModel> { factory }

    private val searchAdapter by lazy {
        PageLinksAdapter(
            data = emptyList(),
            onClick = vm::onOpenPageClicked
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.state.observe(viewLifecycleOwner, this)
        vm.navigation.observe(viewLifecycleOwner, navObserver)
        clearSearchText = searchView.findViewById(R.id.clearSearchText)
        filterInputField = searchView.findViewById(R.id.filterInputField)
        vm.onViewCreated()
    }

    override fun render(state: PageSearchView) {
        when (state) {
            PageSearchView.Init -> {
                recyclerView.invisible()
                tvScreenStateMessage.invisible()
                progressBar.invisible()
                searchView.findViewById<View>(R.id.clearSearchText).setOnClickListener {

                }
                clearSearchText.setOnClickListener {
                    filterInputField.setText(EMPTY_FILTER_TEXT)
                    clearSearchText.invisible()
                }
                filterInputField.doAfterTextChanged { newText ->
                    if (newText != null) {
                        vm.onSearchTextChanged(newText.toString())
                    }
                    if (newText.isNullOrEmpty()) {
                        clearSearchText.invisible()
                    } else {
                        clearSearchText.visible()
                    }
                }
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = searchAdapter
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
                searchAdapter.updateLinks(state.pages)
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

    companion object {
        const val EMPTY_FILTER_TEXT = ""
    }
}