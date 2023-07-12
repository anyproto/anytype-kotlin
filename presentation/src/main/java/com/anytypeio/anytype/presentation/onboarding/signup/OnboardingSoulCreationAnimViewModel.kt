package com.anytypeio.anytype.presentation.onboarding.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.profile.ProfileIconView
import com.anytypeio.anytype.presentation.profile.profileIcon
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OnboardingSoulCreationAnimViewModel @Inject constructor(
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val gradientProvider: SpaceGradientProvider,
    private val configStorage: ConfigStorage,
    private val urlBuilder: UrlBuilder
) : ViewModel() {

    val accountData = storelessSubscriptionContainer.subscribe(
        StoreSearchByIdsParams(
            subscription = ONBOARDING_SUBSCRIPTION_ID,
            keys = listOf(
                Relations.ID,
                Relations.NAME,
                Relations.ICON_IMAGE,
                Relations.ICON_EMOJI,
                Relations.ICON_OPTION
            ),
            targets = listOf(configStorage.get().profile)
        )
    ).map { result ->
        val obj = result.firstOrNull()
        Resultat.Success(
            AccountData(
                name = obj?.name.orEmpty(),
                icon = (obj?.profileIcon(urlBuilder, gradientProvider) as? ProfileIconView.Gradient)
                    ?: ProfileIconView.Gradient(
                        gradientProvider.get(0.0).from,
                        gradientProvider.get(0.0).to
                    )
            )
        )
    }.catch {
        Resultat.Failure(it)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_SUBSCRIPTION_TIMEOUT),
        Resultat.loading()
    )

    data class AccountData(val name: String, val icon: ProfileIconView.Gradient)

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            storelessSubscriptionContainer.unsubscribe(listOf(ONBOARDING_SUBSCRIPTION_ID))
        }
    }

    class Factory @Inject constructor(
        private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
        private val gradientProvider: SpaceGradientProvider,
        private val configStorage: ConfigStorage,
        private val urlBuilder: UrlBuilder
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingSoulCreationAnimViewModel(
                storelessSubscriptionContainer = storelessSubscriptionContainer,
                gradientProvider = gradientProvider,
                configStorage = configStorage,
                urlBuilder = urlBuilder
            ) as T
        }
    }
}

private const val ONBOARDING_SUBSCRIPTION_ID = "onboarding_sub_id"
private const val STOP_SUBSCRIPTION_TIMEOUT = 3000L