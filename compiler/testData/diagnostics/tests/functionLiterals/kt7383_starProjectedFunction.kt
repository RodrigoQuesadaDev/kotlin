// KT-7383 Assertion failed when a star-projection of function type is used

fun foo() {
    val f : Function1<*, *> = { x -> x.toString() }
    <!FUNCTION_EXPECTED, UNUSED_EXPRESSION!>f<!>(1)
}
