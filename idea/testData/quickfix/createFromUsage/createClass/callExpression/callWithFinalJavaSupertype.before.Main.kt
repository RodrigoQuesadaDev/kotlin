// "Create class 'Foo'" "false"
// ACTION: Create function 'Foo'
// ACTION: Convert to block body
// ERROR: Unresolved reference: Foo

internal fun test(): A = <caret>Foo(2, "2")