package com.soniqxpro.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ListOpsTest {
    private val list = listOf("a", "b", "c", "d")

    @Test fun moveForward() = assertEquals(listOf("b", "c", "a", "d"), list.moved(0, 2))

    @Test fun moveBackward() = assertEquals(listOf("a", "d", "b", "c"), list.moved(3, 1))

    @Test fun sameIndexUnchanged() = assertEquals(list, list.moved(1, 1))

    @Test fun invalidIndicesUnchanged() {
        assertEquals(list, list.moved(-1, 2))
        assertEquals(list, list.moved(0, 9))
    }
}
