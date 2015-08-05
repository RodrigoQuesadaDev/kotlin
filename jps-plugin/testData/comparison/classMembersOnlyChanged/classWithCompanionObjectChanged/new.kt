package test

class ClassWithAddedCompanionObject {
    companion object {}
    public fun main() {}
}

class ClassWithRemovedCompanionObject {
    public fun main() {}
}

class ClassWithChangedCompanionObject {
    companion object SecondName {}
    public fun main() {}
}
