package com.anytypeio.anytype.ui.database.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.extensions.toast
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.di.feature.EditDatabaseModule
import com.anytypeio.anytype.presentation.databaseview.modals.EditDatabaseViewModel
import com.anytypeio.anytype.presentation.databaseview.modals.EditDatabaseViewModelFactory
import com.anytypeio.anytype.presentation.databaseview.modals.EditDatabaseState
import com.anytypeio.anytype.ui.base.NavigationBottomSheetFragment
import kotlinx.android.synthetic.main.modal_edit_view.*
import timber.log.Timber
import javax.inject.Inject

class EditDatabaseFragment : NavigationBottomSheetFragment() {

    @Inject
    lateinit var factory: EditDatabaseViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(EditDatabaseViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.modal_edit_view, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.state.observe(viewLifecycleOwner, Observer { render(it) })
        vm.navigation.observe(viewLifecycleOwner, navObserver)
        vm.onViewCreated()
    }

    private fun render(state: EditDatabaseState) {
        when (state) {

            is EditDatabaseState.Init -> {
                item_duplicate.setOnClickListener { vm.onDuplicateClick() }
                item_delete.setOnClickListener { vm.onDeleteClick() }
                editName.setText(state.name)
                iconBack.setOnClickListener { vm.onBackClick() }
                Timber.d("Get Database : ${state.name}")
            }

            is EditDatabaseState.Duplicate -> {
                requireContext().toast("Duplicate database with id:${state.name}")
            }

            is EditDatabaseState.Delete -> {
                requireContext().toast("Delete database with id:${state.name}")
            }
        }
    }

    override fun injectDependencies() {
        componentManager()
            .mainComponent
            .editDatabaseComponentBuilder()
            .editModule(EditDatabaseModule(databaseId = "122423", databaseName = "TestName452077"))
            .build()
            .inject(this)
    }

    override fun releaseDependencies() {
        //todo fix di
    }
}