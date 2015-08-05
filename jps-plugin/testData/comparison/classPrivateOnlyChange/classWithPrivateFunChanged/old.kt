package test

class ClassWithPrivateFunAdded {
    public fun main() {}
    val s = java.lang.String.valueOf(20)
}

class ClassWithPrivateFunRemoved {
    public fun main() {}
    private fun privateFun() {}
}

class ClassWithPrivateFunSignatureChanged {
    public fun main() {}
    private fun privateFun(arg: String) {}
}
