package com.anytypeio.anytype.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.search.ObjectSearchSubscriptionContainer
import com.anytypeio.anytype.domain.widgets.CreateWidget
import com.anytypeio.anytype.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModel(
    private val configStorage: ConfigStorage,
    private val openObject: OpenObject,
    private val createWidget: CreateWidget,
    private val objectSearchSubscriptionContainer: ObjectSearchSubscriptionContainer
) : BaseViewModel() {

    val msg = MutableStateFlow("")

    val obj = MutableSharedFlow<ObjectView>()

    init {

        obj.map {
            it.blocks.mapNotNull { b ->
                val content = b.content
                if (content is Block.Content.Link) {

                }
                null
            }
        }

        viewModelScope.launch {
            val config = configStorage.get()
            openObject(config.widgets).collect { result ->
                when (result) {
                    is Resultat.Failure -> {
                        Timber.e(result.exception, "Error while opening object.")
                    }
                    is Resultat.Loading -> {
                        // Do nothing.
                    }
                    is Resultat.Success -> {
                        Timber.d("Blocks on start: ${result.value.blocks}")
                        obj.emit(result.value)
                    }
                }
            }
        }

//        proceedWithCreatingWidget()
    }

    private fun proceedWithCreatingWidget() {
        viewModelScope.launch {
            val config = configStorage.get()
            createWidget(
                CreateWidget.Params(
                    ctx = config.widgets,
                    source = "bafybasflgzee6ksloqb3bq5edtnfsithqvoymakywjocomehqbfs7gtc"
                )
            ).collect { s ->
                Timber.d("Status whule creating widget: $s")
            }
        }
    }

    fun onStart() {
        Timber.d("onStart")
    }

    class Factory(
        private val configStorage: ConfigStorage,
        private val openObject: OpenObject,
        private val createWidget: CreateWidget,
        private val objectSearchSubscriptionContainer: ObjectSearchSubscriptionContainer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(
            configStorage = configStorage,
            openObject = openObject,
            createWidget = createWidget,
            objectSearchSubscriptionContainer = objectSearchSubscriptionContainer
        ) as T
    }
}

data class Widget(
    val id: Id,
    val source: Id,
)