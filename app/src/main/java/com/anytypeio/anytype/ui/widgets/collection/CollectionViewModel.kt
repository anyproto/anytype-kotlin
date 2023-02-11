package com.anytypeio.anytype.ui.widgets.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.dashboard.DEFAULT_KEYS
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.objects.getProperName
import com.anytypeio.anytype.presentation.objects.toViews
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class CollectionViewModel(
    private val container: StorelessSubscriptionContainer,
    private val workspaceManager: WorkspaceManager,
    private val urlBuilder: UrlBuilder,
    private val getObjectTypes: GetObjectTypes,
    private val dispatchers: AppCoroutineDispatchers
) : ViewModel() {

    val commands = MutableSharedFlow<Command>()

    private val jobs = mutableListOf<Job>()
    private val queryFlow: MutableStateFlow<String> = MutableStateFlow("")

    private suspend fun objectTypes(): StateFlow<List<ObjectWrapper.Type>> {
        val params = GetObjectTypes.Params(
            sorts = emptyList(),
            filters = ObjectSearchConstants.filterObjectTypeLibrary(
                workspaceId = workspaceManager.getCurrentWorkspace()
            ),
            keys = ObjectSearchConstants.defaultKeysObjectType
        )
        return getObjectTypes.asFlow(params).stateIn(viewModelScope)
    }

    @FlowPreview
    private fun queryFlow() = queryFlow
        .debounce(DEBOUNCE_TIMEOUT)
        .distinctUntilChanged()
        .onEach { views.value = Resultat.loading() }

    val views = MutableStateFlow<Resultat<List<DefaultObjectView>>>(Resultat.loading())

    fun onStop(subscription: Id) {
        viewModelScope.launch {
            jobs.clear()
            container.unsubscribe(listOf(subscription))
        }
    }

    fun onStart(subscription: Id) {
        subscribeObjects(subscription)
    }

    private suspend fun buildSearchParams(subscription: Id): StoreSearchParams {
        return StoreSearchParams(
            subscription = subscription,
            keys = DEFAULT_KEYS + Relations.LAST_MODIFIED_DATE,
            filters = ObjectSearchConstants.filterTabSets(
                workspaceId = workspaceManager.getCurrentWorkspace()
            ),
            sorts = ObjectSearchConstants.sortTabRecent,
            limit = ObjectSearchConstants.limitTabRecent,
            offset = 0
        )
    }

    @OptIn(FlowPreview::class)
    private fun subscribeObjects(subscription: Id) {
        jobs += viewModelScope.launch {
            combine(
                container.subscribe(buildSearchParams(subscription)),
                queryFlow(),
                objectTypes()
            ) { objs, query, types ->
                objs.filter {
                    it.description
                    it.getProperName()
                        .lowercase(Locale.getDefault())
                        .contains(query.lowercase(Locale.getDefault()))
                }.toViews(urlBuilder, types)
            }
                .flowOn(dispatchers.io)
                .collect {
                    views.value = Resultat.success(it)
                }
        }
    }


    fun onSearchTextChanged(search: String) {
        queryFlow.value = search
    }

    fun onObjectClicked(view: DefaultObjectView) {
        inCoroutine {
            val target = view.id
            when (view.layout) {
                ObjectType.Layout.PROFILE,
                ObjectType.Layout.BASIC,
                ObjectType.Layout.TODO,
                ObjectType.Layout.NOTE,
                ObjectType.Layout.FILE,
                ObjectType.Layout.IMAGE,
                ObjectType.Layout.BOOKMARK -> {
                    commands.emit(Command.LaunchDocument(id = target))
                }
                ObjectType.Layout.SET -> {
                    commands.emit(Command.LaunchObjectSet(target = target))
                }
                else -> {
                    Timber.e("Unexpected layout: ${view.layout}")
                }
            }
        }
    }

    private inline fun inCoroutine(crossinline block: suspend CoroutineScope.() -> Unit) {
        jobs += viewModelScope.launch { block() }
    }

    class Factory @Inject constructor(
        private val container: StorelessSubscriptionContainer,
        private val workspaceManager: WorkspaceManager,
        private val urlBuilder: UrlBuilder,
        private val getObjectTypes: GetObjectTypes,
        private val dispatchers: AppCoroutineDispatchers,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CollectionViewModel(
                container = container,
                workspaceManager = workspaceManager,
                urlBuilder = urlBuilder,
                getObjectTypes = getObjectTypes,
                dispatchers = dispatchers
            ) as T
        }
    }

    sealed class Command {
        data class LaunchDocument(val id: Id) : Command()
        data class LaunchObjectSet(val target: Id) : Command()
    }
}

private const val DEBOUNCE_TIMEOUT = 300L