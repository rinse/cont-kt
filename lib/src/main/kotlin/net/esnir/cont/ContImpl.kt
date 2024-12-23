package net.esnir.cont

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.slf4j.LoggerFactory
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object ContImpl : Cont {
    private val logger = LoggerFactory.getLogger(ContImpl::class.java)

    override suspend fun <T> callCC(block: suspend (suspend (T) -> Nothing) -> T): T {
        return suspendCancellableCoroutine { cont ->
            val job = CoroutineScope(cont.context).launch {
                try {
                    val t = block { t ->
                        logger.debug("[callCC] The captured continuation k is called in the block. cont.isActive: ${cont.isActive}")
                        if (cont.isActive) {
                            cont.resume(t)
                        }
                        throw CancellationException("The continuation is cancelled")
                    }
                    logger.debug("[callCC] The block returned without calling the captured continuation. cont.isActive: ${cont.isActive}")
                    if (cont.isActive) {
                        cont.resume(t)
                    }
                } catch (e: Throwable) {
                    logger.debug("[callCC] The block has thrown an exception. cont.isActive: ${cont.isActive}")
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
