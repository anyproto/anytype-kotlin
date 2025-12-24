package com.anytypeio.anytype.feature_chats.presentation

import app.cash.turbine.test
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.emojifier.data.EmojiProvider
import com.anytypeio.anytype.emojifier.suggest.EmojiSuggester
import com.anytypeio.anytype.feature_chats.DefaultCoroutineTestRule
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SelectChatIconViewModelTest {

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    private val testDispatcher = coroutineTestRule.dispatcher

    @Mock lateinit var provider: EmojiProvider
    @Mock lateinit var suggester: EmojiSuggester
    private val dispatchers: AppCoroutineDispatchers = AppCoroutineDispatchers(
        io = testDispatcher,
        main = testDispatcher,
        computation = testDispatcher
    )

    private val emojis = arrayOf(
            arrayOf(
            "😀",
            "😃",
            "😄",
            "😁",
            "😆",
            "😅"
        )
    )

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        whenever(provider.emojis).thenReturn(emojis)
    }


    @Test
    fun `should emit clicked emoji`() = runTest {
        val emoji = emojis[0][0]
        val vm = createViewModel()
        vm.onEmojiClicked(emoji)

        assertEquals(emoji, vm.emojiSelected.first())
        assertTrue(vm.isDismissed.first())
    }




    private fun createViewModel(): SelectChatIconViewModel {
        return SelectChatIconViewModel(
            dispatchers = dispatchers,
            provider = provider,
            suggester = suggester
        )
    }
}