import java.io.File
import java.io.Serializable

val File.<!PROPERTY_CONFLICTING_WITH_SYNTHETIC_EXTENSION!>name<!>: String
    get() = getName()

val Serializable.name: String
    get() = ""

val File.<!PROPERTY_CONFLICTING_WITH_SYNTHETIC_EXTENSION!>parent<!>: File
    get() = getParentFile()

class MyFile : File("")

val MyFile.<!PROPERTY_CONFLICTING_WITH_SYNTHETIC_EXTENSION!>isFile<!>: Boolean
    get() = isFile()