package net.esnir.cont

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class ShiftResetScopeTest {
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

    @Test
    fun `test with k, succeeds`() {
        runBlocking {
            val actual: Int = shiftResetScope {
                val context = ThrowableContext2 { message ->
                    shift { _ ->
                        "Error! Message: $message"
                        10000
                    }
                }
                with(context) {
                    safeDiv2(10, 2)
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
                val context = ThrowableContext2 { message ->
                    shift { _ ->
                        "Error! Message: $message"
                        10000
                    }
                }
                with(context) {
                    safeDiv2(10, 0)
                }
            }
            assertEquals(expected = 10000, actual)
            println("Completed: $actual")
        }
    }
}

fun interface ThrowableContext {
    suspend fun throwException(message: String): Nothing
}

suspend fun ThrowableContext.safeDiv(n: Int, m: Int): Int {
    if (m == 0) {
        throwException("Zero Division")
    }
    return n / m
}


fun interface ThrowableContext2 {
    suspend fun throwException(message: String): Int
}

suspend fun ThrowableContext2.safeDiv2(n: Int, m: Int): Int {
    if (m == 0) {
        return throwException("Zero Division")
    }
    return n / m
}
