package test

class ClassWithPrivateVarAdded {
    public fun main() {}
}

class ClassWithPrivateVarRemoved {
    public fun main() {}
    private var x: Int = 100
}

class ClassWithPrivateVarSignatureChanged {
    public fun main() {}
    private var x: Int = 100
}

class ClassWithGetterAndSetterForPrivateVarChanged {
    public fun main() {}
    private var x: Int = 100
}
