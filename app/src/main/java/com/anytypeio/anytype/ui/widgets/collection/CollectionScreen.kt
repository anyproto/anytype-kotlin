package com.anytypeio.anytype.ui.widgets.collection

import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.widgets.ObjectIconWidget
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.setVisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.ui.search.ObjectSearchFragment

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun CollectionScreen(vm: CollectionViewModel) {

    val list by vm.views.collectAsStateWithLifecycle()

    Column(
        Modifier.background(color = colorResource(R.color.background_primary))
    )
    {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.widget_search_view, null)
                val clearSearchText = view.findViewById<View>(R.id.clearSearchText)
                val filterInputField = view.findViewById<EditText>(R.id.filterInputField)
                filterInputField.setHint(R.string.search)
                filterInputField.imeOptions = EditorInfo.IME_ACTION_DONE
                filterInputField.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        return@setOnEditorActionListener false
                    }
                    true
                }
                clearSearchText.setOnClickListener {
                    filterInputField.setText(ObjectSearchFragment.EMPTY_FILTER_TEXT)
                    clearSearchText.invisible()
                }
                filterInputField.doAfterTextChanged { newText ->
                    if (newText != null) {
                        vm.onSearchTextChanged(newText.toString())
                    }
                    if (newText.isNullOrEmpty()) {
                        clearSearchText.invisible()
                    } else {
                        clearSearchText.visible()
                    }
                }
                view
            },
            update = {
                it.findViewById<View>(R.id.progressBar).setVisible(list.isLoading)
            }
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            items(
                items = list.getOrNull() ?: emptyList(), itemContent = { item ->
                    AndroidView(
                        modifier = Modifier.fillMaxWidth(),
                        factory = { context ->
                            LayoutInflater.from(context)
                                .inflate(
                                    R.layout.item_list_object,
                                    null
                                )
                        },
                        update = {
                            it.findViewById<TextView>(R.id.tvTitle).text = item.name
                            it.findViewById<ObjectIconWidget>(R.id.ivIcon).setIcon(item.icon)
                            it.findViewById<TextView>(R.id.tvSubtitle).text = item.typeName
                            it.setOnClickListener { vm.onObjectClicked(item) }
                        }
                    )
                })
        }
    }
}
