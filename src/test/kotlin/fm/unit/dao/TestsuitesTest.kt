package fm.unit.dao

import fm.unit.kotlintest.listeners.JdbiFixture
import fm.unit.model.Testsuite
import io.kotlintest.extensions.TestListener
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.io.File
import org.jdbi.v3.sqlobject.kotlin.onDemand


class TestsuitesTest: StringSpec() {
    // Setup database
    val db = JdbiFixture()

    override fun listeners(): List<TestListener> = listOf(db)

    val exceptionXml = File("fixtures/exception.xml").bufferedReader().use { it.readText() }
    val passXml = File("fixtures/pass.xml").bufferedReader().use { it.readText() }

    init {
        "Testsuites DAO roundtrip" {
            val orgDao = db.jdbi.onDemand<Organizations>()
            val org_id = orgDao.create("jeschkies")

            val repoDao = db.jdbi.onDemand<Repositories>()
            val repo_id = repoDao.create("unit")

            val reportsDao = db.jdbi.onDemand<Reports>()
            val report_id = reportsDao.create(org_id, repo_id,"deadbeaf", "system-test")

            val suiteDao = db.jdbi.onDemand<Testsuites>()
            suiteDao.create(report_id, Testsuite("exception.xml", Testsuite.Payload(exceptionXml)))

            val summaries = suiteDao.summaries()
            summaries shouldContain(Testsuite.Summary(3, 1))
        }

        "Query summary for one report" {
            // Given
            val orgDao = db.jdbi.onDemand<Organizations>()
            val org_id = orgDao.create("jeschkies")

            val repoDao = db.jdbi.onDemand<Repositories>()
            val repo_id = repoDao.create("unit")

            val reportsDao = db.jdbi.onDemand<Reports>()
            val suites = listOf(
                    Testsuite("exception.xml", Testsuite.Payload(exceptionXml)),
                    Testsuite("pas.xml", Testsuite.Payload(passXml))
                )
            val report_id = reportsDao.create(org_id, repo_id, "deadbeaf", "system-test", suites)
            val unknown_report_id = 42

            // When
            val suiteDao = db.jdbi.onDemand<Testsuites>()
            val summary = suiteDao.readSummary(report_id)

            // Then
            suiteDao.readSummary(unknown_report_id) shouldBe(null)
            summary shouldBe (Testsuite.Summary(6, 1))
        }
    }
}
