package test

class ClassWithPrivateValAdded {
    public fun main() {}
}

class ClassWithPrivateValRemoved {
    public fun main() {}
    private val x: Int = 100
}

class ClassWithPrivateValSignatureChanged {
    public fun main() {}
    private val x: Int = 100
}

class ClassWithGetterForPrivateValChanged {
    public fun main() {}
    private val x: Int = 100
}
