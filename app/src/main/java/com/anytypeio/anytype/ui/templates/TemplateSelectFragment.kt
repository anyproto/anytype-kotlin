package com.anytypeio.anytype.ui.templates

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.databinding.FragmentTemplateSelectBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.templates.TemplateSelectViewModel
import com.anytypeio.anytype.ui.base.NavigationFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class TemplateSelectFragment :
    NavigationFragment<FragmentTemplateSelectBinding>(R.layout.fragment_template_select) {

    private val vm by viewModels<TemplateSelectViewModel> { factory }

    @Inject
    lateinit var factory: TemplateSelectViewModel.Factory

    private val ids: List<String> get() = arg(TEMPLATE_IDS_KEY)
    private val type: Id get() = arg(OBJECT_TYPE_KEY)
    private val typeName: String? get() = arg(OBJECT_TYPE_NAME_KEY)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.templateViewPager.adapter = Adapter(ids, this)
        TabLayoutMediator(binding.tabs, binding.templateViewPager) { _, _ -> }.attach()
        vm.navigation.observe(viewLifecycleOwner, navObserver)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { setupTemplateHeaderMessage() }
                launch { setupCancelClicks() }
                launch { setupUseTemplateClicks() }
            }
        }
    }

    private suspend fun setupUseTemplateClicks() {
        binding.btnUseTemplate.clicks().collect {
            vm.onUseTemplate(
                type = type,
                template = ids[binding.templateViewPager.currentItem]
            )
        }
    }

    private suspend fun setupCancelClicks() {
        binding.btnCancel.clicks().collect { vm.onCancel(type) }
    }

    private suspend fun setupTemplateHeaderMessage() {
        val name = typeName
        if (name != null)
            binding.tvTemplateCountOrTutorial.text = getString(
                R.string.type_has_templates, name, ids.size
            )
        else
            binding.tvTemplateCountOrTutorial.text = getString(
                R.string.this_type_has_templates, ids.size
            )
        delay(USE_SWIPE_TO_CHOOSE_MSG_DELAY)
        binding.tvTemplateCountOrTutorial.setText(R.string.swipe_to_choose)
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
    ): FragmentTemplateSelectBinding = inflateBinding(
        inflater, container
    )

    companion object {
        const val TEMPLATE_IDS_KEY = "arg.template.ids"
        const val OBJECT_TYPE_KEY = "arg.template.object_type"
        const val OBJECT_TYPE_NAME_KEY = "arg.template.object_type.name"

        private const val USE_SWIPE_TO_CHOOSE_MSG_DELAY = 2000L
    }

    internal class Adapter(
        private val ids: List<Id>, fragment: Fragment
    ) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = ids.size
        override fun createFragment(position: Int): Fragment {
            return TemplateFragment.new(ids[position])
        }
    }
}