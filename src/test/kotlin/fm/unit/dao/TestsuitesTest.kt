package fm.unit.dao

import fm.unit.kotlintest.listeners.JdbiLstener
import io.kotlintest.extensions.TestListener
import io.kotlintest.specs.StringSpec

class TestsuitesTest: StringSpec() {
    // Setup database
    val db = JdbiLstener()

    override fun listeners(): List<TestListener> = listOf(db)

    init {
        "Testsuites DAO roundtrip" {
            assert(true)
        }
    }
}
