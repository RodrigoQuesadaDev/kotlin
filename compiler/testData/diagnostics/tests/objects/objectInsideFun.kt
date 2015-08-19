interface A {
    val foo: Int
}

fun test(foo: Int) {
    object : A {
        override val foo: Int = foo
    }
}