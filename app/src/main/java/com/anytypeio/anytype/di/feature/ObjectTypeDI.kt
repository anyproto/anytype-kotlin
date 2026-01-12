package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.presentation.objects.ObjectTypeChangeViewModel
import com.anytypeio.anytype.ui.objects.types.pickers.AppDefaultObjectTypeFragment
import com.anytypeio.anytype.ui.objects.types.pickers.CollectionAddObjectTypeFragment
import com.anytypeio.anytype.ui.objects.types.pickers.DataViewSelectSourceFragment
import com.anytypeio.anytype.ui.objects.types.pickers.EditorObjectTypeUpdateFragment
import com.anytypeio.anytype.ui.objects.types.pickers.EmptyDataViewSelectSourceFragment
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent

@Subcomponent(modules = [ObjectTypeChangeModule::class])
@PerScreen
interface ObjectTypeChangeSubComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(
            @BindsInstance vmParams: ObjectTypeChangeViewModel.VmParams
        ): ObjectTypeChangeSubComponent
    }

    fun inject(fragment: DataViewSelectSourceFragment)
    fun inject(fragment: EmptyDataViewSelectSourceFragment)
    fun inject(fragment: AppDefaultObjectTypeFragment)
    fun inject(fragment: CollectionAddObjectTypeFragment)
    fun inject(fragment: EditorObjectTypeUpdateFragment)
}

@Module
object ObjectTypeChangeModule {

}