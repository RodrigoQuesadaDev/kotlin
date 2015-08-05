package test

class ClassWithPrivateSecondaryConstructorsAdded {
    public fun main() {}
}

class ClassWithPrivateSecondaryConstructorsAdded2() {
    public fun main() {}
    constructor(arg: Float) : this() {}
}

class ClassWithPrivateSecondaryConstructorsRemoved() {
    private constructor(arg: Int): this() {}
    private constructor(arg: String): this() {}
    public fun main() {}
}
