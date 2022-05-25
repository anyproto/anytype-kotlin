package com.anytypeio.anytype.core_utils.tools

interface Counter {

    /**
     * @return current value
     */
    fun current(): Int

    /**
     * Increments current value
     */
    fun inc()

    /**
     * Resets current value.
     */
    fun reset()

    class Default : Counter {

        private var number = 0

        override fun current(): Int = number

        override fun inc() {
            number = number.inc()
        }

        override fun reset() {
            number = 0
        }
    }
}