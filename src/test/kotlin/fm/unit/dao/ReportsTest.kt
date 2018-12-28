package fm.unit.dao

import io.kotlintest.extensions.TestListener
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.specs.StringSpec
import org.jdbi.v3.sqlobject.kotlin.onDemand
import fm.unit.kotlintest.listeners.JdbiFixture
import io.kotlintest.shouldBe

class ReportsTest: StringSpec() {
    // Setup database
    val db = JdbiFixture()
    override fun listeners(): List<TestListener> = listOf(db)

    init {
        "Reports DAO roundtrip" {

            val org_dao = db.jdbi.onDemand<Organizations>()
            val org_id = org_dao.create("jeschkies")

            org_dao.read("unknown") shouldBe (null)
            org_dao.read("jeschkies") shouldBe (org_id)

            val repo_dao = db.jdbi.onDemand<Repositories>()
            val repo_id = repo_dao.create("unit")

            repo_dao.read("unknown") shouldBe (null)
            repo_dao.read("unit") shouldBe (repo_id)

            val dao = db.jdbi.onDemand<Reports>()

            dao.create(org_id, repo_id, "deadbeaf", "/jeschkies/unit")
            dao.create(org_id, repo_id,"12345678", "/jeschkies/unit")

            dao.reports() shouldHaveSize (2)
        }
    }
}