package fm.unit.dao

import fm.unit.kotlintest.listeners.JdbiFixture
import fm.unit.model.Payload
import fm.unit.model.Testsuite
import fm.unit.model.TestsuiteSummary
import io.kotlintest.extensions.TestListener
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.specs.StringSpec
import java.io.File
import org.jdbi.v3.sqlobject.kotlin.onDemand



class TestsuitesTest: StringSpec() {
    // Setup database
    val db = JdbiFixture()

    override fun listeners(): List<TestListener> = listOf(db)

    init {
        "Testsuites DAO roundtrip" {
            db.jdbi.registerArgument(PayloadArgumentFactory)

            val org_dao = db.jdbi.onDemand<Organizations>()
            org_dao.insert("jeschkies")

            val repo_dao = db.jdbi.onDemand<Repositories>()
            repo_dao.insert("unit")

            val reports_dao = db.jdbi.onDemand<Reports>()
            reports_dao.insert(Report(0, 1, 1,"deadbeaf", "/jeschkies/unit"))

            val suite_dao = db.jdbi.onDemand<Testsuites>()
            val xmlFile = File("fixtures/exception.xml").bufferedReader().use { it.readText() }
            suite_dao.insert(Testsuite(1, "exception.xml"), Payload(xmlFile))

            val summaries = suite_dao.summaries()
            summaries shouldContain(TestsuiteSummary(3, 1))
        }
    }
}
