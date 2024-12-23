package net.esnir.cont

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.startCoroutine
import kotlin.coroutines.suspendCoroutine

object ContStartCoroutine : Cont {
    override suspend fun <T> callCC(block: suspend (suspend (T) -> Nothing) -> T): T {
        val context = coroutineContext
        return suspendCoroutine { cont ->
            startCoroutine(context, { cont.resume(it) }) {
                block { t ->
                    cont.resume(t)
                    suspendCoroutine { _: Continuation<Nothing> ->
                        // dispose continuation
                    }
                }
            }
        }
    }

    private fun <T> startCoroutine(context: CoroutineContext, cont: (T) -> Unit, block: suspend () -> T) {
        block.startCoroutine(object : Continuation<T> {
            override val context = context
            override fun resumeWith(result: Result<T>) {
                cont(result.getOrThrow())
            }
        })
    }
}
