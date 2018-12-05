package fm.unit.dao

import io.kotlintest.extensions.TestListener
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.specs.StringSpec
import org.jdbi.v3.sqlobject.kotlin.onDemand
import fm.unit.kotlintest.listeners.JdbiFixture

class ReportsTest: StringSpec() {
    // Setup database
    val db = JdbiFixture()
    override fun listeners(): List<TestListener> = listOf(db)

    init {
        "Reports DAO roundtrip" {

            val org_dao = db.jdbi.onDemand<Organizations>()
            org_dao.insert("jeschkies")

            val repo_dao = db.jdbi.onDemand<Repositories>()
            repo_dao.insert("unit")

            val dao = db.jdbi.onDemand<Reports>()

            dao.insert(Report(0, 1, 1,"deadbeaf", "/jeschkies/unit"))
            dao.insert(Report(0, 1, 1,"12345678", "/jeschkies/unit"))

            dao.reports() shouldHaveSize (2)
        }
    }
}