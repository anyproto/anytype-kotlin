package com.agileburo.anytype.ui.database.list

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.layout.ListDividerItemDecoration
import com.agileburo.anytype.core_ui.layout.SpacingItemDecoration
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.domain.database.DatabaseMock
import com.agileburo.anytype.presentation.databaseview.ListBoardViewModel
import com.agileburo.anytype.presentation.databaseview.ListBoardViewModelFactory
import com.agileburo.anytype.presentation.databaseview.ListBoardViewState
import com.agileburo.anytype.ui.base.NavigationFragment
import com.agileburo.anytype.ui.database.filters.FiltersAdapter
import com.agileburo.anytype.ui.database.list.adapter.ListBoardAdapter
import com.agileburo.anytype.ui.database.modals.*
import kotlinx.android.synthetic.main.fragment_list_board.*
import javax.inject.Inject

const val TAG_SWITCH = "tag.switch"
const val TAG_CUSTOMIZE = "tag.customize"
const val TAG_PROPERTIES = "tag.properties"
const val TAG_EDIT_DETAIL = "tag.edit.detail"
const val TAG_REORDER = "tag.details.reorder"

class ListBoardFragment :
    NavigationFragment(R.layout.fragment_list_board), ModalNavigation {

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
                openCustomizeScreen(DatabaseMock.ID)
            }
        }
    }

    override fun openCustomizeScreen(id: String) {
        childFragmentManager.findFragmentByTag(TAG_SWITCH)?.let {
            childFragmentManager.beginTransaction()
                .remove(it)
                .add(
                    CustomizeDisplayFragment.newInstance(id),
                    TAG_CUSTOMIZE
                )
                .commit()
            return
        }
        childFragmentManager.findFragmentByTag(TAG_PROPERTIES)?.let {
            childFragmentManager.beginTransaction()
                .remove(it)
                .add(
                    CustomizeDisplayFragment.newInstance(id),
                    TAG_CUSTOMIZE
                )
                .commit()
            return
        }
        childFragmentManager.beginTransaction()
            .add(
                CustomizeDisplayFragment.newInstance(id),
                TAG_CUSTOMIZE
            )
            .commit()
    }

    override fun openSwitchScreen(id: String) {
        childFragmentManager.findFragmentByTag(TAG_CUSTOMIZE)?.let {
            childFragmentManager.beginTransaction()
                .remove(it)
                .add(
                    SwitchDisplayFragment.newInstance(id),
                    TAG_SWITCH
                )
                .commit()
        }
    }

    override fun openPropertiesScreen(id: String) {
        childFragmentManager.findFragmentByTag(TAG_CUSTOMIZE)?.let {
            childFragmentManager.beginTransaction()
                .remove(it)
                .add(
                    DetailsFragment.newInstance(id),
                    TAG_PROPERTIES
                )
                .commit()
            return
        }
        childFragmentManager.findFragmentByTag(TAG_EDIT_DETAIL)?.let {
            childFragmentManager.beginTransaction()
                .remove(it)
                .add(
                    DetailsFragment.newInstance(id),
                    TAG_PROPERTIES
                )
                .commit()
            return
        }
        childFragmentManager.findFragmentByTag(TAG_REORDER)?.let {
            childFragmentManager.beginTransaction()
                .remove(it)
                .add(
                    DetailsFragment.newInstance(id),
                    TAG_PROPERTIES
                )
                .commit()
            return
        }
    }

    override fun openEditDetail(databaseId: String, detailId: String) {
        childFragmentManager.findFragmentByTag(TAG_PROPERTIES)?.let {
            childFragmentManager.beginTransaction()
                .remove(it)
                .add(
                    DetailEditFragment.newInstance(
                        propertyId = detailId,
                        databaseId = databaseId
                    ),
                    TAG_EDIT_DETAIL
                )
                .commit()
        }
    }

    override fun openReorderDetails(databaseId: String) {
        childFragmentManager.findFragmentByTag(TAG_PROPERTIES)?.let {
            childFragmentManager.beginTransaction()
                .remove(it)
                .add(
                    DetailsReorderFragment.newInstance(
                        id = databaseId
                    ),
                    TAG_REORDER
                )
                .commit()
        }
    }

    override fun injectDependencies() {
        componentManager().contactsComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().contactsComponent.release()
    }
}

interface ModalNavigation {
    fun openCustomizeScreen(id: String)
    fun openSwitchScreen(id: String)
    fun openPropertiesScreen(id: String)
    fun openEditDetail(databaseId: String, detailId: String)
    fun openReorderDetails(databaseId: String)
}