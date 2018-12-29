package fm.unit.dao

import fm.unit.model.Report
import fm.unit.model.Testsuite
import org.jdbi.v3.sqlobject.CreateSqlObject
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import org.jdbi.v3.sqlobject.transaction.Transaction

interface Reports {

    @CreateSqlObject
    fun testsuites(): Testsuites

    /**
     * Insert a new report.
     *
     * Note that the report is empty unless testsuites are inserted that belong to this report.
     *
     * @param organization_id The organization of this report.
     * @param repository_id The repository this report belongs to.
     * @param commit_hash The commit the report belongs to.
     * @param prefix A common prefix to group reports, e.g. `system-test`.
     * @return The id of the inserted report.
     */
    @SqlUpdate("""
        INSERT INTO reports (organization_id, repository_id, commit_hash, prefix)
        VALUES (:organization_id, :repository_id, :commit_hash, :prefix)
    """)
    @GetGeneratedKeys
    fun create(organization_id: Int, repository_id: Int, commit_hash: String, prefix: String): Int

    /**
     * Insert a new report with its testsuite, ie JUnit XML files.
     *
     * @param organization_id The organization of this report.
     * @param repository_id The repository this report belongs to.
     * @param commit_hash The commit the report belongs to.
     * @param prefix A common prefix to group reports, e.g. `system-test`.
     * @param suites A list of the testsuites that will e inserted.
     * @return The id of the inserted report.
     */
    @Transaction
    fun create(organization_id: Int, repository_id: Int, commit_hash: String, prefix: String, suites: List<Testsuite>): Int {
        val suiteDao = testsuites()
        val report_id = create(organization_id, repository_id, commit_hash, prefix)
        suites.forEach { suiteDao.create(report_id, it) }
        return report_id
    }

    @SqlQuery("SELECT report_id, prefix FROM reports")
    fun readReports(): List<Int>


    // TODO(karsten): map to Report.Summary
    data class Foo(val report_id: Int, val tests: Int, val errors: Int)
    @SqlQuery("""
        SELECT reports.report_id,
               SUM((xpath('count(//testcase)', payload))[1]::text::integer) AS tests,
               SUM((xpath('count(//failure)', payload))[1]::text::integer) AS errors
        FROM reports
        LEFT JOIN testsuites ON reports.report_id = testsuites.report_id
        WHERE reports.prefix = :prefix
            AND reports.organization_id = :organization_id
            AND reports.repository_id = :repository_id
        GROUP BY reports.report_id
        ORDER BY reports.report_id
    """)
    fun readReportSummaries(organization_id: Int, repository_id: Int, prefix: String): List<Foo>
}