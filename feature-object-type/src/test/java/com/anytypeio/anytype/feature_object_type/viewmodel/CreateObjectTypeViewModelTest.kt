package com.anytypeio.anytype.feature_object_type.viewmodel

import app.cash.turbine.test
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.types.CreateObjectType
import com.anytypeio.anytype.feature_object_type.DefaultCoroutineTestRule
import com.anytypeio.anytype.feature_object_type.ui.UiIconsPickerState
import com.anytypeio.anytype.feature_object_type.ui.create.UiTypeSetupTitleAndIconState
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIconColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class CreateObjectTypeViewModelTest {

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()
    val spaceId = "test-space-Id"

    @Mock lateinit var createObjectType: CreateObjectType
    @Mock lateinit var analytics: Analytics
    @Mock lateinit var analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate


    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }


    @Test
    fun `should emit Dismiss command on dismiss`() = runTest {

        val vm = createViewModel()

        vm.onDismiss()

        vm.commands.test {
            val item = awaitItem()
            assert(item is CreateTypeCommand.Dismiss)
        }
    }

    @Test
    fun `should change ui state on remove icon`() = runTest {

        val vm = createViewModel()

        vm.onRemoveIcon()

        vm.uiState.test {
            val item = awaitItem() as? UiTypeSetupTitleAndIconState.Visible.CreateNewType
            assert(item?.icon == ObjectIcon.TypeIcon.Default.DEFAULT)
        }

        vm.uiIconsPickerScreen.test {
            val item = awaitItem()
            assert(item is UiIconsPickerState.Hidden)
        }
    }

    @Test
    fun `should change ui state on new icon picked`() = runTest {

        val iconName = "test-icon-name"
        val vm = createViewModel()

        vm.onNewIconPicked(iconName, CustomIconColor.Red)

        vm.uiState.test {
            val item = awaitItem() as? UiTypeSetupTitleAndIconState.Visible.CreateNewType
            assertEquals(item?.icon?.rawValue,iconName)
            assertEquals(item?.icon?.color, CustomIconColor.Red)
        }

        vm.uiIconsPickerScreen.test {
            val item = awaitItem()
            assert(item is UiIconsPickerState.Hidden)
        }
    }

    @Test
    fun `should change make visible on icon click`() = runTest {

        val vm = createViewModel()
        vm.onIconClicked()
        vm.uiIconsPickerScreen.test {
            val item = awaitItem()
            assert(item is UiIconsPickerState.Visible)
        }
    }

    @Test
    fun `should change hide icon on dismiss icon picker`() = runTest {

        val vm = createViewModel()
        vm.onDismissIconPicker()
        vm.uiIconsPickerScreen.test {
            val item = awaitItem()
            assert(item is UiIconsPickerState.Hidden)
        }
    }

    @Test
    fun `should create new type on button click`() = runTest {

        val title = "test-title"
        val plurals = "test-plurals"
        val objectId = "test-object-Id"

        whenever(createObjectType.execute(any())).thenReturn(Resultat.Success(objectId))
        whenever(analyticSpaceHelperDelegate.provideParams(any())).thenReturn(
            AnalyticSpaceHelperDelegate.Params("test-param-permission","test-param-spaceType"))

        val vm = createViewModel()
        vm.onButtonClicked(title, plurals)

        advanceUntilIdle()
        verifyBlocking(createObjectType, times(1)) { execute(any()) }

    }



    private fun createViewModel(): CreateObjectTypeViewModel {
        val vmParams = CreateTypeVmParams(
            spaceId = spaceId
        )
        return CreateObjectTypeViewModel(
            vmParams = vmParams,
            createObjectType = createObjectType,
            analytics = analytics,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate
        )
    }

}