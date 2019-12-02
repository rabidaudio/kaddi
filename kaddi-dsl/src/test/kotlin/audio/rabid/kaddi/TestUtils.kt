package audio.rabid.kaddi

inline fun <reified E : Throwable> expectToThrow(block: () -> Unit): E {
    try {
        block.invoke()
        throw AssertionError("Expected block to throw ${E::class.java} but completed without exception")
    } catch (e: Throwable) {
        if (e is E) {
            return e
        } else {
            throw AssertionError("Expected block to throw ${E::class.java} but instead threw ${e.javaClass}", e)
        }
    }
}
