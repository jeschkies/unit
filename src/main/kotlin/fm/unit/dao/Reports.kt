package fm.unit.dao

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


    // TODO(karsten): join with testsuites.
    @SqlQuery("SELECT (commit_hash) FROM reports ORDER BY report_id")
    fun reports(): List<String>
}