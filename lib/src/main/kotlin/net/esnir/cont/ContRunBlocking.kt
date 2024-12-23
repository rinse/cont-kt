package net.esnir.cont

import kotlinx.coroutines.runBlocking
import kotlin.coroutines.Continuation
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object ContRunBlocking : Cont {
    override suspend fun <T> callCC(block: suspend (suspend (T) -> Nothing) -> T): T {
        val context = coroutineContext
        return suspendCoroutine { cont ->
            val t = runBlocking(context) {
                block { t ->
                    cont.resume(t)
                    suspendCoroutine { cont: Continuation<Nothing> ->
                        // dispose continuation
                    }
                }
            }
            cont.resume(t)
        }
    }
}
