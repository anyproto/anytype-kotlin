package com.anytypeio.anytype.ui.editor.modals

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.const.FileConstants.getPermissionToRequestForImages
import com.anytypeio.anytype.core_utils.ext.GetImageContract
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.parseImagePath
import com.anytypeio.anytype.core_utils.ext.showSnackbar
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetTextInputFragment
import com.anytypeio.anytype.databinding.FragmentPageIconPickerBinding
import com.anytypeio.anytype.library_page_icon_picker_widget.ui.DocumentEmojiIconPickerAdapter
import com.anytypeio.anytype.presentation.editor.picker.EmojiPickerView.Companion.HOLDER_EMOJI_CATEGORY_HEADER
import com.anytypeio.anytype.presentation.editor.picker.EmojiPickerView.Companion.HOLDER_EMOJI_ITEM
import com.anytypeio.anytype.presentation.picker.IconPickerViewModel
import com.anytypeio.anytype.presentation.picker.IconPickerViewModel.ViewState
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

abstract class IconPickerFragmentBase<T> :
    BaseBottomSheetTextInputFragment<FragmentPageIconPickerBinding>() {

    protected val context: Id
        get() = arg(ARG_CONTEXT_ID_KEY)

    /**
     * The target for which we choose icon
     * i.e. Object, callout text block
     */
    protected abstract val target: T

    abstract val vm: IconPickerViewModel<T>

    private val emojiPickerAdapter by lazy {
        DocumentEmojiIconPickerAdapter(
            views = emptyList(),
            onEmojiClicked = { unicode ->
                vm.onEmojiClicked(
                    unicode = unicode,
                    iconable = target
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
            btnRemoveIcon.setOnClickListener { vm.onRemoveClicked(target) }
            tvTabRandom.setOnClickListener { vm.onRandomEmoji(target) }
            tvTabUpload.setOnClickListener { proceedWithImagePick() }
        }
        skipCollapsed()
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
            permissionReadStorage.launch(arrayOf(getPermissionToRequestForImages()))
        } else {
            openGallery()
        }
    }

    private fun hasExternalStoragePermission() = ContextCompat.checkSelfPermission(
        requireActivity(),
        getPermissionToRequestForImages()
    ).let { result -> result == PackageManager.PERMISSION_GRANTED }

    private val permissionReadStorage =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantResults ->
            val readResult = grantResults[getPermissionToRequestForImages()]
            if (readResult == true) {
                openGallery()
            } else {
                binding.root.showSnackbar(R.string.permission_read_denied, Snackbar.LENGTH_SHORT)
            }
        }

    private val getContent = registerForActivityResult(GetImageContract()) { uri: Uri? ->
        if (uri != null) {
            try {
                val path = uri.parseImagePath(requireContext())
                vm.onPickedFromDevice(
                    iconable = target,
                    path = path
                )
            } catch (e: Exception) {
                toast("Error while parsing path for cover image")
                Timber.d(e, "Error while parsing path for cover image")
            }
        } else {
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
        private const val MISSING_CONTEXT_ERROR = "Missing context id"
        private const val UNEXPECTED_VIEW_TYPE_MESSAGE = "Unexpected view type"

        const val ARG_CONTEXT_ID_KEY = "arg.picker.context.id"

        private const val SELECT_IMAGE_CODE = 1
    }
}

