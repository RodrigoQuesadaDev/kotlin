package test

class ClassWithPrivateVarAdded {
    public fun main() {}
    private var x: Int = 100
}

class ClassWithPrivateVarRemoved {
    public fun main() {}
}

class ClassWithPrivateVarSignatureChanged {
    public fun main() {}
    private var x: String = "X"
}

class ClassWithGetterAndSetterForPrivateVarChanged {
    public fun main() {}
    private var x: Int
    get() = 200
    set(value) {}
}
