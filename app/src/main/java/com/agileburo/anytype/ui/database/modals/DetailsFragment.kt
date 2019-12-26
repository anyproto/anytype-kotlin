package com.agileburo.anytype.ui.database.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.layout.ListDividerItemDecoration
import com.agileburo.anytype.core_utils.ui.BaseBottomSheetFragment
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.di.feature.DetailsModule
import com.agileburo.anytype.presentation.databaseview.modals.DetailsViewModel
import com.agileburo.anytype.presentation.databaseview.modals.DetailsViewModelFactory
import com.agileburo.anytype.presentation.databaseview.modals.DetailsViewState
import com.agileburo.anytype.ui.database.list.ModalNavigation
import com.agileburo.anytype.ui.database.modals.adapter.DetailsAdapter
import kotlinx.android.synthetic.main.modal_properties.*
import javax.inject.Inject

class DetailsFragment : BaseBottomSheetFragment() {

    @Inject
    lateinit var factory: DetailsViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(DetailsViewModel::class.java)
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

    private fun render(state: DetailsViewState) {
        when (state) {
            is DetailsViewState.Init -> {
                iconBack.setOnClickListener { vm.onBackClick() }
                reorder.setOnClickListener { vm.onReorderClick() }
                with(recyclerProperties) {
                    layoutManager = LinearLayoutManager(requireContext())
                    addItemDecoration(ListDividerItemDecoration(requireContext()))
                    adapter =
                        DetailsAdapter(
                            {},
                            state.details.toMutableList(),
                            vm::onDetailClick
                        )
                }
            }
            is DetailsViewState.NavigateToCustomize -> {
                (parentFragment as ModalNavigation).openCustomizeScreen(state.id)
            }
            is DetailsViewState.NavigateToEditDetail -> {
                (parentFragment as ModalNavigation).openEditDetail(
                    detailId = state.detailId,
                    databaseId = state.databaseId
                )
            }
            is DetailsViewState.NavigateToReorder -> {
                (parentFragment as ModalNavigation).openReorderDetails(
                    databaseId = state.databaseId
                )
            }
        }
    }

    override fun injectDependencies() {
        componentManager()
            .mainComponent
            .detailsBuilder()
            .propertiesModule(
                DetailsModule(
                    id = arguments?.getString(ARGS_ID) as String
                )
            )
            .build()
            .inject(this)
    }

    override fun releaseDependencies() {
    }

    companion object {
        const val ARGS_ID = "args.id"

        fun newInstance(id: String): DetailsFragment =
            DetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARGS_ID, id)
                }
            }
    }
}