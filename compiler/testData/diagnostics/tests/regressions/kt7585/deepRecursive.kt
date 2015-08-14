open class A<T: A<T>>

open class B: A<C<B>>()

class C<T: B>: A<C<B>>()

fun create(): B {
    return B()
}

fun foo(): A<*> = create()
