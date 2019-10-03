package com.agileburo.anytype.feature_login.ui.login.presentation.navigation

import io.reactivex.Observable

interface SupportNavigation {
    fun observeNavigation(): Observable<NavigationCommand>
}