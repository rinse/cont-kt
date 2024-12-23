package net.esnir.cont

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.reflect.KClass

class ContTest {
    @Test
    fun `test for calling k`() {
        runBlocking {
            val actual = ContImpl.callCC { k ->
                delay(10)
                k("Calling k")
            }
            println("Completed: $actual")
            assertEquals("Calling k", actual = actual)
        }
    }

    @Test
    fun `test for returning a value`() {
        runBlocking {
            val actual = ContImpl.callCC<String> { k ->
                delay(10)
                "Returning a value"
            }
            println("Completed: $actual")
            assertEquals("Returning a value", actual = actual)
        }
    }

    @Test
    fun `test for calling k then returning a value`() {
        runBlocking {
            val actual = ContImpl.callCC { k ->
                launch {
                    delay(10)
                    k("Calling k")
                }
                delay(100)
                "Returning a value"
            }
            println("Completed: $actual")
            assertEquals("Calling k", actual = actual)
        }
    }

    @Test
    fun `test for returning a value then calling k`() {
        runBlocking {
            val actual = ContImpl.callCC { k ->
                launch {
                    delay(100)
                    k("Calling k")
                }
                delay(10)
                "Returning a value"
            }
            println("Completed: $actual")
            assertEquals("Returning a value", actual = actual)
        }
    }

    @Test
    fun `test for cancelling to call k`() {
        runBlocking {
            val job = launch {
                val actual = ContImpl.callCC { k ->
                    delay(100)
                    k("Calling k")
                }
                fail("Shouldn't be called. Returned value: $actual")
            }
            delay(10)
            job.cancelAndJoin()
            println("Completed")
        }
    }

    @Test
    fun `test for cancelling not to call k`() {
        runBlocking {
            val job = launch {
                val actual = ContImpl.callCC<String> { k ->
                    delay(100)
                    "Returning a value"
                }
                fail("Shouldn't be called. Returned value: $actual")
            }
            delay(10)
            job.cancelAndJoin()
            println("Completed: cancelled")
        }
    }

    @Test
    fun `test for cancelling both calling and not calling k`() {
        runBlocking {
            val job = launch {
                val actual = ContImpl.callCC { k ->
                    launch {
                        delay(50)
                        k("Calling k")
                    }
                    delay(50)
                    "Returning a value"
                }
                fail("Shouldn't be called. Returned value: $actual")
            }
            delay(10)
            job.cancelAndJoin()
            println("Completed: cancelled")
        }
    }

    @Test
    fun `test for trying to cancel but k has already called`() {
        runBlocking {
            val job = launch {
                val actual = ContImpl.callCC { k ->
                    delay(10)
                    k("Calling k")
                }
                assertEquals("Calling k", actual)
            }
            delay(50)
            job.cancelAndJoin()
            println("Completed: cancelled")
        }
    }

    @Test
    fun `test for trying to cancel but the given block has already returned`() {
        runBlocking {
            val job = launch {
                val actual = ContImpl.callCC<String> { k ->
                    delay(10)
                    "Returning a value"
                }
                assertEquals("Returning a value", actual)
            }
            delay(50)
            job.cancelAndJoin()
            println("Completed")
        }
    }

    @Test
    fun `test for throwing an exception`() {
        assertThrows(TestException1::class) {
            runBlocking {
                val actual = ContImpl.callCC<String> { k ->
                    delay(10)
                    throw TestException1()
                }
                fail("Shouldn't be called. Returned value: $actual")
            }
        }
    }

    @Test
    fun `test for throwing an exception after calling k`() {
        assertThrows(TestException1::class) {
            runBlocking {
                val actual = ContImpl.callCC<String> { k ->
                    launch {
                        delay(10)
                        k("Calling k")
                    }
                    delay(50)
                    throw TestException1()
                }
                println("Completed: $actual")
            }
        }
    }

    @Test
    fun `test for throwing an exception after the block returning value`() {
        assertThrows(TestException1::class) {
            runBlocking {
                val actual = ContImpl.callCC<String> { k ->
                    launch {
                        delay(50)
                        throw TestException1()
                    }
                    delay(10)
                    "Returning a value"
                }
                println("Completed: $actual")
            }
        }
    }

    @Test
    fun `test for throwing an exception twice`() {
        assertThrows(TestException1::class) {
            try {
                runBlocking {
                    val actual = ContImpl.callCC<String> { k ->
                        launch {
                            delay(10)
                            throw TestException1()
                        }
                        delay(50)
                        fail("Shouldn't be called. Cancelled by the TestException1")
                    }
                    fail("Shouldn't be called. Returned value: $actual")
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                throw e
            }
        }
    }

    @Test
    fun `test for k won't return`() {
        runBlocking {
            val actual = ContImpl.callCC { k ->
                delay(10)
                k("Calling k")
                fail("Shouldn't be called.")
                "Returning a value"
            }
            println("Completed: $actual")
            assertEquals("Calling k", actual = actual)
        }
    }
}

fun <T> assertEquals(expected: T, actual: T) {
    Assertions.assertEquals(expected, actual)
}

inline fun <T : Throwable> assertThrows(exception: KClass<T>, crossinline block: () -> Unit) {
    Assertions.assertThrows(exception.java) {
        block()
    }
}

class TestException1 : Exception()
