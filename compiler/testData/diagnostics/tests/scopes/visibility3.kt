//FILE:file1.kt
package a

private open class A {
    fun bar() {}
}

private var x: Int = 10

private fun foo() {}

private fun bar() {
    val <!UNUSED_VARIABLE!>y<!> = x
    x = 20
}

fun makeA() = A()

private object PO {}

//FILE:file2.kt
package a

fun test() {
    val y = makeA()
    y.<!INVISIBLE_MEMBER!>bar<!>()
    <!INVISIBLE_MEMBER!>foo<!>()

    val <!UNUSED_VARIABLE!>u<!> : <!INVISIBLE_REFERENCE!>A<!> = <!INVISIBLE_MEMBER!>A<!>()

    val <!UNUSED_VARIABLE!>z<!> = <!INVISIBLE_MEMBER!>x<!>
    <!INVISIBLE_MEMBER!>x<!> = 30

    val <!UNUSED_VARIABLE!>po<!> = <!INVISIBLE_MEMBER!>PO<!>
}

class B : <!INVISIBLE_REFERENCE, INVISIBLE_MEMBER!>A<!>() {}

class Q {
    class W {
        fun foo() {
            val <!UNUSED_VARIABLE!>y<!> = makeA() //assure that 'makeA' is visible
        }
    }
}
