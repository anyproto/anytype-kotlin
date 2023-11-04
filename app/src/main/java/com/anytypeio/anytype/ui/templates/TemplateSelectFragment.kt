package com.anytypeio.anytype.ui.templates

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.core_utils.ui.proceed
import com.anytypeio.anytype.databinding.FragmentTemplateSelectBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.templates.TemplateSelectViewModel
import com.anytypeio.anytype.ui.editor.sheets.SetAsDefaultListener
import com.anytypeio.anytype.ui.editor.sheets.TemplateBlankMenuFragment
import com.google.android.material.tabs.TabLayoutMediator
import javax.inject.Inject

class TemplateSelectFragment :
    BaseBottomSheetFragment<FragmentTemplateSelectBinding>(), SetAsDefaultListener {

    private val vm by viewModels<TemplateSelectViewModel> { factory }

    @Inject
    lateinit var factory: TemplateSelectViewModel.Factory

    private val typeId get() = arg<Id>(ARG_TYPE_ID)

    private lateinit var templatesAdapter: TemplateSelectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppBottomSecondarySheetDialogTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        skipCollapsed()
        setFullHeightSheet()
        setupViewPagerAndTabs()
        binding.ivThreeDots.setOnClickListener {
            val currentTemplate =
                binding.templateViewPager.findCurrentFragment(childFragmentManager)
            if (currentTemplate is EditorTemplateFragment) {
                currentTemplate.onDocumentMenuClicked()
            }
            if (currentTemplate is TemplateBlankFragment) {
                val fr = TemplateBlankMenuFragment()
                fr.showChildFragment()
            }
        }
    }

    override fun onSetAsDefaultClicked() {
        vm.proceedWithSettingAsDefaultTemplate(typeId)
    }

    private fun setupViewPagerAndTabs() {
        templatesAdapter = TemplateSelectAdapter(mutableListOf(), this)
        binding.templateViewPager.adapter = templatesAdapter
        TabLayoutMediator(binding.tabs, binding.templateViewPager) { tab, _ ->
            tab.text = TAB_SPACE
        }.attach()
    }

    override fun onStart() {
        jobs += lifecycleScope.subscribe(vm.viewState) { render(it) }
        jobs += lifecycleScope.subscribe(vm.isDismissed) { if (it) exit() }
        proceed(vm.toasts) { msg ->
            Toast.makeText(
                requireContext(),
                msg,
                Toast.LENGTH_SHORT
            ).show()
        }
        super.onStart()
        expand()
        vm.onStart(typeId = typeId)
    }

    private fun render(viewState: TemplateSelectViewModel.ViewState) {
        when (viewState) {
            TemplateSelectViewModel.ViewState.Init -> {
                binding.tvTemplateCountOrTutorial.text = null
            }

            is TemplateSelectViewModel.ViewState.Success -> {
                binding.tvTemplateCountOrTutorial.text = getString(
                    R.string.this_type_has_templates,
                    viewState.templates.size
                )
                templatesAdapter.update(viewState.templates)
            }
        }
    }

    private fun exit() {
        findNavController().popBackStack()
    }

    override fun injectDependencies() {
        componentManager().templateSelectComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().templateSelectComponent.release()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTemplateSelectBinding = FragmentTemplateSelectBinding.inflate(
        inflater, container, false
    )

    companion object {
        const val ARG_TYPE_ID = "arg.template.arg_target_object_type_id"
        const val TAB_SPACE = "   "
    }
}

fun ViewPager2.findCurrentFragment(fragmentManager: FragmentManager): Fragment? {
    return fragmentManager.findFragmentByTag("f$currentItem")
}