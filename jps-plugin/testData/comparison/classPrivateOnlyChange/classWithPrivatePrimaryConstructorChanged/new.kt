package test

class ClassWithPrivatePrimaryConstructorAdded private constructor() {
    public fun main() {}
    private constructor(arg: Int) : this() {}
}

class ClassWithPrivatePrimaryConstructorRemoved {
    public fun main() {}
    private constructor(arg: Int) {}
}

class ClassWithPrivatePrimaryConstructorChanged private constructor(arg: String) {
    public fun main() {}
}
