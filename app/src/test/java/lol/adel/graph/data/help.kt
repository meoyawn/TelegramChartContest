package lol.adel.graph.data

import org.junit.Assert

fun assertEquals(expected: Float, actual: Float): Unit =
    Assert.assertEquals(expected, actual, 0.000001f)
