package com.anytypeio.anytype.ui.page.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.page.LinkAddViewModel
import com.anytypeio.anytype.presentation.page.LinkAddViewModelFactory
import com.anytypeio.anytype.presentation.page.LinkViewState
import com.anytypeio.anytype.ui.page.OnFragmentInteractionListener
import kotlinx.android.synthetic.main.fragment_link.*
import javax.inject.Inject

class SetLinkFragment : BaseBottomSheetFragment() {

    companion object {
        const val ARG_URL = "arg.link.url"
        const val ARG_TEXT = "arg.link.text"
        const val ARG_RANGE_START = "arg.link.range.start"
        const val ARG_RANGE_END = "arg.link.range.end"
        const val ARG_BLOCK_ID = "arg.link.block.id"

        fun newInstance(
            text: String,
            initUrl: String?,
            rangeStart: Int,
            rangeEnd: Int,
            blockId: String
        ) =
            SetLinkFragment().apply {
                arguments = bundleOf(
                    ARG_TEXT to text,
                    ARG_URL to initUrl,
                    ARG_RANGE_START to rangeStart,
                    ARG_RANGE_END to rangeEnd,
                    ARG_BLOCK_ID to blockId
                )
            }
    }

    @Inject
    lateinit var factory: LinkAddViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(LinkAddViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_link, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.state.observe(viewLifecycleOwner, Observer { state -> render(state) })
        arguments?.let {
            vm.onViewCreated(
                text = it.getString(ARG_TEXT, ""),
                initUrl = it.getString(ARG_URL),
                range = IntRange(it.getInt(ARG_RANGE_START), it.getInt(ARG_RANGE_END))
            )
        }
    }

    private fun render(state: LinkViewState) {
        when (state) {
            is LinkViewState.Init -> {
                text.text = state.text
                link.setText(state.url)
                buttonLink.setOnClickListener {
                    vm.onLinkButtonClicked(link.text.toString())
                }
                buttonUnlink.setOnClickListener {
                    vm.onUnlinkButtonClicked()
                }
            }
            is LinkViewState.AddLink -> {
                (parentFragment as? OnFragmentInteractionListener)?.onAddMarkupLinkClicked(
                    link = state.link,
                    range = state.range,
                    blockId = arguments?.getString(ARG_BLOCK_ID, "").orEmpty()
                )
                dismiss()
            }
            is LinkViewState.Unlink -> {
                (parentFragment as? OnFragmentInteractionListener)?.onRemoveMarkupLinkClicked(
                    range = state.range,
                    blockId = arguments?.getString(ARG_BLOCK_ID, "").orEmpty()
                )
                dismiss()
            }
        }
    }

    override fun injectDependencies() {
        componentManager().linkAddComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().linkAddComponent.release()
    }
}