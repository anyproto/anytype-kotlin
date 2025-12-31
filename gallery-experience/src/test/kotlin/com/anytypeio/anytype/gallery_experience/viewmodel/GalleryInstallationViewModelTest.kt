package com.anytypeio.anytype.gallery_experience.viewmodel

import app.cash.turbine.test
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ManifestInfo
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.gallery_experience.DownloadGalleryManifest
import com.anytypeio.anytype.domain.gallery_experience.ImportExperience
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.spaces.CreateSpace
import com.anytypeio.anytype.domain.spaces.GetSpaceViews
import com.anytypeio.anytype.domain.workspace.EventProcessImportChannel
import com.anytypeio.anytype.gallery_experience.DefaultCoroutineTestRule
import com.anytypeio.anytype.gallery_experience.models.GalleryInstallationNavigation
import com.anytypeio.anytype.gallery_experience.models.GalleryInstallationState
import com.anytypeio.anytype.gallery_experience.viewmodel.GalleryInstallationViewModel.ViewModelParams
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
class GalleryInstallationViewModelTest {

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Mock lateinit var downloadGalleryManifest: DownloadGalleryManifest
    @Mock lateinit var importExperience: ImportExperience
    @Mock lateinit var analytics: Analytics
    @Mock lateinit var getSpaceViews: GetSpaceViews
    @Mock lateinit var createSpace: CreateSpace
    @Mock lateinit var urlBuilder: UrlBuilder
    @Mock lateinit var spaceGradientProvider: SpaceGradientProvider
    @Mock lateinit var userPermissionProvider: UserPermissionProvider
    @Mock lateinit var eventProcessChannel: EventProcessImportChannel
    @Mock lateinit var configStorage: ConfigStorage

    private val vmParmas = ViewModelParams(
        deepLinkType = "test-deeplink-type",
        deepLinkSource = "test-deeplink-source"
    )



    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should perform actions on init`() = runTest {

        mockInit()
        val vm = createViewModel()
        advanceUntilIdle()
        vm.mainState.test {
            val item = awaitItem()
            assert(item is GalleryInstallationState.Success)
        }
        verifyBlocking(downloadGalleryManifest, times(1)) { async(any()) }

    }


    @Test
    fun `should get space views on install clicked`() = runTest {
        val testSpaceViews = ObjectWrapper.SpaceView(
            mapOf(

                Relations.ID to "test-id",
                Relations.TARGET_SPACE_ID to "test-target-id"
            )
        )
        val testConfig = stubConfig()
        mockInit()
        whenever(getSpaceViews.async(any())).thenReturn(Resultat.success(listOf(testSpaceViews)))
        whenever(userPermissionProvider.get(SpaceId(testSpaceViews.targetSpaceId!!))).thenReturn(SpaceMemberPermissions.OWNER)
        whenever(configStorage.getOrNull()).thenReturn(testConfig)
        val vm = createViewModel()
        vm.onInstallClicked()
        advanceUntilIdle()
        vm.spacesViewState.test {
            val item = awaitItem()
            assertEquals(1,item.spaces.size)
        }
        verifyBlocking(getSpaceViews, times(1)) { async(any()) }
    }

    @Test
    fun `should create new space when new space clicked`() = runTest {

        mockInit()
        whenever(createSpace.async(any())).thenReturn(
            Resultat.success(
                Command.CreateSpace.Result(
                    SpaceId("test-space-id")
                )
            )
        )
        whenever(importExperience.stream(any())).thenReturn(flowOf(Resultat.success(Unit)))

        val vm = createViewModel()
        vm.mainState.value = GalleryInstallationState.Success(
            info = ManifestInfo(
                title = "test-title",
                schema = "test-schema",
                id = "test-id",
                name = "test-name",
                author = "test-author",
                license = "test-license",
                description = "test-description",
                screenshots = listOf("test-screenshot"),
                downloadLink = "test-download-link",
                fileSize = 100,
                categories = listOf("test-category"),
                language = "test-language"
            )
        )
        vm.onNewSpaceClick()
        advanceUntilIdle()
        verifyBlocking(createSpace, times(1)) { async(any()) }
    }

    private suspend fun mockInit() {
        whenever(downloadGalleryManifest.async(any())).thenReturn(
            Resultat.success(
                ManifestInfo(
                    title = "test-title",
                    schema = "test-schema",
                    id = "test-id",
                    name = "test-name",
                    author = "test-author",
                    license = "test-license",
                    description = "test-description",
                    screenshots = listOf("test-screenshot"),
                    downloadLink = "test-download-link",
                    fileSize = 100,
                    categories = listOf("test-category"),
                    language = "test-language"
                )
            )
        )
    }

    fun stubConfig(
        home: Id = randomUuid(),
        profile: Id = randomUuid(),
        gateway: Url = randomUuid(),
        spaceView: Id = randomUuid(),
        widgets: Id = randomUuid(),
        analytics: Id = randomUuid(),
        device: Id = randomUuid(),
        space: Id = randomUuid(),
        techSpace: Id = randomUuid(),
        network: Id = randomUuid(),
        workspaceObjectId: Id = randomUuid()
    ) : Config = Config(
        home = home,
        profile = profile,
        gateway = gateway,
        spaceView = spaceView,
        space = space,
        techSpace = techSpace,
        widgets = widgets,
        analytics = analytics,
        device = device,
        network = network,
        workspaceObjectId = workspaceObjectId,
        spaceChatId = null
    )

    private fun randomUuid(): String {
        return UUID.randomUUID().toString()
    }


    private fun createViewModel(): GalleryInstallationViewModel {
        return GalleryInstallationViewModel(
            viewModelParams = vmParmas,
            downloadGalleryManifest = downloadGalleryManifest,
            importExperience = importExperience,
            analytics = analytics,
            getSpaceViews = getSpaceViews,
            createSpace = createSpace,
            urlBuilder = urlBuilder,
            spaceGradientProvider = spaceGradientProvider,
            userPermissionProvider = userPermissionProvider,
            eventProcessChannel = eventProcessChannel,
            configStorage = configStorage
        )
    }
}