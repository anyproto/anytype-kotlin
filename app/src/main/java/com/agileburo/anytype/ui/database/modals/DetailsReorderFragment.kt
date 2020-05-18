package com.agileburo.anytype.ui.database.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.agileburo.anytype.ui.database.modals.ModalsNavFragment.Companion.ARGS_DB_ID
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.layout.ListDividerItemDecoration
import com.agileburo.anytype.core_utils.ext.invisible
import com.agileburo.anytype.core_utils.ext.visible
import com.agileburo.anytype.core_utils.ui.BaseBottomSheetFragment
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.di.feature.DetailsReorderModule
import com.agileburo.anytype.presentation.databaseview.modals.DetailsReorderViewModel
import com.agileburo.anytype.presentation.databaseview.modals.DetailsReorderViewModelFactory
import com.agileburo.anytype.presentation.databaseview.modals.DetailsReorderViewState
import com.agileburo.anytype.ui.database.modals.adapter.DetailsAdapter
import com.agileburo.anytype.ui.database.modals.helpers.DetailsTouchHelper
import kotlinx.android.synthetic.main.modal_properties.*
import javax.inject.Inject

class DetailsReorderFragment : BaseBottomSheetFragment() {

    companion object {

        fun newInstance(id: String): DetailsReorderFragment =
            DetailsReorderFragment().apply {
                arguments = Bundle().apply {
                    putString(ARGS_DB_ID, id)
                }
            }
    }

    @Inject
    lateinit var factory: DetailsReorderViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(DetailsReorderViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.modal_properties, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.state.observe(viewLifecycleOwner, Observer { render(it) })
        vm.onViewCreated()
    }

    private fun render(state: DetailsReorderViewState) {
        when (state) {
            is DetailsReorderViewState.Init -> {
                reorder.invisible()
                reorderDone.visible()
                reorderDone.setOnClickListener { vm.onDoneClick() }
                iconBack.setOnClickListener { vm.onDoneClick() }
                with(recyclerProperties) {
                    layoutManager = LinearLayoutManager(requireContext())
                    addItemDecoration(ListDividerItemDecoration(requireContext()))
                    val detailsAdapter =
                        DetailsAdapter(
                            data = state.details.toMutableList(),
                            click = vm::onDetailClick,
                            swap = vm::onSwapDetails,
                            isDragOn = true
                        )
                    adapter = detailsAdapter
                    val callback =
                        DetailsTouchHelper(
                            dragDirs = ItemTouchHelper.UP.or(
                                ItemTouchHelper.DOWN
                            ), adapter = detailsAdapter
                        )
                    val helper = ItemTouchHelper(callback)
                    helper.attachToRecyclerView(this)
                }
            }
            is DetailsReorderViewState.NavigateToDetails -> {
                (parentFragment as ModalNavigation).showDetailsScreen()
            }
            is DetailsReorderViewState.Error -> TODO()
        }
    }

    override fun injectDependencies() {
        componentManager()
            .mainComponent
            .detailsReorderBuilder()
            .module(DetailsReorderModule(id = arguments?.getString(ARGS_DB_ID) as String))
            .build()
            .inject(this)
    }

    override fun releaseDependencies() = Unit
}