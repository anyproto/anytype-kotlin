package com.anytypeio.anytype.presentation.sets.main

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerCardSize
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubDataView
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.ViewerLayoutWidgetUi
import com.anytypeio.anytype.presentation.sets.viewer.ViewerEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argThat
import org.mockito.kotlin.never
import org.mockito.kotlin.verifyBlocking

/**
 * ViewModel-level coverage for the gallery card-size picker.
 *
 * Desktop offers three card sizes (small / medium / large), mobile only two: SMALL and MEDIUM
 * both render as two columns here, LARGE as one. Since `cardSize` lives on the viewer and is
 * synced across devices, a mobile write must not downgrade a size that mobile cannot even
 * represent — see [ObjectSetViewModel.onViewerLayoutWidgetAction].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ObjectSetGalleryCardSizeTest : ObjectSetViewModelTestSetup() {

    private val galleryViewerId = "gallery-view"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
    }

    /** Seeds the layout widget open on the gallery viewer without a middleware round-trip. */
    private fun ObjectSetViewModel.openLayoutWidgetOnGallery() {
        viewerLayoutWidgetState.value = viewerLayoutWidgetState.value.copy(
            showWidget = true,
            viewer = galleryViewerId,
            layoutType = Block.Content.DataView.Viewer.Type.GALLERY
        )
    }

    /**
     * Opens the set as a Collection whose only viewer is a gallery with the given card size, so
     * the reducer holds a real GALLERY viewer that the write path can resolve.
     */
    private fun stubGalleryCollection(cardSize: DVViewerCardSize) {
        val galleryViewer = DVViewer(
            id = galleryViewerId,
            name = "Gallery",
            type = Block.Content.DataView.Viewer.Type.GALLERY,
            sorts = emptyList(),
            filters = emptyList(),
            viewerRelations = emptyList(),
            cardSize = cardSize
        )
        stubOpenObject(
            doc = listOf(StubTitle(), StubDataView(views = listOf(galleryViewer), isCollection = true)),
            details = ObjectViewDetails(
                mapOf(root to mapOf(Relations.LAYOUT to ObjectType.Layout.COLLECTION.code.toDouble()))
            )
        )
    }

    private fun TestScope.givenGalleryOpenedWith(cardSize: DVViewerCardSize): ObjectSetViewModel {
        stubGalleryCollection(cardSize)
        val vm = givenViewModel()
        vm.onStart(view = galleryViewerId)
        advanceUntilIdle()
        vm.openLayoutWidgetOnGallery()
        return vm
    }

    @Test
    fun `selecting Small on a MEDIUM viewer does not write, since mobile renders both the same`() =
        runTest {
            val vm = givenGalleryOpenedWith(DVViewerCardSize.MEDIUM)

            vm.onViewerLayoutWidgetAction(
                ViewerLayoutWidgetUi.Action.CardSize(ViewerLayoutWidgetUi.State.CardSize.Small)
            )
            advanceUntilIdle()

            // Writing SMALL here would silently shrink the desktop layout while changing nothing
            // on mobile, so no viewer update must be sent at all.
            verifyBlocking(viewerDelegate, never()) {
                onEvent(argThat { this is ViewerEvent.UpdateView && viewer.id == galleryViewerId })
            }
        }

    @Test
    fun `selecting Small on a LARGE viewer writes SMALL`() = runTest {
        val vm = givenGalleryOpenedWith(DVViewerCardSize.LARGE)

        vm.onViewerLayoutWidgetAction(
            ViewerLayoutWidgetUi.Action.CardSize(ViewerLayoutWidgetUi.State.CardSize.Small)
        )
        advanceUntilIdle()

        verifyBlocking(viewerDelegate) {
            onEvent(
                argThat {
                    this is ViewerEvent.UpdateView &&
                            viewer.id == galleryViewerId &&
                            viewer.cardSize == DVViewerCardSize.SMALL
                }
            )
        }
    }

    @Test
    fun `selecting Large on a MEDIUM viewer writes LARGE`() = runTest {
        val vm = givenGalleryOpenedWith(DVViewerCardSize.MEDIUM)

        vm.onViewerLayoutWidgetAction(
            ViewerLayoutWidgetUi.Action.CardSize(ViewerLayoutWidgetUi.State.CardSize.Large)
        )
        advanceUntilIdle()

        // Large is a real change on mobile (one column), so it is persisted as usual.
        verifyBlocking(viewerDelegate) {
            onEvent(
                argThat {
                    this is ViewerEvent.UpdateView &&
                            viewer.id == galleryViewerId &&
                            viewer.cardSize == DVViewerCardSize.LARGE
                }
            )
        }
    }

    @Test
    fun `card size selection closes the card size picker`() = runTest {
        val vm = givenGalleryOpenedWith(DVViewerCardSize.MEDIUM)
        vm.onViewerLayoutWidgetAction(ViewerLayoutWidgetUi.Action.CardSizeMenu)

        vm.onViewerLayoutWidgetAction(
            ViewerLayoutWidgetUi.Action.CardSize(ViewerLayoutWidgetUi.State.CardSize.Small)
        )
        advanceUntilIdle()

        // The picker collapses even when the selection is intentionally not persisted.
        kotlin.test.assertFalse(vm.viewerLayoutWidgetState.value.showCardSize)
    }
}
