package com.agileburo.anytype.feature_editor.presentation

import com.agileburo.anytype.core_utils.TrampolineSchedulerProvider
import com.agileburo.anytype.feature_editor.domain.*
import com.agileburo.anytype.feature_editor.factory.BlockFactory
import com.agileburo.anytype.feature_editor.presentation.converter.BlockContentTypeConverter
import com.agileburo.anytype.feature_editor.presentation.converter.BlockContentTypeConverterImpl
import com.agileburo.anytype.feature_editor.presentation.mvvm.EditorViewModel
import com.agileburo.anytype.feature_editor.ui.EditBlockAction
import com.agileburo.anytype.feature_editor.ui.EditorState
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 31.03.2019.
 */
class EditorViewModelTest {

    lateinit var viewModel: EditorViewModel
    lateinit var contentTypeConverter: BlockContentTypeConverter

    @Mock
    lateinit var interactor: EditorInteractor

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        contentTypeConverter = BlockContentTypeConverterImpl()
    }

    @Test
    fun `should start fetching blocks when view model is initialized`() {

        val blocks = listOf(BlockFactory.makeBlock())

        stubGetBlocks(Single.just(blocks))

        viewModel = EditorViewModel(
            interactor = interactor,
            contentTypeConverter = contentTypeConverter,
            schedulerProvider = TrampolineSchedulerProvider()
        )

        verify(interactor, times(1)).getBlocks()

    }

    @Test
    fun `should return list of blocks`() {

        val blocks = listOf(BlockFactory.makeBlock())

        stubGetBlocks(Single.just(blocks))

        viewModel = EditorViewModel(
            interactor = interactor,
            contentTypeConverter = contentTypeConverter,
            schedulerProvider = TrampolineSchedulerProvider()
        )

        val testObserver = viewModel.observeState().test()

        testObserver
            .assertNoErrors()
            .assertValueCount(1)
            .assertValue(EditorState.Result(blocks = blocks))
    }

    @Test
    fun `should return list with size - 1 after block removal`() {

        val blocks = listOf(BlockFactory.makeBlock(), BlockFactory.makeBlock())

        stubGetBlocks(Single.just(blocks))

        viewModel = EditorViewModel(
            interactor = interactor,
            contentTypeConverter = contentTypeConverter,
            schedulerProvider = TrampolineSchedulerProvider()
        )

        val testObserver = viewModel.observeState().test()

        viewModel.onContentTypeClicked(EditBlockAction.ArchiveBlock(blocks[0].id))

        testObserver
            .assertNoErrors()
            .assertValueCount(4)
            .assertValueAt(1, EditorState.Updates(listOf(blocks.last())))
    }

    @Test
    fun `should hide toolbar after delete block`() {

        val blocks = listOf(BlockFactory.makeBlock())

        stubGetBlocks(Single.just(blocks))

        viewModel = EditorViewModel(
            interactor = interactor,
            contentTypeConverter = contentTypeConverter,
            schedulerProvider = TrampolineSchedulerProvider()
        )

        val testObserver = viewModel.observeState().test()

        viewModel.onContentTypeClicked(EditBlockAction.ArchiveBlock(blocks[0].id))

        testObserver.assertNoErrors()
        testObserver.assertValueAt(2, EditorState.HideToolbar)
    }

    private fun stubGetBlocks(single : Single<List<Block>>) {
        Mockito.`when`(interactor.getBlocks()).thenReturn(single)
    }
}