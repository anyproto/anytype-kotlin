package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.domain.relations.AddNewRelationToObject
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.relations.model.RelationView
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class RelationCreateFromScratchBaseViewModel : BaseViewModel() {

    protected val name = MutableStateFlow("")

    val views = MutableStateFlow(
        Relation.Format.values().map { format ->
            RelationView.CreateFromScratch(
                format = format,
                isSelected = format == Relation.Format.LONG_TEXT
            )
        }
    )

    val isActionButtonEnabled = name.map { it.isNotEmpty() }
    val isDismissed = MutableStateFlow(false)

    fun onRelationFormatClicked(format: RelationFormat) {
        views.value = views.value.map { view ->
            view.copy(isSelected = view.format == format)
        }
    }

    fun onNameChanged(input: String) {
        name.value = input
    }

    abstract fun onCreateRelationClicked(ctx: Id)

    companion object {
        const val ACTION_FAILED_ERROR =
            "Error while creating a new relation. Please, try again later"
    }
}

class RelationCreateFromScratchForObjectViewModel(
    private val addNewRelationToObject: AddNewRelationToObject,
    private val dispatcher: Dispatcher<Payload>
) : RelationCreateFromScratchBaseViewModel() {

    override fun onCreateRelationClicked(ctx: Id) {
        viewModelScope.launch {
            addNewRelationToObject(
                AddNewRelationToObject.Params(
                    ctx = ctx,
                    format = views.value.first { it.isSelected }.format,
                    name = name.value
                )
            ).process(
                success = {
                    dispatcher.send(it).also { isDismissed.value = true }
                },
                failure = {
                    Timber.e(it, ACTION_FAILED_ERROR).also { _toasts.emit(ACTION_FAILED_ERROR) }
                }
            )
        }
    }

    class Factory(
        private val addNewRelationToObject: AddNewRelationToObject,
        private val dispatcher: Dispatcher<Payload>
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return RelationCreateFromScratchForObjectViewModel(
                dispatcher = dispatcher,
                addNewRelationToObject = addNewRelationToObject
            ) as T
        }
    }
}