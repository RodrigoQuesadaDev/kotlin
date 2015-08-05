package test

class ClassWithFunAdded {
    public fun main() {}
}

class ClassWithFunRemoved {
    public fun main() {}
    fun removed() {}
}

class ClassWithValAndFunAddedAndRemoved {
    public fun main() {}
    public val valRemoved: Int = 10
    fun funRemoved() {}
}
