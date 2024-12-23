package net.esnir.cont

interface Cont {
    suspend fun <T> callCC(block: suspend (suspend (T) -> Nothing) -> T): T
}
