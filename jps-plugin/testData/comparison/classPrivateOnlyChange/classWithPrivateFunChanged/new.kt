package test

class ClassWithPrivateFunAdded {
    public fun main() {}
    private fun privateFun() {}
    val s = java.lang.String.valueOf(20)
}

class ClassWithPrivateFunRemoved {
    public fun main() {}
}

class ClassWithPrivateFunSignatureChanged {
    public fun main() {}
    private fun privateFun(arg: Int) {}
}
