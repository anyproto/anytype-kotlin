package com.anytypeio.anytype.ui.relations

import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.RelationCreateFromLibraryViewModel
import com.anytypeio.anytype.ui.types.create.REQUEST_CREATE_OBJECT
import javax.inject.Inject

class RelationCreateFromLibraryFragment : RelationCreateFromScratchBaseFragment() {

    @Inject
    lateinit var factory: RelationCreateFromLibraryViewModel.Factory
    override val vm: RelationCreateFromLibraryViewModel by viewModels { factory }

    override fun onStart() {
        super.onStart()
        subscribe(vm.navigation, ::onNavigation)
    }

    private fun onNavigation(navigationEvent: RelationCreateFromLibraryViewModel.Navigation) {
        when (navigationEvent) {
            is RelationCreateFromLibraryViewModel.Navigation.Back -> {
                setFragmentResult(REQUEST_CREATE_OBJECT, bundleOf())
                dismiss()
            }
            else -> {
                // do nothing
            }
        }
    }

    override fun onCreateRelationClicked() {
        vm.onCreateRelationClicked()
    }

    override fun onLimitObjectTypeClicked() {
        findNavController().navigate(
            R.id.limitObjectTypeScreen,
            bundleOf(
                LimitObjectTypeFragment.CTX_KEY to ctx,
                LimitObjectTypeFragment.FLOW_TYPE to LimitObjectTypeFragment.FLOW_LIBRARY
            )
        )
    }

    override fun onConnectWithClicked() {
        findNavController().navigate(
            R.id.relationFormatPickerScreen,
            bundleOf(
                RelationCreateFromScratchFormatPickerFragment.CTX_KEY to ctx,
                RelationCreateFromScratchFormatPickerFragment.FLOW_TYPE to RelationCreateFromScratchFormatPickerFragment.FLOW_LIBRARY
            )
        )
    }

    override fun injectDependencies() {
        componentManager().relationCreationFromLibraryComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().relationCreationFromLibraryComponent.release()
    }

    companion object {
        fun args(query: String) = bundleOf(QUERY_KEY to query)
    }
}