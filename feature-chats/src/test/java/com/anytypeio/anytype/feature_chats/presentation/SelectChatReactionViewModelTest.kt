package com.anytypeio.anytype.feature_chats.presentation

import app.cash.turbine.test
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.chats.ObserveRecentlyUsedChatReactions
import com.anytypeio.anytype.domain.chats.SetRecentlyUsedChatReactions
import com.anytypeio.anytype.domain.chats.ToggleChatMessageReaction
import com.anytypeio.anytype.emojifier.data.EmojiProvider
import com.anytypeio.anytype.emojifier.suggest.EmojiSuggester
import com.anytypeio.anytype.feature_chats.DefaultCoroutineTestRule
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
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
class SelectChatReactionViewModelTest {


    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()
    val dispatcher = AppCoroutineDispatchers(
        io = coroutineTestRule.dispatcher,
        computation = coroutineTestRule.dispatcher,
        main = coroutineTestRule.dispatcher
    )
    @Mock lateinit var emojiProvider: EmojiProvider
    @Mock lateinit var emojiSuggester: EmojiSuggester
    @Mock lateinit var toggleChatMessageReaction: ToggleChatMessageReaction
    @Mock lateinit var setRecentlyUsedChatReactions: SetRecentlyUsedChatReactions
    @Mock lateinit var observeRecentlyUsedChatReactions: ObserveRecentlyUsedChatReactions


    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        whenever(observeRecentlyUsedChatReactions.flow()).thenReturn(emptyFlow())
        whenever(emojiProvider.emojis).thenReturn(emptyArray<Array<String>>())
    }

    @Test
    fun `perform actions on emoji clicked`() = runTest {

        whenever(setRecentlyUsedChatReactions.async(any())).thenReturn(Resultat.Success(Unit))
        whenever(toggleChatMessageReaction.async(any())).thenReturn(Resultat.Success(Unit))


        val vm = createViewModel()

        vm.onEmojiClicked("test-emoji")

        vm.isDismissed.test {
            assertTrue(awaitItem())
        }

        advanceUntilIdle()

        verifyBlocking(setRecentlyUsedChatReactions, times(1)) { async(any()) }
        verifyBlocking(toggleChatMessageReaction, times(1)) { async(any()) }
        verifyBlocking(observeRecentlyUsedChatReactions, times(1)) { flow() }
        verifyBlocking(emojiProvider, times(1)) { emojis }

    }

    private fun createViewModel(): SelectChatReactionViewModel {
        val params = SelectChatReactionViewModel.Params(
            chat = "test-chat",
            msg = "test-msg"
        )
        return SelectChatReactionViewModel(
            vmParams = params,
            provider = emojiProvider,
            suggester = emojiSuggester,
            dispatchers = dispatcher,
            toggleChatMessageReaction = toggleChatMessageReaction,
            setRecentlyUsedChatReactions = setRecentlyUsedChatReactions,
            observeRecentlyUsedChatReactions = observeRecentlyUsedChatReactions
        )
    }
}