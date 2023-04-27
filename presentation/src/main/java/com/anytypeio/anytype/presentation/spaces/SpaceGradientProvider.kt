package com.anytypeio.anytype.presentation.spaces

import javax.inject.Inject

interface SpaceGradientProvider {

    fun get(id: Double): Gradient

    fun randomId(): Int

    class Impl @Inject constructor(): SpaceGradientProvider {

        override fun get(id: Double): Gradient {
            return gradients[id] ?: Gradient("#F6EB7D", "#CBD2FA")
        }

        override fun randomId(): Int {
            return gradients.keys.random().toInt()
        }

        private val gradients = mapOf(
            1.0 to Gradient("#F6EB7D", "#CBD2FA"),
            2.0 to Gradient("#112156", "#CBD2FA"),
            3.0 to Gradient("#FFA15E", "#CBD2FA"),
            4.0 to Gradient("#BC3A54", "#CBD2FA"),
            5.0 to Gradient("#4D7AFF", "#CBD2FA"),
            6.0 to Gradient("#F6EB7D", "#BC3A54"),
            7.0 to Gradient("#112156", "#BC3A54"),
            8.0 to Gradient("#CBD2FA", "#BC3A54"),
            9.0 to Gradient("#FFA25E", "#BC3A54"),
            10.0 to Gradient("#4D7AFF", "#BC3A54"),
            11.0 to Gradient("#CBD2FA", "#FFA25E"),
            12.0 to Gradient("#4D7AFF", "#FFA25E"),
            13.0 to Gradient("#BC3A54", "#FFA25E"),
            14.0 to Gradient("#F6EB7D", "#FFA25E"),
            15.0 to Gradient("#BC3A54", "#F6EB7D"),
            16.0 to Gradient("#4D7AFF", "#F6EB7D")
        )

    }

    data class Gradient(val from: String, val to: String)

}