package test

class ClassWithPrivateSecondaryConstructorsAdded() {
    private constructor(arg: Int) : this() {}
    private constructor(arg: String) : this() {}
    public fun main() {}
}

class ClassWithPrivateSecondaryConstructorsAdded2() {
    public fun main() {}
    private constructor(arg: Int) : this() {}
    private constructor(arg: String) : this() {}
    constructor(arg: Float) : this() {}
}

class ClassWithPrivateSecondaryConstructorsRemoved() {
    public fun main() {}
}
