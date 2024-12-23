package net.esnir.cont

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class ControlOperationTest {
    fun interface ControlOperation {
        suspend fun throwException(message: String): Int
    }

    suspend fun ControlOperation.safeDiv(n: Int, m: Int): Int {
        if (m == 0) {
            return throwException("Zero Division")
        }
        return n / m
    }

    @Test
    fun `test with k, succeeds`() {
        runBlocking {
            val actual: Int = shiftResetScope {
                val context = ControlOperation { message ->
                    shift { _ ->
                        "Error! Message: $message"
                        10000
                    }
                }
                with(context) {
                    safeDiv(10, 2)
                }
            }
            assertEquals(expected = 5, actual)
            println("Completed: $actual")
        }
    }

    @Test
    fun `test with k, exception thrown`() {
        runBlocking {
            val actual: Int = shiftResetScope {
                val context = ControlOperation { message ->
                    shift { _ ->
                        "Error! Message: $message"
                        10000
                    }
                }
                with(context) {
                    safeDiv(10, 0)
                }
            }
            assertEquals(expected = 10000, actual)
            println("Completed: $actual")
        }
    }
}
