// !DIAGNOSTICS: -UNUSED_VARIABLE
// FILE: A.java

import java.util.*;

public class A {
    void foo(List<Object> x) {}
    List<String> bar() {}
}
// FILE: main.kt

fun main(a: A) {
    a.foo(<!JAVA_GENERIC_VARIANCE_VIOLATION!>a.bar()<!>)
}
