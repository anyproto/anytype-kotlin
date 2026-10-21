package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.navigation.DefaultNavigationBackStackInspector
import com.anytypeio.anytype.presentation.navigation.backstack.NavigationBackStackInspector
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class NavigationModule {

    @Binds
    @Singleton
    abstract fun bindNavigationBackStackInspector(
        impl: DefaultNavigationBackStackInspector
    ): NavigationBackStackInspector
}
