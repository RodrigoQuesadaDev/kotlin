package test

class ClassWithPrimaryConstructorChanged constructor() {
    public fun main() {}
}

class ClassWithSecondaryConstructorsAdded {
    public fun main() {}
}

class ClassWithSecondaryConstructorsRemoved() {
    public constructor(arg: Int): this() {}
    constructor(arg: String): this() {}
    public fun main() {}
}
