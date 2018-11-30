package unitfm.dao

import io.kotlintest.extensions.TestListener
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.specs.StringSpec
import org.jdbi.v3.sqlobject.kotlin.onDemand
import unitfm.kotlintest.listeners.JdbiLstener

class ExampleTest : StringSpec() {
    // Setup database
    val db = JdbiLstener()
    override fun listeners(): List<TestListener> = listOf(db)

    init {
        "Reports DAO roundtrip" {

            val dao = db.jdbi.onDemand<Reports>()

            dao.insert(Report(0, "deadbeaf", "/jeschkies/unit/1"))
            dao.insert(Report(0, "12345678", "/jeschkies/unit/2"))

            dao.reports() shouldHaveSize (2)
        }
    }
}