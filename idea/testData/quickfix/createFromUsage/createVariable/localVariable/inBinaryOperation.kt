// "Create local variable 'foo'" "false"
// ERROR: Unresolved reference: foo
// ACTION: Create extension function 'foo'
// ACTION: Replace infix call with ordinary call
fun refer() {
    1 <caret>foo 2
}