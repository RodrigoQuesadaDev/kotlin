package test

class ClassWithPrivatePrimaryConstructorAdded {
    public fun main() {}
    private constructor(arg: Int) {}
}

class ClassWithPrivatePrimaryConstructorRemoved private constructor() {
    public fun main() {}
    private constructor(arg: Int) : this() {}
}

class ClassWithPrivatePrimaryConstructorChanged private constructor() {
    public fun main() {}
}
