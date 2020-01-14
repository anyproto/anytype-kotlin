package com.agileburo.anytype.ui.database.list

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.agileburo.anytype.ModalsNavFragment
import com.agileburo.anytype.ModalsNavFragment.Companion.TAG_CUSTOMIZE
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.layout.ListDividerItemDecoration
import com.agileburo.anytype.core_ui.layout.SpacingItemDecoration
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.presentation.databaseview.ListBoardViewModel
import com.agileburo.anytype.presentation.databaseview.ListBoardViewModelFactory
import com.agileburo.anytype.presentation.databaseview.ListBoardViewState
import com.agileburo.anytype.ui.base.NavigationFragment
import com.agileburo.anytype.ui.database.filters.FiltersAdapter
import com.agileburo.anytype.ui.database.list.adapter.ListBoardAdapter
import kotlinx.android.synthetic.main.fragment_list_board.*
import javax.inject.Inject

class ListBoardFragment :
    NavigationFragment(R.layout.fragment_list_board) {

    @Inject
    lateinit var factory: ListBoardViewModelFactory

    private lateinit var listBoardAdapter: ListBoardAdapter
    private lateinit var filtersAdapter: FiltersAdapter

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(ListBoardViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.state.observe(viewLifecycleOwner, Observer { render(it) })
        vm.navigation.observe(viewLifecycleOwner, navObserver)
        vm.onViewCreated()
    }

    fun render(state: ListBoardViewState) {
        when (state) {
            ListBoardViewState.UiEffect.Init -> {
                with(recyclerContacts) {
                    layoutManager = LinearLayoutManager(requireContext())
                    addItemDecoration(ListDividerItemDecoration(requireContext()))
                    listBoardAdapter =
                        ListBoardAdapter(
                            vm::onContactClick
                        )
                    adapter = listBoardAdapter
                }

                icSettings.setOnClickListener { vm.onCustomizeClick() }
                icEdit.setOnClickListener { vm.onEditDatabaseClick() }

                with(recyclerFilters) {
                    layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    addItemDecoration(
                        SpacingItemDecoration(
                            spacingStart = resources.getDimensionPixelOffset(R.dimen.list_margin_start),
                            firstItemSpacingStart = 0
                        )
                    )
                    filtersAdapter = FiltersAdapter(vm::onFilterClick)
                    adapter = filtersAdapter
                }
                vm.getFilters()
            }
            is ListBoardViewState.ScreenState.SetContacts -> {
                listBoardAdapter.setData(state.contacts)
            }
            is ListBoardViewState.ScreenState.SetFilters -> {
                filtersAdapter.setData(state.filters)
                vm.getContacts()
            }
            ListBoardViewState.ScreenState.Loading -> TODO()
            ListBoardViewState.ScreenState.Error -> TODO()
            ListBoardViewState.ScreenState.OpenBottomSheet -> {
                ModalsNavFragment.newInstance(
                    databaseId = "343253",
                    startTag = TAG_CUSTOMIZE
                ).show(requireActivity().supportFragmentManager, "Modals")
            }
        }
    }

    override fun injectDependencies() {
        componentManager().contactsComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().contactsComponent.release()
    }
}

