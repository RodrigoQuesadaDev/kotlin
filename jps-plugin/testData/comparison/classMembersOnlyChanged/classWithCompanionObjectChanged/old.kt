package test

class ClassWithAddedCompanionObject {
    public fun main() {}
}

class ClassWithRemovedCompanionObject {
    companion object {}
    public fun main() {}
}

class ClassWithChangedCompanionObject {
    companion object FirstName {}
    public fun main() {}
}
