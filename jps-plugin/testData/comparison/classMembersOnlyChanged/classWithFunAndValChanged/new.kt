package test

class ClassWithFunAdded {
    public fun main() {}
    fun added() {}
}

class ClassWithFunRemoved {
    public fun main() {}
}

class ClassWithValAndFunAddedAndRemoved {
    public fun main() {}
    public val valAdded: String = ""
    fun funAdded() {}
}
