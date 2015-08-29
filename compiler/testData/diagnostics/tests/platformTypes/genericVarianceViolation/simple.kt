// !DIAGNOSTICS: -UNUSED_VARIABLE
// FILE: A.java

import java.util.*;

public class A {
    void foo(List<Object> x) {}
    void foo(Iterable<Object> x) {}
    void foo(Iterator<Object> x) {}
    void foo(Set<Object> x) {}
    void foo(Map<Object, Object> x) {}
    void foo(Map.Entry<Object, Object> x) {}

    void foo(List<List<Object>> x) {}
}

// FILE: main.kt

fun main(
        a: A,
        ml: MutableList<String>, l: List<String>,
        ms: MutableSet<String>, s: Set<String>,
        mm: MutableMap<Any, String>, m: Map<Any, String>,
        mme: MutableMap.MutableEntry<Any, String>, me: Map.Entry<Any, String>,
        mll: MutableList<MutableList<String>>, ll: List<List<String>>
) {
    // Lists
    a.foo(<!JAVA_GENERIC_VARIANCE_VIOLATION!>ml<!>)
    a.foo(l)
    a.foo(<!UNCHECKED_CAST!>ml as MutableList<Any><!>)
    a.foo(l as List<Any>)

    // Iterables
    val mit: MutableIterable<String> = ml
    val it: Iterable<String> = ml
    a.foo(mit)
    a.foo(it)

    // Iterators
    a.foo(ml.iterator())
    a.foo(l.iterator())

    // Sets
    a.foo(<!JAVA_GENERIC_VARIANCE_VIOLATION!>ms<!>)
    a.foo(s)
    a.foo(<!UNCHECKED_CAST!>ms as MutableSet<Any><!>)
    a.foo(s as Set<Any>)

    // Maps
    a.foo(<!JAVA_GENERIC_VARIANCE_VIOLATION!>mm<!>)
    a.foo(m)
    a.foo(<!UNCHECKED_CAST!>mm as MutableMap<Any, Any><!>)
    a.foo(m as Map<Any, Any>)

    // Map entries
    a.foo(<!JAVA_GENERIC_VARIANCE_VIOLATION!>mme<!>)
    a.foo(me)
    a.foo(<!UNCHECKED_CAST!>mme as MutableMap.MutableEntry<Any, Any><!>)
    a.foo(me as Map.Entry<Any, Any>)

    // Lists of lists
    a.foo(<!JAVA_GENERIC_VARIANCE_VIOLATION!>mll<!>)
    a.foo(ll)
    a.foo(<!UNCHECKED_CAST!>mll as MutableList<MutableList<Any>><!>)
    a.foo(ll as List<List<Any>>)

}
