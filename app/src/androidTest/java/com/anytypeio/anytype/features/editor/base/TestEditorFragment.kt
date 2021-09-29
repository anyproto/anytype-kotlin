package com.anytypeio.anytype.features.editor.base

import android.os.Bundle
import androidx.lifecycle.ViewModelStore
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.presentation.editor.EditorViewModelFactory
import com.anytypeio.anytype.ui.editor.EditorFragment

class TestEditorFragment : EditorFragment() {

    lateinit var navController: TestNavHostController

    init {
        factory = testViewModelFactory
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupNavController()
    }

    private fun setupNavController() {
        navController = TestNavHostController(requireContext())
        val viewModelStore = ViewModelStore()
        navController.setViewModelStore(viewModelStore)
        navController.setGraph(R.navigation.graph)
        navController.setCurrentDestination(R.id.pageScreen)
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