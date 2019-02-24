package com.agileburo.anytype.di.app

import com.agileburo.anytype.MainActivity
import com.agileburo.anytype.core_utils.di.PerScreen
import dagger.Subcomponent

@PerScreen
@Subcomponent(modules = [MainScreenModule::class])
interface MainScreenComponent{

    fun inject(mainScreen: MainActivity)
}