package com.anytypeio.anytype.features.editor.base

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelStore
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.EditorViewModelFactory
import com.anytypeio.anytype.ui.editor.EditorFragment

class TestEditorFragment : EditorFragment() {

    val viewModel: EditorViewModel get() = vm

    init {
        factory = testViewModelFactory
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupNavController()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbarHeight = resources.getDimensionPixelSize(R.dimen.default_toolbar_height)
        binding.recycler.setPadding(0, toolbarHeight, 0, binding.recycler.paddingBottom)
    }

    private fun setupNavController() {
        val navController = TestNavHostController(requireContext())
        navController.setViewModelStore(ViewModelStore())
        viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
            if (viewLifecycleOwner != null) {
                Navigation.setViewNavController(this.requireView(), navController)
            }
        }
    }

    companion object {
        lateinit var testViewModelFactory: EditorViewModelFactory
    }
}
