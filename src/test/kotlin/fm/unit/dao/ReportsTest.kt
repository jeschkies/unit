package fm.unit.dao

import io.kotlintest.extensions.TestListener
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.specs.StringSpec
import org.jdbi.v3.sqlobject.kotlin.onDemand
import fm.unit.kotlintest.listeners.JdbiFixture
import fm.unit.model.Report
import fm.unit.model.Testsuite
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.shouldBe
import java.io.File

class ReportsTest: StringSpec() {
    // Setup database
    val db = JdbiFixture()
    override fun listeners(): List<TestListener> = listOf(db)

    val exceptionXml = File("fixtures/exception.xml").bufferedReader().use { it.readText() }
    val passXml = File("fixtures/pass.xml").bufferedReader().use { it.readText() }

    init {
        "Reports DAO roundtrip" {
            // Given
            val org_dao = db.jdbi.onDemand<Organizations>()
            val org_id = org_dao.create("jeschkies")

            org_dao.read("unknown") shouldBe (null)
            org_dao.read("jeschkies") shouldBe (org_id)

            val repo_dao = db.jdbi.onDemand<Repositories>()
            val repo_id = repo_dao.create("unit")

            repo_dao.read("unknown") shouldBe (null)
            repo_dao.read("unit") shouldBe (repo_id)

            // When
            val dao = db.jdbi.onDemand<Reports>()
            dao.create(org_id, repo_id, "deadbeaf", "system-test")
            dao.create(org_id, repo_id,"12345678", "system-test")
            dao.create(org_id, repo_id,"12345678", "integration-test")

            // Then
            dao.readReportSummaries(org_id, repo_id, "system-test") shouldHaveSize (2)
            dao.readReportSummaries(org_id, repo_id, "integration-test") shouldHaveSize (1)
        }

        "Summarize last n reports" {
            // Given
            val org_id = db.jdbi.onDemand<Organizations>().create("jeschkies")
            val repo_id = db.jdbi.onDemand<Repositories>().create("unit")

            val reportsDao = db.jdbi.onDemand<Reports>()
            val suites = listOf(
                    Testsuite("exception.xml", Testsuite.Payload(exceptionXml)),
                    Testsuite("pass.xml", Testsuite.Payload(passXml))
            )
            val report_id1 = reportsDao.create(org_id, repo_id, "deadbeaf", "system-test", suites)
            val report_id2 = reportsDao.create(org_id, repo_id, "12345678", "system-test", suites.takeLast(1))

            // When
            val summaries = reportsDao.readReportSummaries(org_id, repo_id, "system-test")

            // Then
            println("Reports: ${reportsDao.readReports()}")
            println("Summaries: $summaries")
            summaries shouldHaveSize (2)
            summaries shouldContain (Report.Summary(report_id1, Testsuite.Summary(0, 0)))
            summaries shouldContain (Report.Summary(report_id2, Testsuite.Summary(0, 0)))
        }
    }
}