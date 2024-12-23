package net.esnir.cont

/**
 * Scope for shift/reset.
 */
class ShiftResetScope<T>(
    private val cont: Cont,
    private var delimited: suspend (T) -> Nothing,
) {
    /**
     * Delimit a continuation in a scope.
     */
    suspend fun reset(block: suspend () -> T): T {
        return cont.callCC { k ->
            val preservedState = this.delimited
            this.delimited = { t: T ->
                this.delimited = preservedState
                k(t)
            }
            this.delimited(block())
        }
    }

    /**
     * Captures the continuation delimited by the nearest enclosing [reset] and pass it to [block].
     */
    suspend fun <U> shift(block: suspend (suspend (U) -> T) -> T): U {
        return cont.callCC { k ->
            val t: T = block { u: U ->
                reset {
                    k(u)
                }
            }
            this.delimited(t)
        }
    }
}

/**
 * Introduce a scope for shift/reset.
 */
suspend fun <T> shiftResetScope(cont: Cont = ContImpl, block: suspend ShiftResetScope<T>.() -> T): T {
    val scope = ShiftResetScope<T>(cont) { _ -> throw Exception("NEVER REACHED") }
    return with(scope) {
        // 'Delimits' the entire block so that [shift] works without enclosing reset in the block
        reset {
            block()
        }
    }
}
