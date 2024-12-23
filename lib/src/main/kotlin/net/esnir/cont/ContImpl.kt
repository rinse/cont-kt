package net.esnir.cont

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object ContImpl : Cont {
    override suspend fun <T> callCC(block: suspend (suspend (T) -> Nothing) -> T): T {
        return suspendCancellableCoroutine { cont ->
            val job = CoroutineScope(cont.context).launch {
                try {
                    val t = block { t ->
                        println("[callCC] The captured continuation k is called in the block. cont.isActive: ${cont.isActive}")
                        if (cont.isActive) {
                            cont.resume(t)
                        }
                        throw CancellationException("The continuation is cancelled")
                    }
                    println("[callCC] The block returned without calling the captured continuation. cont.isActive: ${cont.isActive}")
                    if (cont.isActive) {
                        cont.resume(t)
                    }
                } catch (e: Throwable) {
                    println("[callCC] The block has thrown an exception. cont.isActive: ${cont.isActive}")
                    if (cont.isActive) {
                        cont.resumeWithException(e)
                    } else {
                        throw e
                    }
                }
            }
            cont.invokeOnCancellation { cause ->
                job.cancel(CancellationException(cause))
            }
        }
    }
}
