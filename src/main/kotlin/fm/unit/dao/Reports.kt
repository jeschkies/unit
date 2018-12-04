package fm.unit.dao

import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

data class Report(val id: Int, val commit: String, val key: String)

interface Reports {
    @SqlUpdate("insert into reports (commit, key) values (:report.commit, :report.key)")
    fun insert(report: Report)

    // TODO(karsten): join with testsuites.
    @SqlQuery("SELECT * FROM reports ORDER BY id")
    fun reports(): List<Report>
}