open class A<T: A<T>>

class B: A<B>()

fun create(): B {
    return B()
}

fun foo(): A<*> = create()
