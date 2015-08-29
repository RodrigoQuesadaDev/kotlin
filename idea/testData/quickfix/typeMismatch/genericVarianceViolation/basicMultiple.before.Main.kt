// "Cast expression 'x' to 'MutableList<Any>?'" "true"
// ERROR: Unsafe implicit conversion from kotlin.MutableList<kotlin.String> to kotlin.(Mutable)List<kotlin.Any!>!. Use explicit cast

fun main(x: MutableList<String>) {
    A.foo(<caret>x)
}
