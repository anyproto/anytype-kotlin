package com.anytypeio.anytype.payments

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.UnfetchedProduct
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.payments.playbilling.BillingClientLifecycle
import com.anytypeio.anytype.payments.playbilling.BillingClientState
import junit.framework.TestCase.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import net.bytebuddy.utility.RandomString
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

/**
 * Covers [BillingClientLifecycle.onProductDetailsResponse], whose signature changed in GPBL 8.0.0
 * from a plain product list to a [QueryProductDetailsResult] carrying unfetched products too.
 *
 * The callback is synchronous -- it only reads the configured subscription ids and writes to
 * [BillingClientLifecycle.builderSubProductWithProductDetails] -- so it can be driven directly,
 * without connecting a BillingClient.
 */
class BillingProductDetailsResponseTest {

    private val dispatcher = StandardTestDispatcher(TestCoroutineScheduler())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatchers = AppCoroutineDispatchers(
        io = dispatcher,
        main = dispatcher,
        computation = dispatcher
    )

    private lateinit var billing: BillingClientLifecycle

    private val subscriptionId = "io.anytype.builder.${RandomString.make()}"

    @Before
    fun setUp() {
        billing = BillingClientLifecycle(
            dispatchers = dispatchers,
            applicationContext = mock<Context>(),
            scope = CoroutineScope(dispatcher)
        )
        billing.setupSubIds(listOf(subscriptionId))
    }

    @Test
    fun `starts out loading, so the assertions below are not vacuous`() {
        assertIs<BillingClientState.Loading>(billing.builderSubProductWithProductDetails.value)
    }

    @Test
    fun `resolved subscription is published as connected`() {
        val product = subscriptionProduct(subscriptionId)

        billing.onProductDetailsResponse(
            billingResult(BillingClient.BillingResponseCode.OK),
            queryResult(productDetails = listOf(product))
        )

        val state = assertIs<BillingClientState.Connected>(
            billing.builderSubProductWithProductDetails.value
        )
        assertEquals(listOf(product), state.productDetails)
    }

    /**
     * The case the unfetched list exists for: Google Play answered OK but could only resolve part
     * of what we asked for. The resolved subscription must still reach the UI.
     */
    @Test
    fun `partially resolved response still connects with the products that came back`() {
        val resolved = subscriptionProduct(subscriptionId)

        billing.onProductDetailsResponse(
            billingResult(BillingClient.BillingResponseCode.OK),
            queryResult(
                productDetails = listOf(resolved),
                unfetched = listOf(unfetchedProduct("io.anytype.co-creator"))
            )
        )

        val state = assertIs<BillingClientState.Connected>(
            billing.builderSubProductWithProductDetails.value
        )
        assertEquals(listOf(resolved), state.productDetails)
    }

    /**
     * Documents current behaviour rather than endorsing it: an OK response that resolves nothing
     * is reported as an error, so a single bad product id yields an error screen.
     */
    @Test
    fun `ok response that resolves nothing is reported as an error`() {
        billing.onProductDetailsResponse(
            billingResult(BillingClient.BillingResponseCode.OK),
            queryResult(unfetched = listOf(unfetchedProduct(subscriptionId)))
        )

        assertIs<BillingClientState.Error>(billing.builderSubProductWithProductDetails.value)
    }

    @Test
    fun `products outside the configured subscription ids are discarded`() {
        billing.onProductDetailsResponse(
            billingResult(BillingClient.BillingResponseCode.OK),
            queryResult(productDetails = listOf(subscriptionProduct("io.anytype.some.other.plan")))
        )

        assertIs<BillingClientState.Error>(billing.builderSubProductWithProductDetails.value)
    }

    @Test
    fun `failed response is reported as an error even when products are attached`() {
        billing.onProductDetailsResponse(
            billingResult(BillingClient.BillingResponseCode.NETWORK_ERROR),
            queryResult(productDetails = listOf(subscriptionProduct(subscriptionId)))
        )

        assertIs<BillingClientState.Error>(billing.builderSubProductWithProductDetails.value)
    }

    private fun billingResult(responseCode: Int): BillingResult =
        BillingResult.newBuilder().setResponseCode(responseCode).build()

    private fun queryResult(
        productDetails: List<ProductDetails> = emptyList(),
        unfetched: List<UnfetchedProduct> = emptyList()
    ): QueryProductDetailsResult = QueryProductDetailsResult.create(productDetails, unfetched)

    private fun subscriptionProduct(productId: String): ProductDetails = mock {
        on { getProductId() } doReturn productId
        on { getProductType() } doReturn BillingClient.ProductType.SUBS
    }

    private fun unfetchedProduct(productId: String): UnfetchedProduct = mock {
        on { getProductId() } doReturn productId
        on { getStatusCode() } doReturn BillingClient.BillingResponseCode.ITEM_UNAVAILABLE
    }
}
