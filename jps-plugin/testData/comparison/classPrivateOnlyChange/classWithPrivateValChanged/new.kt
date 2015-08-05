package test

class ClassWithPrivateValAdded {
    public fun main() {}
    private val x: Int = 100
}

class ClassWithPrivateValRemoved {
    public fun main() {}
}

class ClassWithPrivateValSignatureChanged {
    public fun main() {}
    private val x: String = "X"
}

class ClassWithGetterForPrivateValChanged {
    public fun main() {}
    private val x: Int
    get() = 200
}
