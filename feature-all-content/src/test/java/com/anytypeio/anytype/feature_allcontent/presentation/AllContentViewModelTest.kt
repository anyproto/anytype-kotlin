package com.anytypeio.anytype.feature_allcontent.presentation

import com.anytypeio.anytype.core_models.primitives.SpaceId
import kotlin.test.Test
import kotlin.test.assertEquals
import net.bytebuddy.utility.RandomString
import org.junit.Before

class AllContentViewModelTest {

    private val spaceId = SpaceId("spaceId-${RandomString.make()}")
    private val vmParams = AllContentViewModel.VmParams(spaceId = spaceId)

    private lateinit var viewModel: AllContentViewModel

    @Before
    fun setup() {
        viewModel = AllContentViewModel(vmParams = vmParams)
    }

    @Test
    fun `should has proper vm params`() {
        //assertEquals(expected = spaceId, actual = viewModel.)
    }
}