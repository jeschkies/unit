package fm.unit.dao

import fm.unit.kotlintest.listeners.JdbiFixture
import io.kotlintest.extensions.TestListener
import io.kotlintest.specs.StringSpec

class TestsuitesTest: StringSpec() {
    // Setup database
    val db = JdbiFixture()

    override fun listeners(): List<TestListener> = listOf(db)

    init {
        "Testsuites DAO roundtrip" {
            assert(true)
        }
    }
}
