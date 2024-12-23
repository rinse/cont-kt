package net.esnir.cont

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class ThrowableContextTest {
    fun interface ThrowableContext {
        suspend fun throwException(message: String): Nothing
    }

    suspend fun ThrowableContext.safeDiv(n: Int, m: Int): Int {
        if (m == 0) {
            return throwException("Zero Division")
        }
        return n / m
    }

    @Test
    fun `test without k, succeeds`() {
        runBlocking {
            val actual: String = shiftResetScope {
                val context = ThrowableContext { message ->
                    shift { _ ->
                        "Error! Message: $message"
                    }
                }
                with(context) {
                    val r = safeDiv(10, 2)
                    "Result: $r"
                }
            }
            assertEquals(expected = "Result: 5", actual)
            println("Completed: $actual")
        }
    }

    @Test
    fun `test without k, exception thrown`() {
        runBlocking {
            val actual: String = shiftResetScope {
                val context = ThrowableContext { message ->
                    shift { _ ->
                        "Error! Message: $message"
                    }
                }
                with(context) {
                    val r = safeDiv(10, 0)
                    "Result: $r"
                }
            }
            assertEquals(expected = "Error! Message: Zero Division", actual)
            println("Completed: $actual")
        }
    }
}
