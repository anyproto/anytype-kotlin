package com.anytypeio.anytype.ui.editor.modals

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetTextInputFragment
import com.anytypeio.anytype.databinding.FragmentPageIconPickerBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.library_page_icon_picker_widget.ui.DocumentEmojiIconPickerAdapter
import com.anytypeio.anytype.presentation.editor.picker.EmojiPickerView.Companion.HOLDER_EMOJI_CATEGORY_HEADER
import com.anytypeio.anytype.presentation.editor.picker.EmojiPickerView.Companion.HOLDER_EMOJI_ITEM
import com.anytypeio.anytype.presentation.editor.picker.ObjectIconPickerBaseViewModel
import com.anytypeio.anytype.presentation.editor.picker.ObjectIconPickerBaseViewModel.ViewState
import com.anytypeio.anytype.presentation.editor.picker.ObjectIconPickerViewModel
import com.anytypeio.anytype.presentation.editor.picker.ObjectIconPickerViewModelFactory
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import javax.inject.Inject

abstract class ObjectIconPickerBaseFragment : BaseBottomSheetTextInputFragment<FragmentPageIconPickerBinding>() {

    protected val target: String
        get() = requireArguments()
            .getString(ARG_TARGET_ID_KEY)
            ?: throw IllegalStateException(MISSING_TARGET_ERROR)

    protected val context: String
        get() = requireArguments()
            .getString(ARG_CONTEXT_ID_KEY)
            ?: throw IllegalStateException(MISSING_CONTEXT_ERROR)

    abstract val vm : ObjectIconPickerBaseViewModel

    private val emojiPickerAdapter by lazy {
        DocumentEmojiIconPickerAdapter(
            views = emptyList(),
            onEmojiClicked = { unicode ->
                vm.onEmojiClicked(
                    unicode = unicode,
                    target = target,
                    context = context
                )
            }
        )
    }

    override val textInput: EditText get() = binding.filterInputField

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()
        with(binding) {
            clearSearchText.setOnClickListener {
                filterInputField.setText(EMPTY_FILTER_TEXT)
                clearSearchText.invisible()
            }
            filterInputField.doAfterTextChanged { vm.onQueryChanged(it.toString()) }
            btnRemoveIcon.setOnClickListener { vm.onRemoveClicked(context) }
            tvTabRandom.setOnClickListener { vm.onRandomEmoji(ctx = context, target = target) }
            tvTabUpload.setOnClickListener { proceedWithImagePick() }
        }
        expand()
    }

    private fun setupRecycler() {
        binding.pickerRecycler.apply {
            setItemViewCacheSize(EMOJI_RECYCLER_ITEM_VIEW_CACHE_SIZE)
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, PAGE_ICON_PICKER_DEFAULT_SPAN_COUNT).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int) =
                        when (val type = emojiPickerAdapter.getItemViewType(position)) {
                            HOLDER_EMOJI_ITEM -> 1
                            HOLDER_EMOJI_CATEGORY_HEADER -> PAGE_ICON_PICKER_DEFAULT_SPAN_COUNT
                            else -> throw IllegalStateException("$UNEXPECTED_VIEW_TYPE_MESSAGE: $type")
                        }
                }
            }
            adapter = emojiPickerAdapter
        }
    }

    override fun onStart() {
        with(lifecycleScope) {
            jobs += subscribe(vm.state()) { render(it) }
        }
        super.onStart()
    }

    private fun render(state: ViewState) {
        with(binding) {
            when (state) {
                is ViewState.Success -> {
                    if (filterInputField.text.isNotEmpty())
                        clearSearchText.visible()
                    else
                        clearSearchText.invisible()
                    emojiPickerAdapter.update(state.views)
                    progressBar.invisible()
                }
                is ViewState.Loading -> {
                    clearSearchText.invisible()
                    progressBar.visible()
                }
                is ViewState.Init -> {
                    clearSearchText.visible()
                    progressBar.invisible()
                }
                is ViewState.Exit -> dismiss()
            }
        }
    }

    override fun onDestroyView() {
        dialog?.setOnShowListener(null)
        super.onDestroyView()
    }

    private fun proceedWithImagePick() {
        if (!hasExternalStoragePermission()) {
            permissionReadStorage.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        } else {
            openGallery()
        }
    }

    private fun hasExternalStoragePermission() = ContextCompat.checkSelfPermission(
        requireActivity(),
        Manifest.permission.READ_EXTERNAL_STORAGE
    ).let { result -> result == PackageManager.PERMISSION_GRANTED }

    private val permissionReadStorage =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantResults ->
            val readResult = grantResults[Manifest.permission.READ_EXTERNAL_STORAGE]
            if (readResult == true) {
                openGallery()
            } else {
                binding.root.showSnackbar(R.string.permission_read_denied, Snackbar.LENGTH_SHORT)
            }
        }

    val getContent = registerForActivityResult(GetImageContract()) { uri: Uri? ->
        if (uri != null) {
            try {
                val path = uri.parseImagePath(requireContext())
                vm.onPickedFromDevice(
                    ctx = context,
                    path = path
                )
            } catch (e: Exception) {
                toast("Error while parsing path for cover image")
                Timber.d(e, "Error while parsing path for cover image")
            }
        } else {
            toast("Error while upload cover image, URI is null")
            Timber.e("Error while upload cover image, URI is null")
        }
    }

    private fun openGallery() {
        getContent.launch(SELECT_IMAGE_CODE)
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPageIconPickerBinding = FragmentPageIconPickerBinding.inflate(
        inflater, container, false
    )

    companion object {
        private const val EMPTY_FILTER_TEXT = ""
        private const val PAGE_ICON_PICKER_DEFAULT_SPAN_COUNT = 6
        private const val EMOJI_RECYCLER_ITEM_VIEW_CACHE_SIZE = 2000
        private const val MISSING_TARGET_ERROR = "Missing target id"
        private const val MISSING_CONTEXT_ERROR = "Missing context id"
        private const val UNEXPECTED_VIEW_TYPE_MESSAGE = "Unexpected view type"

        const val ARG_CONTEXT_ID_KEY = "arg.picker.context.id"
        const val ARG_TARGET_ID_KEY = "arg.picker.target.id"

        private const val SELECT_IMAGE_CODE = 1
        private const val COULD_NOT_PARSE_PATH_ERROR = "Could not parse path to your image"
        private const val REQUEST_PERMISSION_CODE = 2
    }
}

open class ObjectIconPickerFragment : ObjectIconPickerBaseFragment() {

    @Inject
    lateinit var factory: ObjectIconPickerViewModelFactory
    override val vm by viewModels<ObjectIconPickerViewModel> { factory }

    override fun injectDependencies() {
        componentManager().objectIconPickerComponent.get(context).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectIconPickerComponent.release(context)
    }

    companion object {
        fun new(context: String, target: String) = ObjectIconPickerFragment().apply {
            arguments = bundleOf(
                ARG_CONTEXT_ID_KEY to context,
                ARG_TARGET_ID_KEY to target
            )
        }
    }
}