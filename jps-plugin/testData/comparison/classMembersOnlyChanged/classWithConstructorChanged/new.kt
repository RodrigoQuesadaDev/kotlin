package test

class ClassWithPrimaryConstructorChanged constructor(arg: String) {
    public fun main() {}
}

class ClassWithSecondaryConstructorsAdded() {
    constructor(arg: Int): this() {}
    constructor(arg: String): this() {}
    public fun main() {}
}

class ClassWithSecondaryConstructorsRemoved() {
    public fun main() {}
}
