package com.agileburo.anytype.presentation.auth.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_utils.common.Event
import com.agileburo.anytype.domain.auth.interactor.ObserveAccounts
import com.agileburo.anytype.domain.auth.interactor.StartLoadingAccounts
import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.image.AvatarBlob
import com.agileburo.anytype.domain.image.LoadImage
import com.agileburo.anytype.presentation.auth.model.SelectAccountView
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class SelectAccountViewModel(
    private val startLoadingAccounts: StartLoadingAccounts,
    private val observeAccounts: ObserveAccounts,
    private val loadImage: LoadImage
) : ViewModel(), SupportNavigation<Event<AppNavigation.Command>> {

    override val navigation: MutableLiveData<Event<AppNavigation.Command>> = MutableLiveData()

    val state by lazy { MutableLiveData<List<SelectAccountView>>() }

    private val accountChannel = Channel<Account>()
    private val imageChannel = Channel<AvatarBlob>()

    private val accounts = accountChannel
        .consumeAsFlow()
        .onEach { account -> loadImage(account) }
        .scan(emptyList<Account>()) { list, value -> list + value }
        .drop(1)

    private val images = imageChannel
        .consumeAsFlow()
        .scan(emptyList<AvatarBlob>()) { list, value -> list + value }

    init {
        startObservingAccounts()
        startLoadingAccount()

        accounts
            .combine(images) { acc, blobs ->
                acc.associateWith { account ->
                    blobs.firstOrNull { it.id == account.id }
                }
            }
            .onEach { result ->
                state.postValue(
                    result.map { (account, image) ->
                        SelectAccountView.AccountView(
                            id = account.id,
                            name = account.name,
                            image = image?.blob
                        )
                    }
                )
            }
            .catch { e -> Timber.e(e, "Error while creating view state") }
            .launchIn(viewModelScope)
    }

    private fun startLoadingAccount() {
        startLoadingAccounts.invoke(
            viewModelScope, StartLoadingAccounts.Params()
        ) { result ->
            result.either(
                fnL = { e -> Timber.e(e, "Error while account loading") },
                fnR = { Timber.d("Account loading successfully finished") }
            )
        }
    }

    private fun startObservingAccounts() {
        viewModelScope.launch {
            observeAccounts.build().collect { account ->
                accountChannel.send(account)
            }
        }
    }

    private fun loadImage(account: Account) {
        account.avatar?.let { avatar ->
            avatar.smallest?.let { smallest ->
                loadImage.invoke(
                    scope = viewModelScope,
                    params = LoadImage.Param(
                        id = avatar.id,
                        size = smallest
                    )
                ) { result ->
                    result.either(
                        fnL = { e ->
                            Timber.e(e, "Could not load avatar: ${avatar.id}")
                        },
                        fnR = { blob -> onImageLoaded(account, blob) }
                    )
                }
            } ?: Timber.d("No avatar sizes available for account: ${account.id}")
        } ?: Timber.d("Account had no avatar: ${account.id}")
    }

    private fun onImageLoaded(
        account: Account,
        blob: ByteArray
    ) {
        viewModelScope.launch {
            imageChannel.send(
                AvatarBlob(
                    id = account.id,
                    blob = blob
                )
            )
        }
    }

    fun onProfileClicked(id: String) {
        navigation.postValue(Event(AppNavigation.Command.SetupSelectedAccountScreen(id)))
    }

    fun onAddProfileClicked() {
        // TODO
    }

    override fun onCleared() {
        super.onCleared()
        imageChannel.close()
        accountChannel.close()
    }
}