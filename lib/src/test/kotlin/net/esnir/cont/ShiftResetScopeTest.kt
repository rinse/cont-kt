package net.esnir.cont

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class ShiftResetScopeTest {
    @Test
    fun `test for calling k`() {
        runBlocking {
            val actual: String = shiftResetScope {
                val r: String = shift { k ->
                    k("Call with k")
                }
                "[INFO] $r"
            }
            assertEquals(expected = "[INFO] Call with k", actual)
            println("Completed: $actual")
        }
    }

    @Test
    fun `test for not calling k`() {
        runBlocking {
            val actual: String = shiftResetScope {
                val r: Nothing = shift<Nothing> { k ->
                    "Returning a value" // not call k
                }
                "[INFO] $r"
            }
            assertEquals(expected = "Returning a value", actual)
            println("Completed: $actual")
        }
    }

    @Test
    fun `test for not calling k 2`() {
        runBlocking {
            val actual: String = shiftResetScope {
                val r: String = reset {
                    val r = shift<String> { k ->
                        "Returning a value" // not call k
                    }
                    "$r, this is skipped"
                }
                "[INFO] $r"
            }
            assertEquals(expected = "[INFO] Returning a value", actual)
            println("Completed: $actual")
        }
    }

    @Test
    fun `test with examples 1`() {
        runBlocking {
            val actual: Int = shiftResetScope {
                val a = reset {
                    val b = shift { k ->
                        k(2) * 3 // [* 3] is skipped
                    }
                    b * 5
                }
                a * 7
            }
            assertEquals(expected = 70, actual)
            println("Completed: $actual")
        }
    }
}
