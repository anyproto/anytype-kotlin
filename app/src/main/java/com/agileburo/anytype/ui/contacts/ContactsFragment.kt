package com.agileburo.anytype.ui.contacts

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.layout.ListDividerItemDecoration
import com.agileburo.anytype.core_ui.layout.SpacingItemDecoration
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.presentation.contacts.ContactsViewModel
import com.agileburo.anytype.presentation.contacts.ContactsViewModelFactory
import com.agileburo.anytype.presentation.contacts.ContactsViewState
import com.agileburo.anytype.ui.base.NavigationFragment
import com.agileburo.anytype.ui.filters.FiltersAdapter
import kotlinx.android.synthetic.main.fragment_contacts.*
import javax.inject.Inject

class ContactsFragment :
    NavigationFragment(R.layout.fragment_contacts), Observer<ContactsViewState> {

    @Inject
    lateinit var factory: ContactsViewModelFactory

    private lateinit var contactsAdapter: ContactsAdapter
    private lateinit var filtersAdapter: FiltersAdapter

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(ContactsViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.state.observe(viewLifecycleOwner, this)
        vm.onViewCreated()
    }

    override fun onChanged(state: ContactsViewState) {
        when (state) {
            ContactsViewState.UiEffect.Init -> {
                with(recyclerContacts) {
                    layoutManager = LinearLayoutManager(requireContext())
                    addItemDecoration(ListDividerItemDecoration(requireContext()))
                    contactsAdapter = ContactsAdapter(vm::onContactClick)
                }

                with(recyclerFilters) {
                    layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    addItemDecoration(
                        SpacingItemDecoration(
                            spacingStart = 20,
                            firstItemSpacingStart = 0
                        )
                    )
                    filtersAdapter = FiltersAdapter(vm::onFilterClick)
                }
                vm.getFilters()
            }
            is ContactsViewState.ScreenState.Contacts -> {
                contactsAdapter.updateData(state.contacts)
            }
            is ContactsViewState.ScreenState.Filters -> {
                filtersAdapter.updateData(state.filters)
                vm.getContacts()
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